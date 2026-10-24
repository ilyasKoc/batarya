package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.BatterySaver
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DataUsage
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeviceThermostat
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SdCard
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TipsAndUpdates
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.Lan
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.AppTimeLimitEntity
import com.example.data.entity.AppUsageDataEntity
import com.example.data.entity.TimelineEventEntity
import com.example.system.CpuCoreInfo
import com.example.system.FullSystemTelemetry
import com.example.system.LiveBatteryInfo
import com.example.system.SystemTelemetryHelper
import com.example.ui.components.AppUsageDonutChart
import com.example.ui.components.DonutSlice
import com.example.ui.components.MonthlyScreenTimeBarChart
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.OrangeFlame
import com.example.ui.theme.RedCritical
import com.example.viewmodel.CategoryUsageSummary

enum class AnalysisViewTab(
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val testTag: String
) {
    HARDWARE("Donanım & Telemetri", Icons.Default.Speed, "tab_analysis_hardware"),
    USAGE("Kullanım Karnesi", Icons.Default.PieChart, "tab_analysis_usage"),
    NETWORK("Ağ & Standby", Icons.Default.Wifi, "tab_analysis_network"),
    PRO_LABS("Pro Labs & Teşhis", Icons.Default.Bolt, "tab_analysis_pro_labs")
}

@Composable
fun MonthlyAnalysisScreen(
    monthlyUsages: List<AppUsageDataEntity>,
    liveBattery: LiveBatteryInfo = LiveBatteryInfo(),
    timelineEvents: List<TimelineEventEntity> = emptyList(),
    categoryBreakdown: List<CategoryUsageSummary> = emptyList(),
    appTimeLimits: List<AppTimeLimitEntity> = emptyList(),
    systemTelemetry: FullSystemTelemetry? = null,
    onRefreshTelemetry: () -> Unit = {},
    onSetAppTimeLimit: (packageName: String, appName: String, limitMinutes: Int) -> Unit = { _, _, _ -> },
    onToggleAppLimit: (packageName: String, isEnabled: Boolean) -> Unit = { _, _ -> },
    onDeleteAppLimit: (packageName: String) -> Unit = {}
) {
    val context = LocalContext.current
    val effectiveTelemetry = remember(systemTelemetry) {
        systemTelemetry ?: SystemTelemetryHelper.collectTelemetry(context)
    }

    var selectedViewTab by remember { mutableStateOf(AnalysisViewTab.HARDWARE) }

    val totalScreenMinutes = monthlyUsages.sumOf { it.screenTimeMinutes }
    val avgHours = if (monthlyUsages.isNotEmpty()) (totalScreenMinutes / 60f) / 30f else 0f
    val monthlyHours = (1..30).map { day -> day to avgHours }

    val categorySlices = remember(categoryBreakdown) {
        categoryBreakdown.map { cat ->
            DonutSlice(
                label = cat.categoryName,
                value = cat.totalMinutes.toFloat().coerceAtLeast(1f),
                color = Color(cat.colorHex)
            )
        }
    }

    val unlockCount = timelineEvents.count { it.eventType == "SCREEN_ON" }

    var showLimitDialog by remember { mutableStateOf(false) }
    var selectedAppForLimit by remember { mutableStateOf<Pair<String, String>?>(null) }
    var customLimitMinutesText by remember { mutableStateOf("45") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("monthly_analysis_screen")
    ) {
        // --- 1. PINNED HEADER (Sistem Analizi & Telemetri + CANLI Badge + Refresh) ---
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                    Text(
                        text = "Sistem Analizi & Telemetri",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Canlı Donanım, Güç & Kullanım Karnesi",
                        style = MaterialTheme.typography.labelSmall,
                        color = CyanAccent,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = EmeraldPrimary.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.35f))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .background(EmeraldPrimary, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "CANLI (1.5s)",
                                style = MaterialTheme.typography.labelSmall,
                                color = EmeraldPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = onRefreshTelemetry,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("analysis_refresh_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Yenile",
                            tint = CyanAccent,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // --- 2. PINNED TAB ROW (ALWAYS ACCESSIBLE, NEVER SCROLLS AWAY) ---
        ScrollableTabRow(
            selectedTabIndex = selectedViewTab.ordinal,
            edgePadding = 12.dp,
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
            contentColor = CyanAccent,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedViewTab.ordinal]),
                    color = CyanAccent
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("monthly_tab_row")
        ) {
            AnalysisViewTab.values().forEach { tab ->
                val isSelected = selectedViewTab == tab
                Tab(
                    selected = isSelected,
                    onClick = { selectedViewTab = tab },
                    text = {
                        Text(
                            text = tab.title,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) CyanAccent else MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    },
                    icon = {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = null,
                            tint = if (isSelected) CyanAccent else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    modifier = Modifier.testTag(tab.testTag)
                )
            }
        }

        // --- 3. SEÇİLİ SEKME İÇERİĞİ (HER SEKME AYRI LAZYCOLUMN VE TASARIM KAYMASI YOK) ---
        Crossfade(
            targetState = selectedViewTab,
            label = "analysis_view_tab_crossfade",
            modifier = Modifier.weight(1f)
        ) { currentTab ->
            when (currentTab) {
                AnalysisViewTab.HARDWARE -> {
                    val mem = effectiveTelemetry.memory
                    val stor = effectiveTelemetry.storage
                    val cpu = effectiveTelemetry.cpu
                    val power = effectiveTelemetry.power

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("monthly_hardware_scroll"),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // PRO LABS HIZLI ERİŞİM KARTI (Tıklanınca Pro Labs sekmesine geçer)
                        item {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedViewTab = AnalysisViewTab.PRO_LABS }
                                    .testTag("btn_open_pro_labs"),
                                shape = RoundedCornerShape(16.dp),
                                color = CyanAccent.copy(alpha = 0.12f),
                                border = BorderStroke(1.5.dp, CyanAccent)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        modifier = Modifier.weight(1f),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(42.dp)
                                                .background(CyanAccent, RoundedCornerShape(10.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Bolt,
                                                contentDescription = null,
                                                tint = Color.Black,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = "🔬 PRO LABS MERKEZİ",
                                                    style = MaterialTheme.typography.titleSmall,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = CyanAccent
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = EmeraldPrimary.copy(alpha = 0.2f)
                                                ) {
                                                    Text(
                                                        text = "SEKME",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.Bold,
                                                        color = EmeraldPrimary,
                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "⚡ Güç Tasarrufu Grafiği • Kablo Testi • Teşhis • Hız Testi",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Aç",
                                        tint = CyanAccent,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }

                        // --- A. RAM KULLANIMI VE KALAN HAFIZA KARTLARI ---
                        item {
                            Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // RAM KULLANIM KARTI
                    Card(
                        modifier = Modifier.weight(1f).testTag("card_ram_telemetry"),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(ElectricBlue.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Memory, contentDescription = null, tint = ElectricBlue, modifier = Modifier.size(20.dp))
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (mem.ramUsagePercent > 80) RedCritical.copy(alpha = 0.15f) else ElectricBlue.copy(alpha = 0.12f)
                                ) {
                                    Text(
                                        text = "%${mem.ramUsagePercent} Dolu",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (mem.ramUsagePercent > 80) RedCritical else ElectricBlue,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            Text(text = "RAM (Bellek)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${mem.usedRamMb} MB / ${mem.totalRamMb} MB",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Kalan: ${mem.availableRamMb} MB Boş",
                                style = MaterialTheme.typography.labelSmall,
                                color = EmeraldPrimary,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(10.dp))
                            // Progress bar
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(Color.White.copy(alpha = 0.1f))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth((mem.ramUsagePercent / 100f).coerceIn(0.05f, 1f))
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(
                                            Brush.horizontalGradient(
                                                listOf(ElectricBlue, if (mem.ramUsagePercent > 80) RedCritical else CyanAccent)
                                            )
                                        )
                                )
                            }
                        }
                    }

                    // DAHİLİ HAFIZA (DEPOLAMA) KARTI
                    Card(
                        modifier = Modifier.weight(1f).testTag("card_storage_telemetry"),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(CyanAccent.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.SdCard, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(20.dp))
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = CyanAccent.copy(alpha = 0.12f)
                                ) {
                                    Text(
                                        text = "%${stor.usedPercent} Dolu",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = CyanAccent,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            Text(text = "Dahili Hafıza", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${stor.usedGb} GB / ${stor.totalGb} GB",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Kalan: ${stor.freeGb} GB Boş",
                                style = MaterialTheme.typography.labelSmall,
                                color = CyanAccent,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(10.dp))
                            // Progress bar
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(Color.White.copy(alpha = 0.1f))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth((stor.usedPercent / 100f).coerceIn(0.05f, 1f))
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(
                                            Brush.horizontalGradient(
                                                listOf(CyanAccent, EmeraldPrimary)
                                            )
                                        )
                                )
                            }
                        }
                    }
                }
            }

            // --- B. DETAYLI RAM DAĞILIMI (JVM / HEAP / NATIVE / GRAFİK) ---
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("card_ram_breakdown"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "RAM & Bellek Dağılım Detayları",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            RectangularMetricCard(
                                label = "Dalvik (JVM)",
                                value = "${mem.dalvikHeapMb} MB",
                                subtitle = "Java / Kotlin",
                                color = ElectricBlue,
                                modifier = Modifier.weight(1f)
                            )
                            RectangularMetricCard(
                                label = "Native Heap",
                                value = "${mem.nativeHeapMb} MB",
                                subtitle = "C++ & Driver",
                                color = CyanAccent,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            RectangularMetricCard(
                                label = "Grafik / GPU",
                                value = "${mem.graphicsMemoryMb} MB",
                                subtitle = "Ekran & Çizim",
                                color = EmeraldPrimary,
                                modifier = Modifier.weight(1f)
                            )
                            RectangularMetricCard(
                                label = "Kod & Stack",
                                value = "${mem.codeAndStackMb} MB",
                                subtitle = "Thread & Alan",
                                color = OrangeFlame,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Düşük Bellek Eşiği: ${mem.thresholdMb} MB",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = if (mem.isLowMemory) "⚠️ Düşük Bellek" else "✓ Bellek Güvenli",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (mem.isLowMemory) RedCritical else EmeraldPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // --- C. İŞLEMCİ (CPU) ANLIK HIZ & ÇEKİRDEK FREKANS ANALİZİ ---
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("card_cpu_telemetry"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(OrangeFlame.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Speed, contentDescription = null, tint = OrangeFlame, modifier = Modifier.size(20.dp))
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "İşlemci (CPU) Anlık Hızları",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${cpu.coresCount} Çekirdek • ${cpu.architecture} • ${cpu.socModel}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = OrangeFlame.copy(alpha = 0.12f),
                                border = BorderStroke(1.dp, OrangeFlame.copy(alpha = 0.3f))
                            ) {
                                Text(
                                    text = "%${cpu.overallUsagePercent} Yük",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = OrangeFlame,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // CPU Load Progress Bar
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(7.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color.White.copy(alpha = 0.08f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth((cpu.overallUsagePercent / 100f).coerceIn(0.05f, 1f))
                                    .height(7.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(CyanAccent, OrangeFlame, if (cpu.overallUsagePercent > 80) RedCritical else OrangeFlame)
                                        )
                                    )
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Live Core Frequencies Grid
                        Text(
                            text = "Anlık Çekirdek Frekansları (MHz / GHz):",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            cpu.coreFrequencies.chunked(2).forEach { rowCores ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    rowCores.forEach { core ->
                                        CpuCoreCard(
                                            core = core,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                    if (rowCores.size == 1) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Governor: ${cpu.cpuGovernor}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Ort. Hız: ${"%.2f".format(cpu.averageFreqMhz / 1000f)} GHz",
                                style = MaterialTheme.typography.labelSmall,
                                color = OrangeFlame,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // --- D. ANLIK PİL TÜKETİM HIZI VE ŞARJ HIZI ANALİZİ ---
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("card_power_telemetry"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background((if (power.isCharging) EmeraldPrimary else RedCritical).copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (power.isCharging) Icons.Default.Bolt else Icons.Default.ElectricBolt,
                                        contentDescription = null,
                                        tint = if (power.isCharging) EmeraldPrimary else RedCritical,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = if (power.isCharging) "Şarj Hızı & Gücü" else "Pil Tüketim Hızı & Gücü",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = power.chargingSpeedClassification,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (power.isCharging) EmeraldPrimary else AmberWarning,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = (if (power.isCharging) EmeraldPrimary else CyanAccent).copy(alpha = 0.12f),
                                border = BorderStroke(1.dp, (if (power.isCharging) EmeraldPrimary else CyanAccent).copy(alpha = 0.3f))
                            ) {
                                Text(
                                    text = "${power.powerWatts} W",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (power.isCharging) EmeraldPrimary else CyanAccent,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Technical Power Breakdown Grid
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            TelemetryDetailBox(
                                title = if (power.isCharging) "Anlık Şarj Akımı" else "Anlık Tüketim",
                                value = "${if (power.isCharging && power.instantCurrentMa > 0) "+" else ""}${power.instantCurrentMa} mA",
                                subtitle = "Ortalama: ${power.averageCurrentMa} mA",
                                color = if (power.isCharging) EmeraldPrimary else RedCritical,
                                modifier = Modifier.weight(1f)
                            )
                            TelemetryDetailBox(
                                title = "Şarj / Pil Voltajı",
                                value = "${power.voltageVolts} V",
                                subtitle = "${power.voltageMv} mV",
                                color = ElectricBlue,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            TelemetryDetailBox(
                                title = if (power.isCharging) "Şarj Gücü (mW)" else "Tüketim Gücü",
                                value = "${power.powerMilliwatts} mW",
                                subtitle = "${power.powerWatts} Watt",
                                color = OrangeFlame,
                                modifier = Modifier.weight(1f)
                            )
                            TelemetryDetailBox(
                                title = if (power.isCharging) "Kalan Dolum Süresi" else "Saatlik Tüketim Hızı",
                                value = if (power.isCharging) "~${power.estimatedTimeRemainingMinutes} dk" else "%${power.estimatedDrainRatePerHourPercent} / sa",
                                subtitle = if (power.isCharging) "Tam Doluma" else "Kalan: ~${power.estimatedTimeRemainingMinutes / 60} sa ${power.estimatedTimeRemainingMinutes % 60} dk",
                                color = if (power.isCharging) EmeraldPrimary else AmberWarning,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Additional technical specs row
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = "Bağlantı Türü:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(text = power.chargePlugType, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = "Pil Sıcaklığı & Sağlık:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(text = "${power.temperatureCelsius}°C • ${power.batteryHealth}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = if (power.temperatureCelsius > 42f) RedCritical else EmeraldPrimary)
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = "Kalan Enerji / Şarj:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(text = "${power.energyCounterWh} Wh • ${power.chargeCounterMah} mAh (${power.batteryTechnology})", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = CyanAccent)
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = "Isıl Durum (Thermal Throttling):", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(text = power.thermalStatus, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

        // ==========================================
        // TAB 2: KULLANIM VE KATEGORİ KARNESİ (MAKARNA / DONUT GRAFİĞİ)
        // ==========================================
        AnalysisViewTab.USAGE -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("monthly_usage_scroll"),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // --- 2. MONTHLY SCREEN TIME BAR CHART ---
                        item {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("monthly_screen_chart_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        if (monthlyUsages.isNotEmpty() && avgHours > 0f) {
                            MonthlyScreenTimeBarChart(
                                dailyHours = monthlyHours,
                                averageHours = avgHours
                            )
                        } else {
                            Text(
                                text = "Aylık Ekran Süresi Grafiği",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Henüz 30 günlük ekran süresi verisi toplanmadı.\nCihaz kullanıldıkça gerçek veriler burada gösterilecektir.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // --- 3. MONTHLY METRIC COUNTERS ---
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AnalysisSummaryCard(
                        title = "Kayıtlı Kilit Açma",
                        value = "$unlockCount kez",
                        subtitle = if (unlockCount > 0) "Kayıtlı açılış seansı" else "Henüz kilit kaydı yok",
                        icon = Icons.Default.PhoneAndroid,
                        color = CyanAccent,
                        modifier = Modifier.weight(1f)
                    )
                    AnalysisSummaryCard(
                        title = "Pil Sağlık Durumu",
                        value = if (liveBattery.percentage > 0) "%${liveBattery.percentage} (${liveBattery.health})" else liveBattery.health,
                        subtitle = "${liveBattery.technology} • ${liveBattery.voltageMv} mV",
                        icon = Icons.Default.ElectricBolt,
                        color = EmeraldPrimary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // --- 4. UYGULAMA KATEGORİSİ DAĞILIMI (MAKARNA / DONUT GRAFİK ANALİZİ) ---
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("monthly_category_donut_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Kategori Dağılımı (Makarna Grafik)",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Sosyal Medya, Oyun, Video ve Üretkenlik Payları",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Icon(
                                imageVector = Icons.Default.PieChart,
                                contentDescription = null,
                                tint = CyanAccent,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        if (categorySlices.isNotEmpty()) {
                            val totalMinutes = categoryBreakdown.sumOf { it.totalMinutes }
                            val totalHours = totalMinutes / 60

                            AppUsageDonutChart(
                                slices = categorySlices,
                                centerTitle = if (totalHours > 0) "$totalHours Saat" else "$totalMinutes Dk",
                                centerSubtitle = "Toplam Süre"
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Category Details List
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                categoryBreakdown.forEach { cat ->
                                    val catColor = Color(cat.colorHex)
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(12.dp)
                                                            .background(catColor, CircleShape)
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(
                                                        text = cat.categoryName,
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }

                                                Text(
                                                    text = "%${"%.1f".format(cat.percentage)} (${cat.totalMinutes} dk)",
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = catColor
                                                )
                                            }

                                            if (cat.topApps.isNotEmpty()) {
                                                Spacer(modifier = Modifier.height(6.dp))
                                                Text(
                                                    text = "Öne çıkanlar: ${cat.topApps.joinToString(", ")}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }

                                            Spacer(modifier = Modifier.height(6.dp))
                                            // Progress bar
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(5.dp)
                                                    .clip(RoundedCornerShape(3.dp))
                                                    .background(Color.White.copy(alpha = 0.1f))
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth((cat.percentage / 100f).coerceIn(0.02f, 1f))
                                                        .height(5.dp)
                                                        .clip(RoundedCornerShape(3.dp))
                                                        .background(catColor)
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

            // --- 5. UYGULAMA SÜRE LİMİTLERİ (DİJİTAL DETOKS) ---
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("app_limits_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Uygulama Süre Limitleri (Detoks)",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Belirlenen süre aşıldığında sistem uyarır",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            IconButton(
                                onClick = {
                                    val topApp = monthlyUsages.firstOrNull()
                                    if (topApp != null) {
                                        selectedAppForLimit = topApp.packageName to topApp.appName
                                    } else {
                                        selectedAppForLimit = "com.instagram.android" to "Instagram"
                                    }
                                    showLimitDialog = true
                                },
                                modifier = Modifier.testTag("btn_add_app_limit")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Limit Ekle",
                                    tint = CyanAccent
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        if (appTimeLimits.isNotEmpty()) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                appTimeLimits.forEach { limit ->
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = limit.appName,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                    text = "Günlük Limit: ${limit.limitMinutes} dakika",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = CyanAccent
                                                )
                                            }

                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Switch(
                                                    checked = limit.isEnabled,
                                                    onCheckedChange = { onToggleAppLimit(limit.packageName, it) },
                                                    colors = SwitchDefaults.colors(
                                                        checkedThumbColor = Color.Black,
                                                        checkedTrackColor = CyanAccent
                                                    )
                                                )

                                                IconButton(onClick = { onDeleteAppLimit(limit.packageName) }) {
                                                    Icon(
                                                        imageVector = Icons.Default.Delete,
                                                        contentDescription = "Sil",
                                                        tint = RedCritical.copy(alpha = 0.7f),
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "Henüz süre limiti eklenmedi.",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Aşağıdaki popüler uygulamalardan birini seçerek hemen dijital detoks limiti oluşturabilirsiniz.",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        listOf(
                                            Triple("com.instagram.android", "Instagram", 45),
                                            Triple("com.google.android.youtube", "YouTube", 60),
                                            Triple("com.zhiliaoapp.musically", "TikTok", 30)
                                        ).forEach { (pkg, name, defaultLimit) ->
                                            OutlinedButton(
                                                onClick = {
                                                    onSetAppTimeLimit(pkg, name, defaultLimit)
                                                },
                                                modifier = Modifier.weight(1f),
                                                shape = RoundedCornerShape(8.dp),
                                                border = BorderStroke(1.dp, CyanAccent.copy(alpha = 0.5f))
                                            ) {
                                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                    Text(text = name, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = CyanAccent)
                                                    Text(text = "$defaultLimit dk", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
        }
    }

        // ==========================================
        // TAB 3: AĞ & STANDBY DOZE KOVALARI
        // ==========================================
        AnalysisViewTab.NETWORK -> {
            val net = effectiveTelemetry.network
            val buckets = effectiveTelemetry.standbyBuckets

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("monthly_network_scroll"),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 1. AĞ VE TELEMETRİ KARTI
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().testTag("card_network_telemetry"),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, CyanAccent.copy(alpha = 0.35f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Üst Ağ Başlığı
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
                                            .size(40.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(CyanAccent.copy(alpha = 0.15f))
                                            .border(1.dp, CyanAccent.copy(alpha = 0.3f), RoundedCornerShape(10.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Wifi,
                                            contentDescription = null,
                                            tint = CyanAccent,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = net.networkType,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = EmeraldPrimary.copy(alpha = 0.15f)
                                            ) {
                                                Text(
                                                    text = net.networkGeneration,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = EmeraldPrimary,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                        Text(
                                            text = "${net.wifiStandard} • ${net.wifiFrequencyBand}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = CyanAccent.copy(alpha = 0.15f),
                                    border = BorderStroke(1.dp, CyanAccent.copy(alpha = 0.4f))
                                ) {
                                    Text(
                                        text = "${net.wifiLinkSpeedMbps} Mbps",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = CyanAccent,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // 2x2 Dikdörtgen Ağ Metrik Kartları
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                RectangularMetricCard(
                                    label = "Sinyal Gücü",
                                    value = "%${net.wifiSignalPercent}",
                                    subtitle = "${net.wifiRssiDbm} dBm",
                                    icon = Icons.Default.SignalCellularAlt,
                                    color = CyanAccent,
                                    modifier = Modifier.weight(1f)
                                )
                                RectangularMetricCard(
                                    label = "Bağlantı Hızı",
                                    value = "${net.wifiLinkSpeedMbps} Mbps",
                                    subtitle = net.wifiFrequencyBand,
                                    icon = Icons.Default.Speed,
                                    color = EmeraldPrimary,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                RectangularMetricCard(
                                    label = "Yerel IP",
                                    value = net.ipAddress.ifBlank { "127.0.0.1" },
                                    subtitle = "IPv4 Protokolü",
                                    icon = Icons.Default.Lan,
                                    color = ElectricBlue,
                                    modifier = Modifier.weight(1f)
                                )
                                RectangularMetricCard(
                                    label = "Veri Paketleri",
                                    value = "${net.totalRxPackets + net.totalTxPackets}",
                                    subtitle = "Ort. ${net.avgPacketSizeBytes} Bayt",
                                    icon = Icons.Default.SwapVert,
                                    color = OrangeFlame,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Operatör & Standart Alt Bilgi Şeridi
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Operatör: ${net.carrierName.ifBlank { "Yerel Wi-Fi" }}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "Durum: ${if (net.isConnected) "✓ Bağlı" else "Bağlı Değil"}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (net.isConnected) EmeraldPrimary else RedCritical
                                    )
                                }
                            }
                        }
                    }
                }

                // 2. ANDROID STANDBY & DOZE KOVALARI KARTI
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().testTag("card_standby_buckets"),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(ElectricBlue.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Bedtime,
                                        contentDescription = null,
                                        tint = ElectricBlue,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Android Standby & Doze Kovaları",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "İşletim sisteminin arka plan güç tasarrufu kısıtlama seviyeleri.",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Doze Modu Açıklama Şeritleri (4'lü Rehber)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Surface(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(6.dp),
                                    color = EmeraldPrimary.copy(alpha = 0.12f),
                                    border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.3f))
                                ) {
                                    Column(modifier = Modifier.padding(4.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("Aktif", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = EmeraldPrimary, fontSize = 10.sp)
                                        Text("Kısıt Yok", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 8.5.sp)
                                    }
                                }
                                Surface(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(6.dp),
                                    color = ElectricBlue.copy(alpha = 0.12f),
                                    border = BorderStroke(1.dp, ElectricBlue.copy(alpha = 0.3f))
                                ) {
                                    Column(modifier = Modifier.padding(4.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("Çalışma", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = ElectricBlue, fontSize = 10.sp)
                                        Text("Düşük Gecikme", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 8.5.sp)
                                    }
                                }
                                Surface(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(6.dp),
                                    color = AmberWarning.copy(alpha = 0.12f),
                                    border = BorderStroke(1.dp, AmberWarning.copy(alpha = 0.3f))
                                ) {
                                    Column(modifier = Modifier.padding(4.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("Sık", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = AmberWarning, fontSize = 10.sp)
                                        Text("30 Dk Periyot", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 8.5.sp)
                                    }
                                }
                                Surface(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(6.dp),
                                    color = RedCritical.copy(alpha = 0.12f),
                                    border = BorderStroke(1.dp, RedCritical.copy(alpha = 0.3f))
                                ) {
                                    Column(modifier = Modifier.padding(4.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("Nadir", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = RedCritical, fontSize = 10.sp)
                                        Text("Maks. Kısıt", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 8.5.sp)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            if (buckets.isEmpty()) {
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Info, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = "Sistem arka plan Standby kova verileri taranıyor...",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    buckets.take(8).forEach { item ->
                                        val badgeColor = when {
                                            item.restrictionLevel.contains("Kısıtlama Yok", ignoreCase = true) || item.bucketName.contains("Aktif", ignoreCase = true) -> EmeraldPrimary
                                            item.bucketName.contains("Çalışma", ignoreCase = true) -> ElectricBlue
                                            item.bucketName.contains("Sık", ignoreCase = true) -> AmberWarning
                                            else -> RedCritical
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                            border = BorderStroke(1.dp, badgeColor.copy(alpha = 0.25f))
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth().padding(10.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(
                                                    modifier = Modifier.weight(1f),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(8.dp)
                                                            .background(badgeColor, CircleShape)
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Column {
                                                        Text(
                                                            text = item.appName,
                                                            style = MaterialTheme.typography.bodySmall,
                                                            fontWeight = FontWeight.Bold,
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis
                                                        )
                                                        Text(
                                                            text = "${item.bucketName} • ${item.category}",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = badgeColor,
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis
                                                        )
                                                    }
                                                }

                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = badgeColor.copy(alpha = 0.15f),
                                                    border = BorderStroke(1.dp, badgeColor.copy(alpha = 0.4f))
                                                ) {
                                                    Text(
                                                        text = item.restrictionLevel,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = badgeColor,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
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

                // 3. AKILLI AĞ & STANDBY OPTİMİZASYON İPUÇLARI
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                        border = BorderStroke(1.dp, AmberWarning.copy(alpha = 0.25f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.TipsAndUpdates, contentDescription = null, tint = AmberWarning, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Ağ & Standby Enerji Tasarruf İpuçları",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            TipRow(
                                title = "Wi-Fi & Zayıf Mobil Sinyal",
                                desc = "Hücresel çekim gücü 1 çubuğa düştüğünde modem arama gücünü 3 katına çıkarır. Zayıf çeken yerlerde Wi-Fi'ye geçmek pili korur."
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            TipRow(
                                title = "Derin Uyku (Doze) Modu",
                                desc = "Cihaz hareket etmediğinde ve ekran kapalıyken sistem ağ trafiğini toplu pencerelerde iletir, gece bekleme kaybını %1'in altına düşürür."
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            TipRow(
                                title = "Nadir Standby Kovası",
                                desc = "Kullanmadığınız uygulamaları 'Kısıtlı' kovaya alarak arka plan senkronizasyonunu ve uyanma çağrılarını durdurabilirsiniz."
                            )
                        }
                    }
                }
            }
        }

                AnalysisViewTab.PRO_LABS -> {
                    ProLabContent(
                        liveBattery = liveBattery,
                        timelineEvents = timelineEvents,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }

    if (showLimitDialog && selectedAppForLimit != null) {
        val (pkg, name) = selectedAppForLimit!!
        AlertDialog(
            onDismissRequest = { showLimitDialog = false },
            title = { Text(text = "$name için Günlük Süre Limiti") },
            text = {
                Column {
                    Text(
                        text = "Bu uygulama için günlük maksimum kullanım süresi (dakika) belirleyin. Süre dolmaya yaklaştığında ve aşıldığında bildirim verilir.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = customLimitMinutesText,
                        onValueChange = { customLimitMinutesText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Günlük Süre Limiti (Dakika)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(30, 45, 60, 90).forEach { mins ->
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { customLimitMinutesText = mins.toString() },
                                shape = RoundedCornerShape(6.dp),
                                color = if (customLimitMinutesText == mins.toString()) OrangeFlame.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                border = BorderStroke(1.dp, if (customLimitMinutesText == mins.toString()) OrangeFlame else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                            ) {
                                Box(modifier = Modifier.padding(vertical = 6.dp), contentAlignment = Alignment.Center) {
                                    Text(text = "$mins dk", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val mins = customLimitMinutesText.toIntOrNull() ?: 45
                        onSetAppTimeLimit(pkg, name, mins)
                        showLimitDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OrangeFlame)
                ) {
                    Text("Kaydet", fontWeight = FontWeight.Bold, color = Color.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLimitDialog = false }) {
                    Text("İptal")
                }
            }
        )
    }
}

@Composable
fun CpuCoreCard(
    core: CpuCoreInfo,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Core #${core.coreIndex}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = CyanAccent
                )
                Text(
                    text = "${core.curFreqMhz} MHz",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = OrangeFlame
                )
            }
            Spacer(modifier = Modifier.height(3.dp))
            val percent = if (core.maxFreqMhz > core.minFreqMhz) {
                ((core.curFreqMhz - core.minFreqMhz).toFloat() / (core.maxFreqMhz - core.minFreqMhz)).coerceIn(0.1f, 1f)
            } else 0.5f

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color.White.copy(alpha = 0.1f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(percent)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(OrangeFlame)
                )
            }
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = "${core.minFreqMhz} - ${core.maxFreqMhz} MHz",
                style = MaterialTheme.typography.labelSmall,
                fontSize = 9.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun RectangularMetricCard(
    label: String,
    value: String,
    subtitle: String? = null,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    color: Color = CyanAccent,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.25f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = color,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (!subtitle.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun TelemetryDetailBox(
    title: String,
    value: String,
    subtitle: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(text = title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = color, maxLines = 1)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = subtitle, style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
        }
    }
}

@Composable
fun TelemetryMiniMetric(
    label: String,
    value: String,
    color: Color
) {
    Column {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = value, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = color)
    }
}

@Composable
fun AnalysisSummaryCard(
    title: String,
    value: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = color,
                maxLines = 1
            )
        }
    }
}

@Composable
fun TipRow(title: String, desc: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .padding(top = 6.dp)
                .size(7.dp)
                .background(CyanAccent, CircleShape)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 18.sp)
        }
    }
}
