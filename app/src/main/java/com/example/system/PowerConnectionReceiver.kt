package com.example.system

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import android.util.Log
import com.example.MainActivity

/**
 * ⚡ Şarj Bağlantısı & Güç Olayları Alıcısı (PowerConnectionReceiver)
 * 
 * Şarj kablosu takıldığında (ACTION_POWER_CONNECTED), çıkarıldığında (ACTION_POWER_DISCONNECTED)
 * veya düşük pil eşiklerinde tetiklenir.
 * 
 * - Ekranı anında uyandırır (WakeLock).
 * - Ön plan koruma servisini ve watchdog'u canlı tutar.
 * - Anlık şarj gücü (Watt, Volt, Akım) ve doluluk seviyesini bildirimle kullanıcıya sunar.
 * - Ana ekran widget'larını anında günceller.
 */
class PowerConnectionReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "PowerConnectionReceiver"
        private const val WAKELOCK_TAG = "VoltGuard:PowerConnectionWakeLock"
    }

    @Suppress("DEPRECATION")
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return

        val action = intent.action
        Log.d(TAG, "PowerConnectionReceiver tetiklendi: $action")

        val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
        
        // Ekranın anında açılması için geçici ekran uyandırma kilidi (WakeLock) edin
        var screenWakeLock: PowerManager.WakeLock? = null
        try {
            val flags = PowerManager.SCREEN_BRIGHT_WAKE_LOCK or
                    PowerManager.ACQUIRE_CAUSES_WAKEUP or
                    PowerManager.ON_AFTER_RELEASE
            screenWakeLock = powerManager?.newWakeLock(flags, WAKELOCK_TAG)
            screenWakeLock?.setReferenceCounted(false)
            screenWakeLock?.acquire(10000L) // 10 saniye süreyle
        } catch (_: Exception) {
            try {
                screenWakeLock = powerManager?.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKELOCK_TAG)
                screenWakeLock?.setReferenceCounted(false)
                screenWakeLock?.acquire(15000L)
            } catch (_: Exception) {}
        }

        try {
            when (action) {
                Intent.ACTION_POWER_CONNECTED -> {
                    Log.d(TAG, "⚡ Şarj cihazı takıldı! Servis uyandırılıyor ve bildirim gönderiliyor.")
                    ScreenUsageHelper.notifyChargingStatusChanged(context, true)
                    
                    if (AutoStartHelper.isAutoWakeOnChargerEnabled(context)) {
                        // 1. Ön plan servisini başlat (Doğrudan veya AlarmManager ile garantiye al)
                        try {
                            VoltGuardService.startService(context)
                        } catch (e: Exception) {
                            Log.w(TAG, "Doğrudan startForegroundService hatası, AlarmManager ile tetikleniyor: ${e.message}")
                        }
                        AutoStartHelper.scheduleImmediateServiceStart(context)
                        AutoStartHelper.scheduleWatchdog(context)

                        // 2. Anlık şarj telemetrisini çek ve yüksek öncelikli bildirim göster
                        val batteryInfo = BatteryMonitor.getImmediateBatteryInfo(context)
                        NotificationHelper.sendPowerConnectedNotification(
                            context = context,
                            currentLevel = batteryInfo.percentage,
                            plugType = batteryInfo.plugType,
                            wattage = batteryInfo.chargingWattage
                        )

                        // 3. Kullanıcı arayüzünü ön plana getirme denemesi
                        try {
                            val launchIntent = Intent(context, MainActivity::class.java).apply {
                                this.action = Intent.ACTION_MAIN
                                addCategory(Intent.CATEGORY_LAUNCHER)
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                                        Intent.FLAG_ACTIVITY_SINGLE_TOP or
                                        Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                                putExtra(NotificationHelper.EXTRA_NAVIGATE_TAB, NotificationHelper.TAB_BATTERY)
                            }
                            context.startActivity(launchIntent)
                        } catch (e: Exception) {
                            Log.w(TAG, "Direkt aktivite başlatma sistemce kısıtlandı (Bildirim üzerinden açılabilir): ${e.message}")
                        }

                        // 4. Widget'ları güncelle
                        VoltGuardWidgetProvider.updateAllWidgets(context)
                    }
                }
                Intent.ACTION_POWER_DISCONNECTED -> {
                    Log.d(TAG, "⚡ Şarj kablosu çıkarıldı! Servis kesintisiz çalışmaya devam ediyor.")
                    ScreenUsageHelper.recordUnpluggedNow(context)
                    if (AutoStartHelper.isBackgroundProtectionEnabled(context)) {
                        VoltGuardService.startService(context)
                        AutoStartHelper.scheduleWatchdog(context)
                        VoltGuardWidgetProvider.updateAllWidgets(context)
                    }
                }
                Intent.ACTION_BATTERY_LOW,
                Intent.ACTION_BATTERY_OKAY -> {
                    Log.d(TAG, "Batarya eşik yayını ($action). Koruma servisi kontrol ediliyor.")
                    if (AutoStartHelper.isBackgroundProtectionEnabled(context)) {
                        VoltGuardService.startService(context)
                        AutoStartHelper.scheduleWatchdog(context)
                    }
                }
                Intent.ACTION_BOOT_COMPLETED,
                "android.intent.action.LOCKED_BOOT_COMPLETED",
                "android.intent.action.QUICKBOOT_POWERON",
                "com.htc.intent.action.QUICKBOOT_POWERON",
                Intent.ACTION_MY_PACKAGE_REPLACED,
                Intent.ACTION_REBOOT -> {
                    Log.d(TAG, "Cihaz açıldı veya paket güncellendi. Koruma başlatılıyor.")
                    if (AutoStartHelper.isBootStartEnabled(context) && AutoStartHelper.isBackgroundProtectionEnabled(context)) {
                        VoltGuardService.startService(context)
                        AutoStartHelper.scheduleWatchdog(context)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "PowerConnectionReceiver hatası: ${e.message}", e)
        } finally {
            try {
                if (screenWakeLock != null && screenWakeLock.isHeld) {
                    screenWakeLock.release()
                }
            } catch (_: Exception) {}
        }
    }
}
