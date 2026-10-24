package com.example.system

import android.content.Context
import android.content.SharedPreferences

/**
 * ⚡ Güç ve Servis Modülleri Durum Modeli (PowerFeatureState)
 * 
 * Uygulamanın arka plan servislerinin, watchdog alarmının, telemetri sorgulamasının,
 * ekran takibinin ve gece şarj korumasının tek tek açık/kapalı durumunu saklar.
 */
data class PowerFeatureState(
    val isServiceEnabled: Boolean = true,
    val isWatchdogEnabled: Boolean = true,
    val isLiveTelemetryPollingEnabled: Boolean = true,
    val isScreenUsageTrackingEnabled: Boolean = true,
    val isWidgetSyncEnabled: Boolean = true,
    val isSmartChargingAutomationEnabled: Boolean = true,
    val isThermalAndFullAlarmsEnabled: Boolean = true,
    val isNightGuardEnabled: Boolean = true
)

/**
 * 🎛️ Modüler Güç & Arka Plan Özellikleri Yapılandırıcısı (PowerFeatureConfigHelper)
 * 
 * Kullanıcının ihtiyaç duymadığı arka plan görevlerini kapatabilmesini (Eko / Dengeli / Tam İzleme)
 * ve batarya tasarrufunu en üst seviyeye çıkarmasını sağlayan ayar yöneticisi.
 */
object PowerFeatureConfigHelper {

    private const val PREFS_NAME = "voltguard_feature_power_prefs"

    private const val KEY_SERVICE_ENABLED = "key_feat_service_enabled"
    private const val KEY_WATCHDOG_ENABLED = "key_feat_watchdog_enabled"
    private const val KEY_LIVE_TELEMETRY_POLLING = "key_feat_live_telemetry_polling"
    private const val KEY_SCREEN_USAGE_TRACKING = "key_feat_screen_usage_tracking"
    private const val KEY_WIDGET_SYNC = "key_feat_widget_sync"
    private const val KEY_SMART_CHARGING_AUTOMATION = "key_feat_smart_charging_automation"
    private const val KEY_THERMAL_AND_FULL_ALARMS = "key_feat_thermal_and_full_alarms"
    private const val KEY_NIGHT_GUARD = "key_feat_night_guard"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Tüm modüllerin güncel durumlarını tek bir nesne olarak döndürür.
     */
    fun getAllStates(context: Context): PowerFeatureState {
        val prefs = getPrefs(context)
        return PowerFeatureState(
            isServiceEnabled = prefs.getBoolean(KEY_SERVICE_ENABLED, AutoStartHelper.isBackgroundProtectionEnabled(context)),
            isWatchdogEnabled = prefs.getBoolean(KEY_WATCHDOG_ENABLED, true),
            isLiveTelemetryPollingEnabled = prefs.getBoolean(KEY_LIVE_TELEMETRY_POLLING, false), // Isınmayı önlemek için varsayılan kapalı
            isScreenUsageTrackingEnabled = prefs.getBoolean(KEY_SCREEN_USAGE_TRACKING, true),
            isWidgetSyncEnabled = prefs.getBoolean(KEY_WIDGET_SYNC, true),
            isSmartChargingAutomationEnabled = prefs.getBoolean(KEY_SMART_CHARGING_AUTOMATION, true),
            isThermalAndFullAlarmsEnabled = prefs.getBoolean(KEY_THERMAL_AND_FULL_ALARMS, true),
            isNightGuardEnabled = prefs.getBoolean(KEY_NIGHT_GUARD, true)
        )
    }

    fun isServiceEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_SERVICE_ENABLED, AutoStartHelper.isBackgroundProtectionEnabled(context))
    }

    fun setServiceEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_SERVICE_ENABLED, enabled).apply()
        AutoStartHelper.setBackgroundProtectionEnabled(context, enabled)
    }

    fun isWatchdogEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_WATCHDOG_ENABLED, true)
    }

    fun setWatchdogEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_WATCHDOG_ENABLED, enabled).apply()
        if (enabled) {
            AutoStartHelper.scheduleWatchdog(context)
        } else {
            AutoStartHelper.setBackgroundProtectionEnabled(context, false)
        }
    }

    fun isLiveTelemetryPollingEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_LIVE_TELEMETRY_POLLING, false)
    }

    fun setLiveTelemetryPollingEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_LIVE_TELEMETRY_POLLING, enabled).apply()
    }

    fun isScreenUsageTrackingEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_SCREEN_USAGE_TRACKING, true)
    }

    fun setScreenUsageTrackingEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_SCREEN_USAGE_TRACKING, enabled).apply()
    }

    fun isWidgetSyncEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_WIDGET_SYNC, true)
    }

    fun setWidgetSyncEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_WIDGET_SYNC, enabled).apply()
    }

    fun isSmartChargingAutomationEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_SMART_CHARGING_AUTOMATION, true)
    }

    fun setSmartChargingAutomationEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_SMART_CHARGING_AUTOMATION, enabled).apply()
    }

    fun isThermalAndFullAlarmsEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_THERMAL_AND_FULL_ALARMS, true)
    }

    fun setThermalAndFullAlarmsEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_THERMAL_AND_FULL_ALARMS, enabled).apply()
    }

    fun isNightGuardEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_NIGHT_GUARD, true)
    }

    fun setNightGuardEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_NIGHT_GUARD, enabled).apply()
    }

    /**
     * Tek Dokunuşla "Ultra Eko Mod"
     * Arka plan tüketimini sıfırlamak için tüm arka plan servislerini ve periyodik döngüleri kapatır.
     */
    fun applyZeroDrainEcoPreset(context: Context) {
        getPrefs(context).edit()
            .putBoolean(KEY_SERVICE_ENABLED, false)
            .putBoolean(KEY_WATCHDOG_ENABLED, false)
            .putBoolean(KEY_LIVE_TELEMETRY_POLLING, false)
            .putBoolean(KEY_SCREEN_USAGE_TRACKING, false)
            .putBoolean(KEY_WIDGET_SYNC, false)
            .putBoolean(KEY_SMART_CHARGING_AUTOMATION, false)
            .putBoolean(KEY_THERMAL_AND_FULL_ALARMS, false)
            .putBoolean(KEY_NIGHT_GUARD, false)
            .apply()

        VoltGuardService.stopService(context)
        AutoStartHelper.setBackgroundProtectionEnabled(context, false)
    }

    /**
     * "Dengeli Mod"
     * Temel pil alarmları ve ekran takibini açık tutar, ağır telemetri yoklamasını kapatır.
     */
    fun applyBalancedPreset(context: Context) {
        getPrefs(context).edit()
            .putBoolean(KEY_SERVICE_ENABLED, true)
            .putBoolean(KEY_WATCHDOG_ENABLED, false)
            .putBoolean(KEY_LIVE_TELEMETRY_POLLING, false)
            .putBoolean(KEY_SCREEN_USAGE_TRACKING, true)
            .putBoolean(KEY_WIDGET_SYNC, true)
            .putBoolean(KEY_SMART_CHARGING_AUTOMATION, true)
            .putBoolean(KEY_THERMAL_AND_FULL_ALARMS, true)
            .putBoolean(KEY_NIGHT_GUARD, true)
            .apply()

        VoltGuardService.startService(context)
    }

    /**
     * "Tam İzleme Modu"
     * Tüm koruma, telemetri ve otomasyon özelliklerini aktif eder.
     */
    fun applyFullMonitoringPreset(context: Context) {
        getPrefs(context).edit()
            .putBoolean(KEY_SERVICE_ENABLED, true)
            .putBoolean(KEY_WATCHDOG_ENABLED, true)
            .putBoolean(KEY_LIVE_TELEMETRY_POLLING, true)
            .putBoolean(KEY_SCREEN_USAGE_TRACKING, true)
            .putBoolean(KEY_WIDGET_SYNC, true)
            .putBoolean(KEY_SMART_CHARGING_AUTOMATION, true)
            .putBoolean(KEY_THERMAL_AND_FULL_ALARMS, true)
            .putBoolean(KEY_NIGHT_GUARD, true)
            .apply()

        AutoStartHelper.setBackgroundProtectionEnabled(context, true)
    }
}
