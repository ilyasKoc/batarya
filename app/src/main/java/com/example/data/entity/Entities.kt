package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 🔋 Batarya Anlık Durum Kaydı Varlığı (Entity)
 * 
 * Belirli zaman aralıklarında veya şarj durumu değiştiğinde voltaj, sıcaklık,
 * yüzde ve şarj durumunu zaman damgasıyla saklar.
 * 
 * @property id Benzersiz otomatik artan kayıt kimliği
 * @property timestamp Kaydın alındığı milisaniye zaman damgası
 * @property level Batarya doluluk yüzdesi (0 - 100)
 * @property isCharging Cihazın şarjda olup olmadığı bilgisi
 * @property temperatureCelsius Batarya sıcaklığı (Santigrat derece)
 * @property voltageMv Batarya gerilimi (Milivolt cinsinden)
 * @property status Batarya şarj durumu açıklaması (Şarj Oluyor, Deşarj, Tam vb.)
 */
@Entity(tableName = "battery_snapshots")
data class BatterySnapshotEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val level: Int,
    val isCharging: Boolean,
    val temperatureCelsius: Float,
    val voltageMv: Int,
    val status: String
)

/**
 * ⏱️ Cihaz ve Ekran Zaman Çizelgesi Olay Kaydı Varlığı (Entity)
 * 
 * Ekran açılma/kapanma, uygulama başlatma ve kullanım sürelerini kronolojik
 * olarak günlük zaman çizelgesinde göstermek için saklar.
 * 
 * @property id Benzersiz otomatik artan olay kimliği
 * @property timestamp Olayın gerçekleştiği milisaniye zaman damgası
 * @property eventType Olay türü ("SCREEN_ON", "SCREEN_OFF", "APP_USAGE")
 * @property appName Olayla ilişkili uygulamanın görünen adı
 * @property packageName Uygulamanın Android paket adı
 * @property durationMinutes Olay süresi (dakika)
 * @property durationSeconds Olay süresi (saniye)
 * @property details Olay ile ilgili ek ayrıntılar veya notlar
 * @property dayOffset Gün farkı (0: Bugün, 1: Dün, 2: 2 gün önce vb.)
 */
@Entity(tableName = "timeline_events")
data class TimelineEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val eventType: String, // "SCREEN_ON", "SCREEN_OFF", "APP_USAGE"
    val appName: String,
    val packageName: String,
    val durationMinutes: Int = 0,
    val durationSeconds: Int = 0,
    val details: String = "",
    val dayOffset: Int = 0 // 0 = Bugün, 1 = Dün, 2 = 2 Gün Önce
)

/**
 * 📊 Uygulama Ağ ve Ekran Süresi Kullanım Verisi Varlığı (Entity)
 * 
 * Her bir uygulamanın Wi-Fi, Hücresel veri tüketimini ve toplam ekran süresini
 * periyodik (günlük, haftalık, aylık) olarak saklar.
 * 
 * @property id Benzersiz otomatik artan kayıt kimliği
 * @property appName Uygulamanın adı
 * @property packageName Uygulamanın paket adı
 * @property periodType Analiz periyodu ("DAILY", "WEEKLY", "MONTHLY")
 * @property wifiBytes Wi-Fi üzerinden harcanan toplam bayt
 * @property mobileBytes Mobil veri üzerinden harcanan toplam bayt
 * @property screenTimeMinutes Uygulamanın açık kaldığı toplam dakika
 * @property category Uygulama kategorisi (Sosyal, Oyun, Araçlar vb.)
 */
@Entity(tableName = "app_usage_records")
data class AppUsageDataEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val appName: String,
    val packageName: String,
    val periodType: String, // "DAILY", "WEEKLY", "MONTHLY"
    val wifiBytes: Long,
    val mobileBytes: Long,
    val screenTimeMinutes: Int,
    val category: String = "Genel"
)

/**
 * 🌐 İnternet ve Veri Kotası Yapılandırma Varlığı (Entity)
 * 
 * Mobil veri ve Wi-Fi için belirlenen aylık kota limitlerini ve uyarı eşiklerini depolar.
 * 
 * @property id Tekil yapılandırma kimliği (Varsayılan 1)
 * @property mobileLimitMb Mobil internet kotası (Megabayt cinsinden)
 * @property wifiLimitMb Wi-Fi internet kotası (Megabayt cinsinden)
 * @property warningThresholdPercent Kota uyarı yüzdesi (Örn: %80 dolunca uyar)
 * @property isAlertEnabled Kota aşım bildirimlerinin açık olup olmadığı
 * @property billingCycleDay Fatura dönemi başlangıç günü (1 - 31)
 */
@Entity(tableName = "data_limit_configs")
data class DataLimitConfigEntity(
    @PrimaryKey val id: Int = 1,
    val mobileLimitMb: Long = 10240, // 10 GB default
    val wifiLimitMb: Long = 51200,   // 50 GB default
    val warningThresholdPercent: Int = 80,
    val isAlertEnabled: Boolean = true,
    val billingCycleDay: Int = 1
)

/**
 * ⏳ Uygulama Günlük Kullanım Süresi Limiti Varlığı (Entity)
 * 
 * Belirli uygulamalar için (Instagram, TikTok vb.) günlük ekran süresi sınırlarını saklar.
 * 
 * @property packageName Kısıtlanacak uygulamanın paket adı (Primary Key)
 * @property appName Uygulama adı
 * @property limitMinutes Günlük izin verilen maksimum kullanım süresi (dakika)
 * @property isEnabled Süre sınırının aktif olup olmadığı
 * @property alertThresholdMinutes Limite yaklaşırken kaç dakika kala uyarılacağı
 */
@Entity(tableName = "app_time_limits")
data class AppTimeLimitEntity(
    @PrimaryKey val packageName: String,
    val appName: String,
    val limitMinutes: Int = 45,
    val isEnabled: Boolean = true,
    val alertThresholdMinutes: Int = 5
)

/**
 * 🧘 Odaklanma (Pomodoro & Deep Work) Seansı Varlığı (Entity)
 * 
 * Kullanıcının dikkat dağınıklığını önlemek için başlattığı odak seanslarının geçmişini tutar.
 * 
 * @property id Benzersiz otomatik artan seans kimliği
 * @property timestamp Seansın yapıldığı zaman damgası
 * @property durationMinutes Seans süresi (dakika)
 * @property sessionType Seans türü ("POMODORO_25", "DEEP_WORK_50", "QUICK_15", "CUSTOM")
 * @property completed Seansın başarıyla tamamlanıp tamamlanmadığı
 * @property distractionCount Seans sırasında ekranı açma veya uygulamadan çıkma sayısı
 * @property notes Kullanıcının seans hakkındaki notları
 */
@Entity(tableName = "focus_sessions")
data class FocusSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val durationMinutes: Int,
    val sessionType: String, // "POMODORO_25", "DEEP_WORK_50", "QUICK_15", "CUSTOM"
    val completed: Boolean = true,
    val distractionCount: Int = 0,
    val notes: String = ""
)

/**
 * 🏆 Kullanıcı Oyunlaştırma, Seviye ve Başarı Varlığı (Entity)
 * 
 * Batarya tasarrufu, odaklanma ve şarj disiplini ile kazanılan XP, seviye ve rozetleri saklar.
 * 
 * @property id Tekil kullanıcı profili kimliği (Varsayılan 1)
 * @property totalXp Toplam kazanılan deneyim puanı (XP)
 * @property currentLevel Mevcut seviye derecesi
 * @property dailyStreak Kesintisiz günlük hedef tamamlama serisi
 * @property lastActiveTimestamp Son etkinlik zaman damgası
 * @property totalFocusMinutes Toplam odaklanılan dakika miktarı
 * @property unlockedBadgesCsv Kilidi açılmış rozetlerin virgülle ayrılmış anahtarları
 */
@Entity(tableName = "user_gamification")
data class UserGamificationEntity(
    @PrimaryKey val id: Int = 1,
    val totalXp: Int = 120,
    val currentLevel: Int = 1,
    val dailyStreak: Int = 1,
    val lastActiveTimestamp: Long = System.currentTimeMillis(),
    val totalFocusMinutes: Int = 0,
    val unlockedBadgesCsv: String = "EARLY_BIRD" // comma-separated badge keys
)

/**
 * 🤖 Yapay Zeka (AI) Enerji & Davranış Koçluğu Raporu Varlığı (Entity)
 * 
 * Gemini AI veya yerel analiz motoru tarafından üretilen enerji verimliliği ve alışkanlık raporunu saklar.
 * 
 * @property id Tekil rapor kimliği (Varsayılan 1)
 * @property timestamp Raporun üretildiği zaman damgası
 * @property score 100 üzerinden hesaplanan dijital sağlık ve batarya koruma skoru
 * @property personaTitle Kullanıcı kullanım tarzı unvanı (Örn: "Dengeli Dijital Kaşif", "Gece Kuşu")
 * @property summary Raporun genel özet metni
 * @property strengthsJson Güçlü yönlerin JSON dizi formatındaki listesi
 * @property risksJson Risk veya iyileştirme gerektiren alanların JSON dizi formatı
 * @property suggestionsJson Kullanıcıya özel akıllı tasarruf ve sağlık önerileri listesi
 */
@Entity(tableName = "ai_coach_reports")
data class AiCoachReportEntity(
    @PrimaryKey val id: Int = 1,
    val timestamp: Long = System.currentTimeMillis(),
    val score: Int = 85,
    val personaTitle: String = "Dengeli Dijital Kaşif",
    val summary: String = "Günlük telefon kullanımınız ve uyku düzeniniz genel olarak dengeli.",
    val strengthsJson: String = "[\"Gece ekranı zamanında kapattınız\", \"Sosyal medya süreniz dengeli\"]",
    val risksJson: String = "[\"Günün ilk saatinde hızlı kilit açma\"]",
    val suggestionsJson: String = "[\"25 dakikalık odak seansı başlatmayı deneyin\", \"23:00'ten sonra mavi ışık filtresini açın\"]"
)
