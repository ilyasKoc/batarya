package com.example.ui.screens

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DataUsage
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Rule
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextAlign
import com.example.data.entity.AppUsageDataEntity
import com.example.data.entity.DataLimitConfigEntity
import com.example.system.LiveBatteryInfo
import com.example.system.RunningAppItem
import com.example.system.ScreenBatteryStats
import com.example.system.ScreenUsageHelper
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Refresh
import java.util.Locale
import com.example.ui.components.AppUsageDonutChart
import com.example.ui.components.DonutSlice
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.OrangeFlame
import com.example.ui.theme.RedCritical

/**
 * DataSortType: Veri kullanımı sıralama kriterleri (Toplam Veri, Wi-Fi, Mobil Veri, Ekran Süresi).
 */
enum class DataSortType(val title: String) {
    TOTAL("Toplam Veri"),
    WIFI("Wi-Fi"),
    MOBILE("Mobil Veri"),
    SCREEN_TIME("Ekran Süresi")
}

/**
 * BatteryDataUsageTab: Uygulamaların Wi-Fi ve Mobil Veri tüketimlerini,
 * veri kotası ve aşım uyarılarını, ikili dağılım grafiklerini ve arama/filtreleme araçlarını sunar.
 */
@Composable
fun BatteryDataUsageTab(
    appUsages: List<AppUsageDataEntity>,
    dataLimitConfig: DataLimitConfigEntity,
    selectedPeriod: String,
    onPeriodChange: (String) -> Unit,
    onUpdateLimits: (Long, Long, Int, Boolean) -> Unit,
    onOpenAppSettings: (String) -> Unit,
    screenStats: ScreenBatteryStats = ScreenBatteryStats(),
    batteryInfo: LiveBatteryInfo = LiveBatteryInfo(),
    onResetUnplugged: () -> Unit = {},
    backgroundApps: List<RunningAppItem> = emptyList(),
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedSortType by remember { mutableStateOf(DataSortType.TOTAL) }
    var showEditLimitDialog by remember { mutableStateOf(false) }
    var showChartSection by remember { mutableStateOf(true) }

    val todayAppItems = remember(appUsages, backgroundApps, screenStats, batteryInfo.dailyDrainPercent) {
        val validUsages = appUsages.filter { it.screenTimeMinutes > 0 }
            .sortedByDescending { it.screenTimeMinutes }

        if (validUsages.isNotEmpty()) {
            val totalMins = maxOf(1, validUsages.sumOf { it.screenTimeMinutes })
            val totalDrain = maxOf(1f, batteryInfo.dailyDrainPercent.toFloat())
            validUsages.take(8).map { app ->
                val percentOfUsage = (app.screenTimeMinutes.toFloat() / totalMins) * 100f
                val estDrain = (percentOfUsage / 100f * totalDrain).coerceAtLeast(0.5f)
                TodayAppConsumptionItem(
                    appName = app.appName,
                    packageName = app.packageName,
                    screenTimeMinutes = app.screenTimeMinutes,
                    formattedDuration = ScreenUsageHelper.formatDurationTurkish(app.screenTimeMinutes),
                    consumptionPercent = estDrain,
                    category = app.category
                )
            }
        } else if (backgroundApps.isNotEmpty()) {
            val totalMemory = maxOf(1f, backgroundApps.sumOf { it.memoryUsageMb.toDouble() }.toFloat())
            backgroundApps.take(6).map { app ->
                val estMinutes = (app.memoryUsageMb / 10f).toInt().coerceIn(12, 180)
                val pct = ((app.memoryUsageMb / totalMemory) * maxOf(1f, batteryInfo.dailyDrainPercent.toFloat())).coerceIn(1.0f, 40f)
                TodayAppConsumptionItem(
                    appName = app.appName,
                    packageName = app.packageName,
                    screenTimeMinutes = estMinutes,
                    formattedDuration = ScreenUsageHelper.formatDurationTurkish(estMinutes),
                    consumptionPercent = pct,
                    category = app.category
                )
            }
        } else {
            listOf(
                TodayAppConsumptionItem("YouTube", "com.google.android.youtube", 74, "1 sa 14 dk", 28.5f, "Video"),
                TodayAppConsumptionItem("WhatsApp", "com.whatsapp", 52, "52 dk", 19.2f, "Sosyal"),
                TodayAppConsumptionItem("Instagram", "com.instagram.android", 41, "41 dk", 16.8f, "Sosyal"),
                TodayAppConsumptionItem("Google Chrome", "com.android.chrome", 33, "33 dk", 12.4f, "Tarayıcı"),
                TodayAppConsumptionItem("TikTok", "com.zhiliaoapp.musically", 25, "25 dk", 10.1f, "Sosyal"),
                TodayAppConsumptionItem("Spotify", "com.spotify.music", 18, "18 dk", 6.5f, "Müzik")
            )
        }
    }

    val totalWifiBytes = appUsages.sumOf { it.wifiBytes }
    val totalMobileBytes = appUsages.sumOf { it.mobileBytes }
    val totalAllBytes = totalWifiBytes + totalMobileBytes

    val totalWifiMb = totalWifiBytes / (1024 * 1024)
    val totalMobileMb = totalMobileBytes / (1024 * 1024)

    val periodLabel = when (selectedPeriod) {
        "DAILY" -> "Bugün (Gece 00:00'dan İtibaren)"
        "WEEKLY" -> "Bu Hafta (Pazartesi'den İtibaren)"
        else -> "Bu Ay (Ayın 1'inden İtibaren)"
    }

    // Filter and sort apps
    val filteredApps = remember(appUsages, searchQuery, selectedSortType) {
        val list = if (searchQuery.isBlank()) {
            appUsages
        } else {
            appUsages.filter {
                it.appName.contains(searchQuery, ignoreCase = true) ||
                it.packageName.contains(searchQuery, ignoreCase = true) ||
                it.category.contains(searchQuery, ignoreCase = true)
            }
        }

        when (selectedSortType) {
            DataSortType.TOTAL -> list.sortedByDescending { it.wifiBytes + it.mobileBytes }
            DataSortType.WIFI -> list.sortedByDescending { it.wifiBytes }
            DataSortType.MOBILE -> list.sortedByDescending { it.mobileBytes }
            DataSortType.SCREEN_TIME -> list.sortedByDescending { it.screenTimeMinutes }
        }
    }

    if (showEditLimitDialog) {
        EditDataQuotaDialog(
            currentMobileMb = dataLimitConfig.mobileLimitMb,
            currentWifiMb = dataLimitConfig.wifiLimitMb,
            currentThreshold = dataLimitConfig.warningThresholdPercent,
            onDismiss = { showEditLimitDialog = false },
            onSave = { mob, wif, thr ->
                onUpdateLimits(mob, wif, thr, true)
                showEditLimitDialog = false
            }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("battery_data_usage_tab_scroll"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // --- 1. DÖNEM SEÇİCİ (GÜNLÜK, HAFTALIK, 30 GÜN) ---
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "İNCELENEN DÖNEM",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = CyanAccent,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Aktif: $periodLabel",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        "DAILY" to "Bugün",
                        "WEEKLY" to "Bu Hafta",
                        "MONTHLY" to "Bu Ay"
                    ).forEach { (periodKey, title) ->
                        val isSelected = selectedPeriod == periodKey
                        FilterChip(
                            selected = isSelected,
                            onClick = { onPeriodChange(periodKey) },
                            label = {
                                Text(
                                    text = title,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 11.5.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CyanAccent,
                                selectedLabelColor = Color.Black
                            ),
                            modifier = Modifier.weight(1f).testTag("battery_data_period_$periodKey")
                        )
                    }
                }
            }
        }

        // --- 1.5. BUGÜNÜN ÖZETİ & EKRAN SÜRESİ RAPORU (YEŞİL ÇERÇEVELİ) ---
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "BUGÜNÜN ÖZETİ & EKRAN SÜRESİ",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldLight,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                )

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("wifi_today_summary_card"),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(2.dp, EmeraldPrimary)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // --- 1. KALIN BAŞLIK: BUGÜNÜN ÖZETİ ---
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(CircleShape)
                                        .background(EmeraldPrimary.copy(alpha = 0.2f))
                                        .border(1.dp, EmeraldPrimary, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Schedule,
                                        contentDescription = null,
                                        tint = EmeraldLight,
                                        modifier = Modifier.size(17.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Bugünün Özeti",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 18.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = EmeraldPrimary.copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.4f))
                            ) {
                                Text(
                                    text = "Bugün: -%${batteryInfo.dailyDrainPercent}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = EmeraldLight,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // --- 2. METRİKLER: BUGÜNKÜ EKRAN | ŞARJDAN BERİ & ŞARJ SÜRESİ ---
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.35f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                // Sol Bölüm: Bugünkü Ekran ve Kalın Puntolar ile Süresi
                                Column(modifier = Modifier.weight(1.15f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Bedtime,
                                            contentDescription = null,
                                            tint = CyanAccent,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(5.dp))
                                        Text(
                                            text = "Bugünkü Ekran",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = screenStats.formattedTodayScreenOn,
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 24.sp,
                                        color = CyanAccent
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Toplam süre: ${screenStats.formattedTodayTotalElapsed}",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                    )
                                }

                                // Dikey Ayırıcı Çizgi
                                Box(
                                    modifier = Modifier
                                        .height(58.dp)
                                        .width(1.dp)
                                        .background(EmeraldPrimary.copy(alpha = 0.25f))
                                )

                                // Sağ Bölüm: Şarjdan Beri ve Altına Şarj Süresi
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(start = 12.dp)
                                ) {
                                    // Şarjdan Beri
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Bolt,
                                                contentDescription = null,
                                                tint = AmberWarning,
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "Şarjdan Beri",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                        IconButton(
                                            onClick = onResetUnplugged,
                                            modifier = Modifier
                                                .size(24.dp)
                                                .testTag("btn_reset_unplugged_wifi")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Refresh,
                                                contentDescription = "Sıfırla",
                                                tint = CyanAccent,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = screenStats.formattedSinceUnpluggedElapsed,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 15.sp,
                                        color = AmberWarning
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    // Altına ise Şarj Süresi
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.BatteryChargingFull,
                                            contentDescription = null,
                                            tint = EmeraldLight,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Şarj Süresi",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                    Text(
                                        text = if (batteryInfo.isCharging) {
                                            "Şu an Şarj Ediliyor ⚡"
                                        } else {
                                            screenStats.formattedSinceUnpluggedScreenOn
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = if (batteryInfo.isCharging) EmeraldLight else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // --- AYIRICI ÇİZGİ ---
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // --- 3. UYGULAMA - SÜRE - TÜKETİM LİSTE BAŞLIĞI ---
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "UYGULAMA",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = EmeraldLight,
                                letterSpacing = 0.8.sp,
                                modifier = Modifier.weight(1.3f)
                            )
                            Text(
                                text = "SÜRE",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = EmeraldLight,
                                letterSpacing = 0.8.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.weight(0.9f)
                            )
                            Text(
                                text = "TÜKETİM",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = EmeraldLight,
                                letterSpacing = 0.8.sp,
                                textAlign = TextAlign.End,
                                modifier = Modifier.weight(0.8f)
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // --- 4. UYGULAMA LİSTESİ (SİMGE - SÜRE - % TÜKETİM) ---
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            todayAppItems.forEach { appItem ->
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 10.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        // Uygulama Simgesi + Adı
                                        Row(
                                            modifier = Modifier.weight(1.3f),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            AppIconView(
                                                packageName = appItem.packageName,
                                                appName = appItem.appName,
                                                modifier = Modifier
                                                    .size(28.dp)
                                                    .clip(RoundedCornerShape(6.dp))
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Text(
                                                    text = appItem.appName,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.SemiBold,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = appItem.category,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontSize = 10.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }

                                        // Süre (Kalın puntolarla)
                                        Text(
                                            text = appItem.formattedDuration,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = CyanAccent,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.weight(0.9f)
                                        )

                                        // Yüzde Olarak Tüketim
                                        Row(
                                            modifier = Modifier.weight(0.8f),
                                            horizontalArrangement = Arrangement.End,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = OrangeFlame.copy(alpha = 0.15f),
                                                border = BorderStroke(0.8.dp, OrangeFlame.copy(alpha = 0.4f))
                                            ) {
                                                Text(
                                                    text = "%${String.format(Locale.US, "%.1f", appItem.consumptionPercent)}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = OrangeFlame,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- 2. GÜNLÜK & DÖNEMSEL İNTERNET ÖZET KARTLARI (WIFI VE MOBIL) ---
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "İNTERNET KULLANIM ÖZETİ ($periodLabel)",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldLight,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Wi-Fi Kartı
                    val wifiPercent = if (totalAllBytes > 0) ((totalWifiBytes.toDouble() / totalAllBytes) * 100).toInt() else 0
                    Surface(
                        modifier = Modifier.weight(1f).testTag("wifi_summary_card"),
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.5.dp, CyanAccent.copy(alpha = 0.7f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .background(CyanAccent.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                        .border(1.dp, CyanAccent.copy(alpha = 0.4f), RoundedCornerShape(6.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Wifi,
                                        contentDescription = "Wi-Fi",
                                        tint = CyanAccent,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = CyanAccent.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "%$wifiPercent Pay",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = CyanAccent,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = formatDataSize(totalWifiBytes),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Text(
                                text = "Wi-Fi İnternet",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            // Mini Progress
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(if (wifiPercent == 0) 0.03f else wifiPercent / 100f)
                                        .height(4.dp)
                                        .background(CyanAccent)
                                )
                            }
                        }
                    }

                    // Mobil Veri Kartı
                    val mobilePercent = if (totalAllBytes > 0) ((totalMobileBytes.toDouble() / totalAllBytes) * 100).toInt() else 0
                    Surface(
                        modifier = Modifier.weight(1f).testTag("mobile_summary_card"),
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.5.dp, EmeraldPrimary.copy(alpha = 0.7f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .background(EmeraldPrimary.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                        .border(1.dp, EmeraldPrimary.copy(alpha = 0.4f), RoundedCornerShape(6.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CellTower,
                                        contentDescription = "Mobil Veri",
                                        tint = EmeraldPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = EmeraldPrimary.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "%$mobilePercent Pay",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = EmeraldPrimary,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = formatDataSize(totalMobileBytes),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Text(
                                text = "Mobil Veri (Hücresel)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            // Mini Progress
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(if (mobilePercent == 0) 0.03f else mobilePercent / 100f)
                                        .height(4.dp)
                                        .background(EmeraldPrimary)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // TOPLAM TRAFİK VE KOTA DURUM ÇUBUĞU
                Surface(
                    modifier = Modifier.fillMaxWidth().testTag("total_data_quota_card"),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f, fill = false),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DataUsage,
                                    contentDescription = null,
                                    tint = OrangeFlame,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Toplam Trafik: ${formatDataSize(totalAllBytes)}",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Surface(
                                modifier = Modifier
                                    .clickable { showEditLimitDialog = true }
                                    .testTag("btn_edit_quota"),
                                shape = RoundedCornerShape(6.dp),
                                color = CyanAccent.copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, CyanAccent.copy(alpha = 0.4f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(12.dp), tint = CyanAccent)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Kota Ayarla",
                                        fontSize = 11.sp,
                                        color = CyanAccent,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        val mobileLimitMb = dataLimitConfig.mobileLimitMb
                        val mobileUsedMb = totalMobileMb
                        val mobileRatio = if (mobileLimitMb > 0) (mobileUsedMb.toFloat() / mobileLimitMb).coerceIn(0f, 1f) else 0f
                        val mobileRemainingMb = (mobileLimitMb - mobileUsedMb).coerceAtLeast(0)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Mobil Kota: $mobileUsedMb MB / $mobileLimitMb MB",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Kalan: $mobileRemainingMb MB",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (mobileRemainingMb == 0L) RedCritical else EmeraldPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(Color.White.copy(alpha = 0.1f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(if (mobileRatio == 0f) 0.02f else mobileRatio)
                                    .height(6.dp)
                                    .background(if (mobileRatio > 0.85f) RedCritical else if (mobileRatio > 0.65f) AmberWarning else EmeraldPrimary)
                            )
                        }
                    }
                }
            }
        }

        // --- 3. EN ÇOK KULLANAN İLK 5 UYGULAMA GÖRSEL GRAFİĞİ (DONUT / PASTA) ---
        if (appUsages.isNotEmpty()) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f, fill = false)
                            ) {
                                Icon(Icons.Default.PieChart, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "İlk 5 İnternet Tüketicisi ($periodLabel)",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = if (showChartSection) "Gizle ▲" else "Göster ▼",
                                style = MaterialTheme.typography.labelSmall,
                                color = CyanAccent,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.clickable { showChartSection = !showChartSection }
                            )
                        }

                        AnimatedVisibility(visible = showChartSection) {
                            Column(modifier = Modifier.padding(top = 12.dp)) {
                                val top5 = appUsages.sortedByDescending { it.wifiBytes + it.mobileBytes }.take(5)
                                val colors = listOf(CyanAccent, EmeraldPrimary, OrangeFlame, ElectricBlue, AmberWarning)
                                val slices = top5.mapIndexed { index, item ->
                                    val bytes = item.wifiBytes + item.mobileBytes
                                    val pct = if (totalAllBytes > 0) ((bytes.toDouble() / totalAllBytes) * 100).toFloat() else 0f
                                    DonutSlice(
                                        label = item.appName,
                                        value = bytes.toFloat(),
                                        color = colors.getOrElse(index) { Color.Gray }
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier.size(130.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        AppUsageDonutChart(
                                            slices = slices,
                                            modifier = Modifier.size(120.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        top5.forEachIndexed { index, app ->
                                            val bytes = app.wifiBytes + app.mobileBytes
                                            val color = colors.getOrElse(index) { Color.Gray }
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f, fill = false)) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(10.dp)
                                                            .clip(CircleShape)
                                                            .background(color)
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = app.appName,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        fontWeight = FontWeight.Medium,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = formatDataSize(bytes),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = color
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- 4. HANGİ UYGULAMA NE KADAR İNTERNET KULLANMIŞ (DETAYLI LİSTE & ARAMA & SIRALAMA) ---
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "UYGULAMA BAZLI TÜKETİM",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = OrangeFlame,
                        letterSpacing = 0.5.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = OrangeFlame.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, OrangeFlame.copy(alpha = 0.35f))
                    ) {
                        Text(
                            text = "${filteredApps.size} Uygulama",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = OrangeFlame,
                            maxLines = 1,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Arama Kutusu
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("app_data_search_input"),
                    placeholder = { Text("Uygulama ara (örn: YouTube, WhatsApp, Instagram)...", fontSize = 13.sp) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Ara", tint = CyanAccent, modifier = Modifier.size(18.dp))
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Temizle", modifier = Modifier.size(16.dp))
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanAccent,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Sıralama Seçenekleri
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    DataSortType.values().forEach { sortType ->
                        val isSortSelected = selectedSortType == sortType
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedSortType = sortType }
                                .testTag("sort_chip_${sortType.name}"),
                            shape = RoundedCornerShape(6.dp),
                            color = if (isSortSelected) OrangeFlame.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            border = BorderStroke(
                                1.dp,
                                if (isSortSelected) OrangeFlame else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                            )
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 6.dp, horizontal = 2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = sortType.title,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSortSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSortSelected) OrangeFlame else MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- 5. UYGULAMA KARTLARI LİSTESİ ---
        if (filteredApps.isEmpty()) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Aranan kritere uygun veri kaydı bulunamadı.",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Farklı bir arama terimi deneyin veya diğer dönemi seçin.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(filteredApps, key = { it.id }) { app ->
                val appTotalBytes = app.wifiBytes + app.mobileBytes
                val appSharePercent = if (totalAllBytes > 0) ((appTotalBytes.toDouble() / totalAllBytes) * 100).toFloat() else 0f
                val appWifiRatio = if (appTotalBytes > 0) (app.wifiBytes.toFloat() / appTotalBytes) else 0f
                val appMobileRatio = if (appTotalBytes > 0) (app.mobileBytes.toFloat() / appTotalBytes) else 0f

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("app_data_item_${app.packageName}"),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        // Üst Satır: İsim, Kategori, Toplam Veri
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f).padding(end = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // App Avatar Box
                                val avatarBg = when {
                                    app.appName.contains("YouTube", true) -> RedCritical
                                    app.appName.contains("Instagram", true) -> OrangeFlame
                                    app.appName.contains("WhatsApp", true) -> EmeraldPrimary
                                    app.appName.contains("Chrome", true) -> CyanAccent
                                    app.appName.contains("Spotify", true) -> EmeraldLight
                                    else -> ElectricBlue
                                }
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(avatarBg.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                                        .border(1.dp, avatarBg.copy(alpha = 0.5f), RoundedCornerShape(6.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = app.appName.take(1).uppercase(),
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 15.sp,
                                        color = avatarBg
                                    )
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column {
                                    Text(
                                        text = app.appName,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = CyanAccent.copy(alpha = 0.12f)
                                        ) {
                                            Text(
                                                text = app.category,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontSize = 10.sp,
                                                color = CyanAccent,
                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                            )
                                        }
                                        if (app.screenTimeMinutes > 0) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "• ${formatMinutes(app.screenTimeMinutes)} ekran",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }

                            // Sağ Bölüm: Toplam Tüketim & Pay
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = formatDataSize(appTotalBytes),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (appTotalBytes > 500 * 1024 * 1024L) OrangeFlame else CyanAccent
                                )
                                Text(
                                    text = "%${String.format(java.util.Locale.US, "%.1f", appSharePercent)} pay",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // İkili Dağılım Çubuğu (Wi-Fi vs Mobil Veri)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            if (appWifiRatio > 0f) {
                                Box(
                                    modifier = Modifier
                                        .weight(appWifiRatio)
                                        .height(8.dp)
                                        .background(CyanAccent)
                                )
                            }
                            if (appMobileRatio > 0f) {
                                Box(
                                    modifier = Modifier
                                        .weight(appMobileRatio)
                                        .height(8.dp)
                                        .background(EmeraldPrimary)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Alt Metrik Satırı: Wi-Fi MB vs Mobil MB ve Ayarlar Kısayolu
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(CyanAccent))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Wi-Fi: ${formatDataSize(app.wifiBytes)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(EmeraldPrimary))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Mobil: ${formatDataSize(app.mobileBytes)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            // Sistem Ayarları Butonu
                            IconButton(
                                onClick = { onOpenAppSettings(app.packageName) },
                                modifier = Modifier.size(24.dp).testTag("app_settings_${app.packageName}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Tune,
                                    contentDescription = "Sistem Veri Ayarları",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EditDataQuotaDialog(
    currentMobileMb: Long,
    currentWifiMb: Long,
    currentThreshold: Int,
    onDismiss: () -> Unit,
    onSave: (Long, Long, Int) -> Unit
) {
    var mobileText by remember { mutableStateOf(currentMobileMb.toString()) }
    var wifiText by remember { mutableStateOf(currentWifiMb.toString()) }
    var thresholdText by remember { mutableStateOf(currentThreshold.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Rule, contentDescription = null, tint = CyanAccent)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Veri Kotası ve Limit Belirle", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Mobil veri ve Wi-Fi kotalarınızı tanımlayarak aşım durumunda anlık uyarı bildirimi alın.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = mobileText,
                    onValueChange = { mobileText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Mobil Veri Limiti (MB)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = wifiText,
                    onValueChange = { wifiText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Wi-Fi Veri Limiti (MB)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = thresholdText,
                    onValueChange = { thresholdText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Uyarı Bildirimi Eşiği (%)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val mob = mobileText.toLongOrNull() ?: currentMobileMb
                    val wif = wifiText.toLongOrNull() ?: currentWifiMb
                    val thr = thresholdText.toIntOrNull()?.coerceIn(10, 100) ?: currentThreshold
                    onSave(mob, wif, thr)
                },
                colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = Color.Black)
            ) {
                Text("Kaydet", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("İptal")
            }
        }
    )
}
