package com.example.system

import android.content.Context
import android.content.Intent
import com.example.data.entity.AppUsageDataEntity
import com.example.data.entity.BatterySnapshotEntity
import com.example.data.entity.TimelineEventEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 📄 Rapor Oluşturma & Dışa Aktarma Yardımcısı (ReportExportHelper)
 * 
 * Batarya sağlığı, donanım telemetrisi, derin uyku analizleri ve uygulama tüketimlerini
 * detaylı metin veya CSV tablosu formatında üretip sistem paylaşım menüsüyle paylaşılmasını sağlar.
 */
object ReportExportHelper {

    /**
     * Kullanıcı için okunabilir, profesyonel teşhis ve sağlık metin raporu üretir.
     */
    fun generateDiagnosticTextReport(
        batteryInfo: LiveBatteryInfo,
        snapshots: List<BatterySnapshotEntity>,
        appUsages: List<AppUsageDataEntity>,
        timelineEvents: List<TimelineEventEntity>,
        standbyAnalysis: StandbyDrainAnalysis
    ): String {
        val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale("tr", "TR"))
        val nowStr = dateFormat.format(Date())

        val sb = StringBuilder()
        sb.appendLine("==========================================")
        sb.appendLine("⚡ VOLTGUARD PROFESYONEL TEŞHİS RAPORU ⚡")
        sb.appendLine("Oluşturulma Tarihi: $nowStr")
        sb.appendLine("==========================================")
        sb.appendLine()
        sb.appendLine("📊 1. ANLIK BATARYA & DONANIM DURUMU")
        sb.appendLine("------------------------------------------")
        sb.appendLine("• Pil Seviyesi: %${batteryInfo.percentage}")
        sb.appendLine("• Durum: ${batteryInfo.status}")
        sb.appendLine("• Bağlantı Türü: ${batteryInfo.plugType}")
        sb.appendLine("• Şarj Hızı / Protokol: ${batteryInfo.chargeSpeedCategory}")
        if (batteryInfo.isCharging) {
            sb.appendLine("• Anlık Şarj Gücü: ${String.format(Locale.US, "%.1f", batteryInfo.chargingWattage)} Watt")
        }
        sb.appendLine("• Akım (Current): ${batteryInfo.currentNowMa} mA")
        sb.appendLine("• Gerilim (Voltage): ${batteryInfo.voltageMv} mV")
        sb.appendLine("• Sıcaklık: ${String.format(Locale.US, "%.1f", batteryInfo.temperatureCelsius)} °C")
        sb.appendLine("• Batarya Sağlığı: ${batteryInfo.health}")
        sb.appendLine("• Tahmini Pil Sağlık Oranı: %${batteryInfo.batteryWearHealthPercent}")
        sb.appendLine("• Tahmini Kalan Süre: ${String.format(Locale.US, "%.1f", batteryInfo.estimatedHoursRemaining)} saat")
        sb.appendLine()
        sb.appendLine("🌙 2. DERİN UYKU & BEKLEME (STANDBY) ANALİZİ")
        sb.appendLine("------------------------------------------")
        sb.appendLine("• Bekleme Tüketim Oranı: %${String.format(Locale.US, "%.2f", standbyAnalysis.drainPerHour)} / saat")
        sb.appendLine("• Derin Uyku (Doze) Verimliliği: %${standbyAnalysis.deepSleepPercent}")
        sb.appendLine("• Bekleme Derecesi: ${standbyAnalysis.statusGrade}")
        sb.appendLine("• Teşhis Özeti: ${standbyAnalysis.statusDescription}")
        if (standbyAnalysis.wakelockWarning != null) {
            sb.appendLine("• Uyarı: ${standbyAnalysis.wakelockWarning}")
        }
        sb.appendLine()
        sb.appendLine("📱 3. EN ÇOK TÜKETEN UYGULAMALAR (İLK 5)")
        sb.appendLine("------------------------------------------")
        val topApps = appUsages.sortedByDescending { it.screenTimeMinutes }.take(5)
        if (topApps.isEmpty()) {
            sb.appendLine("Henüz kayıtlı uygulama kullanım verisi bulunmuyor.")
        } else {
            topApps.forEachIndexed { i, app ->
                val mins = app.screenTimeMinutes
                val totalMb = (app.wifiBytes + app.mobileBytes) / (1024 * 1024)
                sb.appendLine("${i + 1}. ${app.appName}: $mins dk ekran süresi | $totalMb MB veri tüketimi")
            }
        }
        sb.appendLine()
        sb.appendLine("📈 4. GEÇMİŞ GÜNLÜK İSTATİSTİKLER")
        sb.appendLine("------------------------------------------")
        sb.appendLine("• Toplam Kayıtlı Snapshot: ${snapshots.size}")
        sb.appendLine("• Toplam Kayıtlı Eylem / Olay: ${timelineEvents.size}")
        sb.appendLine()
        sb.appendLine("==========================================")
        sb.appendLine("VoltGuard Akıllı Batarya & Sistem Yöneticisi")
        sb.appendLine("==========================================")

        return sb.toString()
    }

    /**
     * Excel veya elektronik tablolarda açılabilen virgülle ayrılmış CSV formatında rapor üretir.
     */
    fun generateCsvReport(
        snapshots: List<BatterySnapshotEntity>,
        appUsages: List<AppUsageDataEntity>
    ): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
        val sb = StringBuilder()

        sb.appendLine("=== BATARYA SNAPSHOT GÜNLÜĞÜ ===")
        sb.appendLine("Tarih,Pil Seviyesi (%),Sicaklik (C),Sarj Oluyor Mu,Voltaj (mV)")
        snapshots.takeLast(100).forEach { s ->
            val d = dateFormat.format(Date(s.timestamp))
            sb.appendLine("$d,${s.level},${String.format(Locale.US, "%.1f", s.temperatureCelsius)},${if (s.isCharging) "EVET" else "HAYIR"},${s.voltageMv}")
        }

        sb.appendLine()
        sb.appendLine("=== UYGULAMA KULLANIM VE İNTERNET RAPORU ===")
        sb.appendLine("Uygulama Adi,Paket Adi,Ekran Suresi (Dakika),WiFi Veri (MB),Mobil Veri (MB)")
        appUsages.sortedByDescending { it.screenTimeMinutes }.forEach { a ->
            val wifiMb = a.wifiBytes / (1024 * 1024)
            val mobileMb = a.mobileBytes / (1024 * 1024)
            sb.appendLine("\"${a.appName}\",\"${a.packageName}\",${a.screenTimeMinutes},$wifiMb,$mobileMb")
        }

        return sb.toString()
    }

    /**
     * Üretilen metin raporunu Android'in sistem paylaşım iletişim kutusu üzerinden gönderir.
     */
    fun shareReport(context: Context, title: String, content: String) {
        try {
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TITLE, title)
                putExtra(Intent.EXTRA_SUBJECT, "VoltGuard Teşhis Raporu")
                putExtra(Intent.EXTRA_TEXT, content)
                type = "text/plain"
            }
            val shareIntent = Intent.createChooser(sendIntent, "VoltGuard Raporunu Paylaş")
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(shareIntent)
        } catch (_: Exception) {}
    }
}
