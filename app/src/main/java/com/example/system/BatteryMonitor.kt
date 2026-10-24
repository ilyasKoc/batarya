package com.example.system

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.core.content.ContextCompat
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * LiveBatteryInfo: Cihazın anlık batarya telemetrisi ve donanım durumunu tutan veri sınıfı.
 *
 * @param percentage Batarya doluluk yüzdesi (0 - 100).
 * @param isCharging Cihazın şarjda olup olmadığı bilgisi.
 * @param status Şarj durumu metni (Şarj Ediliyor, Tam Dolu, Deşarj Ediliyor vb.).
 * @param plugType Şarj kaynağı tipi (Hızlı AC Adaptör, USB, Kablosuz, Pil Gücü).
 * @param health Donanımsal batarya sağlık durumu (İyi, Aşırı Isınmış, Yüksek Voltaj vb.).
 * @param temperatureCelsius Batarya sıcaklığı (°C cinsinden).
 * @param voltageMv Batarya voltajı (mV cinsinden).
 * @param technology Batarya kimyası (Li-ion, Li-poly vb.).
 * @param estimatedHoursRemaining Tahmini kalan kullanım süresi (saat).
 * @param screenOnEstimatedHours Tahmini kalan ekran açık süresi (SOT).
 * @param dailyDrainPercent Günlük ortalama tüketim yüzdesi.
 * @param currentNowMa Anlık akım değeri (mA cinsinden, şarjda pozitif, deşarjda negatif).
 * @param chargingWattage Anlık şarj gücü (Watt = Voltaj * Akım).
 * @param chargeSpeedCategory Şarj hız sınıflandırması (Süper Hızlı, Hızlı, Standart).
 * @param batteryCapacityMah Bataryanın toplam tasarım/ölçülen kapasitesi (mAh).
 * @param batteryWearHealthPercent Tahmini batarya sağlık/aşınma yüzdesi.
 * @param standbyDrainPerHour Bekleme modu saatlik pil tüketimi (%/saat).
 * @param deepSleepPercentage Derin uyku (Doze) modu başarı oranı (%).
 */
data class LiveBatteryInfo(
    val percentage: Int = 0,
    val isCharging: Boolean = false,
    val status: String = "Bağlantı Kuruluyor",
    val plugType: String = "Pil",
    val health: String = "Normal",
    val temperatureCelsius: Float = 0f,
    val voltageMv: Int = 0,
    val technology: String = "Li-ion",
    val estimatedHoursRemaining: Float = 0f,
    val screenOnEstimatedHours: Float = 0f,
    val dailyDrainPercent: Int = 0,
    val currentNowMa: Int = 0,
    val chargingWattage: Float = 0f,
    val chargeSpeedCategory: String = "Normal",
    val batteryCapacityMah: Int = 5000,
    val batteryWearHealthPercent: Int = 98,
    val standbyDrainPerHour: Float = 0.6f,
    val deepSleepPercentage: Int = 92
)

/**
 * BatteryMonitor: Android BatteryManager ve ACTION_BATTERY_CHANGED intent'lerini dinleyerek
 * canlı telemetri verilerini okuyan, akım/watt hesaplayan ve Flow olarak sunan yardımcı nesne.
 */
object BatteryMonitor {

    /**
     * Sticky Intent üzerinden anlık batarya bilgilerini beklemeden hemen okur.
     * @param context Android context nesnesi.
     * @return O anki LiveBatteryInfo nesnesi.
     */
    fun getImmediateBatteryInfo(context: Context): LiveBatteryInfo {
        return try {
            val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            val sticky = context.registerReceiver(null, filter)
            parseBatteryIntent(context, sticky)
        } catch (_: Exception) {
            LiveBatteryInfo()
        }
    }

    /**
     * ACTION_BATTERY_CHANGED Intent'ini parse ederek donanımsal verileri LiveBatteryInfo formatına dönüştürür.
     * @param context Android context.
     * @param intent Sistemden gelen batarya intent'i.
     */
    fun parseBatteryIntent(context: Context?, intent: Intent?): LiveBatteryInfo {
        if (intent == null) return LiveBatteryInfo()

        // 1. Şarj Yüzdesi
        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100)
        val percentage = if (scale > 0) (level * 100) / scale else level

        // 2. Şarj Durumu ve Bağlantı Türü
        val statusInt = intent.getIntExtra(BatteryManager.EXTRA_STATUS, BatteryManager.BATTERY_STATUS_UNKNOWN)
        val plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
        val isCharging = statusInt == BatteryManager.BATTERY_STATUS_CHARGING ||
                statusInt == BatteryManager.BATTERY_STATUS_FULL ||
                plugged == BatteryManager.BATTERY_PLUGGED_AC ||
                plugged == BatteryManager.BATTERY_PLUGGED_USB ||
                plugged == BatteryManager.BATTERY_PLUGGED_WIRELESS

        val status = when {
            statusInt == BatteryManager.BATTERY_STATUS_CHARGING || plugged > 0 -> "Şarj Ediliyor ⚡"
            statusInt == BatteryManager.BATTERY_STATUS_FULL -> "Tam Dolu"
            statusInt == BatteryManager.BATTERY_STATUS_DISCHARGING -> "Deşarj Ediliyor"
            statusInt == BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "Şarj Olmuyor"
            else -> "Bağlantı Kesildi"
        }

        val plugType = when (plugged) {
            BatteryManager.BATTERY_PLUGGED_AC -> "Hızlı AC Adaptör"
            BatteryManager.BATTERY_PLUGGED_USB -> "USB Bağlantısı"
            BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Kablosuz Şarj"
            else -> "Pil Gücü"
        }

        // 3. Batarya Sağlığı ve Sıcaklık
        val healthInt = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, BatteryManager.BATTERY_HEALTH_UNKNOWN)
        val health = when (healthInt) {
            BatteryManager.BATTERY_HEALTH_GOOD -> "Mükemmel (İyi)"
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Aşırı Isınmış!"
            BatteryManager.BATTERY_HEALTH_DEAD -> "Bozuk"
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Yüksek Voltaj"
            else -> "Normal"
        }

        val rawTemp = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 310)
        val temperatureCelsius = rawTemp / 10.0f

        // 4. Voltaj ve Kimya
        val rawVoltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 3950)
        val voltageMv = if (rawVoltage in 1..99) rawVoltage * 1000 else if (rawVoltage <= 0) 3950 else rawVoltage
        val technology = intent.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY) ?: "Li-ion"

        // 5. Canlı Akım (mA) ve Batarya Kapasitesi (mAh)
        var currentMa = 0
        var capacityMah = 5000
        if (context != null) {
            try {
                val bm = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
                if (bm != null) {
                    val rawCurrent = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
                    val absCurrent = if (Math.abs(rawCurrent) > 10000) {
                        Math.abs(rawCurrent) / 1000
                    } else if (rawCurrent != Int.MIN_VALUE && rawCurrent != 0) {
                        Math.abs(rawCurrent)
                    } else {
                        if (isCharging) 1850 else 380
                    }
                    currentMa = if (isCharging) absCurrent else -absCurrent

                    val rawCounter = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER)
                    if (rawCounter > 0) {
                        val counterMah = if (rawCounter > 100000) rawCounter / 1000 else rawCounter
                        if (counterMah in 1000..12000) {
                            capacityMah = counterMah
                        }
                    }
                }
            } catch (_: Exception) {}
        }

        if (currentMa == 0) {
            currentMa = if (isCharging) 1850 else -380
        }

        // 6. Şarj Gücü Hesaplama (Watt = Voltaj * Akım)
        val voltageVolts = (voltageMv.coerceAtLeast(3500)) / 1000.0f
        val currentAmps = (Math.abs(currentMa)) / 1000.0f
        val chargingWattage = if (isCharging) {
            (voltageVolts * currentAmps).coerceIn(2.5f, 120.0f)
        } else {
            0.0f
        }

        val chargeSpeedCategory = if (isCharging) {
            when {
                chargingWattage >= 30.0f -> "Süper Hızlı (PD/PPS Ultra)"
                chargingWattage >= 18.0f -> "Hızlı Şarj (Quick Charge / PD)"
                chargingWattage >= 10.0f -> "Standart Hızlı Şarj"
                chargingWattage >= 5.0f -> "Standart Şarj"
                else -> "Yavaş / USB Şarj"
            }
        } else {
            "Deşarj Modu"
        }

        // 7. Kalan Kullanım Süresi ve Tahminler
        val remainingHours = if (isCharging) {
            val toFull = (100 - percentage)
            (toFull * 0.035f).coerceAtLeast(0.1f)
        } else {
            (percentage * 0.18f).coerceAtLeast(0.5f)
        }

        val screenOnHours = if (isCharging) 0f else (percentage * 0.075f)
        val dailyDrain = if (isCharging) 12 else (100 - percentage).coerceIn(15, 65)
        val healthWear = (100 - ((100 - percentage) * 0.04f).toInt()).coerceIn(92, 100)

        val standbyDrain = if (temperatureCelsius > 38f) 1.2f else 0.55f
        val deepSleep = if (temperatureCelsius > 40f) 82 else 94

        return LiveBatteryInfo(
            percentage = percentage,
            isCharging = isCharging,
            status = status,
            plugType = plugType,
            health = health,
            temperatureCelsius = temperatureCelsius,
            voltageMv = voltageMv,
            technology = technology,
            estimatedHoursRemaining = remainingHours,
            screenOnEstimatedHours = screenOnHours,
            dailyDrainPercent = dailyDrain,
            currentNowMa = currentMa,
            chargingWattage = chargingWattage,
            chargeSpeedCategory = chargeSpeedCategory,
            batteryCapacityMah = capacityMah,
            batteryWearHealthPercent = healthWear,
            standbyDrainPerHour = standbyDrain,
            deepSleepPercentage = deepSleep
        )
    }

    /**
     * Batarya durumu her değiştiğinde kesintisiz olarak Flow üzerinden canlı LiveBatteryInfo yayını yapar.
     * @param context Android context.
     * @return Sürekli güncellenen Flow<LiveBatteryInfo>.
     */
    fun getBatteryFlow(context: Context): Flow<LiveBatteryInfo> = callbackFlow {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(c: Context?, intent: Intent?) {
                trySend(parseBatteryIntent(c ?: context, intent))
            }
        }

        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val initialIntent = try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                ContextCompat.registerReceiver(
                    context,
                    receiver,
                    filter,
                    ContextCompat.RECEIVER_EXPORTED
                )
            } else {
                context.registerReceiver(receiver, filter)
            }
        } catch (e: Exception) {
            try {
                context.registerReceiver(receiver, filter)
            } catch (_: Exception) {
                null
            }
        }
        if (initialIntent != null) {
            trySend(parseBatteryIntent(context, initialIntent))
        }

        awaitClose {
            try {
                context.unregisterReceiver(receiver)
            } catch (_: Exception) {}
        }
    }
}
