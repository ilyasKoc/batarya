package com.example.system

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.viewmodel.PowerMode
import java.util.Calendar
import java.util.Locale

class VoltGuardService : Service() {

    private var batteryReceiver: BroadcastReceiver? = null
    private var isReceiverRegistered = false
    private var lastChargingReminderTime = 0L
    private var isAutoPowerSaveCurrentlyActive = false
    private var hasAlerted80Percent = false
    private var hasAlerted100Percent = false
    private var hasAlertedNightBuffer = false
    private var hasAlertedNightTrickle = false
    private var hasAlertedMorningReady = false
    private var lastOverheatAlertTime = 0L
    private var lastLowBatteryAlertTime = 0L

    companion object {
        private const val TAG = "VoltGuardService"
        const val NOTIFICATION_ID = 1000
        const val ACTION_START_SERVICE = "com.example.system.ACTION_START_SERVICE"
        const val ACTION_STOP_SERVICE = "com.example.system.ACTION_STOP_SERVICE"

        fun startService(context: Context) {
            try {
                val intent = Intent(context, VoltGuardService::class.java).apply {
                    action = ACTION_START_SERVICE
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    ContextCompat.startForegroundService(context, intent)
                } else {
                    context.startService(intent)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start VoltGuardService", e)
            }
        }

        fun stopService(context: Context) {
            try {
                val intent = Intent(context, VoltGuardService::class.java).apply {
                    action = ACTION_STOP_SERVICE
                }
                context.stopService(intent)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to stop VoltGuardService", e)
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "VoltGuardService onCreate")

        NotificationHelper.initChannels(this)
        val initialNotification = NotificationHelper.buildForegroundServiceNotification(this)
        startForeground(NOTIFICATION_ID, initialNotification)

        registerBatteryListener()
        AutoStartHelper.scheduleWatchdog(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP_SERVICE) {
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return START_NOT_STICKY
        }

        val initialNotification = NotificationHelper.buildForegroundServiceNotification(this)
        startForeground(NOTIFICATION_ID, initialNotification)

        return START_STICKY
    }

    private fun registerBatteryListener() {
        if (isReceiverRegistered) return

        batteryReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when (intent?.action) {
                    Intent.ACTION_BATTERY_CHANGED -> {
                        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100)
                        val percent = if (scale > 0) (level * 100) / scale else level
                        val tempTenths = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0)
                        val tempCelsius = tempTenths / 10.0f
                        val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                                status == BatteryManager.BATTERY_STATUS_FULL

                        val notification = NotificationHelper.buildForegroundServiceNotification(
                            context = this@VoltGuardService,
                            batteryPercent = percent,
                            tempCelsius = tempCelsius,
                            isCharging = isCharging
                        )
                        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                        nm?.notify(NOTIFICATION_ID, notification)

                        if (PowerFeatureConfigHelper.isWidgetSyncEnabled(this@VoltGuardService)) {
                            VoltGuardWidgetProvider.updateAllWidgets(this@VoltGuardService)
                        }

                        if (PowerFeatureConfigHelper.isSmartChargingAutomationEnabled(this@VoltGuardService)) {
                            checkSmartChargingAutomations(percent, isCharging)
                        }
                        checkNightChargingGuard(percent, isCharging)
                        if (PowerFeatureConfigHelper.isThermalAndFullAlarmsEnabled(this@VoltGuardService)) {
                            checkAlarmsAndAlerts(percent, tempCelsius, isCharging)
                        }
                    }
                    Intent.ACTION_POWER_CONNECTED -> {
                        Log.d(TAG, "⚡ Service received ACTION_POWER_CONNECTED")
                        hasAlerted80Percent = false
                        hasAlerted100Percent = false
                        hasAlertedNightBuffer = false
                        hasAlertedNightTrickle = false
                        hasAlertedMorningReady = false
                        if (PowerFeatureConfigHelper.isWidgetSyncEnabled(this@VoltGuardService)) {
                            VoltGuardWidgetProvider.updateAllWidgets(this@VoltGuardService)
                        }
                    }
                    Intent.ACTION_POWER_DISCONNECTED -> {
                        Log.d(TAG, "⚡ Service received ACTION_POWER_DISCONNECTED")
                        hasAlerted80Percent = false
                        hasAlerted100Percent = false
                        hasAlertedNightBuffer = false
                        hasAlertedNightTrickle = false
                        hasAlertedMorningReady = false
                        if (PowerFeatureConfigHelper.isWidgetSyncEnabled(this@VoltGuardService)) {
                            VoltGuardWidgetProvider.updateAllWidgets(this@VoltGuardService)
                        }
                    }
                }
            }
        }

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_BATTERY_CHANGED)
            addAction(Intent.ACTION_POWER_CONNECTED)
            addAction(Intent.ACTION_POWER_DISCONNECTED)
        }
        registerReceiver(batteryReceiver, filter)
        isReceiverRegistered = true
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        Log.d(TAG, "onTaskRemoved: Application swiped from recents. Auto-respawning...")

        if (AutoStartHelper.isBackgroundProtectionEnabled(this)) {
            scheduleImmediateRestart()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "VoltGuardService onDestroy")

        if (isReceiverRegistered && batteryReceiver != null) {
            try {
                unregisterReceiver(batteryReceiver)
            } catch (_: Exception) {}
            isReceiverRegistered = false
        }

        if (AutoStartHelper.isBackgroundProtectionEnabled(this)) {
            Log.d(TAG, "Service destroyed unexpectedly, triggering auto-restart...")
            scheduleImmediateRestart()
        }
    }

    private fun scheduleImmediateRestart() {
        try {
            val restartIntent = Intent(applicationContext, RestartReceiver::class.java).apply {
                action = RestartReceiver.ACTION_RESTART_SERVICE
            }
            val pendingIntent = PendingIntent.getBroadcast(
                applicationContext,
                9999,
                restartIntent,
                PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
            )
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as? AlarmManager
            val restartTime = SystemClock.elapsedRealtime() + 1000L // 1 second

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager?.setExactAndAllowWhileIdle(
                    AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    restartTime,
                    pendingIntent
                )
            } else {
                alarmManager?.setExact(
                    AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    restartTime,
                    pendingIntent
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to schedule immediate restart", e)
            // Fallback direct start
            try {
                VoltGuardService.startService(applicationContext)
            } catch (_: Exception) {}
        }
    }

    private fun checkSmartChargingAutomations(percent: Int, isCharging: Boolean) {
        val config = SmartChargingScheduleHelper.loadConfig(this)
        val cal = Calendar.getInstance()
        val currentHour = cal.get(Calendar.HOUR_OF_DAY)
        val currentMinute = cal.get(Calendar.MINUTE)

        // 1. Otomatik Güç Tasarrufu Programı
        if (config.autoPowerSaveEnabled) {
            val inWindow = SmartChargingScheduleHelper.isTimeInWindow(
                currentHour, currentMinute,
                config.startHour, config.startMinute,
                config.endHour, config.endMinute
            )

            val powerPrefs = getSharedPreferences("voltguard_power_prefs", Context.MODE_PRIVATE)

            if (inWindow && !isAutoPowerSaveCurrentlyActive) {
                isAutoPowerSaveCurrentlyActive = true
                powerPrefs.edit()
                    .putString("current_power_mode", config.targetPowerMode.name)
                    .putBoolean("is_auto_power_save_active", true)
                    .apply()

                val timeRange = String.format(Locale.getDefault(), "%02d:%02d - %02d:%02d", config.startHour, config.startMinute, config.endHour, config.endMinute)
                NotificationHelper.sendAutoPowerSaveNotification(
                    context = this,
                    isActivated = true,
                    timeRange = timeRange,
                    modeTitle = config.targetPowerMode.title
                )
            } else if (!inWindow && isAutoPowerSaveCurrentlyActive) {
                isAutoPowerSaveCurrentlyActive = false
                powerPrefs.edit()
                    .putString("current_power_mode", PowerMode.NORMAL.name)
                    .putBoolean("is_auto_power_save_active", false)
                    .apply()

                val timeRange = String.format(Locale.getDefault(), "%02d:%02d - %02d:%02d", config.startHour, config.startMinute, config.endHour, config.endMinute)
                NotificationHelper.sendAutoPowerSaveNotification(
                    context = this,
                    isActivated = false,
                    timeRange = timeRange,
                    modeTitle = config.targetPowerMode.title
                )
            }
        }

        // 2. Akıllı Şarj Hatırlatıcısı (%20 veya altı)
        if (config.smartChargeReminderEnabled && !isCharging && percent in 1..20) {
            val now = System.currentTimeMillis()
            if (now - lastChargingReminderTime > (3 * 3600 * 1000L)) {
                lastChargingReminderTime = now
                val slotStr = String.format(Locale.getDefault(), "%02d:%02d - %02d:%02d", 22, 30, 23, 45)
                NotificationHelper.sendSmartChargingReminder(
                    context = this,
                    batteryPercent = percent,
                    hoursRemainingText = "yaklaşık 1-2 saat",
                    recommendedSlotText = slotStr
                )
            }
        }
    }

    private fun checkAlarmsAndAlerts(percent: Int, tempCelsius: Float, isCharging: Boolean) {
        val inQuiet = CustomAlarmSoundHelper.isInQuietHours(this)

        // 1. %80 Batarya Koruma Sağlık Uyarısı
        if (isCharging && percent >= 80 && percent < 100 && !hasAlerted80Percent) {
            if (CustomAlarmSoundHelper.isCharge80AlertEnabled(this)) {
                hasAlerted80Percent = true
                if (!inQuiet) {
                    CustomAlarmSoundHelper.playAlarmTone(this, durationMs = 2000L)
                }
            }
        }

        // 2. %100 Tam Dolum Alarmı
        if (isCharging && percent >= 100 && !hasAlerted100Percent) {
            if (CustomAlarmSoundHelper.isCharge100AlertEnabled(this)) {
                hasAlerted100Percent = true
                if (!inQuiet) {
                    CustomAlarmSoundHelper.playAlarmTone(this, durationMs = 3000L)
                }
            }
        }

        // 3. Termal Aşırı Isınma Siren Alarmı
        val overheatThreshold = CustomAlarmSoundHelper.getOverheatThresholdTemp(this)
        if (tempCelsius >= overheatThreshold && CustomAlarmSoundHelper.isOverheatAlertEnabled(this)) {
            val now = System.currentTimeMillis()
            if (now - lastOverheatAlertTime > (10 * 60 * 1000L)) {
                lastOverheatAlertTime = now
                CustomAlarmSoundHelper.playAlarmTone(this, AlarmToneStyle.SIREN, durationMs = 3500L)
            }
        }

        // 4. Düşük Pil Uyarısı & Otomatik Ultra Tasarruf
        val lowThreshold = CustomAlarmSoundHelper.getLowBatteryThreshold(this)
        if (!isCharging && percent <= lowThreshold && percent > 0) {
            val now = System.currentTimeMillis()
            if (now - lastLowBatteryAlertTime > (20 * 60 * 1000L)) {
                lastLowBatteryAlertTime = now
                if (CustomAlarmSoundHelper.isLowBatteryAlertEnabled(this) && !inQuiet) {
                    CustomAlarmSoundHelper.playAlarmTone(this, durationMs = 2000L)
                }
                if (CustomAlarmSoundHelper.isAutoUltraSaverEnabled(this)) {
                    val powerPrefs = getSharedPreferences("voltguard_power_prefs", Context.MODE_PRIVATE)
                    powerPrefs.edit()
                        .putString("current_power_mode", PowerMode.ULTRA_SAVER.name)
                        .putBoolean("is_auto_power_save_active", true)
                        .apply()
                }
            }
        }
    }

    /**
     * 🌙 Akıllı Gece & Uzun Ömür Şarj Koruması Kontrolü
     * - Gece saatlerinde (%80 Tamponlama ve Uyanış Öncesi Damlama Şarjı)
     */
    private fun checkNightChargingGuard(percent: Int, isCharging: Boolean) {
        val config = NightChargingGuardHelper.loadConfig(this)
        if (!config.isEnabled || !isCharging) return

        val status = NightChargingGuardHelper.evaluateNightGuardStatus(this, percent, isCharging)

        when (status.phase) {
            NightGuardPhase.BUFFER_LOCKED_80 -> {
                if (!hasAlertedNightBuffer) {
                    hasAlertedNightBuffer = true
                    NotificationHelper.sendNightChargingBufferNotification(
                        context = this,
                        currentPercent = percent,
                        targetBuffer = config.chargeLimitPercent,
                        wakeUpTimeFormatted = status.wakeUpTimeFormatted,
                        isTrickleScheduled = config.trickleChargeBufferEnabled
                    )
                    if (!config.autoQuietMode && !CustomAlarmSoundHelper.isInQuietHours(this)) {
                        CustomAlarmSoundHelper.playAlarmTone(this, durationMs = 1500L)
                    }
                }
            }
            NightGuardPhase.TRICKLE_WAKE_TOPUP -> {
                if (!hasAlertedNightTrickle) {
                    hasAlertedNightTrickle = true
                    NotificationHelper.sendNightTrickleTopupNotification(
                        context = this,
                        currentPercent = percent,
                        wakeUpTimeFormatted = status.wakeUpTimeFormatted
                    )
                }
            }
            NightGuardPhase.WAKE_READY_100 -> {
                if (!hasAlertedMorningReady) {
                    hasAlertedMorningReady = true
                    NotificationHelper.sendMorningReadyNotification(
                        context = this,
                        currentPercent = percent
                    )
                    if (!config.autoQuietMode && !CustomAlarmSoundHelper.isInQuietHours(this)) {
                        CustomAlarmSoundHelper.playAlarmTone(this, durationMs = 2500L)
                    }
                }
            }
            else -> {
                // Diğer durumlarda işlem yok
            }
        }
    }
}
