package com.example.system

import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.data.db.AppDatabase
import com.example.data.entity.AppTimeLimitEntity
import com.example.data.entity.AppUsageDataEntity
import com.example.data.entity.BatterySnapshotEntity
import com.example.data.entity.FocusSessionEntity
import com.example.data.entity.TimelineEventEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * DatabaseStats: SQLite Room veritabanındaki toplam kayıt ve tahmini dosya boyutu istatistikleri.
 *
 * @param totalBatterySnapshots Toplam batarya telemetri kaydı.
 * @param totalTimelineEvents Toplam zaman çizelgesi olayı.
 * @param totalAppUsageRecords Toplam uygulama veri kullanım kaydı.
 * @param totalFocusSessions Toplam odaklanma/Pomodoro seansı.
 * @param totalAppLimits Toplam uygulama süre limiti.
 * @param estimatedSizeKb Tahmini veritabanı boyutu (KB).
 * @param lastBackupDate Son yedekleme tarihi formatlı metni.
 */
data class DatabaseStats(
    val totalBatterySnapshots: Int,
    val totalTimelineEvents: Int,
    val totalAppUsageRecords: Int,
    val totalFocusSessions: Int,
    val totalAppLimits: Int,
    val estimatedSizeKb: Float,
    val lastBackupDate: String
)

/**
 * DatabaseBackupHelper: Room veritabanı tablolarını ve kullanıcı ayarlarını
 * JSON formatında dışa aktaran (Export), içe aktararak geri yükleyen (Import)
 * ve paylaşım menüsünü (Share Intent) açan yardımcı nesne.
 */
object DatabaseBackupHelper {

    private const val TAG = "DatabaseBackupHelper"
    private const val PREFS_NAME = "voltguard_backup_prefs"
    private const val KEY_LAST_BACKUP_TIME = "key_last_backup_time"
    private const val KEY_AUTO_WEEKLY_BACKUP = "key_auto_weekly_backup_enabled"

    /**
     * Otomatik haftalık yedeklemenin aktif olup olmadığını döner.
     */
    fun isAutoWeeklyBackupEnabled(context: Context): Boolean {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_AUTO_WEEKLY_BACKUP, true)
    }

    /**
     * Otomatik haftalık yedekleme tercihini günceller.
     */
    fun setAutoWeeklyBackupEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putBoolean(KEY_AUTO_WEEKLY_BACKUP, enabled).apply()
    }

    /**
     * Son yapılan yedeklemenin tarihini formatlı Türkçe metin olarak döner.
     */
    fun getLastBackupDate(context: Context): String {
        val lastTime = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getLong(KEY_LAST_BACKUP_TIME, 0L)
        if (lastTime == 0L) return "Henüz yedek alınmadı"
        val sdf = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale("tr"))
        return sdf.format(Date(lastTime))
    }

    private fun markBackupCompleted(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putLong(KEY_LAST_BACKUP_TIME, System.currentTimeMillis()).apply()
    }

    /**
     * Veritabanındaki tüm tablolardaki satır sayılarını ve tahmini boyutu hesaplar.
     */
    suspend fun getDatabaseStats(context: Context): DatabaseStats = withContext(Dispatchers.IO) {
        val dao = AppDatabase.getDatabase(context).appDao()
        val snapshots = dao.getAllBatterySnapshotsDirect()
        val events = dao.getAllTimelineEventsDirect()
        val usage = dao.getAllAppUsageRecordsDirect()
        val focus = dao.getAllFocusSessionsDirect()
        val limits = dao.getAllAppTimeLimitsDirect()

        val totalRecords = snapshots.size + events.size + usage.size + focus.size + limits.size
        val estimatedSizeKb = (totalRecords * 160f) / 1024f

        DatabaseStats(
            totalBatterySnapshots = snapshots.size,
            totalTimelineEvents = events.size,
            totalAppUsageRecords = usage.size,
            totalFocusSessions = focus.size,
            totalAppLimits = limits.size,
            estimatedSizeKb = if (estimatedSizeKb < 5f) 8.4f else estimatedSizeKb,
            lastBackupDate = getLastBackupDate(context)
        )
    }

    /**
     * Tüm veritabanı kayıtlarını ve sistem ayarlarını okuyarak yapılandırılmış bir JSON metni üretir.
     * @param context Android context.
     * @return Biçimlendirilmiş JSON dizesi.
     */
    suspend fun exportDatabaseToJson(context: Context): String = withContext(Dispatchers.IO) {
        try {
            val dao = AppDatabase.getDatabase(context).appDao()
            val root = JSONObject()
            root.put("app", "VoltGuard")
            root.put("schemaVersion", 4)
            root.put("exportedAt", System.currentTimeMillis())
            root.put("exportDate", SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()))

            // 1. Battery Snapshots
            val snapshots = dao.getAllBatterySnapshotsDirect()
            val snapshotsArray = JSONArray()
            for (item in snapshots) {
                val obj = JSONObject()
                obj.put("timestamp", item.timestamp)
                obj.put("level", item.level)
                obj.put("isCharging", item.isCharging)
                obj.put("temperatureCelsius", item.temperatureCelsius.toDouble())
                obj.put("voltageMv", item.voltageMv)
                obj.put("status", item.status)
                snapshotsArray.put(obj)
            }
            root.put("batterySnapshots", snapshotsArray)

            // 2. Timeline Events
            val timelineEvents = dao.getAllTimelineEventsDirect()
            val timelineArray = JSONArray()
            for (item in timelineEvents) {
                val obj = JSONObject()
                obj.put("timestamp", item.timestamp)
                obj.put("eventType", item.eventType)
                obj.put("appName", item.appName)
                obj.put("packageName", item.packageName)
                obj.put("durationMinutes", item.durationMinutes)
                obj.put("durationSeconds", item.durationSeconds)
                obj.put("details", item.details)
                obj.put("dayOffset", item.dayOffset)
                timelineArray.put(obj)
            }
            root.put("timelineEvents", timelineArray)

            // 3. App Usage Records
            val appUsage = dao.getAllAppUsageRecordsDirect()
            val appUsageArray = JSONArray()
            for (item in appUsage) {
                val obj = JSONObject()
                obj.put("appName", item.appName)
                obj.put("packageName", item.packageName)
                obj.put("periodType", item.periodType)
                obj.put("wifiBytes", item.wifiBytes)
                obj.put("mobileBytes", item.mobileBytes)
                obj.put("screenTimeMinutes", item.screenTimeMinutes)
                obj.put("category", item.category)
                appUsageArray.put(obj)
            }
            root.put("appUsageRecords", appUsageArray)

            // 4. Focus Sessions
            val focusSessions = dao.getAllFocusSessionsDirect()
            val focusArray = JSONArray()
            for (item in focusSessions) {
                val obj = JSONObject()
                obj.put("timestamp", item.timestamp)
                obj.put("durationMinutes", item.durationMinutes)
                obj.put("sessionType", item.sessionType)
                obj.put("completed", item.completed)
                obj.put("distractionCount", item.distractionCount)
                obj.put("notes", item.notes)
                focusArray.put(obj)
            }
            root.put("focusSessions", focusArray)

            // 5. App Time Limits
            val limits = dao.getAllAppTimeLimitsDirect()
            val limitsArray = JSONArray()
            for (item in limits) {
                val obj = JSONObject()
                obj.put("packageName", item.packageName)
                obj.put("appName", item.appName)
                obj.put("limitMinutes", item.limitMinutes)
                obj.put("isEnabled", item.isEnabled)
                obj.put("alertThresholdMinutes", item.alertThresholdMinutes)
                limitsArray.put(obj)
            }
            root.put("appTimeLimits", limitsArray)

            // 6. Settings & Preferences
            val alarmPrefs = JSONObject()
            alarmPrefs.put("selectedTone", CustomAlarmSoundHelper.getSelectedTone(context).id)
            alarmPrefs.put("lowBatteryAlert", CustomAlarmSoundHelper.isLowBatteryAlertEnabled(context))
            alarmPrefs.put("lowBatteryThreshold", CustomAlarmSoundHelper.getLowBatteryThreshold(context))
            alarmPrefs.put("charge80Alert", CustomAlarmSoundHelper.isCharge80AlertEnabled(context))
            alarmPrefs.put("charge100Alert", CustomAlarmSoundHelper.isCharge100AlertEnabled(context))
            alarmPrefs.put("overheatAlert", CustomAlarmSoundHelper.isOverheatAlertEnabled(context))
            alarmPrefs.put("overheatThreshold", CustomAlarmSoundHelper.getOverheatThresholdTemp(context).toDouble())
            alarmPrefs.put("autoUltraSaver", CustomAlarmSoundHelper.isAutoUltraSaverEnabled(context))
            root.put("settingsPreferences", alarmPrefs)

            markBackupCompleted(context)
            root.toString(2)
        } catch (e: Exception) {
            Log.e(TAG, "Database export failed", e)
            "{\"error\": \"${e.message}\"}"
        }
    }

    /**
     * Dışarıdan sağlanan JSON yedek dosyasını ayrıştırarak veritabanı tablolarına geri yükler.
     * @param context Android context.
     * @param jsonStr Yedeğin JSON içeriği.
     * @return Başarılıysa true, hata oluşursa false.
     */
    suspend fun importDatabaseFromJson(context: Context, jsonStr: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val root = JSONObject(jsonStr)
            val dao = AppDatabase.getDatabase(context).appDao()

            // 1. Snapshots
            if (root.has("batterySnapshots")) {
                val arr = root.getJSONArray("batterySnapshots")
                val list = mutableListOf<BatterySnapshotEntity>()
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    list.add(
                        BatterySnapshotEntity(
                            timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                            level = obj.optInt("level", 50),
                            isCharging = obj.optBoolean("isCharging", false),
                            temperatureCelsius = obj.optDouble("temperatureCelsius", 28.0).toFloat(),
                            voltageMv = obj.optInt("voltageMv", 4000),
                            status = obj.optString("status", "Good")
                        )
                    )
                }
                if (list.isNotEmpty()) {
                    dao.insertBatterySnapshots(list)
                }
            }

            // 2. Timeline
            if (root.has("timelineEvents")) {
                val arr = root.getJSONArray("timelineEvents")
                val list = mutableListOf<TimelineEventEntity>()
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    list.add(
                        TimelineEventEntity(
                            timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                            eventType = obj.optString("eventType", "SCREEN_ON"),
                            appName = obj.optString("appName", "Sistem"),
                            packageName = obj.optString("packageName", "android"),
                            durationMinutes = obj.optInt("durationMinutes", 0),
                            durationSeconds = obj.optInt("durationSeconds", 0),
                            details = obj.optString("details", ""),
                            dayOffset = obj.optInt("dayOffset", 0)
                        )
                    )
                }
                if (list.isNotEmpty()) {
                    dao.insertTimelineEvents(list)
                }
            }

            // 3. App Usage
            if (root.has("appUsageRecords")) {
                val arr = root.getJSONArray("appUsageRecords")
                val list = mutableListOf<AppUsageDataEntity>()
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    list.add(
                        AppUsageDataEntity(
                            appName = obj.optString("appName", "Uygulama"),
                            packageName = obj.optString("packageName", "com.example"),
                            periodType = obj.optString("periodType", "DAILY"),
                            wifiBytes = obj.optLong("wifiBytes", 0L),
                            mobileBytes = obj.optLong("mobileBytes", 0L),
                            screenTimeMinutes = obj.optInt("screenTimeMinutes", 10),
                            category = obj.optString("category", "Genel")
                        )
                    )
                }
                if (list.isNotEmpty()) {
                    dao.insertAppUsageRecords(list)
                }
            }

            // 4. Focus Sessions
            if (root.has("focusSessions")) {
                val arr = root.getJSONArray("focusSessions")
                val list = mutableListOf<FocusSessionEntity>()
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    list.add(
                        FocusSessionEntity(
                            timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                            durationMinutes = obj.optInt("durationMinutes", 25),
                            sessionType = obj.optString("sessionType", "POMODORO_25"),
                            completed = obj.optBoolean("completed", true),
                            distractionCount = obj.optInt("distractionCount", 0),
                            notes = obj.optString("notes", "")
                        )
                    )
                }
                if (list.isNotEmpty()) {
                    dao.insertFocusSessions(list)
                }
            }

            // 5. App Time Limits
            if (root.has("appTimeLimits")) {
                val arr = root.getJSONArray("appTimeLimits")
                val list = mutableListOf<AppTimeLimitEntity>()
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    list.add(
                        AppTimeLimitEntity(
                            packageName = obj.optString("packageName", "com.example.app"),
                            appName = obj.optString("appName", "Uygulama"),
                            limitMinutes = obj.optInt("limitMinutes", 45),
                            isEnabled = obj.optBoolean("isEnabled", true),
                            alertThresholdMinutes = obj.optInt("alertThresholdMinutes", 5)
                        )
                    )
                }
                if (list.isNotEmpty()) {
                    dao.insertAppTimeLimits(list)
                }
            }

            // 6. Settings
            if (root.has("settingsPreferences")) {
                val prefObj = root.getJSONObject("settingsPreferences")
                val toneId = prefObj.optString("selectedTone", AlarmToneStyle.RADAR.id)
                val tone = AlarmToneStyle.entries.find { it.id == toneId } ?: AlarmToneStyle.RADAR
                CustomAlarmSoundHelper.setSelectedTone(context, tone)
                CustomAlarmSoundHelper.setLowBatteryAlertEnabled(context, prefObj.optBoolean("lowBatteryAlert", true))
                CustomAlarmSoundHelper.setLowBatteryThreshold(context, prefObj.optInt("lowBatteryThreshold", 15))
                CustomAlarmSoundHelper.setCharge80AlertEnabled(context, prefObj.optBoolean("charge80Alert", true))
                CustomAlarmSoundHelper.setCharge100AlertEnabled(context, prefObj.optBoolean("charge100Alert", true))
                CustomAlarmSoundHelper.setOverheatAlertEnabled(context, prefObj.optBoolean("overheatAlert", true))
                CustomAlarmSoundHelper.setOverheatThresholdTemp(context, prefObj.optDouble("overheatThreshold", 42.0).toFloat())
                CustomAlarmSoundHelper.setAutoUltraSaverEnabled(context, prefObj.optBoolean("autoUltraSaver", true))
            }

            markBackupCompleted(context)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Database import failed", e)
            false
        }
    }

    /**
     * Oluşturulan JSON yedek metnini Android paylaşım iletişim kutusu üzerinden dışarıya aktarır.
     */
    fun shareBackupJson(context: Context, jsonStr: String) {
        try {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/json"
                putExtra(Intent.EXTRA_SUBJECT, "VoltGuard_Yedek_${SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())}.json")
                putExtra(Intent.EXTRA_TEXT, jsonStr)
            }
            context.startActivity(Intent.createChooser(intent, "VoltGuard Yedeğini Paylaş"))
        } catch (e: Exception) {
            Log.e(TAG, "Share failed", e)
        }
    }
}
