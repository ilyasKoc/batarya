package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.entity.AppTimeLimitEntity
import com.example.data.entity.AppUsageDataEntity
import com.example.data.entity.AiCoachReportEntity
import com.example.data.entity.BatterySnapshotEntity
import com.example.data.entity.DataLimitConfigEntity
import com.example.data.entity.FocusSessionEntity
import com.example.data.entity.TimelineEventEntity
import com.example.data.entity.UserGamificationEntity
import kotlinx.coroutines.flow.Flow

/**
 * AppDao: VoltGuard uygulamasının Room Veritabanı Erişim Nesnesi (Data Access Object).
 * Batarya anlık görüntüleri, zaman çizelgesi olayları, uygulama veri tüketimleri,
 * dijital denge süre limitleri, odaklanma seansları ve AI koç raporlarını yönetir.
 */
@Dao
interface AppDao {

    // ==========================================
    // 1. BATARYA ANLIK GÖRÜNTÜLERİ (SNAPSHOTS)
    // ==========================================

    /**
     * En son kaydedilen batarya anlık görüntülerini belirtilen adet limitiyle getirir.
     * @param limit Getirilecek maksimum kayıt sayısı.
     * @return Azalan zaman damgasına göre sıralı Flow listesi.
     */
    @Query("SELECT * FROM battery_snapshots ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentBatterySnapshots(limit: Int): Flow<List<BatterySnapshotEntity>>

    /**
     * Belirli bir zaman damgasından (sinceTimestamp) itibaren kaydedilen batarya anlık görüntülerini getirir.
     * @param sinceTimestamp Başlangıç zaman damgası (milisaniye).
     * @return Artan zaman damgasına göre sıralı Flow listesi.
     */
    @Query("SELECT * FROM battery_snapshots WHERE timestamp >= :sinceTimestamp ORDER BY timestamp ASC")
    fun getBatterySnapshotsSince(sinceTimestamp: Long): Flow<List<BatterySnapshotEntity>>

    /**
     * Tek bir batarya anlık görüntüsünü veritabanına ekler veya günceller.
     * @param snapshot Eklenecek anlık görüntü varlığı.
     * @return Eklenen kaydın benzersiz satır ID'si.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBatterySnapshot(snapshot: BatterySnapshotEntity): Long

    /**
     * Birden fazla batarya anlık görüntüsünü toplu olarak veritabanına ekler.
     * @param snapshots Eklenecek anlık görüntü varlıkları listesi.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBatterySnapshots(snapshots: List<BatterySnapshotEntity>)

    // ==========================================
    // 2. ZAMAN ÇİZELGESİ VE GÜNLÜK OLAYLAR
    // ==========================================

    /**
     * Belirli bir gün ofsetine (0: Bugün, 1: Dün, vb.) ait zaman çizelgesi olaylarını getirir.
     * @param dayOffset Gün ofseti değeri.
     * @return Kronolojik olarak sıralanmış olaylar Flow listesi.
     */
    @Query("SELECT * FROM timeline_events WHERE dayOffset = :dayOffset ORDER BY timestamp ASC")
    fun getTimelineEventsForDay(dayOffset: Int): Flow<List<TimelineEventEntity>>

    /**
     * Tüm zaman çizelgesi olaylarını en yeniden eskiye doğru maksimum 200 adet olacak şekilde getirir.
     * @return Olaylar Flow listesi.
     */
    @Query("SELECT * FROM timeline_events ORDER BY timestamp DESC LIMIT 200")
    fun getAllTimelineEvents(): Flow<List<TimelineEventEntity>>

    /**
     * Tek bir zaman çizelgesi olayını (şarj takıldı, sıcaklık uyarısı, ekran açıldı vb.) kaydeder.
     * @param event Eklenecek olay nesnesi.
     * @return Eklenen kaydın ID'si.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimelineEvent(event: TimelineEventEntity): Long

    /**
     * Birden fazla zaman çizelgesi olayını toplu olarak kaydeder.
     * @param events Olay listesi.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimelineEvents(events: List<TimelineEventEntity>)

    /**
     * Belirli bir gün ofsetine ait zaman çizelgesi olaylarını temizler.
     * @param dayOffset Silinecek gün ofseti.
     */
    @Query("DELETE FROM timeline_events WHERE dayOffset = :dayOffset")
    suspend fun deleteTimelineEventsForDay(dayOffset: Int)

    // ==========================================
    // 3. UYGULAMA İNTERNET VERİ TÜKETİMİ
    // ==========================================

    /**
     * Belirtilen periyot tipine göre (DAILY, WEEKLY, MONTHLY) uygulamaların internet tüketim kayıtlarını getirir.
     * @param periodType Periyot tipi ("DAILY", "WEEKLY", "MONTHLY").
     * @return Toplam veri tüketimine göre azalan sıralı Flow listesi.
     */
    @Query("SELECT * FROM app_usage_records WHERE periodType = :periodType ORDER BY (wifiBytes + mobileBytes) DESC")
    fun getAppUsageByPeriod(periodType: String): Flow<List<AppUsageDataEntity>>

    /**
     * Uygulama internet kullanım kayıtlarını toplu olarak kaydeder.
     * @param records Kullanım kayıtları listesi.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppUsageRecords(records: List<AppUsageDataEntity>)

    /**
     * Belirli bir periyot tipine ait internet kullanım kayıtlarını temizler.
     * @param periodType Silinecek periyot tipi.
     */
    @Query("DELETE FROM app_usage_records WHERE periodType = :periodType")
    suspend fun deleteAppUsageByPeriod(periodType: String)

    // ==========================================
    // 4. VERİ KOTASI VE LİMİT AYARLARI
    // ==========================================

    /**
     * Cihazın genel mobil ve Wi-Fi veri kotası konfigürasyonunu getirir.
     * @return Veri kotası konfigürasyonu Flow nesnesi.
     */
    @Query("SELECT * FROM data_limit_configs WHERE id = 1 LIMIT 1")
    fun getDataLimitConfig(): Flow<DataLimitConfigEntity?>

    /**
     * Veri kotası ve sınırlandırma konfigürasyonunu ekler veya günceller.
     * @param config Güncellenecek veri kotası nesnesi.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateDataLimitConfig(config: DataLimitConfigEntity)

    // ==========================================
    // 5. UYGULAMA SÜRE VE DİJİTAL DENGE LİMİTLERİ
    // ==========================================

    /**
     * Tanımlanmış tüm uygulama günlük ekran süresi limitlerini uygulama ismine göre sıralı getirir.
     * @return Uygulama süre limitleri Flow listesi.
     */
    @Query("SELECT * FROM app_time_limits ORDER BY appName ASC")
    fun getAllAppTimeLimits(): Flow<List<AppTimeLimitEntity>>

    /**
     * Belirli bir paket adına sahip uygulamanın süre limitini getirir.
     * @param packageName Uygulamanın paket adı.
     * @return Uygulama limit nesnesi Flow'u.
     */
    @Query("SELECT * FROM app_time_limits WHERE packageName = :packageName LIMIT 1")
    fun getAppTimeLimit(packageName: String): Flow<AppTimeLimitEntity?>

    /**
     * Bir uygulama için günlük ekran süresi limitini ekler veya günceller.
     * @param limit Kaydedilecek limit nesnesi.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateAppTimeLimit(limit: AppTimeLimitEntity)

    /**
     * Uygulama süre limitlerini toplu olarak ekler veya günceller.
     * @param limits Limitler listesi.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppTimeLimits(limits: List<AppTimeLimitEntity>)

    /**
     * Belirli bir uygulamanın süre limitini siler.
     * @param packageName Silinecek uygulamanın paket adı.
     */
    @Query("DELETE FROM app_time_limits WHERE packageName = :packageName")
    suspend fun deleteAppTimeLimit(packageName: String)

    /**
     * Tanımlanmış tüm uygulama süre limitlerini temizler.
     */
    @Query("DELETE FROM app_time_limits")
    suspend fun clearAppTimeLimits()

    // ==========================================
    // 6. ODAKLANMA SEANSLARI (POMODORO / FOCUS)
    // ==========================================

    /**
     * Tamamlanmış ve devam eden tüm odaklanma seanslarını en yeniden eskiye doğru getirir.
     * @return Odaklanma seansları Flow listesi.
     */
    @Query("SELECT * FROM focus_sessions ORDER BY timestamp DESC")
    fun getAllFocusSessions(): Flow<List<FocusSessionEntity>>

    /**
     * Yeni bir odaklanma seansı kaydı oluşturur.
     * @param session Kaydedilecek seans nesnesi.
     * @return Eklenen seansın satır ID'si.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFocusSession(session: FocusSessionEntity): Long

    /**
     * Başarıyla tamamlanan toplam odaklanma süresini (dakika cinsinden) hesaplar.
     * @return Toplam odaklanma dakikası Flow'u.
     */
    @Query("SELECT SUM(durationMinutes) FROM focus_sessions WHERE completed = 1")
    fun getTotalCompletedFocusMinutes(): Flow<Int?>

    // ==========================================
    // 7. OYUNLAŞTIRMA VE BAŞARIM SİSTEMİ
    // ==========================================

    /**
     * Kullanıcının pil tasarrufu başarımları, seviyesi ve puan durumunu getirir.
     * @return Oyunlaştırma verisi Flow nesnesi.
     */
    @Query("SELECT * FROM user_gamification WHERE id = 1 LIMIT 1")
    fun getUserGamification(): Flow<UserGamificationEntity?>

    /**
     * Kullanıcı oyunlaştırma ve başarım verilerini kaydeder veya günceller.
     * @param gamification Güncellenecek oyunlaştırma varlığı.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateGamification(gamification: UserGamificationEntity)

    // ==========================================
    // 8. GEMINI DESTEKLİ AI KOÇ RAPORLARI
    // ==========================================

    /**
     * En son oluşturulan yapay zeka batarya koçu analiz raporunu getirir.
     * @return AI koç raporu Flow nesnesi.
     */
    @Query("SELECT * FROM ai_coach_reports WHERE id = 1 LIMIT 1")
    fun getAiCoachReport(): Flow<AiCoachReportEntity?>

    /**
     * Yapay zeka batarya koçu raporunu veritabanına kaydeder veya günceller.
     * @param report Kaydedilecek AI koç raporu.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateAiCoachReport(report: AiCoachReportEntity)

    // ==========================================
    // 9. VERİTABANI YEDEKLEME VE GERİ YÜKLEME (DIRECT / SYNCHRONOUS)
    // ==========================================

    /**
     * JSON/CSV yedekleme işlemi için tüm batarya anlık görüntülerini doğrudan liste olarak döner.
     */
    @Query("SELECT * FROM battery_snapshots ORDER BY timestamp ASC")
    suspend fun getAllBatterySnapshotsDirect(): List<BatterySnapshotEntity>

    /**
     * Yedekleme için tüm zaman çizelgesi olaylarını doğrudan liste olarak döner.
     */
    @Query("SELECT * FROM timeline_events ORDER BY timestamp ASC")
    suspend fun getAllTimelineEventsDirect(): List<TimelineEventEntity>

    /**
     * Yedekleme için tüm uygulama internet kullanım kayıtlarını doğrudan döner.
     */
    @Query("SELECT * FROM app_usage_records")
    suspend fun getAllAppUsageRecordsDirect(): List<AppUsageDataEntity>

    /**
     * Yedekleme için tüm odaklanma seanslarını doğrudan döner.
     */
    @Query("SELECT * FROM focus_sessions")
    suspend fun getAllFocusSessionsDirect(): List<FocusSessionEntity>

    /**
     * Yedekleme için tüm uygulama süre limitlerini doğrudan döner.
     */
    @Query("SELECT * FROM app_time_limits")
    suspend fun getAllAppTimeLimitsDirect(): List<AppTimeLimitEntity>

    /**
     * Yedekleme için oyunlaştırma durumunu doğrudan döner.
     */
    @Query("SELECT * FROM user_gamification WHERE id = 1 LIMIT 1")
    suspend fun getUserGamificationDirect(): UserGamificationEntity?

    /**
     * Yedekleme için en son AI koç raporunu doğrudan döner.
     */
    @Query("SELECT * FROM ai_coach_reports WHERE id = 1 LIMIT 1")
    suspend fun getAiCoachReportDirect(): AiCoachReportEntity?

    /**
     * Geri yükleme işlemi sırasında odaklanma seanslarını toplu olarak ekler.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFocusSessions(sessions: List<FocusSessionEntity>)

    // ==========================================
    // 10. VERİ TABLOSU TEMİZLEME METOTLARI
    // ==========================================

    /**
     * Tüm zaman çizelgesi olaylarını veritabanından tamamen siler.
     */
    @Query("DELETE FROM timeline_events")
    suspend fun clearTimelineEvents()

    /**
     * Tüm uygulama internet kullanım kayıtlarını veritabanından tamamen siler.
     */
    @Query("DELETE FROM app_usage_records")
    suspend fun clearAppUsageRecords()

    /**
     * Tüm batarya anlık görüntülerini veritabanından tamamen siler.
     */
    @Query("DELETE FROM battery_snapshots")
    suspend fun clearBatterySnapshots()

    /**
     * Tüm odaklanma seanslarını veritabanından tamamen siler.
     */
    @Query("DELETE FROM focus_sessions")
    suspend fun clearFocusSessions()
}
