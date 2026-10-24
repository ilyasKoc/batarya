package com.example.system

import android.content.Context
import android.content.SharedPreferences
import java.util.Locale

/**
 * 🌡️ Sıcaklık Gösterim Birimi
 */
enum class TemperatureUnit(val symbol: String, val title: String) {
    CELSIUS("°C", "Santigrat (°C)"),
    FAHRENHEIT("°F", "Fahrenhayt (°F)")
}

/**
 * 🌡️ Sıcaklık Birimi Tercih Yardımcısı (TemperatureUnitHelper)
 * 
 * Celsius (°C) ve Fahrenheit (°F) arasında dinamik dönüşüm ve formatlama sağlar.
 */
object TemperatureUnitHelper {
    private const val PREFS_NAME = "voltguard_temp_prefs"
    private const val KEY_UNIT = "pref_temp_unit"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Seçili sıcaklık birimini döndürür (Varsayılan Celsius).
     */
    fun getUnit(context: Context): TemperatureUnit {
        val saved = getPrefs(context).getString(KEY_UNIT, TemperatureUnit.CELSIUS.name)
        return try {
            TemperatureUnit.valueOf(saved ?: TemperatureUnit.CELSIUS.name)
        } catch (_: Exception) {
            TemperatureUnit.CELSIUS
        }
    }

    /**
     * Sıcaklık birimi tercihini kaydeder.
     */
    fun setUnit(context: Context, unit: TemperatureUnit) {
        getPrefs(context).edit().putString(KEY_UNIT, unit.name).apply()
    }

    /**
     * Celsius değerini hedef birime matematiksel olarak dönüştürür.
     */
    fun convertCelsius(celsius: Float, targetUnit: TemperatureUnit): Float {
        return when (targetUnit) {
            TemperatureUnit.CELSIUS -> celsius
            TemperatureUnit.FAHRENHEIT -> (celsius * 9f / 5f) + 32f
        }
    }

    /**
     * Sıcaklık değerini kullanıcının kayıtlı birim tercihine göre formatlar.
     */
    fun format(celsius: Float, context: Context): String {
        val unit = getUnit(context)
        val value = convertCelsius(celsius, unit)
        return "${String.format(Locale.US, "%.1f", value)}${unit.symbol}"
    }

    /**
     * Belirtilen birim parametresine göre sıcaklığı formatlar.
     */
    fun formatWithUnit(celsius: Float, unit: TemperatureUnit): String {
        val value = convertCelsius(celsius, unit)
        return "${String.format(Locale.US, "%.1f", value)}${unit.symbol}"
    }
}
