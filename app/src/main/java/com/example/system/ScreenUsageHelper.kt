package com.example.system

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.os.PowerManager
import android.os.SystemClock
import com.example.data.entity.TimelineEventEntity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * 📊 Ekran ve Batarya Süre İstatistikleri Modeli (ScreenBatteryStats)
 * 
 * Ekran açık (SOT - Screen On Time) ve bekleme (Standby) sürelerini,
 * hem gece 00:00'dan itibaren (Bugünkü Ekran) hem de cihaz son şarjdan
 * çıkarıldığından itibaren (Şarjdan Çıktıktan Sonra Alınan Ekran Süresi) ayrı ayrı tutar.
 */
data class ScreenBatteryStats(
    // 1. Gece 00:00'dan İtibaren (Bugün)
    val todayScreenOnMinutes: Int = 0,
    val todayStandbyMinutes: Int = 0,
    val formattedTodayScreenOn: String = "0 dk",
    val formattedTodayStandby: String = "0 dk",
    val todayScreenPercent: Int = 0,
    val todayStandbyPercent: Int = 100,
    val formattedTodayTotalElapsed: String = "0 dk",

    // 2. Şarjdan Çıktıktan Sonra (Son Şarjdan Beri Alınan Ekran Süresi)
    val sinceUnpluggedScreenOnMinutes: Int = 0,
    val sinceUnpluggedStandbyMinutes: Int = 0,
    val formattedSinceUnpluggedScreenOn: String = "0 dk",
    val formattedSinceUnpluggedStandby: String = "0 dk",
    val sinceUnpluggedScreenPercent: Int = 0,
    val sinceUnpluggedStandbyPercent: Int = 100,
    val lastUnpluggedTimestamp: Long = 0L,
    val formattedUnpluggedTime: String = "",
    val formattedSinceUnpluggedElapsed: String = "0 dk",
    val isCharging: Boolean = false
)

/**
 * ⏱️ Ekran Açık Kalma Süresi Hesaplayıcı (ScreenUsageHelper)
 * 
 * Android UsageEvents API (SCREEN_INTERACTIVE / SCREEN_NON_INTERACTIVE ve ACTIVITY_RESUMED / PAUSED)
 * ile gerçek zamanlı PowerManager durumunu birleştirerek:
 * 1) Gece 00:00'dan itibaren bugünkü ekran süresini,
 * 2) Şarjdan çıktıktan sonra alınan gerçek ekran süresini (SOT) milisaniye hassasiyetiyle hesaplar.
 */
object ScreenUsageHelper {

    private const val PREFS_NAME = "battery_screen_tracker_prefs"
    private const val KEY_LAST_UNPLUGGED_TIMESTAMP = "last_unplugged_timestamp"
    private const val KEY_PREV_IS_CHARGING = "prev_is_charging"
    private const val KEY_ACCUMULATED_UNPLUGGED_SCREEN_MS = "accumulated_unplugged_screen_ms"
    private const val KEY_LAST_INTERACTIVE_TICK_MS = "last_interactive_tick_ms"
    private const val KEY_WAS_SCREEN_INTERACTIVE = "was_screen_interactive"

    /**
     * Bugünün başlangıç anını (gece 00:00:00.000) milisaniye cinsinden hesaplar.
     */
    fun getTodayMidnightMillis(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    /**
     * Şarj durumu değiştiğinde (fişten çekildi / takıldı) son şarjdan çıkış zaman damgasını günceller.
     * Cihaz şarjdan çıkarıldığı anda ekran süresi sayacını sıfırdan başlatır.
     *
     * @param context Android context.
     * @param isCharging Güncel şarj durumu.
     */
    fun notifyChargingStatusChanged(context: Context, isCharging: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val prevCharging = prefs.getBoolean(KEY_PREV_IS_CHARGING, false)
        val now = System.currentTimeMillis()
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
        val isInteractive = powerManager?.isInteractive ?: true

        if (prevCharging && !isCharging) {
            // Şarj kablosu çıkarıldı -> şarjdan sonraki ekran süresi sayacını sıfırla ve başlat
            prefs.edit()
                .putLong(KEY_LAST_UNPLUGGED_TIMESTAMP, now)
                .putBoolean(KEY_PREV_IS_CHARGING, false)
                .putLong(KEY_ACCUMULATED_UNPLUGGED_SCREEN_MS, 0L)
                .putLong(KEY_LAST_INTERACTIVE_TICK_MS, now)
                .putBoolean(KEY_WAS_SCREEN_INTERACTIVE, isInteractive)
                .apply()
        } else if (!prevCharging && isCharging) {
            // Şarja takıldı -> mevcut ekran süresini güncelle ve şarj durumunu işaretle
            updateRealtimeInteractiveAccumulator(context, now)
            prefs.edit()
                .putBoolean(KEY_PREV_IS_CHARGING, true)
                .apply()
        }
    }

    /**
     * Kullanıcı sıfırlama butonuna bastığında veya şarjdan çıkarıldığında
     * anlık zaman damgasını "şarjdan çıkış anı" olarak kaydeder ve ekran süresini sıfırlar.
     */
    fun recordUnpluggedNow(context: Context) {
        val now = System.currentTimeMillis()
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
        val isInteractive = powerManager?.isInteractive ?: true
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putLong(KEY_LAST_UNPLUGGED_TIMESTAMP, now)
            .putBoolean(KEY_PREV_IS_CHARGING, false)
            .putLong(KEY_ACCUMULATED_UNPLUGGED_SCREEN_MS, 0L)
            .putLong(KEY_LAST_INTERACTIVE_TICK_MS, now)
            .putBoolean(KEY_WAS_SCREEN_INTERACTIVE, isInteractive)
            .apply()
    }

    /**
     * Ekran açıldığında (SCREEN_ON) veya kapandığında (SCREEN_OFF) canlı ekran sayacını günceller.
     */
    fun onScreenStateChanged(context: Context, isScreenOn: Boolean) {
        val now = System.currentTimeMillis()
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val unpluggedTime = getLastUnpluggedTimestamp(context)
        val lastTick = prefs.getLong(KEY_LAST_INTERACTIVE_TICK_MS, unpluggedTime).coerceAtLeast(unpluggedTime)
        val wasInteractive = prefs.getBoolean(KEY_WAS_SCREEN_INTERACTIVE, true)
        var accumulated = prefs.getLong(KEY_ACCUMULATED_UNPLUGGED_SCREEN_MS, 0L)

        if (wasInteractive && now > lastTick) {
            val delta = (now - lastTick).coerceAtMost(4 * 3600_000L)
            accumulated += delta
        }

        prefs.edit()
            .putLong(KEY_ACCUMULATED_UNPLUGGED_SCREEN_MS, accumulated)
            .putLong(KEY_LAST_INTERACTIVE_TICK_MS, now)
            .putBoolean(KEY_WAS_SCREEN_INTERACTIVE, isScreenOn)
            .apply()
    }

    /**
     * Ekran açıkken geçen süreyi milisaniye bazında kesintisiz biriktirir.
     */
    private fun updateRealtimeInteractiveAccumulator(context: Context, now: Long): Long {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val unpluggedTime = getLastUnpluggedTimestamp(context)
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
        val isInteractive = powerManager?.isInteractive ?: true

        val lastTick = prefs.getLong(KEY_LAST_INTERACTIVE_TICK_MS, unpluggedTime).coerceAtLeast(unpluggedTime)
        val wasInteractive = prefs.getBoolean(KEY_WAS_SCREEN_INTERACTIVE, isInteractive)
        var accumulated = prefs.getLong(KEY_ACCUMULATED_UNPLUGGED_SCREEN_MS, 0L)

        if ((wasInteractive || isInteractive) && now > lastTick) {
            val gap = now - lastTick
            // Eğer iki kontrol arası makul bir aktif kullanım aralığıysa veya ekran kesintisiz açıksa ekle
            if (isInteractive && gap <= 15 * 60_000L) {
                accumulated += gap
            }
        }

        prefs.edit()
            .putLong(KEY_ACCUMULATED_UNPLUGGED_SCREEN_MS, accumulated)
            .putLong(KEY_LAST_INTERACTIVE_TICK_MS, now)
            .putBoolean(KEY_WAS_SCREEN_INTERACTIVE, isInteractive)
            .apply()

        return accumulated.coerceIn(0L, (now - unpluggedTime).coerceAtLeast(0L))
    }

    /**
     * Cihazın şarjdan son çekilme zamanını getirir.
     */
    fun getLastUnpluggedTimestamp(context: Context): Long {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val saved = prefs.getLong(KEY_LAST_UNPLUGGED_TIMESTAMP, 0L)
        if (saved > 0L) return saved

        val bootTime = System.currentTimeMillis() - SystemClock.elapsedRealtime()
        val defaultTime = maxOf(bootTime, getTodayMidnightMillis())
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
        val isInteractive = powerManager?.isInteractive ?: true

        prefs.edit()
            .putLong(KEY_LAST_UNPLUGGED_TIMESTAMP, defaultTime)
            .putLong(KEY_LAST_INTERACTIVE_TICK_MS, defaultTime)
            .putBoolean(KEY_WAS_SCREEN_INTERACTIVE, isInteractive)
            .apply()
        return defaultTime
    }

    /**
     * Belirtilen [startTime, endTime] aralığındaki gerçek ekran açık (Screen-On) süresini hesaplar.
     *
     * - [startTime] anında ekran zaten açıksa (örneğin şarjdan çıkarıldığı sırada ekran açıksa),
     *   [startTime] öncesindeki son SCREEN_INTERACTIVE durumunu tespit ederek [startTime] anından
     *   itibaren ekran süresini eksiksiz sayar.
     * - Günlük toplam (isFullDayQuery = true) dışında günlük kümülatif UsageStats kovalarını
     *   karıştırmaz; böylece "Şarjdan Çıktıktan Sonra" süresi yalnızca şarjdan çıktıktan sonraki
     *   gerçek ekran süresini verir.
     *
     * @param context Android context.
     * @param startTime Başlangıç zaman damgası.
     * @param endTime Bitiş zaman damgası.
     * @param fallbackEvents Veritabanındaki yedek zaman çizelgesi olayları.
     * @param isFullDayQuery Gece 00:00'dan itibaren tam gün sorgusu olup olmadığı.
     * @return Ekran açık süresi (milisaniye).
     */
    fun calculateScreenOnMillis(
        context: Context,
        startTime: Long,
        endTime: Long,
        fallbackEvents: List<TimelineEventEntity> = emptyList(),
        isFullDayQuery: Boolean = false
    ): Long {
        if (startTime >= endTime) return 0L
        var resultMillis = 0L

        val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
        val isCurrentlyInteractive = powerManager?.isInteractive ?: false

        if (RealUsageStatsHelper.hasUsageAccess(context)) {
            try {
                val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
                if (usageStatsManager != null) {
                    // startTime anında ekranın zaten açık olup olmadığını bilmek için gece yarısından (veya 3 saat öncesinden) itibaren olayları tara
                    val lookbackStart = minOf(startTime, getTodayMidnightMillis())
                    val events = usageStatsManager.queryEvents(lookbackStart, endTime)
                    val event = UsageEvents.Event()

                    var screenInteractiveMillis = 0L
                    var currentInteractiveStart: Long? = null
                    var sawAnyInteractiveEvent = false

                    var activityForegroundMillis = 0L
                    var currentActivityStart: Long? = null

                    while (events != null && events.hasNextEvent()) {
                        events.getNextEvent(event)
                        val t = event.timeStamp

                        when (event.eventType) {
                            UsageEvents.Event.SCREEN_INTERACTIVE -> {
                                sawAnyInteractiveEvent = true
                                if (currentInteractiveStart == null) {
                                    currentInteractiveStart = t
                                }
                            }
                            UsageEvents.Event.SCREEN_NON_INTERACTIVE -> {
                                sawAnyInteractiveEvent = true
                                currentInteractiveStart?.let { onStart ->
                                    if (t > startTime) {
                                        val clippedStart = maxOf(onStart, startTime)
                                        val clippedEnd = minOf(t, endTime)
                                        if (clippedEnd > clippedStart) {
                                            screenInteractiveMillis += (clippedEnd - clippedStart)
                                        }
                                    }
                                    currentInteractiveStart = null
                                }
                            }
                            UsageEvents.Event.ACTIVITY_RESUMED -> {
                                if (currentActivityStart == null) {
                                    currentActivityStart = t
                                }
                            }
                            UsageEvents.Event.ACTIVITY_PAUSED,
                            UsageEvents.Event.ACTIVITY_STOPPED -> {
                                currentActivityStart?.let { actStart ->
                                    if (t > startTime) {
                                        val clippedStart = maxOf(actStart, startTime)
                                        val clippedEnd = minOf(t, endTime)
                                        if (clippedEnd > clippedStart) {
                                            activityForegroundMillis += (clippedEnd - clippedStart)
                                        }
                                    }
                                    currentActivityStart = null
                                }
                            }
                        }
                    }

                    // Eğer döngü sonunda ekran hâlâ açıksa, şu ana (endTime) kadar olan kısmı ekle
                    if (currentInteractiveStart != null && isCurrentlyInteractive) {
                        val clippedStart = maxOf(currentInteractiveStart, startTime)
                        if (endTime > clippedStart) {
                            screenInteractiveMillis += (endTime - clippedStart)
                        }
                    } else if (!sawAnyInteractiveEvent && isCurrentlyInteractive && !isFullDayQuery) {
                        // Hiç SCREEN_INTERACTIVE olayı gelmediyse ancak ekran şu an açıksa
                        if (currentActivityStart != null) {
                            val clippedStart = maxOf(currentActivityStart, startTime)
                            if (endTime > clippedStart) {
                                activityForegroundMillis += (endTime - clippedStart)
                            }
                        }
                    }

                    if (currentActivityStart != null && isCurrentlyInteractive) {
                        val clippedStart = maxOf(currentActivityStart, startTime)
                        if (endTime > clippedStart) {
                            activityForegroundMillis += (endTime - clippedStart)
                        }
                    }

                    resultMillis = maxOf(screenInteractiveMillis, activityForegroundMillis)

                    // SADECE tam gün (Bugünkü Ekran - 00:00'dan beri) sorgusunda günlük UsageStats toplamıyla doğrula.
                    // Şarjdan çıktıktan sonraki sorguda günlük kovalar tüm günü içerdiği için ASLA eklenmez!
                    if (isFullDayQuery) {
                        val stats = usageStatsManager.queryUsageStats(
                            UsageStatsManager.INTERVAL_DAILY,
                            startTime,
                            endTime
                        )
                        val totalForeground = stats?.filter {
                            it.totalTimeInForeground > 0 && it.lastTimeUsed >= startTime
                        }?.sumOf { it.totalTimeInForeground } ?: 0L

                        resultMillis = maxOf(resultMillis, totalForeground)
                    }
                }
            } catch (_: Exception) {
            }
        }

        // Yedek: Veritabanındaki timeline olaylarını [startTime, endTime] aralığına kırparak hesapla
        if (resultMillis <= 0L && fallbackEvents.isNotEmpty()) {
            val sortedAll = fallbackEvents.sortedBy { it.timestamp }
            var lastOn: Long? = null
            var screenPairMs = 0L

            for (ev in sortedAll) {
                if (ev.eventType == "SCREEN_ON") {
                    if (lastOn == null) lastOn = ev.timestamp
                } else if (ev.eventType == "SCREEN_OFF" && lastOn != null) {
                    if (ev.timestamp > startTime) {
                        val clippedStart = maxOf(lastOn, startTime)
                        val clippedEnd = minOf(ev.timestamp, endTime)
                        if (clippedEnd > clippedStart) {
                            screenPairMs += (clippedEnd - clippedStart)
                        }
                    }
                    lastOn = null
                }
            }
            if (lastOn != null && isCurrentlyInteractive) {
                val clippedStart = maxOf(lastOn, startTime)
                if (endTime > clippedStart) {
                    screenPairMs += (endTime - clippedStart)
                }
            }

            val windowAppEvents = fallbackEvents.filter { it.timestamp in startTime..endTime && it.eventType == "APP_USAGE" }
            val appUsageSecs = windowAppEvents.sumOf {
                if (it.durationSeconds > 0) it.durationSeconds else it.durationMinutes * 60
            }

            resultMillis = maxOf(appUsageSecs * 1000L, screenPairMs)
        }

        return resultMillis.coerceIn(0L, (endTime - startTime).coerceAtLeast(0L))
    }

    /**
     * Dakika ve saniye cinsinden süreyi Türkçe metin olarak formatlar (örn: "2 sa 15 dk" veya "4 dk 20 sn").
     */
    fun formatDurationTurkish(totalMinutes: Int, secondsRem: Int = 0): String {
        val safeMins = totalMinutes.coerceAtLeast(0)
        val safeSecs = secondsRem.coerceIn(0, 59)
        val hours = safeMins / 60
        val remMins = safeMins % 60

        return when {
            hours == 0 && remMins == 0 && safeSecs > 0 -> "$safeSecs sn"
            hours == 0 && remMins == 0 -> "0 dk"
            hours == 0 && remMins < 10 && safeSecs > 0 -> "$remMins dk $safeSecs sn"
            hours == 0 -> "$remMins dk"
            remMins == 0 -> "$hours sa"
            else -> "$hours sa $remMins dk"
        }
    }

    /**
     * Günün başından (00:00) ve şarjdan çıkıştan itibaren ekran açık / bekleme sürelerinin tamamını hesaplar.
     */
    fun calculateScreenBatteryStats(
        context: Context,
        isCharging: Boolean,
        fallbackEvents: List<TimelineEventEntity> = emptyList()
    ): ScreenBatteryStats {
        val now = System.currentTimeMillis()
        val midnight = getTodayMidnightMillis()
        val todayElapsedMs = (now - midnight).coerceAtLeast(1000L)
        val todayElapsedMinutes = (todayElapsedMs / 60000L).toInt()

        // Şarjdan çıkış zamanını ve canlı ekran sayacını güncelle
        notifyChargingStatusChanged(context, isCharging)
        val realtimeUnpluggedScreenMs = updateRealtimeInteractiveAccumulator(context, now)

        // 1. Bugün (00:00'dan itibaren) ekran süresi
        val todayScreenOnMs = maxOf(
            calculateScreenOnMillis(
                context = context,
                startTime = midnight,
                endTime = now,
                fallbackEvents = fallbackEvents,
                isFullDayQuery = true
            ),
            if (getLastUnpluggedTimestamp(context) >= midnight) realtimeUnpluggedScreenMs else 0L
        ).coerceAtMost(todayElapsedMs)

        val todayScreenOnMinutes = (todayScreenOnMs / 60000L).toInt()
        val todayScreenOnSecs = ((todayScreenOnMs % 60000L) / 1000L).toInt()

        val todayStandbyMinutes = (todayElapsedMinutes - todayScreenOnMinutes).coerceAtLeast(0)
        val todayTotalMins = (todayScreenOnMinutes + todayStandbyMinutes).coerceAtLeast(1)
        val todayScreenPercent = ((todayScreenOnMinutes * 100f) / todayTotalMins).toInt().coerceIn(0, 100)
        val todayStandbyPercent = 100 - todayScreenPercent

        // 2. Şarjdan Çıktıktan Sonra alınan ekran süresi
        val unpluggedTime = getLastUnpluggedTimestamp(context)
        val sinceUnpluggedMs = (now - unpluggedTime).coerceAtLeast(1000L)
        val sinceUnpluggedElapsedMinutes = (sinceUnpluggedMs / 60000L).toInt()
        val sinceUnpluggedElapsedSecs = ((sinceUnpluggedMs % 60000L) / 1000L).toInt()

        val usageEventsUnpluggedMs = calculateScreenOnMillis(
            context = context,
            startTime = unpluggedTime,
            endTime = now,
            fallbackEvents = fallbackEvents,
            isFullDayQuery = false
        )

        val sinceUnpluggedScreenOnMs = maxOf(usageEventsUnpluggedMs, realtimeUnpluggedScreenMs)
            .coerceIn(0L, sinceUnpluggedMs)

        val sinceUnpluggedScreenOnMinutes = (sinceUnpluggedScreenOnMs / 60000L).toInt()
        val sinceUnpluggedScreenOnSecs = ((sinceUnpluggedScreenOnMs % 60000L) / 1000L).toInt()

        val sinceUnpluggedStandbyMinutes = (sinceUnpluggedElapsedMinutes - sinceUnpluggedScreenOnMinutes).coerceAtLeast(0)
        val sinceUnpluggedTotalMins = (sinceUnpluggedScreenOnMinutes + sinceUnpluggedStandbyMinutes).coerceAtLeast(1)
        val sinceUnpluggedScreenPercent = ((sinceUnpluggedScreenOnMinutes * 100f) / sinceUnpluggedTotalMins).toInt().coerceIn(0, 100)
        val sinceUnpluggedStandbyPercent = 100 - sinceUnpluggedScreenPercent

        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val formattedUnpluggedTime = if (unpluggedTime > 0) timeFormat.format(Date(unpluggedTime)) else "00:00"

        return ScreenBatteryStats(
            todayScreenOnMinutes = todayScreenOnMinutes,
            todayStandbyMinutes = todayStandbyMinutes,
            formattedTodayScreenOn = formatDurationTurkish(todayScreenOnMinutes, todayScreenOnSecs),
            formattedTodayStandby = formatDurationTurkish(todayStandbyMinutes),
            todayScreenPercent = todayScreenPercent,
            todayStandbyPercent = todayStandbyPercent,
            formattedTodayTotalElapsed = formatDurationTurkish(todayElapsedMinutes),

            sinceUnpluggedScreenOnMinutes = sinceUnpluggedScreenOnMinutes,
            sinceUnpluggedStandbyMinutes = sinceUnpluggedStandbyMinutes,
            formattedSinceUnpluggedScreenOn = formatDurationTurkish(sinceUnpluggedScreenOnMinutes, sinceUnpluggedScreenOnSecs),
            formattedSinceUnpluggedStandby = formatDurationTurkish(sinceUnpluggedStandbyMinutes),
            sinceUnpluggedScreenPercent = sinceUnpluggedScreenPercent,
            sinceUnpluggedStandbyPercent = sinceUnpluggedStandbyPercent,
            lastUnpluggedTimestamp = unpluggedTime,
            formattedUnpluggedTime = formattedUnpluggedTime,
            formattedSinceUnpluggedElapsed = formatDurationTurkish(sinceUnpluggedElapsedMinutes, sinceUnpluggedElapsedSecs),
            isCharging = isCharging
        )
    }
}
