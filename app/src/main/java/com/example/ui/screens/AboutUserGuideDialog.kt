package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Cable
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DataUsage
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.OrangeFlame
import com.example.ui.theme.PurpleNeon
import com.example.ui.theme.RedCritical

data class FeatureGuideItem(
    val id: String,
    val title: String,
    val category: String, // "Şarj & Batarya", "Koruma & Alarm", "Laboratuvar & Test", "Veri & Analiz"
    val icon: ImageVector,
    val accentColor: Color,
    val summary: String,
    val whatItDoes: String,
    val howToAccess: String,
    val proTip: String
)

@Composable
fun AboutUserGuideDialog(
    onDismiss: () -> Unit,
    onNavigateToLab: (() -> Unit)? = null,
    onNavigateToData: (() -> Unit)? = null,
    onNavigateToAiCoach: (() -> Unit)? = null
) {
    val context = LocalContext.current
    var selectedCategoryIndex by remember { mutableIntStateOf(0) } // 0: Tümü, 1: Şarj & Batarya, 2: Koruma & Alarm, 3: Laboratuvar, 4: Veri & AI
    val expandedFeatureIds = remember { mutableStateListOf<String>() }

    val guideItems = remember {
        listOf(
            FeatureGuideItem(
                id = "live_telemetry",
                title = "Canlı Donanım & Batarya Telemetrisi",
                category = "Şarj & Batarya",
                icon = Icons.Default.Bolt,
                accentColor = CyanAccent,
                summary = "Cihazın anlık gerilimini, çekilen akımı ve donanım gücünü nanosaniye hassasiyetinde ölçer.",
                whatItDoes = "Bataryanızın canlı voltajını (mV), çekilen/verilen anlık akımı (mA), şarj gücünü (Watt) ve sıcaklığını (°C) Android donanım sensörlerinden doğrudan okur. 4 farklı güç modu (Dengeli, Güç Tasarrufu, Ultra Tasarruf, Performans) sunar.",
                howToAccess = "Ana ekrandaki (Batarya) büyük daire gösterge kartına dokunun veya alt menüden 'Telemetri' sekmesine geçin.",
                proTip = "Şarj aletinizin gerçekte kaç Watt verdiğini anlık olarak buradaki canlı göstergeden takip edebilirsiniz."
            ),
            FeatureGuideItem(
                id = "power_feature_switches",
                title = "Pil Tüketen Özellikleri Yönet (Modüler Anahtarlar)",
                category = "Koruma & Alarm",
                icon = Icons.Default.Tune,
                accentColor = EmeraldPrimary,
                summary = "Uygulamanın pil tüketmesini ve ısınmasını önlemek için tüm özellikleri ayrı ayrı açıp kapatın.",
                whatItDoes = "Watchdog, Canlı CPU frekans taraması, Ön plan servisi, Alarmlar ve Gece korumasını tek tek veya 'Sıfır Arka Plan Tüketimi (Ultra Eko)' ön ayarıyla tek dokunuşla kapatarak uygulamanın sadece siz ekrandayken çalışmasını sağlar.",
                howToAccess = "Ayarlar menüsünün en başındaki 'Pil Tüketen Özellikleri Yönet' kartından veya Ana Ekran > Gelişmiş Araçlar > 'Pil Tüketen Özellikleri Yönet' butonundan erişebilirsiniz.",
                proTip = "Telefonunuzun ısınmasını istemiyorsanız 'Canlı CPU Taraması' ve 'Watchdog' anahtarlarını kapatmanız yeterlidir."
            ),
            FeatureGuideItem(
                id = "battery_calibration",
                title = "Şarj Döngüsü & Voltaj Kalibrasyonu",
                category = "Şarj & Batarya",
                icon = Icons.Default.Refresh,
                accentColor = PurpleNeon,
                summary = "Donanım voltaj sapmasını giderir ve batarya yüzde göstergesini sıfırlar.",
                whatItDoes = "4 aşamalı kalibrasyon motoru sayesinde Kernel OCV voltaj tablosunu, Coulomb kapasite sayacını ve akım sapmasını eşitler. Yanlış gösteren pil yüzdesini düzeltir.",
                howToAccess = "Ana ekrandaki 'Döngü & Kalibrasyon' kartına dokunun veya Ayarlar menüsünden 'Kalibrasyon Motoru'nu başlatın.",
                proTip = "Her 30 günde bir veya pil seviyeniz aniden %100'den düşüyorsa tam kalibrasyon yapmanız önerilir."
            ),
            FeatureGuideItem(
                id = "night_guard",
                title = "Akıllı Gece Şarj Kalkanı",
                category = "Koruma & Alarm",
                icon = Icons.Default.Bedtime,
                accentColor = PurpleNeon,
                summary = "Gece uykudayken bataryanın %100 yüksek voltajda şişmesini engeller.",
                whatItDoes = "23:00 - 07:00 saatleri arasında cihaz şarja takılıyken hedef limite (%80) ulaştığında şarjı keserek lityum hücrelerin kimyasal yapısını korur.",
                howToAccess = "Ayarlar > 'Akıllı Gece Şarj Koruması' butonuna tıklayarak hedef limiti (%75-%90) ve koruma saatlerini yapılandırabilirsiniz.",
                proTip = "Gece boyunca telefonun 4.35V üzerinde kalmasını önlemek pil ömrünü 2.4 kata kadar uzatır."
            ),
            FeatureGuideItem(
                id = "smart_alarms",
                title = "Akıllı Sesli Alarmlar & Melodi Sistemi",
                category = "Koruma & Alarm",
                icon = Icons.Default.VolumeUp,
                accentColor = CyanAccent,
                summary = "%80 sağlıklı şarj, tam şarj, aşırı ısınma ve düşük pil sesli ikazları.",
                whatItDoes = "4 farklı siber ve yumuşak alarm melodisi ile şarj sınırlarını, aşırı ısınmayı (>42°C) ve kritik düşük pili sesli/titreşimli olarak bildirir. Gece sessiz saatler modu içerir.",
                howToAccess = "Ayarlar > 'Gelişmiş Alarm & Ses Menüsü' veya 'Alarm Sesi & Melodi Seçimi' bölümünden yapılandırın.",
                proTip = "Pili %80'e ulaştığında prizden çekmek hücre aşınmasını minimuma indirir."
            ),
            FeatureGuideItem(
                id = "stress_test",
                title = "Batarya Hücre & Stres Testi",
                category = "Laboratuvar & Test",
                icon = Icons.Default.Science,
                accentColor = PurpleNeon,
                summary = "30 saniyelik kontrollü yük altında lityum hücre voltaj sarkmasını (sag) ve iç direnci ölçer.",
                whatItDoes = "İşlemciye kontrollü yük uygulayarak gerilim düşüşünü (Δ mV) ölçer. Voltaj sarkması ne kadar düşükse batarya sağlığı o kadar kusursuzdur.",
                howToAccess = "Ana ekrandaki 'Hücre Stres Testi' butonuna tıklayın veya 'Pro Lab' sekmesinden testi başlatın.",
                proTip = "Test sırasında şarj aletini çıkarıp oda sıcaklığında test etmeniz en hassas sonucu verir."
            ),
            FeatureGuideItem(
                id = "charger_benchmark",
                title = "Kablo & Adaptör Testi (Benchmark)",
                category = "Laboratuvar & Test",
                icon = Icons.Default.Cable,
                accentColor = EmeraldPrimary,
                summary = "Kablo direnci, amper kararlılığı ve adaptörün gerçek kalitesini puanlar.",
                whatItDoes = "Cihaz şarja takılıyken 10 saniyelik benchmark uygulayarak akım dalgalanmasını ve kablo iletim direncini donanımsal olarak değerlendirir (A+, A, B- notu verir).",
                howToAccess = "Ana ekrandaki 'Kablo & Adaptör Testi' butonuna veya Pro Lab > 'Kablo & Adaptör' sekmesine dokunun.",
                proTip = "Kusurlu veya aşınmış kablolar telefonunuzun şarj süresini 3 kata kadar uzatabilir."
            ),
            FeatureGuideItem(
                id = "phone_cooler",
                title = "Batarya Soğutucu & Isı Endeksi",
                category = "Koruma & Alarm",
                icon = Icons.Default.AcUnit,
                accentColor = OrangeFlame,
                summary = "Termal yükü düşürür, arka plan sızıntılarını dondurur ve uygulama ısı payını (+°C) gösterir.",
                whatItDoes = "Kamera sensörü, 3D grafik motoru veya arka plan servislerinin bataryaya kaç derece sıcaklık eklediğini gösterir ve tek tıkla termal sınır uygular.",
                howToAccess = "Ana ekrandaki 'Batarya Soğutucu' ve 'Uygulama Isı Endeksi' butonlarından anında çalıştırabilirsiniz.",
                proTip = "37°C üzerindeki sıcaklıklarda soğutucuyu çalıştırarak bataryanın termal yıpranmasını önleyin."
            ),
            FeatureGuideItem(
                id = "data_usage",
                title = "İnternet & Veri Kullanım Takibi",
                category = "Veri & Analiz",
                icon = Icons.Default.DataUsage,
                accentColor = CyanAccent,
                summary = "Wi-Fi ve Mobil veri tüketimini uygulama bazında ayırır, kota limitlerini denetler.",
                whatItDoes = "Bugün, Bu Hafta ve Bu Ay periyotlarında hangi uygulamanın ne kadar internet tükettiğini gösterir. Kota aşım uyarıları ile faturanızı korur.",
                howToAccess = "Alt menüden 'Veri Kullanımı' sekmesine geçin veya Ayarlar > 'Veri Kotası & Uyarılar' bölümünü kullanın.",
                proTip = "Arka planda gizli veri tüketen uygulamaları buradan anında tespit edebilirsiniz."
            ),
            FeatureGuideItem(
                id = "ai_coach",
                title = "Gemini AI Enerji Koçu",
                category = "Veri & Analiz",
                icon = Icons.Default.AutoAwesome,
                accentColor = EmeraldPrimary,
                summary = "Kullanım alışkanlıklarınıza özel yapay zeka destekli pil tasarruf rehberliği.",
                whatItDoes = "Günlük pil deşarj hızınızı, ekran sürenizi ve arka plan uygulamalarınızı analiz ederek cihazınıza özel akıllı optimizasyon tavsiyeleri sunar.",
                howToAccess = "Alt menüdeki 'AI Koç' sekmesine dokunarak kişisel enerji raporunuzu görüntüleyin.",
                proTip = "AI Koç'un önerilerini haftalık olarak uygulayarak günlük %20'ye varan pil tasarrufu elde edebilirsiniz."
            ),
            FeatureGuideItem(
                id = "backup_restore",
                title = "JSON Veritabanı Yedekleme & Geri Yükleme",
                category = "Veri & Analiz",
                icon = Icons.Default.Storage,
                accentColor = PurpleNeon,
                summary = "Tüm pil olayları, kullanım kayıtları ve ayarları %100 yerel JSON ile yedekleyin.",
                whatItDoes = "Cihazınızdaki tüm geçmişi tek tıkla panoya kopyalar veya geri yükler. Hiçbir veriniz internete gönderilmez, gizliliğiniz tamamen korunur.",
                howToAccess = "Ayarlar > 'JSON Yedek' butonuna dokunarak yedek oluşturabilir veya geri yükleyebilirsiniz.",
                proTip = "Telefon değiştirmeden önce JSON yedeğinizi kaydedip yeni cihazınıza tek tıkla aktarabilirsiniz."
            )
        )
    }

    val categories = listOf("Tümü", "Şarj & Batarya", "Koruma & Alarm", "Laboratuvar & Test", "Veri & Analiz")

    val filteredItems = remember(guideItems, selectedCategoryIndex) {
        if (selectedCategoryIndex == 0) {
            guideItems
        } else {
            val cat = categories[selectedCategoryIndex]
            guideItems.filter { it.category == cat }
        }
    }

    fun shareAppInfo() {
        val text = """
            🛡️ VoltGuard Battery & Hardware Manager v2.6.0
            
            Gelişmiş Batarya Sağlığı, Canlı Donanım Telemetrisi, Akıllı Gece Şarj Koruması, Hücre Stres Testi ve Gemini AI Enerji Koçu.
            
            %100 Yerel, Güvenli ve Kesintisiz Batarya Koruma Sistemi.
        """.trimIndent()
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
        }
        context.startActivity(Intent.createChooser(sendIntent, "VoltGuard'ı Paylaş"))
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .widthIn(max = 580.dp)
                .fillMaxHeight(0.94f)
                .clip(RoundedCornerShape(24.dp))
                .testTag("about_user_guide_dialog"),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.2.dp, CyanAccent.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
            ) {
                // 1. Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f, fill = false),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(CyanAccent, EmeraldPrimary)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MenuBook,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Hakkında & Kullanım Kılavuzu",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "VoltGuard v2.6.0 • Tüm Özellikler & Rehber",
                                style = MaterialTheme.typography.bodySmall,
                                color = CyanAccent,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Kapat",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 2. App Branding & Version Card
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    border = BorderStroke(1.dp, CyanAccent.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f, fill = false),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .background(EmeraldPrimary.copy(alpha = 0.2f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Security, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(16.dp))
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "VoltGuard Akıllı Batarya Yöneticisi",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.5.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = EmeraldPrimary.copy(alpha = 0.18f),
                                border = BorderStroke(0.8.dp, EmeraldPrimary.copy(alpha = 0.45f))
                            ) {
                                Text(
                                    text = "v2.6.0 Pro",
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldPrimary,
                                    maxLines = 1,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "VoltGuard, akıllı telefonunuzun lityum-iyon batarya sağlığını korumak, aşırı ısınmayı engellemek, şarj kayıplarını minimize etmek ve donanım telemetrisini gerçek zamanlı takip etmek için geliştirilmiş profesyonel bir güç yönetim aracıdır.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp,
                            lineHeight = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 3. Category Filter Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.take(3).forEachIndexed { idx, catName ->
                        val isSelected = selectedCategoryIndex == idx
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategoryIndex = idx },
                            label = { Text(catName, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, maxLines = 1) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CyanAccent.copy(alpha = 0.2f),
                                selectedLabelColor = CyanAccent
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.drop(3).forEachIndexed { dropIdx, catName ->
                        val actualIdx = dropIdx + 3
                        val isSelected = selectedCategoryIndex == actualIdx
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategoryIndex = actualIdx },
                            label = { Text(catName, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, maxLines = 1) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PurpleNeon.copy(alpha = 0.2f),
                                selectedLabelColor = PurpleNeon
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 4. Feature Guide Cards (Accordion List)
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredItems) { item ->
                        val isExpanded = expandedFeatureIds.contains(item.id)

                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (isExpanded) item.accentColor.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            border = BorderStroke(1.dp, if (isExpanded) item.accentColor.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .clickable {
                                    if (isExpanded) {
                                        expandedFeatureIds.remove(item.id)
                                    } else {
                                        expandedFeatureIds.add(item.id)
                                    }
                                }
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                // Title & Accordion Header
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f, fill = false)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(34.dp)
                                                .background(item.accentColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = item.icon,
                                                contentDescription = null,
                                                tint = item.accentColor,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = item.title,
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = item.category,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = item.accentColor,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }

                                    Icon(
                                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = item.summary,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.5.sp,
                                    lineHeight = 16.sp
                                )

                                // Expanded Content: What it does, How to access, Pro Tip
                                AnimatedVisibility(visible = isExpanded) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 10.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        // 1. Ne Yapar?
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(modifier = Modifier.padding(8.dp)) {
                                                Text(
                                                    text = "⚙️ Ne İşe Yarar?",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = CyanAccent
                                                )
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = item.whatItDoes,
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                    lineHeight = 15.sp
                                                )
                                            }
                                        }

                                        // 2. Nasıl Erişilir?
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(modifier = Modifier.padding(8.dp)) {
                                                Text(
                                                    text = "📍 Nasıl Erişilir / Kullanılır?",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = EmeraldPrimary
                                                )
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = item.howToAccess,
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                    lineHeight = 15.sp
                                                )
                                            }
                                        }

                                        // 3. Uzman İpucu (Pro Tip)
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = OrangeFlame.copy(alpha = 0.1f),
                                            border = BorderStroke(0.8.dp, OrangeFlame.copy(alpha = 0.35f)),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "💡 İpucu: ${item.proTip}",
                                                    fontSize = 10.5.sp,
                                                    color = OrangeFlame,
                                                    lineHeight = 15.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 5. Bottom Actions (Paylaş & Kapat)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { shareAppInfo() },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Paylaş", fontSize = 13.sp)
                    }

                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyanAccent,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1.3f)
                            .height(44.dp)
                    ) {
                        Text("Anladım", fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
                    }
                }
            }
        }
    }
}
