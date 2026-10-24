package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.entity.AiCoachReportEntity
import com.example.data.entity.AppTimeLimitEntity
import com.example.data.entity.AppUsageDataEntity
import com.example.data.entity.BatterySnapshotEntity
import com.example.data.entity.DataLimitConfigEntity
import com.example.data.entity.FocusSessionEntity
import com.example.data.entity.TimelineEventEntity
import com.example.data.entity.UserGamificationEntity

/**
 * 🗄️ VoltGuard Yerel SQLite (Room) Veritabanı Sınıfı
 * 
 * Uygulamanın tüm geçmiş batarya kayıtlarını, zaman çizelgesi olaylarını,
 * uygulama kullanım verilerini, veri ve süre limitlerini, odak seanslarını,
 * oyunlaştırma (XP/Rozet) ve AI koçluk analizlerini cihazda güvenle saklar.
 */
@Database(
    entities = [
        BatterySnapshotEntity::class,
        TimelineEventEntity::class,
        AppUsageDataEntity::class,
        DataLimitConfigEntity::class,
        AppTimeLimitEntity::class,
        FocusSessionEntity::class,
        UserGamificationEntity::class,
        AiCoachReportEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    /**
     * Veritabanı işlemlerine erişim sağlayan Veri Erişim Nesnesi (DAO) referansı.
     */
    abstract fun appDao(): AppDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Veritabanı Singleton nesnesini döndürür.
         * Thread-safe (iş parçacığı güvenli) synchronized blok ile çift kontrol mekanizması uygular.
         * 
         * @param context Uygulama bağlamı
         * @return Aktif AppDatabase örneği
         */
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "voltguard_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
