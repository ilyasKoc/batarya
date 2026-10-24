package com.example.system

import android.app.AppOpsManager
import android.app.usage.NetworkStats
import android.app.usage.NetworkStatsManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.os.Build
import android.os.Process
import com.example.data.entity.AppUsageDataEntity
import com.example.data.entity.TimelineEventEntity
import java.util.Calendar

/**
 * RealUsageStatsHelper: Android UsageStatsManager, NetworkStatsManager ve UsageEvents API'lerini
 * kullanarak cihazın gerçek ekran sürelerini, uygulama açılışlarını ve Wi-Fi / Mobil veri harcamalarını
 * sıfır hata ile toplayan yardımcı nesne.
 */
object RealUsageStatsHelper {

    /**
     * Kullanıcının uygulamaya 'Kullanım Erişimi' (Usage Access Permission) izni verip vermediğini doğrular.
     * @param context Android context.
     * @return İzin verilmişse true, verilmemişse false.
     */
    fun hasUsageAccess(context: Context): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as? AppOpsManager ?: return false
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        } else {
            @Suppress("DEPRECATION")
            appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    /**
     * İstenen periyot tipine ("DAILY", "WEEKLY", "MONTHLY") göre başlangıç zaman damgasını hesaplar.
     * @param periodType "DAILY" (bugün gece 00:00), "WEEKLY" (Pazartesi 00:00), "MONTHLY" (ayın 1'i 00:00).
     */
    fun getPeriodStartTime(periodType: String): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)

        return when (periodType) {
            "DAILY" -> {
                cal.timeInMillis
            }
            "WEEKLY" -> {
                val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
                val daysSinceMonday = if (dayOfWeek == Calendar.SUNDAY) 6 else dayOfWeek - Calendar.MONDAY
                cal.add(Calendar.DAY_OF_YEAR, -daysSinceMonday)
                cal.timeInMillis
            }
            "MONTHLY" -> {
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.timeInMillis
            }
            else -> {
                cal.timeInMillis
            }
        }
    }

    /**
     * Android UsageStatsManager ve NetworkStatsManager sistem servislerinden
     * tüm uygulamaların gerçek ön plan ekran sürelerini ve Wi-Fi / Mobil veri tüketimlerini okur.
     *
     * @param context Android context.
     * @param periodType "DAILY", "WEEKLY" veya "MONTHLY".
     * @return Tüketim miktarına göre sıralı AppUsageDataEntity listesi.
     */
    fun fetchRealAppUsages(context: Context, periodType: String): List<AppUsageDataEntity> {
        if (!hasUsageAccess(context)) return emptyList()

        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
            ?: return emptyList()
        val packageManager = context.packageManager
        val networkStatsManager = context.getSystemService(Context.NETWORK_STATS_SERVICE) as? NetworkStatsManager

        val now = System.currentTimeMillis()
        val startTime = getPeriodStartTime(periodType)

        val stats = try {
            usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_BEST,
                startTime,
                now
            ) ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }

        // Ön plan sürelerini paket adına göre topla
        val aggregatedForeground = mutableMapOf<String, Long>()
        for (item in stats) {
            if (item.totalTimeInForeground > 0) {
                aggregatedForeground[item.packageName] = (aggregatedForeground[item.packageName] ?: 0L) + item.totalTimeInForeground
            }
        }

        // UID bazında gerçek Wi-Fi ve Mobil veri trafiğini sorgula
        val uidWifiBytes = mutableMapOf<Int, Long>()
        val uidMobileBytes = mutableMapOf<Int, Long>()

        if (networkStatsManager != null) {
            try {
                val wifiStats = networkStatsManager.querySummary(
                    ConnectivityManager.TYPE_WIFI,
                    null,
                    startTime,
                    now
                )
                val bucket = NetworkStats.Bucket()
                while (wifiStats.hasNextBucket()) {
                    wifiStats.getNextBucket(bucket)
                    if (bucket.uid > 0) {
                        uidWifiBytes[bucket.uid] = (uidWifiBytes[bucket.uid] ?: 0L) + (bucket.rxBytes + bucket.txBytes)
                    }
                }
                wifiStats.close()
            } catch (_: Exception) {}

            try {
                val mobileStats = networkStatsManager.querySummary(
                    ConnectivityManager.TYPE_MOBILE,
                    null,
                    startTime,
                    now
                )
                val bucket = NetworkStats.Bucket()
                while (mobileStats.hasNextBucket()) {
                    mobileStats.getNextBucket(bucket)
                    if (bucket.uid > 0) {
                        uidMobileBytes[bucket.uid] = (uidMobileBytes[bucket.uid] ?: 0L) + (bucket.rxBytes + bucket.txBytes)
                    }
                }
                mobileStats.close()
            } catch (_: Exception) {}
        }

        val allPackages = mutableSetOf<String>()
        allPackages.addAll(aggregatedForeground.keys)

        val allNetworkUids = uidWifiBytes.keys + uidMobileBytes.keys
        for (uid in allNetworkUids) {
            val pkgs = try { packageManager.getPackagesForUid(uid) } catch (_: Exception) { null }
            if (pkgs != null) {
                for (p in pkgs) {
                    allPackages.add(p)
                }
            }
        }

        val result = mutableListOf<AppUsageDataEntity>()

        for (pkgName in allPackages) {
            val foregroundMillis = aggregatedForeground[pkgName] ?: 0L
            val minutes = (foregroundMillis / (60 * 1000)).toInt()

            var wifiBytes = 0L
            var mobileBytes = 0L

            try {
                val uid = packageManager.getPackageUid(pkgName, 0)
                if (uid > 0) {
                    val directWifi = uidWifiBytes[uid] ?: 0L
                    val directMob = uidMobileBytes[uid] ?: 0L

                    if (directWifi == 0L && directMob == 0L && networkStatsManager != null && foregroundMillis > 0) {
                        try {
                            val wifiBucket = networkStatsManager.queryDetailsForUid(
                                ConnectivityManager.TYPE_WIFI,
                                null,
                                startTime,
                                now,
                                uid
                            )
                            val itemBucket = NetworkStats.Bucket()
                            var wSum = 0L
                            while (wifiBucket.hasNextBucket()) {
                                wifiBucket.getNextBucket(itemBucket)
                                wSum += (itemBucket.rxBytes + itemBucket.txBytes)
                            }
                            wifiBucket.close()
                            wifiBytes = wSum

                            val mobileBucket = networkStatsManager.queryDetailsForUid(
                                ConnectivityManager.TYPE_MOBILE,
                                null,
                                startTime,
                                now,
                                uid
                            )
                            var mSum = 0L
                            while (mobileBucket.hasNextBucket()) {
                                mobileBucket.getNextBucket(itemBucket)
                                mSum += (itemBucket.rxBytes + itemBucket.txBytes)
                            }
                            mobileBucket.close()
                            mobileBytes = mSum
                        } catch (_: Exception) {}
                    } else {
                        val pkgsInUid = try { packageManager.getPackagesForUid(uid)?.size ?: 1 } catch (_: Exception) { 1 }
                        wifiBytes = if (pkgsInUid > 1) directWifi / pkgsInUid else directWifi
                        mobileBytes = if (pkgsInUid > 1) directMob / pkgsInUid else directMob
                    }
                }
            } catch (_: Exception) {}

            if (minutes <= 0 && wifiBytes <= 0L && mobileBytes <= 0L) continue

            val (appName, category) = resolveAppInfo(packageManager, pkgName)

            result.add(
                AppUsageDataEntity(
                    appName = appName,
                    packageName = pkgName,
                    periodType = periodType,
                    wifiBytes = wifiBytes,
                    mobileBytes = mobileBytes,
                    screenTimeMinutes = minutes,
                    category = category
                )
            )
        }

        return result.sortedByDescending { it.wifiBytes + it.mobileBytes }
    }

    /**
     * Android UsageEvents API üzerinden günün ekran açılma (SCREEN_INTERACTIVE),
     * ekran kilitlenme (SCREEN_NON_INTERACTIVE) ve uygulama kullanım (ACTIVITY_RESUMED/PAUSED) olaylarını okur.
     *
     * @param context Android context.
     * @param dayOffset Gün ofseti (0: Bugün, 1: Dün, 2: 2 gün önce).
     * @return Zaman damgasına göre sıralı TimelineEventEntity listesi.
     */
    fun fetchRealTimelineEvents(context: Context, dayOffset: Int): List<TimelineEventEntity> {
        if (!hasUsageAccess(context)) return emptyList()

        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
            ?: return emptyList()
        val packageManager = context.packageManager

        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        cal.add(Calendar.DAY_OF_YEAR, -dayOffset)

        val dayStart = cal.timeInMillis
        val dayEnd = if (dayOffset == 0) System.currentTimeMillis() else dayStart + (24 * 3600 * 1000L) - 1

        val events = try {
            usageStatsManager.queryEvents(dayStart, dayEnd)
        } catch (_: Exception) {
            null
        } ?: return emptyList()

        val timelineList = mutableListOf<TimelineEventEntity>()
        val appEvent = UsageEvents.Event()
        var lastResumedPkg: String? = null
        var lastResumedTime: Long = 0L
        var isScreenCurrentlyOn = false

        while (events.hasNextEvent()) {
            events.getNextEvent(appEvent)
            val eventType = appEvent.eventType
            val time = appEvent.timeStamp

            when (eventType) {
                UsageEvents.Event.SCREEN_INTERACTIVE -> {
                    if (!isScreenCurrentlyOn) {
                        isScreenCurrentlyOn = true
                        timelineList.add(
                            TimelineEventEntity(
                                timestamp = time,
                                eventType = "SCREEN_ON",
                                appName = "Sistem",
                                packageName = "android",
                                details = "Kilit açıldı",
                                dayOffset = dayOffset
                            )
                        )
                    }
                }
                UsageEvents.Event.SCREEN_NON_INTERACTIVE -> {
                    if (isScreenCurrentlyOn) {
                        isScreenCurrentlyOn = false
                        timelineList.add(
                            TimelineEventEntity(
                                timestamp = time,
                                eventType = "SCREEN_OFF",
                                appName = "Sistem",
                                packageName = "android",
                                details = "Ekran kilitlendi",
                                dayOffset = dayOffset
                            )
                        )
                    }
                }
                UsageEvents.Event.ACTIVITY_RESUMED -> {
                    val pkg = appEvent.packageName
                    lastResumedPkg = pkg
                    lastResumedTime = time
                }
                UsageEvents.Event.ACTIVITY_PAUSED -> {
                    val pkg = appEvent.packageName
                    if (pkg == lastResumedPkg && lastResumedTime > 0) {
                        val durationMs = time - lastResumedTime
                        val durationSecs = (durationMs / 1000).toInt().coerceAtLeast(1)
                        val durationMins = (durationMs / (60 * 1000)).toInt()
                        if (durationMs > 2_000L) {
                            val (appName, _) = resolveAppInfo(packageManager, pkg)
                            timelineList.add(
                                TimelineEventEntity(
                                    timestamp = lastResumedTime,
                                    eventType = "APP_USAGE",
                                    appName = appName,
                                    packageName = pkg,
                                    durationMinutes = durationMins,
                                    durationSeconds = durationSecs,
                                    details = "$appName kullanıldı",
                                    dayOffset = dayOffset
                                )
                            )
                        }
                    }
                    lastResumedPkg = null
                    lastResumedTime = 0L
                }
            }
        }

        return timelineList.sortedBy { it.timestamp }
    }

    private fun resolveAppInfo(packageManager: PackageManager, pkgName: String): Pair<String, String> {
        return AppNamesHelper.resolveAppInfo(packageManager, pkgName)
    }
}
