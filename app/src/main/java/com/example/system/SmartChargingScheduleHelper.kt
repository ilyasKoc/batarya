package com.example.system

import android.content.Context
import android.content.SharedPreferences
import com.example.data.entity.BatterySnapshotEntity
import com.example.data.entity.TimelineEventEntity
import com.example.viewmodel.PowerMode
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class SmartChargingScheduleConfig(
    val autoPowerSaveEnabled: Boolean = true,
    val startHour: Int = 23,
    val startMinute: Int = 0,
    val endHour: Int = 7,
    val endMinute: Int = 0,
    val targetPowerMode: PowerMode = PowerMode.BALANCED,
    val smartChargeReminderEnabled: Boolean = true,
    val healthyChargeLimitEnabled: Boolean = true,
    val healthyChargeLimitPercent: Int = 85
)

enum class ForecastStateType {
    NORMAL,
    CHARGE_SLOT,
    POWER_SAVE,
    HIGH_DRAIN
}

data class HourlyForecastSlot(
    val hour: Int, // 0..23
    val predictedLevel: Int, // 0..100
    val stateType: ForecastStateType,
    val label: String
)

data class ChargingTimeSlot(
    val title: String,
    val startHour: Int,
    val startMinute: Int,
    val endHour: Int,
    val endMinute: Int,
    val formattedTimeRange: String,
    val reason: String,
    val targetLevelPercent: Int,
    val isRecommendedNow: Boolean
)

data class PowerSaveWindow(
    val title: String,
    val startHour: Int,
    val startMinute: Int,
    val endHour: Int,
    val endMinute: Int,
    val formattedTimeRange: String,
    val expectedGainMinutes: Int,
    val reason: String
)

data class SmartChargingAnalysisResult(
    val predictedDepletionTimeFormatted: String, // e.g. "Bugün 22:30"
    val hoursRemainingFormatted: String, // e.g. "7 saat 45 dk"
    val depletionWarningLevel: String, // "GÜVENLİ", "DİKKAT", "KRİTİK"
    val recommendedChargeSlots: List<ChargingTimeSlot>,
    val recommendedPowerSaveWindows: List<PowerSaveWindow>,
    val hourlyForecast: List<HourlyForecastSlot>,
    val healthAdvice: String,
    val routinePersona: String,
    val isCurrentlyInPowerSaveWindow: Boolean,
    val isCurrentlyInChargingWindow: Boolean
)

object SmartChargingScheduleHelper {

    private const val PREFS_NAME = "voltguard_charging_schedule"
    private const val KEY_AUTO_POWER_SAVE = "auto_power_save_enabled"
    private const val KEY_START_HOUR = "start_hour"
    private const val KEY_START_MIN = "start_min"
    private const val KEY_END_HOUR = "end_hour"
    private const val KEY_END_MIN = "end_min"
    private const val KEY_TARGET_MODE = "target_power_mode"
    private const val KEY_REMINDER_ENABLED = "smart_reminder_enabled"
    private const val KEY_HEALTHY_LIMIT = "healthy_limit_enabled"
    private const val KEY_HEALTHY_PERCENT = "healthy_limit_percent"

    fun loadConfig(context: Context): SmartChargingScheduleConfig {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val modeStr = prefs.getString(KEY_TARGET_MODE, PowerMode.BALANCED.name) ?: PowerMode.BALANCED.name
        val targetMode = try { PowerMode.valueOf(modeStr) } catch (_: Exception) { PowerMode.BALANCED }

        return SmartChargingScheduleConfig(
            autoPowerSaveEnabled = prefs.getBoolean(KEY_AUTO_POWER_SAVE, true),
            startHour = prefs.getInt(KEY_START_HOUR, 23),
            startMinute = prefs.getInt(KEY_START_MIN, 0),
            endHour = prefs.getInt(KEY_END_HOUR, 7),
            endMinute = prefs.getInt(KEY_END_MIN, 0),
            targetPowerMode = targetMode,
            smartChargeReminderEnabled = prefs.getBoolean(KEY_REMINDER_ENABLED, true),
            healthyChargeLimitEnabled = prefs.getBoolean(KEY_HEALTHY_LIMIT, true),
            healthyChargeLimitPercent = prefs.getInt(KEY_HEALTHY_PERCENT, 85)
        )
    }

    fun saveConfig(context: Context, config: SmartChargingScheduleConfig) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putBoolean(KEY_AUTO_POWER_SAVE, config.autoPowerSaveEnabled)
            .putInt(KEY_START_HOUR, config.startHour)
            .putInt(KEY_START_MIN, config.startMinute)
            .putInt(KEY_END_HOUR, config.endHour)
            .putInt(KEY_END_MIN, config.endMinute)
            .putString(KEY_TARGET_MODE, config.targetPowerMode.name)
            .putBoolean(KEY_REMINDER_ENABLED, config.smartChargeReminderEnabled)
            .putBoolean(KEY_HEALTHY_LIMIT, config.healthyChargeLimitEnabled)
            .putInt(KEY_HEALTHY_PERCENT, config.healthyChargeLimitPercent)
            .apply()
    }

    fun isTimeInWindow(
        currentHour: Int,
        currentMinute: Int,
        startHour: Int,
        startMinute: Int,
        endHour: Int,
        endMinute: Int
    ): Boolean {
        val currentMinutes = currentHour * 60 + currentMinute
        val startMinutes = startHour * 60 + startMinute
        val endMinutes = endHour * 60 + endMinute

        return if (startMinutes <= endMinutes) {
            currentMinutes in startMinutes until endMinutes
        } else {
            // Overnights (e.g. 23:00 to 07:00)
            currentMinutes >= startMinutes || currentMinutes < endMinutes
        }
    }

    fun analyzeSchedule(
        context: Context,
        liveBattery: LiveBatteryInfo,
        snapshots: List<BatterySnapshotEntity>,
        events: List<TimelineEventEntity>,
        config: SmartChargingScheduleConfig
    ): SmartChargingAnalysisResult {
        val cal = Calendar.getInstance()
        val currentHour = cal.get(Calendar.HOUR_OF_DAY)
        val currentMinute = cal.get(Calendar.MINUTE)

        // 1. Drain rate calculation (% per hour)
        var drainRatePerHour = 6.5f // Default estimate (~15 hours total life)
        if (snapshots.size >= 2) {
            val nonChargingSnaps = snapshots.filter { !it.isCharging }
            if (nonChargingSnaps.size >= 2) {
                val oldest = nonChargingSnaps.first()
                val newest = nonChargingSnaps.last()
                val diffHours = (newest.timestamp - oldest.timestamp) / 3600000.0f
                if (diffHours >= 0.5f) {
                    val drop = (oldest.level - newest.level).coerceAtLeast(1)
                    drainRatePerHour = (drop / diffHours).coerceIn(3.0f, 18.0f)
                }
            }
        }

        // 2. Predict depletion time (when battery reaches 15% threshold)
        val currentLevel = liveBattery.percentage.coerceIn(1, 100)
        val remainingPercentToCritical = (currentLevel - 15).coerceAtLeast(0)
        val hoursRemaining = if (liveBattery.isCharging) {
            // Charging to 100%
            val chargeRemaining = (100 - currentLevel).coerceAtLeast(0)
            chargeRemaining / 45.0f // ~45% per hour fast charge
        } else {
            remainingPercentToCritical / drainRatePerHour
        }

        val depletionCal = Calendar.getInstance().apply {
            add(Calendar.MINUTE, (hoursRemaining * 60).toInt())
        }
        val timeFmt = SimpleDateFormat("HH:mm", Locale.getDefault())
        val depletionTimeStr = if (liveBattery.isCharging) {
            "Tam Şarj: ${timeFmt.format(depletionCal.time)}"
        } else {
            val isTomorrow = depletionCal.get(Calendar.DAY_OF_YEAR) != cal.get(Calendar.DAY_OF_YEAR)
            val prefix = if (isTomorrow) "Yarın " else "Bugün "
            "$prefix${timeFmt.format(depletionCal.time)}"
        }

        val hoursRemainingText = if (liveBattery.isCharging) {
            val m = (hoursRemaining * 60).toInt()
            "${m / 60} sa ${m % 60} dk içinde tam şarj"
        } else {
            val m = (hoursRemaining * 60).toInt()
            "${m / 60} saat ${m % 60} dakika kaldı"
        }

        val warningLevel = when {
            liveBattery.isCharging -> "GÜVENLİ"
            currentLevel <= 20 -> "KRİTİK"
            currentLevel <= 40 -> "DİKKAT"
            else -> "GÜVENLİ"
        }

        // 3. Detect historical charging routines from events
        val chargingPlugHours = events.filter { it.eventType == "POWER_CONNECTED" }.map {
            val c = Calendar.getInstance().apply { timeInMillis = it.timestamp }
            c.get(Calendar.HOUR_OF_DAY)
        }

        val hasNightRoutine = chargingPlugHours.any { it in 22..23 || it in 0..2 } || chargingPlugHours.isEmpty()
        val hasAfternoonRoutine = chargingPlugHours.any { it in 13..16 }

        // Recommended Charge Slots
        val chargeSlots = mutableListOf<ChargingTimeSlot>()

        // Night slot recommendation
        val nightStartH = 22
        val nightEndH = 23
        val isNightNow = isTimeInWindow(currentHour, currentMinute, nightStartH, 30, 23, 45)
        chargeSlots.add(
            ChargingTimeSlot(
                title = "Akşam Ana Şarj Penceresi",
                startHour = nightStartH,
                startMinute = 30,
                endHour = nightEndH,
                endMinute = 45,
                formattedTimeRange = "22:30 - 23:45",
                reason = "Yatmadan önce cihazı %85 seviyesine şarj ederek gece boyu prizde aşırı beklemesini önleyin.",
                targetLevelPercent = config.healthyChargeLimitPercent,
                isRecommendedNow = isNightNow || (currentLevel <= 25 && currentHour >= 21)
            )
        )

        // Afternoon top-up recommendation if heavy usage
        val afternoonStartH = 14
        val afternoonEndH = 14
        val isAfternoonNow = isTimeInWindow(currentHour, currentMinute, afternoonStartH, 0, afternoonEndH, 30)
        chargeSlots.add(
            ChargingTimeSlot(
                title = "Öğleden Sonra Hızlı Destek",
                startHour = afternoonStartH,
                startMinute = 0,
                endHour = afternoonEndH,
                endMinute = 30,
                formattedTimeRange = "14:00 - 14:30",
                reason = "Günün ikinci yarısındaki yoğun toplantı ve kullanım öncesi 20 dakikalık hızlı şarj desteği.",
                targetLevelPercent = 80,
                isRecommendedNow = isAfternoonNow && currentLevel < 50
            )
        )

        // Recommended Power Save Windows
        val powerSaveWindows = mutableListOf<PowerSaveWindow>()

        // 1. Overnight window
        val nightPsRangeStr = String.format(Locale.getDefault(), "%02d:%02d - %02d:%02d", config.startHour, config.startMinute, config.endHour, config.endMinute)
        powerSaveWindows.add(
            PowerSaveWindow(
                title = "Gece Otomatik Derin Tasarruf",
                startHour = config.startHour,
                startMinute = config.startMinute,
                endHour = config.endHour,
                endMinute = config.endMinute,
                formattedTimeRange = nightPsRangeStr,
                expectedGainMinutes = 110,
                reason = "Uykudayken gereksiz arka plan senkronizasyonunu dondurarak sabaha kadar sıfır pil kaybı hedefler."
            )
        )

        // 2. Evening peak window
        powerSaveWindows.add(
            PowerSaveWindow(
                title = "Akşam Zirve Tasarrufu",
                startHour = 19,
                startMinute = 0,
                endHour = 21,
                endMinute = 30,
                formattedTimeRange = "19:00 - 21:30",
                expectedGainMinutes = 45,
                reason = "İşten dönüş ve prizden uzak saatlerde pilin kritik eşiğe düşmesini engeller."
            )
        )

        // 4. Build 24-hour visual forecast curve
        val hourlyForecast = mutableListOf<HourlyForecastSlot>()
        var simulatedLevel = currentLevel.toFloat()

        for (h in 0..23) {
            val isCurrentSlotHour = (h == currentHour)
            val isPowerSave = isTimeInWindow(h, 0, config.startHour, config.startMinute, config.endHour, config.endMinute)
            val isChargeSlot = chargeSlots.any { isTimeInWindow(h, 0, it.startHour, it.startMinute, it.endHour, it.endMinute) }
            val isPeakDrain = (h in 18..21) || (h in 12..14)

            val state = when {
                isChargeSlot -> ForecastStateType.CHARGE_SLOT
                isPowerSave -> ForecastStateType.POWER_SAVE
                isPeakDrain -> ForecastStateType.HIGH_DRAIN
                else -> ForecastStateType.NORMAL
            }

            // Estimate hourly level for the curve
            val hourDelta = h - currentHour
            val projected = if (liveBattery.isCharging && hourDelta > 0) {
                (currentLevel + (hourDelta * 40)).coerceAtMost(100)
            } else if (hourDelta > 0) {
                val drainMod = if (isPowerSave) 1.5f else if (isPeakDrain) 9.0f else drainRatePerHour
                (currentLevel - (hourDelta * drainMod)).toInt().coerceIn(5, 100)
            } else {
                // Past hour approximation
                (currentLevel + (kotlin.math.abs(hourDelta) * 5)).coerceIn(currentLevel, 100)
            }

            hourlyForecast.add(
                HourlyForecastSlot(
                    hour = h,
                    predictedLevel = projected,
                    stateType = state,
                    label = String.format(Locale.getDefault(), "%02d:00", h)
                )
            )
        }

        val inPowerSaveNow = isTimeInWindow(
            currentHour, currentMinute,
            config.startHour, config.startMinute,
            config.endHour, config.endMinute
        )

        val inChargeWindowNow = chargeSlots.any { it.isRecommendedNow }

        val routine = if (hasNightRoutine && hasAfternoonRoutine) {
            "Dengeli Günlük Rutin"
        } else if (hasNightRoutine) {
            "Gece Şarj Alışkanlığı"
        } else {
            "Dinamik Şarj Kullanıcısı"
        }

        val healthAdvice = if (config.healthyChargeLimitEnabled) {
            "Pil ömrünüzü 2 kata kadar uzatmak için cihazı %20 seviyesine düştüğünde şarja takıp %${config.healthyChargeLimitPercent} seviyesinde prizden çekmeniz önerilir."
        } else {
            "Gece boyu %100 seviyesinde prizde kalmak batarya hücrelerine voltaj stresi bindirir. Akıllı bildirimlerle %85 korumasını etkinleştirin."
        }

        return SmartChargingAnalysisResult(
            predictedDepletionTimeFormatted = depletionTimeStr,
            hoursRemainingFormatted = hoursRemainingText,
            depletionWarningLevel = warningLevel,
            recommendedChargeSlots = chargeSlots,
            recommendedPowerSaveWindows = powerSaveWindows,
            hourlyForecast = hourlyForecast,
            healthAdvice = healthAdvice,
            routinePersona = routine,
            isCurrentlyInPowerSaveWindow = inPowerSaveNow,
            isCurrentlyInChargingWindow = inChargeWindowNow
        )
    }
}
