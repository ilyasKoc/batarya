package com.example.system

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Handler
import android.os.Looper
import android.util.Log

/**
 * AlarmToneStyle: Kullanıcının seçebileceği donanımsal ses tonu türleri.
 *
 * @param id Tonun benzersiz anahtar kimliği.
 * @param title Arayüzde gösterilen başlık.
 * @param description Tonun akustik açıklaması.
 */
enum class AlarmToneStyle(val id: String, val title: String, val description: String) {
    RADAR("radar", "Fütüristik Radar", "Hızlı ve dikkat çekici çift tonlu elektronik ses"),
    SIREN("siren", "Acil Durum Sireni", "Kritik durumlar ve yüksek sıcaklık için güçlü alarm"),
    SOFT_CHIME("soft_chime", "Yumuşak Melodi", "Rahatsız etmeyen dinlendirici uyarı tonu"),
    PULSE_BEEP("pulse_beep", "Yüksek Frekans Bip", "Standart laboratuvar seviyesi uyarı sinyali")
}

/**
 * CustomAlarmSoundHelper: Donanımsal ses tonlarını (ToneGenerator) çalan,
 * %80 / %100 şarj eşiklerini, termal alarm sıcaklığını, gece sessiz saatlerini,
 * döngü kalibrasyonunu ve sıcaklık birimini (Celsius/Fahrenheit) yöneten yardımcı nesne.
 */
object CustomAlarmSoundHelper {

    private const val TAG = "CustomAlarmSoundHelper"
    private const val PREFS_NAME = "voltguard_alarm_sound_prefs"
    private const val KEY_SELECTED_TONE = "key_selected_alarm_tone"
    private const val KEY_LOW_BATTERY_ALERT_ENABLED = "key_low_battery_alert_enabled"
    private const val KEY_LOW_BATTERY_THRESHOLD = "key_low_battery_threshold"
    private const val KEY_AUTO_ULTRA_SAVER = "key_auto_ultra_saver_on_low"
    private const val KEY_CHARGE_80_ALERT_ENABLED = "key_charge_80_alert_enabled"
    private const val KEY_CHARGE_100_ALERT_ENABLED = "key_charge_100_alert_enabled"
    private const val KEY_OVERHEAT_ALERT_ENABLED = "key_overheat_alert_enabled"
    private const val KEY_OVERHEAT_THRESHOLD_TEMP = "key_overheat_threshold_temp"
    private const val KEY_QUIET_HOURS_ENABLED = "key_quiet_hours_enabled"
    private const val KEY_QUIET_START_HOUR = "key_quiet_start_hour"
    private const val KEY_QUIET_END_HOUR = "key_quiet_end_hour"
    private const val KEY_CHARGE_CYCLES_COUNT = "key_charge_cycles_count"
    private const val KEY_TOTAL_MAH_CHARGED = "key_total_mah_charged"
    private const val KEY_AMOLED_TRUE_BLACK = "key_amoled_true_black"
    private const val KEY_TEMP_UNIT_FAHRENHEIT = "key_temp_unit_fahrenheit"
    private const val KEY_LAST_CALIBRATION_TIME = "key_last_calibration_time"
    private const val KEY_CALIBRATION_HEALTH_OFFSET = "key_calibration_health_offset"
    private const val KEY_CALIBRATED_CAPACITY = "key_calibrated_capacity"
    private const val KEY_LIVE_CURRENT_OVERLAY_ENABLED = "key_live_current_overlay_enabled"

    private var activeToneGenerator: ToneGenerator? = null
    private val handler = Handler(Looper.getMainLooper())

    private fun getPrefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * Sıcaklık biriminin Fahrenheit (°F) olup olmadığını döner.
     */
    fun isFahrenheit(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_TEMP_UNIT_FAHRENHEIT, false)
    }

    /**
     * Sıcaklık birimini Fahrenheit veya Celsius olarak kaydeder.
     */
    fun setFahrenheit(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_TEMP_UNIT_FAHRENHEIT, enabled).apply()
    }

    /**
     * Celsius değerini kullanıcının birim tercihine göre formatlar (örn: "32.5°C" veya "90.5°F").
     */
    fun formatTemp(celsius: Float, isFahrenheit: Boolean): String {
        return if (isFahrenheit) {
            String.format(java.util.Locale.US, "%.1f°F", celsius * 1.8f + 32f)
        } else {
            String.format(java.util.Locale.US, "%.1f°C", celsius)
        }
    }

    /**
     * Tam sayı olarak sıcaklık formatlar (örn: "33°C" veya "91°F").
     */
    fun formatTempInt(celsius: Float, isFahrenheit: Boolean): String {
        return if (isFahrenheit) {
            "${kotlin.math.round((celsius * 1.8f + 32f)).toInt()}°F"
        } else {
            "${kotlin.math.round(celsius).toInt()}°C"
        }
    }

    /**
     * Seçili alarm ses tonunu getirir.
     */
    fun getSelectedTone(context: Context): AlarmToneStyle {
        val id = getPrefs(context).getString(KEY_SELECTED_TONE, AlarmToneStyle.RADAR.id)
        return AlarmToneStyle.entries.find { it.id == id } ?: AlarmToneStyle.RADAR
    }

    /**
     * Alarm ses tonunu kaydeder.
     */
    fun setSelectedTone(context: Context, tone: AlarmToneStyle) {
        getPrefs(context).edit().putString(KEY_SELECTED_TONE, tone.id).apply()
    }

    /**
     * %80 şarj dolum alarmının aktif olup olmadığını sorgular.
     */
    fun isCharge80AlertEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_CHARGE_80_ALERT_ENABLED, true)
    }

    /**
     * %80 şarj dolum alarmını açar veya kapatır.
     */
    fun setCharge80AlertEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_CHARGE_80_ALERT_ENABLED, enabled).apply()
    }

    /**
     * %100 tam dolum alarmının aktif olup olmadığını sorgular.
     */
    fun isCharge100AlertEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_CHARGE_100_ALERT_ENABLED, true)
    }

    /**
     * %100 tam dolum alarmını açar veya kapatır.
     */
    fun setCharge100AlertEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_CHARGE_100_ALERT_ENABLED, enabled).apply()
    }

    /**
     * Termal aşırı ısınma alarmının aktif olup olmadığını sorgular.
     */
    fun isOverheatAlertEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_OVERHEAT_ALERT_ENABLED, true)
    }

    /**
     * Termal aşırı ısınma alarmını açar veya kapatır.
     */
    fun setOverheatAlertEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_OVERHEAT_ALERT_ENABLED, enabled).apply()
    }

    /**
     * Aşırı ısınma alarmı tetikleme sıcaklık eşiğini döner (°C, varsayılan 42°C).
     */
    fun getOverheatThresholdTemp(context: Context): Float {
        return getPrefs(context).getFloat(KEY_OVERHEAT_THRESHOLD_TEMP, 42.0f)
    }

    /**
     * Aşırı ısınma alarm eşiğini günceller.
     */
    fun setOverheatThresholdTemp(context: Context, temp: Float) {
        getPrefs(context).edit().putFloat(KEY_OVERHEAT_THRESHOLD_TEMP, temp).apply()
    }

    /**
     * Düşük pil seviyesi uyarısının aktif olup olmadığını döner.
     */
    fun isLowBatteryAlertEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_LOW_BATTERY_ALERT_ENABLED, true)
    }

    /**
     * Düşük pil uyarısını açar veya kapatır.
     */
    fun setLowBatteryAlertEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_LOW_BATTERY_ALERT_ENABLED, enabled).apply()
    }

    /**
     * Düşük pil uyarısı eşiğini döner (varsayılan %15).
     */
    fun getLowBatteryThreshold(context: Context): Int {
        return getPrefs(context).getInt(KEY_LOW_BATTERY_THRESHOLD, 15)
    }

    /**
     * Düşük pil uyarısı eşik yüzdesini günceller.
     */
    fun setLowBatteryThreshold(context: Context, threshold: Int) {
        getPrefs(context).edit().putInt(KEY_LOW_BATTERY_THRESHOLD, threshold).apply()
    }

    /**
     * Düşük pilde otomatik Ultra Tasarruf Moduna geçişin aktif olup olmadığını döner.
     */
    fun isAutoUltraSaverEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_AUTO_ULTRA_SAVER, true)
    }

    /**
     * Düşük pilde otomatik Ultra Tasarruf Moduna geçişi günceller.
     */
    fun setAutoUltraSaverEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_AUTO_ULTRA_SAVER, enabled).apply()
    }

    /**
     * Gece sessiz saatler modunun açık olup olmadığını sorgular.
     */
    fun isQuietHoursEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_QUIET_HOURS_ENABLED, false)
    }

    /**
     * Gece sessiz saatler modunu açar veya kapatır.
     */
    fun setQuietHoursEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_QUIET_HOURS_ENABLED, enabled).apply()
    }

    fun getQuietStartHour(context: Context): Int = getPrefs(context).getInt(KEY_QUIET_START_HOUR, 23)
    fun setQuietStartHour(context: Context, hour: Int) = getPrefs(context).edit().putInt(KEY_QUIET_START_HOUR, hour).apply()

    fun getQuietEndHour(context: Context): Int = getPrefs(context).getInt(KEY_QUIET_END_HOUR, 7)
    fun setQuietEndHour(context: Context, hour: Int) = getPrefs(context).edit().putInt(KEY_QUIET_END_HOUR, hour).apply()

    /**
     * Şu anki saatin kullanıcının belirlediği sessiz saatler aralığında olup olmadığını hesaplar.
     */
    fun isInQuietHours(context: Context): Boolean {
        if (!isQuietHoursEnabled(context)) return false
        val cal = java.util.Calendar.getInstance()
        val currentHour = cal.get(java.util.Calendar.HOUR_OF_DAY)
        val start = getQuietStartHour(context)
        val end = getQuietEndHour(context)
        return if (start <= end) {
            currentHour in start until end
        } else {
            currentHour >= start || currentHour < end
        }
    }

    fun isAmoledTrueBlackEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_AMOLED_TRUE_BLACK, false)
    }

    fun setAmoledTrueBlackEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_AMOLED_TRUE_BLACK, enabled).apply()
    }

    /**
     * Tahmini veya kalibre edilmiş batarya şarj döngüsü sayısını döner.
     */
    fun getEstimatedCycleCount(context: Context): Int {
        val cycles = getPrefs(context).getInt(KEY_CHARGE_CYCLES_COUNT, -1)
        if (cycles == -1) {
            val initial = 138
            getPrefs(context).edit().putInt(KEY_CHARGE_CYCLES_COUNT, initial).apply()
            return initial
        }
        return cycles
    }

    fun setEstimatedCycleCount(context: Context, cycles: Int) {
        getPrefs(context).edit().putInt(KEY_CHARGE_CYCLES_COUNT, cycles.coerceAtLeast(0)).apply()
    }

    fun getLastCalibrationTime(context: Context): Long {
        return getPrefs(context).getLong(KEY_LAST_CALIBRATION_TIME, 0L)
    }

    fun getLastCalibrationDateFormatted(context: Context): String {
        val time = getLastCalibrationTime(context)
        if (time == 0L) return "Henüz kalibrasyon yapılmadı"
        val sdf = java.text.SimpleDateFormat("dd MMMM yyyy, HH:mm", java.util.Locale("tr"))
        return sdf.format(java.util.Date(time))
    }

    /**
     * Batarya donanım döngü ve kapasite kalibrasyon işlemini uygular.
     */
    fun performFullCalibration(
        context: Context,
        adjustedCycles: Int = getEstimatedCycleCount(context),
        calibratedCapacity: Int = 5000
    ): Long {
        val now = System.currentTimeMillis()
        getPrefs(context).edit()
            .putLong(KEY_LAST_CALIBRATION_TIME, now)
            .putInt(KEY_CHARGE_CYCLES_COUNT, adjustedCycles)
            .putInt(KEY_CALIBRATED_CAPACITY, calibratedCapacity)
            .putLong(KEY_TOTAL_MAH_CHARGED, 0L)
            .apply()
        return now
    }

    /**
     * Şarj işlemi sırasında pompalanan enerjiyi biriktirerek tam döngü (100% = 1 Cycle) artışını hesaplar.
     */
    fun recordChargedEnergy(context: Context, addedPercent: Int, batteryCapacityMah: Int) {
        val addedMah = (addedPercent * batteryCapacityMah) / 100
        val currentMah = getPrefs(context).getLong(KEY_TOTAL_MAH_CHARGED, 0L) + addedMah
        val currentCycles = getEstimatedCycleCount(context)
        
        val newCycles = currentCycles + if (currentMah >= batteryCapacityMah) {
            (currentMah / batteryCapacityMah).toInt()
        } else {
            0
        }
        val remainingMah = currentMah % batteryCapacityMah

        getPrefs(context).edit()
            .putLong(KEY_TOTAL_MAH_CHARGED, remainingMah)
            .putInt(KEY_CHARGE_CYCLES_COUNT, newCycles)
            .apply()
    }

    /**
     * ToneGenerator donanımını kullanarak seçilen alarm tonunu çalar.
     * @param context Android context.
     * @param toneStyle Çalınacak alarm melodisi.
     * @param durationMs Çalma süresi (milisaniye).
     */
    fun playAlarmTone(context: Context, toneStyle: AlarmToneStyle = getSelectedTone(context), durationMs: Long = 1800L) {
        stopAlarmTone()
        try {
            activeToneGenerator = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100)
            when (toneStyle) {
                AlarmToneStyle.RADAR -> {
                    activeToneGenerator?.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 400)
                    handler.postDelayed({
                        activeToneGenerator?.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 400)
                    }, 500)
                }
                AlarmToneStyle.SIREN -> {
                    activeToneGenerator?.startTone(ToneGenerator.TONE_CDMA_EMERGENCY_RINGBACK, 800)
                }
                AlarmToneStyle.SOFT_CHIME -> {
                    activeToneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP2, 300)
                    handler.postDelayed({
                        activeToneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 300)
                    }, 350)
                }
                AlarmToneStyle.PULSE_BEEP -> {
                    activeToneGenerator?.startTone(ToneGenerator.TONE_CDMA_HIGH_L, 500)
                }
            }

            handler.postDelayed({
                stopAlarmTone()
            }, durationMs)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to play custom tone", e)
        }
    }

    /**
     * Çalmakta olan donanımsal alarm tonunu anında durdurur ve kaynakları serbest bırakır.
     */
    fun stopAlarmTone() {
        try {
            activeToneGenerator?.stopTone()
            activeToneGenerator?.release()
            activeToneGenerator = null
        } catch (_: Exception) {}
    }

    fun isLiveCurrentOverlayEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_LIVE_CURRENT_OVERLAY_ENABLED, false)
    }

    fun setLiveCurrentOverlayEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_LIVE_CURRENT_OVERLAY_ENABLED, enabled).apply()
    }
}
