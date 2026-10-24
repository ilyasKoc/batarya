package com.example.data.repository

import com.example.data.db.AppDao
import com.example.data.entity.AiCoachReportEntity
import com.example.data.entity.AppTimeLimitEntity
import com.example.data.entity.AppUsageDataEntity
import com.example.data.entity.BatterySnapshotEntity
import com.example.data.entity.DataLimitConfigEntity
import com.example.data.entity.FocusSessionEntity
import com.example.data.entity.TimelineEventEntity
import com.example.data.entity.UserGamificationEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext

/**
 * AppRepository: VoltGuard uygulamasının merkezi veri deposu (Repository).
 * Room DAO ile ViewModel / Servisler arasındaki veri akışını soyutlar,
 * asenkron IO işlemlerini yönetir ve veri senkronizasyonunu sağlar.
 *
 * @param appDao Room veritabanı erişim arayüzü.
 */
class AppRepository(private val appDao: AppDao) {

    // ==========================================
    // 1. BATARYA ANLIK GÖRÜNTÜLERİ (SNAPSHOTS)
    // ==========================================

    /**
     * En son kaydedilen batarya telemetrisi anlık görüntülerini (şarj, voltaj, sıcaklık, akım) getirir.
     * @param limit Getirilecek maksimum kayıt sayısı (varsayılan 48 adet).
     */
    fun getRecentBatterySnapshots(limit: Int = 48): Flow<List<BatterySnapshotEntity>> =
        appDao.getRecentBatterySnapshots(limit)

    /**
     * Belirli bir andan (sinceTimestamp) itibaren kaydedilen telemetri verilerini grafikler için getirir.
     * @param sinceTimestamp Başlangıç zamanı (milisaniye).
     */
    fun getBatterySnapshotsSince(sinceTimestamp: Long): Flow<List<BatterySnapshotEntity>> =
        appDao.getBatterySnapshotsSince(sinceTimestamp)

    /**
     * Canlı batarya telemetri kaydını arka planda (IO Dispatcher) veritabanına yazar.
     * @param snapshot Kaydedilecek batarya anlık görüntüsü.
     */
    suspend fun recordBatterySnapshot(snapshot: BatterySnapshotEntity) = withContext(Dispatchers.IO) {
        appDao.insertBatterySnapshot(snapshot)
    }

    // ==========================================
    // 2. ZAMAN ÇİZELGESİ VE GÜNLÜK OLAYLAR
    // ==========================================

    /**
     * Belirli bir gün için (0: Bugün, 1: Dün vb.) batarya şarj, deşarj ve ekran olaylarını getirir.
     * @param dayOffset Gün ofseti.
     */
    fun getTimelineEventsForDay(dayOffset: Int): Flow<List<TimelineEventEntity>> =
        appDao.getTimelineEventsForDay(dayOffset)

    /**
     * Tüm zaman çizelgesi olaylarını kronolojik akış için getirir.
     */
    fun getAllTimelineEvents(): Flow<List<TimelineEventEntity>> =
        appDao.getAllTimelineEvents()

    /**
     * Yeni bir sistem veya batarya olayını (şarj takıldı, sıcaklık uyarısı vb.) veritabanına ekler.
     * @param event Eklenecek olay.
     */
    suspend fun addTimelineEvent(event: TimelineEventEntity) = withContext(Dispatchers.IO) {
        appDao.insertTimelineEvent(event)
    }

    // ==========================================
    // 3. UYGULAMA İNTERNET VERİ TÜKETİMİ
    // ==========================================

    /**
     * Uygulamaların internet tüketim kayıtlarını periyot bazında (DAILY, WEEKLY, MONTHLY) getirir.
     * @param periodType Periyot türü.
     */
    fun getAppUsageByPeriod(periodType: String): Flow<List<AppUsageDataEntity>> =
        appDao.getAppUsageByPeriod(periodType)

    // ==========================================
    // 4. VERİ KOTASI VE LİMİT AYARLARI
    // ==========================================

    /**
     * Tanımlı hücresel ve Wi-Fi veri kotası konfigürasyonunu getirir.
     */
    fun getDataLimitConfig(): Flow<DataLimitConfigEntity?> =
        appDao.getDataLimitConfig()

    /**
     * Veri kotası konfigürasyonunu günceller.
     * @param config Yeni veri kotası ayarları.
     */
    suspend fun updateDataLimitConfig(config: DataLimitConfigEntity) = withContext(Dispatchers.IO) {
        appDao.insertOrUpdateDataLimitConfig(config)
    }

    // ==========================================
    // 5. UYGULAMA SÜRE VE DİJİTAL DENGE LİMİTLERİ
    // ==========================================

    /**
     * Tanımlı tüm uygulama günlük ekran süresi limitlerini getirir.
     */
    fun getAllAppTimeLimits(): Flow<List<AppTimeLimitEntity>> =
        appDao.getAllAppTimeLimits()

    /**
     * Belirli bir uygulamanın süre limitini sorgular.
     * @param packageName Uygulama paket adı.
     */
    fun getAppTimeLimit(packageName: String): Flow<AppTimeLimitEntity?> =
        appDao.getAppTimeLimit(packageName)

    /**
     * Uygulama ekran süresi limitini kaydeder veya günceller.
     * @param limit Limit nesnesi.
     */
    suspend fun saveAppTimeLimit(limit: AppTimeLimitEntity) = withContext(Dispatchers.IO) {
        appDao.insertOrUpdateAppTimeLimit(limit)
    }

    /**
     * Uygulama ekran süresi limitini siler.
     * @param packageName Silinecek paket adı.
     */
    suspend fun deleteAppTimeLimit(packageName: String) = withContext(Dispatchers.IO) {
        appDao.deleteAppTimeLimit(packageName)
    }

    // ==========================================
    // 6. ODAKLANMA SEANSLARI (POMODORO / FOCUS)
    // ==========================================

    /**
     * Kullanıcının tamamladığı tüm odaklanma seanslarını getirir.
     */
    fun getAllFocusSessions(): Flow<List<FocusSessionEntity>> =
        appDao.getAllFocusSessions()

    /**
     * Toplam başarıyla tamamlanan odaklanma dakikasını hesaplar.
     */
    fun getTotalCompletedFocusMinutes(): Flow<Int?> =
        appDao.getTotalCompletedFocusMinutes()

    /**
     * Yeni bir odaklanma seansını kaydeder.
     * @param session Kaydedilecek seans.
     */
    suspend fun recordFocusSession(session: FocusSessionEntity) = withContext(Dispatchers.IO) {
        appDao.insertFocusSession(session)
    }

    // ==========================================
    // 7. OYUNLAŞTIRMA VE BAŞARIM SİSTEMİ
    // ==========================================

    /**
     * Kullanıcının pil tasarrufu seviyesi ve başarım durumunu getirir.
     */
    fun getUserGamification(): Flow<UserGamificationEntity?> =
        appDao.getUserGamification()

    /**
     * Kullanıcı oyunlaştırma durumunu günceller.
     * @param gamification Güncellenecek oyunlaştırma varlığı.
     */
    suspend fun saveUserGamification(gamification: UserGamificationEntity) = withContext(Dispatchers.IO) {
        appDao.insertOrUpdateGamification(gamification)
    }

    // ==========================================
    // 8. GEMINI DESTEKLİ AI KOÇ RAPORLARI
    // ==========================================

    /**
     * Kayıtlı en son AI batarya koçu analiz raporunu getirir.
     */
    fun getAiCoachReport(): Flow<AiCoachReportEntity?> =
        appDao.getAiCoachReport()

    /**
     * Yeni oluşturulan AI koç analiz raporunu kaydeder.
     * @param report Kaydedilecek AI raporu.
     */
    suspend fun saveAiCoachReport(report: AiCoachReportEntity) = withContext(Dispatchers.IO) {
        appDao.insertOrUpdateAiCoachReport(report)
    }

    // ==========================================
    // 9. SENKRONİZASYON VE TEMİZLEME İŞLEMLERİ
    // ==========================================

    /**
     * Veritabanındaki eski/örnek verileri temizler.
     */
    suspend fun clearAllSampleData() = withContext(Dispatchers.IO) {
        appDao.clearTimelineEvents()
        appDao.clearAppUsageRecords()
        appDao.clearBatterySnapshots()
    }

    /**
     * Android NetworkStatsManager'dan okunan gerçek uygulama veri tüketimlerini veritabanı ile senkronize eder.
     * @param records Gerçek tüketim kayıtları.
     * @param periodType Periyot tipi ("DAILY", "WEEKLY", "MONTHLY").
     */
    suspend fun syncRealAppUsages(records: List<AppUsageDataEntity>, periodType: String) = withContext(Dispatchers.IO) {
        appDao.deleteAppUsageByPeriod(periodType)
        if (records.isNotEmpty()) {
            appDao.insertAppUsageRecords(records)
        }
    }

    /**
     * Android UsageEvents API'den okunan gerçek şarj ve ekran olaylarını veritabanı ile senkronize eder.
     * @param events Gerçek zaman çizelgesi olayları.
     * @param dayOffsets Senkronize edilecek gün ofsetleri.
     */
    suspend fun syncRealTimelineEvents(events: List<TimelineEventEntity>, dayOffsets: List<Int> = listOf(0, 1, 2)) = withContext(Dispatchers.IO) {
        for (day in dayOffsets) {
            appDao.deleteTimelineEventsForDay(day)
        }
        if (events.isNotEmpty()) {
            appDao.insertTimelineEvents(events)
        }
    }

    /**
     * Uygulama ilk kez yüklendiğinde varsayılan veri kotası ayarlarını oluşturur.
     */
    suspend fun initializeDefaultDataIfEmpty() = withContext(Dispatchers.IO) {
        val currentLimits = appDao.getDataLimitConfig().firstOrNull()
        if (currentLimits == null) {
            appDao.insertOrUpdateDataLimitConfig(
                DataLimitConfigEntity(
                    id = 1,
                    mobileLimitMb = 10240, // 10 GB
                    wifiLimitMb = 51200,   // 50 GB
                    warningThresholdPercent = 80,
                    isAlertEnabled = true,
                    billingCycleDay = 1
                )
            )
            clearAllSampleData()
        }
    }
}
