package com.example.system

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.os.SystemClock
import android.provider.Settings
import android.util.Log

/**
 * AutoStartHelper: VoltGuard'ın 7/24 arka planda kesintisiz çalışmasını,
 * cihaz yeniden başlatıldığında (Boot) otomatik ayağa kalkmasını,
 * Xiaomi (MIUI), Samsung, Huawei, Oppo/Realme, Vivo gibi agresif pil yöneticilerinde
 * otomatik başlatma (Auto-start) izinlerine yönlendirmeyi ve Watchdog bekçisini yöneten nesne.
 */
object AutoStartHelper {

    private const val TAG = "AutoStartHelper"
    private const val PREFS_NAME = "voltguard_autostart_prefs"
    private const val KEY_BACKGROUND_PROTECTION = "key_background_protection_enabled"
    private const val KEY_AUTOSTART_ON_BOOT = "key_autostart_on_boot"
    private const val KEY_AUTO_WAKE_CHARGER = "key_auto_wake_charger"
    private const val KEY_KEEP_ALIVE_LOCK = "key_keep_alive_lock"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * 7/24 kesintisiz arka plan korumasının aktif olup olmadığını sorgular.
     */
    fun isBackgroundProtectionEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_BACKGROUND_PROTECTION, true)
    }

    /**
     * Arka plan koruma servisini açar veya kapatır. Açıldığında VoltGuardService ve Watchdog'u başlatır.
     */
    fun setBackgroundProtectionEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_BACKGROUND_PROTECTION, enabled).apply()
        if (enabled) {
            VoltGuardService.startService(context)
            scheduleWatchdog(context)
        } else {
            VoltGuardService.stopService(context)
            cancelWatchdog(context)
        }
    }

    /**
     * Cihaz açılışında otomatik başlama (Boot Start) yetkisini sorgular.
     */
    fun isBootStartEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_AUTOSTART_ON_BOOT, true)
    }

    fun setBootStartEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_AUTOSTART_ON_BOOT, enabled).apply()
    }

    /**
     * Şarj cihazı takıldığında ekranı ve canlı izlemeyi otomatik uyandırma ayarını sorgular.
     */
    fun isAutoWakeOnChargerEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_AUTO_WAKE_CHARGER, true)
    }

    fun setAutoWakeOnChargerEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_AUTO_WAKE_CHARGER, enabled).apply()
    }

    fun isKeepAliveLockEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_KEEP_ALIVE_LOCK, true)
    }

    fun setKeepAliveLockEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_KEEP_ALIVE_LOCK, enabled).apply()
    }

    /**
     * Servisin ve bekçi alarmının aktif olduğundan emin olur (Canlandırma fonksiyonu).
     */
    fun ensureProtectionActive(context: Context) {
        try {
            VoltGuardService.startService(context)
            scheduleWatchdog(context)
        } catch (e: Exception) {
            Log.e(TAG, "ensureProtectionActive failed", e)
        }
    }

    /**
     * Cihazın Pil Optimizasyonlarından (Doze Whitelist) muaf tutulup tutulmadığını sorgular.
     */
    fun isIgnoringBatteryOptimizations(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
            powerManager?.isIgnoringBatteryOptimizations(context.packageName) ?: false
        } else {
            true
        }
    }

    /**
     * Kullanıcıya sistemin 'Pil Optimizasyonunu Yoksay' (Kısıtlamasız Çalışma) iletişim kutusunu açar.
     */
    fun requestIgnoreBatteryOptimizations(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                if (!isIgnoringBatteryOptimizations(context)) {
                    val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                        data = Uri.parse("package:${context.packageName}")
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                }
            } catch (e: Exception) {
                try {
                    val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                } catch (e2: Exception) {
                    Log.e(TAG, "Cannot open battery optimization settings", e2)
                }
            }
        }
    }

    /**
     * Xiaomi, Huawei, Samsung, Oppo, Vivo, OnePlus gibi üreticilerin özel 'Otomatik Başlatma' (Auto-start)
     * izin sayfasına yönlendirme yapar.
     * @return Başarıyla üretici sayfası açıldıysa true, aksi halde false.
     */
    fun openAutoStartSettings(context: Context): Boolean {
        val intents = listOf(
            // Xiaomi / Redmi / POCO MIUI Auto-start
            Intent().setComponent(ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity")),
            // Huawei / Honor
            Intent().setComponent(ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.optimize.process.ProtectActivity")),
            Intent().setComponent(ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity")),
            // Oppo / Realme ColorOS
            Intent().setComponent(ComponentName("com.coloros.safecenter", "com.coloros.safecenter.permission.startup.StartupAppListActivity")),
            Intent().setComponent(ComponentName("com.coloros.safecenter", "com.coloros.safecenter.startupapp.StartupAppListActivity")),
            Intent().setComponent(ComponentName("com.oppo.safe", "com.oppo.safe.permission.startup.StartupAppListActivity")),
            // Vivo
            Intent().setComponent(ComponentName("com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.BgStartUpManagerActivity")),
            Intent().setComponent(ComponentName("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity")),
            // Samsung Device Care / Auto-start
            Intent().setComponent(ComponentName("com.samsung.android.lool", "com.samsung.android.sm.ui.battery.BatteryActivity")),
            Intent().setComponent(ComponentName("com.samsung.android.sm", "com.samsung.android.sm.ui.battery.BatteryActivity")),
            // Asus
            Intent().setComponent(ComponentName("com.asus.mobilemanager", "com.asus.mobilemanager.autostart.AutoStartActivity")),
            // OnePlus
            Intent().setComponent(ComponentName("com.oneplus.security", "com.oneplus.security.chainlaunch.view.ChainLaunchAppListActivity"))
        )

        for (intent in intents) {
            try {
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                if (context.packageManager.queryIntentActivities(intent, 0).isNotEmpty()) {
                    context.startActivity(intent)
                    return true
                }
            } catch (_: Exception) {}
        }

        BackgroundAppsHelper.openAppDetailsSettings(context, context.packageName)
        return false
    }

    /**
     * Sistemin uygulamayı bellekten temizlemesi durumunda 15 dakikada bir kontrol eden bekçi (Watchdog) alarmını kurar.
     */
    fun scheduleWatchdog(context: Context) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
            val intent = Intent(context, RestartReceiver::class.java).apply {
                action = RestartReceiver.ACTION_WATCHDOG_PING
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                8899,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val intervalMs = 15 * 60 * 1000L
            val triggerAt = SystemClock.elapsedRealtime() + intervalMs

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAt, pendingIntent)
            } else {
                alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAt, pendingIntent)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to schedule watchdog", e)
        }
    }

    /**
     * Bekçi alarmını iptal eder.
     */
    private fun cancelWatchdog(context: Context) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
            val intent = Intent(context, RestartReceiver::class.java).apply {
                action = RestartReceiver.ACTION_WATCHDOG_PING
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                8899,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
        } catch (_: Exception) {}
    }

    /**
     * AlarmManager kullanarak anlık (50ms sonra) servis başlatma tetikler.
     * Bu yöntem, Android 12+ arka plan servis başlatma kısıtlamalarını (ForegroundServiceStartNotAllowedException) aşmak için kullanılır.
     */
    fun scheduleImmediateServiceStart(context: Context) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
            val intent = Intent(context, RestartReceiver::class.java).apply {
                action = RestartReceiver.ACTION_RESTART_SERVICE
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                9988,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val triggerAt = SystemClock.elapsedRealtime() + 50L
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAt, pendingIntent)
            } else {
                alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAt, pendingIntent)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to schedule immediate service start", e)
        }
    }
}
