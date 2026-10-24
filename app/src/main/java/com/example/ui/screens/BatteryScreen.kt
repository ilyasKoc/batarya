package com.example.ui.screens

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.entity.AppUsageDataEntity
import com.example.data.entity.BatterySnapshotEntity
import com.example.data.entity.DataLimitConfigEntity
import com.example.system.AppIconCache
import com.example.system.LiveBatteryInfo
import com.example.system.RunningAppItem
import com.example.system.ScreenBatteryStats
import com.example.system.ScreenUsageHelper
import com.example.system.SmartChargingAnalysisResult
import com.example.system.SmartChargingScheduleConfig
import com.example.system.StandbyDrainAnalysis
import com.example.ui.components.ChartPoint
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.OrangeFlame
import com.example.ui.theme.PurpleNeon
import com.example.ui.theme.RedCritical
import com.example.viewmodel.NightDrainReport
import com.example.viewmodel.PowerMode

enum class BatteryScreenTab(
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val testTag: String
) {
    SUMMARY_SCREEN("Bugün Kullanılan", Icons.Default.Schedule, "tab_summary_screen"),
    POWER_SAVINGS("Tasarruf & RAM", Icons.Default.PowerSettingsNew, "tab_power_savings"),
    HARDWARE_TOOLS("Araçlar & Test", Icons.Default.Speed, "tab_hardware_tools"),
    ALARMS_GUARD("Alarmlar & Koruma", Icons.Default.NotificationsActive, "tab_alarms_guard"),
    ANALYSIS_HISTORY("Analiz & Geçmiş", Icons.Default.Analytics, "tab_analysis_history"),
    INTERNET_DATA("İnternet & Veri", Icons.Default.Wifi, "tab_internet_data")
}

data class TodayAppConsumptionItem(
    val appName: String,
    val packageName: String,
    val screenTimeMinutes: Int,
    val formattedDuration: String,
    val consumptionPercent: Float,
    val category: String = "Genel"
)

data class StandbyHighDrainAppItem(
    val appName: String,
    val packageName: String,
    val estimatedDrainPercent: Float,
    val backgroundMinutes: Int,
    val wakeupsCount: Int,
    val memoryMb: Int,
    val category: String,
    val riskLevel: String, // "KRİTİK", "YÜKSEK", "ORTA", "DÜŞÜK"
    val reason: String,
    val isKillable: Boolean = true
)

@Composable
fun BatteryScreen(
    batteryInfo: LiveBatteryInfo,
    snapshots: List<BatterySnapshotEntity>,
    powerMode: PowerMode,
    screenStats: ScreenBatteryStats = ScreenBatteryStats(),
    onResetUnplugged: () -> Unit = {},
    onPowerModeChange: (PowerMode) -> Unit,
    fullChargeAlarmEnabled: Boolean,
    fullChargeTargetPercent: Int,
    onToggleAlarm: (Boolean) -> Unit,
    onSetAlarmTarget: (Int) -> Unit,
    highTempAlarmEnabled: Boolean = true,
    highTempThresholdCelsius: Float = 43.0f,
    onToggleHighTempAlarm: (Boolean) -> Unit = {},
    onSetHighTempThreshold: (Float) -> Unit = {},
    onTestHighTempAlarm: () -> Unit = {},
    nightDrainReport: NightDrainReport = NightDrainReport(),
    backgroundApps: List<RunningAppItem> = emptyList(),
    isCleaningMemory: Boolean = false,
    optimizationMessage: String? = null,
    onRefreshBackgroundApps: () -> Unit = {},
    onKillBackgroundApp: (String) -> Unit = {},
    onKillAllBackgroundApps: () -> Unit = {},
    onClearOptimizationMessage: () -> Unit = {},
    onOpenAppSettings: (String) -> Unit = {},
    appUsages: List<AppUsageDataEntity> = emptyList(),
    dataLimitConfig: DataLimitConfigEntity = DataLimitConfigEntity(),
    selectedPeriod: String = "DAILY",
    onPeriodChange: (String) -> Unit = {},
    onUpdateLimits: (Long, Long, Int, Boolean) -> Unit = { _, _, _, _ -> },
    standbyAnalysis: StandbyDrainAnalysis = StandbyDrainAnalysis(),
    onExportDiagnosticReport: () -> Unit = {},
    onExportCsvReport: () -> Unit = {},
    killedStandbyPackages: Set<String> = emptySet(),
    onKillStandbyApp: (String) -> Unit = {},
    onKillAllStandbyApps: (Collection<String>) -> Unit = {},
    onResetStandbyOptimization: () -> Unit = {},
    onNavigateToAiReport: () -> Unit = {},
    smartChargingAnalysis: SmartChargingAnalysisResult = SmartChargingAnalysisResult(
        predictedDepletionTimeFormatted = "Bugün 22:30",
        hoursRemainingFormatted = "7 saat kaldı",
        depletionWarningLevel = "GÜVENLİ",
        recommendedChargeSlots = emptyList(),
        recommendedPowerSaveWindows = emptyList(),
        hourlyForecast = emptyList(),
        healthAdvice = "",
        routinePersona = "Dengeli Rutin",
        isCurrentlyInPowerSaveWindow = false,
        isCurrentlyInChargingWindow = false
    ),
    smartChargingConfig: SmartChargingScheduleConfig = SmartChargingScheduleConfig(),
    onUpdateSmartChargingConfig: (SmartChargingScheduleConfig) -> Unit = {},
    onApplyAiChargingRecommendation: () -> Unit = {}
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(BatteryScreenTab.SUMMARY_SCREEN) }
    var showHighDrainAppsDialog by remember { mutableStateOf(false) }
    var showSmartChargingDialog by remember { mutableStateOf(false) }
    var showChargingOverlay by remember { mutableStateOf(false) }
    var showChargerBenchmark by remember { mutableStateOf(false) }
    var showPhoneCooler by remember { mutableStateOf(false) }
    var showBatteryCalibration by remember { mutableStateOf(false) }
    var calibratedCycles by remember { mutableIntStateOf(com.example.system.CustomAlarmSoundHelper.getEstimatedCycleCount(context)) }
    var lastCalibrationDate by remember { mutableStateOf(com.example.system.CustomAlarmSoundHelper.getLastCalibrationDateFormatted(context)) }
    var showStressTest by remember { mutableStateOf(false) }
    var showAppThermalIndex by remember { mutableStateOf(false) }
    var showNightGuardDialog by remember { mutableStateOf(false) }
    var showSmartAlarmsDialog by remember { mutableStateOf(false) }
    var showWeeklySummaryDialog by remember { mutableStateOf(false) }
    var showDatabaseBackupDialog by remember { mutableStateOf(false) }
    var showModularPowerFeaturesDialog by remember { mutableStateOf(false) }
    var isHighDrainStandbyExpanded by remember { mutableStateOf(false) }
    var highDrainDialogFilter by remember { mutableIntStateOf(0) } // 0 = Tümü, 1 = Yüksek/Kritik, 2 = Wakelock

    val levelColor = when {
        batteryInfo.percentage <= 20 -> RedCritical
        batteryInfo.percentage <= 40 -> AmberWarning
        else -> EmeraldPrimary
    }

    // Infinite breathing pulse animation for the battery gauge
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val highDrainStandbyApps = remember(backgroundApps, screenStats) {
        val apps = mutableListOf<StandbyHighDrainAppItem>()
        if (backgroundApps.isNotEmpty()) {
            backgroundApps.take(8).forEachIndexed { idx, bgApp ->
                val estimatedDrain = (0.8f + (bgApp.memoryUsageMb / 180f) + (idx * 0.35f)).coerceIn(0.5f, 6.5f)
                val wakeups = (4 + idx * 5 + (bgApp.memoryUsageMb / 45f).toInt()).coerceAtLeast(2)
                val risk = when {
                    estimatedDrain > 3.0f || wakeups > 18 -> "KRİTİK"
                    estimatedDrain > 1.8f || wakeups > 10 -> "YÜKSEK"
                    estimatedDrain > 1.0f -> "ORTA"
                    else -> "DÜŞÜK"
                }
                val reason = when {
                    bgApp.category.contains("Sosyal", ignoreCase = true) -> "Arka plan soket bağlantısı ve anlık bildirim kuyruğu"
                    bgApp.category.contains("Oyun", ignoreCase = true) -> "Arka planda uykuyu bölen ses ve grafik motoru"
                    bgApp.category.contains("Medya", ignoreCase = true) || bgApp.category.contains("Video", ignoreCase = true) -> "Medya önbellekleme ve ses senkronizasyonu"
                    wakeups > 12 -> "Sık aralıklarla CPU uyanması (Wakelock) tetikliyor"
                    else -> "Arka planda RAM ve CPU döngüsü tutuyor"
                }
                apps.add(
                    StandbyHighDrainAppItem(
                        appName = bgApp.appName,
                        packageName = bgApp.packageName,
                        estimatedDrainPercent = estimatedDrain,
                        backgroundMinutes = (12 + idx * 14 + (bgApp.memoryUsageMb / 10f).toInt()),
                        wakeupsCount = wakeups,
                        memoryMb = bgApp.memoryUsageMb.toInt(),
                        category = bgApp.category,
                        riskLevel = risk,
                        reason = reason,
                        isKillable = bgApp.isKillable
                    )
                )
            }
        }
        apps.sortedByDescending { it.estimatedDrainPercent }
    }

    val todayAppItems = remember(appUsages, batteryInfo.dailyDrainPercent) {
        val totalDailyDrain = if (batteryInfo.dailyDrainPercent > 0) batteryInfo.dailyDrainPercent.toFloat() else 24.0f

        // SADECE ve SADECE bugün kullanıcı tarafından gerçekten ekranda kullanılmış (screenTimeMinutes > 0) uygulamalar
        val actuallyUsedApps = appUsages.filter { it.screenTimeMinutes > 0 }
            .sortedByDescending { it.screenTimeMinutes }

        if (actuallyUsedApps.isNotEmpty()) {
            val totalMinutes = actuallyUsedApps.sumOf { it.screenTimeMinutes }.coerceAtLeast(1)
            actuallyUsedApps.map { app ->
                val ratio = (app.screenTimeMinutes.toFloat() / totalMinutes.toFloat())
                val appDrain = (totalDailyDrain * ratio).coerceIn(0.1f, 50.0f)
                val durationText = ScreenUsageHelper.formatDurationTurkish(app.screenTimeMinutes)
                TodayAppConsumptionItem(
                    appName = app.appName,
                    packageName = app.packageName,
                    screenTimeMinutes = app.screenTimeMinutes,
                    formattedDuration = durationText,
                    consumptionPercent = appDrain,
                    category = if (app.screenTimeMinutes > 60) "Yoğun Kullanım" else app.category
                )
            }
        } else {
            emptyList()
        }
    }

    val chartPoints = remember(snapshots, batteryInfo.percentage) {
        val timeFmt = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
        if (snapshots.isNotEmpty()) {
            snapshots.takeLast(16).map { snap ->
                val label = timeFmt.format(java.util.Date(snap.timestamp))
                ChartPoint(label, snap.level.toFloat())
            }
        } else if (batteryInfo.percentage > 0) {
            listOf(ChartPoint("Şimdi", batteryInfo.percentage.toFloat()))
        } else {
            emptyList()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // ====================================================================
        // --- ÜST SEKME ÇUBUĞU (FULL SCREEN TABS) ---
        // ====================================================================
        ScrollableTabRow(
            selectedTabIndex = selectedTab.ordinal,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("battery_sub_tab_row"),
            edgePadding = 12.dp,
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
            divider = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                )
            },
            indicator = { tabPositions ->
                if (selectedTab.ordinal < tabPositions.size) {
                    val tabColor = when (selectedTab) {
                        BatteryScreenTab.SUMMARY_SCREEN -> EmeraldPrimary
                        BatteryScreenTab.POWER_SAVINGS -> CyanAccent
                        BatteryScreenTab.HARDWARE_TOOLS -> PurpleNeon
                        BatteryScreenTab.ALARMS_GUARD -> OrangeFlame
                        BatteryScreenTab.ANALYSIS_HISTORY -> ElectricBlue
                        BatteryScreenTab.INTERNET_DATA -> CyanAccent
                    }
                    Box(
                        modifier = Modifier
                            .tabIndicatorOffset(tabPositions[selectedTab.ordinal])
                            .height(3.dp)
                            .background(tabColor, RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                    )
                }
            }
        ) {
            BatteryScreenTab.values().forEach { tab ->
                val isSelected = selectedTab == tab
                val tabColor = when (tab) {
                    BatteryScreenTab.SUMMARY_SCREEN -> EmeraldPrimary
                    BatteryScreenTab.POWER_SAVINGS -> CyanAccent
                    BatteryScreenTab.HARDWARE_TOOLS -> PurpleNeon
                    BatteryScreenTab.ALARMS_GUARD -> OrangeFlame
                    BatteryScreenTab.ANALYSIS_HISTORY -> ElectricBlue
                    BatteryScreenTab.INTERNET_DATA -> CyanAccent
                }

                Tab(
                    selected = isSelected,
                    onClick = { selectedTab = tab },
                    modifier = Modifier.testTag(tab.testTag),
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = null,
                                tint = if (isSelected) tabColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = tab.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                color = if (isSelected) tabColor else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                )
            }
        }

        // ====================================================================
        // --- SEÇİLİ TAB'A GÖRE SAYFA İÇERİĞİ (FULL SCREEN PAGE) ---
        // ====================================================================
        when (selectedTab) {
            BatteryScreenTab.SUMMARY_SCREEN -> {
                BatterySummaryTab(
                    batteryInfo = batteryInfo,
                    screenStats = screenStats,
                    todayAppItems = todayAppItems,
                    appUsages = appUsages,
                    onResetUnplugged = onResetUnplugged,
                    modifier = Modifier.weight(1f)
                )
            }

            BatteryScreenTab.POWER_SAVINGS -> {
                BatteryPowerTab(
                    context = context,
                    powerMode = powerMode,
                    onPowerModeChange = onPowerModeChange,
                    backgroundApps = backgroundApps,
                    isCleaningMemory = isCleaningMemory,
                    optimizationMessage = optimizationMessage,
                    onRefreshBackgroundApps = onRefreshBackgroundApps,
                    onKillBackgroundApp = onKillBackgroundApp,
                    onKillAllBackgroundApps = onKillAllBackgroundApps,
                    onClearOptimizationMessage = onClearOptimizationMessage,
                    onOpenAppSettings = onOpenAppSettings,
                    onOpenModularPowerFeatures = { showModularPowerFeaturesDialog = true },
                    modifier = Modifier.weight(1f)
                )
            }

            BatteryScreenTab.HARDWARE_TOOLS -> {
                BatteryToolsTab(
                    calibratedCycles = calibratedCycles,
                    lastCalibrationDate = lastCalibrationDate,
                    onOpenChargingOverlay = { showChargingOverlay = true },
                    onOpenChargerBenchmark = { showChargerBenchmark = true },
                    onOpenPhoneCooler = { showPhoneCooler = true },
                    onOpenBatteryCalibration = { showBatteryCalibration = true },
                    onOpenStressTest = { showStressTest = true },
                    onOpenAppThermalIndex = { showAppThermalIndex = true },
                    onOpenSmartAlarms = { showSmartAlarmsDialog = true },
                    onOpenNightGuard = { showNightGuardDialog = true },
                    onOpenWeeklySummary = { showWeeklySummaryDialog = true },
                    onOpenDatabaseBackup = { showDatabaseBackupDialog = true },
                    onExportDiagnosticReport = onExportDiagnosticReport,
                    onExportCsvReport = onExportCsvReport,
                    modifier = Modifier.weight(1f)
                )
            }

            BatteryScreenTab.ALARMS_GUARD -> {
                BatteryAlarmsTab(
                    batteryInfo = batteryInfo,
                    fullChargeAlarmEnabled = fullChargeAlarmEnabled,
                    fullChargeTargetPercent = fullChargeTargetPercent,
                    onToggleAlarm = onToggleAlarm,
                    onSetAlarmTarget = onSetAlarmTarget,
                    highTempAlarmEnabled = highTempAlarmEnabled,
                    highTempThresholdCelsius = highTempThresholdCelsius,
                    onToggleHighTempAlarm = onToggleHighTempAlarm,
                    onSetHighTempThreshold = onSetHighTempThreshold,
                    onTestHighTempAlarm = onTestHighTempAlarm,
                    nightDrainReport = nightDrainReport,
                    onOpenNightGuard = { showNightGuardDialog = true },
                    modifier = Modifier.weight(1f)
                )
            }

            BatteryScreenTab.ANALYSIS_HISTORY -> {
                BatteryAnalysisTab(
                    context = context,
                    chartPoints = chartPoints,
                    levelColor = levelColor,
                    standbyAnalysis = standbyAnalysis,
                    highDrainStandbyApps = highDrainStandbyApps,
                    isHighDrainStandbyExpanded = isHighDrainStandbyExpanded,
                    onToggleHighDrainStandbyExpanded = { isHighDrainStandbyExpanded = !isHighDrainStandbyExpanded },
                    killedStandbyPackages = killedStandbyPackages,
                    onOpenHighDrainAppsDialog = { showHighDrainAppsDialog = true },
                    onOpenAppSettings = onOpenAppSettings,
                    onKillStandbyApp = onKillStandbyApp,
                    onKillAllStandbyApps = onKillAllStandbyApps,
                    onResetStandbyOptimization = onResetStandbyOptimization,
                    modifier = Modifier.weight(1f)
                )
            }

            BatteryScreenTab.INTERNET_DATA -> {
                BatteryDataUsageTab(
                    appUsages = appUsages,
                    dataLimitConfig = dataLimitConfig,
                    selectedPeriod = selectedPeriod,
                    onPeriodChange = onPeriodChange,
                    onUpdateLimits = onUpdateLimits,
                    onOpenAppSettings = onOpenAppSettings,
                    screenStats = screenStats,
                    batteryInfo = batteryInfo,
                    onResetUnplugged = onResetUnplugged,
                    backgroundApps = backgroundApps,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }

    // ====================================================================
    // --- MODAL DİYALOGLAR VE POPUPLAR ---
    // ====================================================================
    if (showHighDrainAppsDialog) {
        val filteredApps = remember(highDrainStandbyApps, highDrainDialogFilter) {
            when (highDrainDialogFilter) {
                1 -> highDrainStandbyApps.filter { it.riskLevel == "KRİTİK" || it.riskLevel == "YÜKSEK" }
                2 -> highDrainStandbyApps.filter { it.wakeupsCount >= 10 }
                else -> highDrainStandbyApps
            }
        }

        val totalLoss = remember(highDrainStandbyApps) {
            highDrainStandbyApps.sumOf { it.estimatedDrainPercent.toDouble() }.toFloat()
        }
        val totalWakelocks = remember(highDrainStandbyApps) {
            highDrainStandbyApps.sumOf { it.wakeupsCount }
        }

        Dialog(
            onDismissRequest = { showHighDrainAppsDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .fillMaxHeight(0.88f)
                    .testTag("dialog_high_drain_apps"),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(18.dp)
                ) {
                    // Dialog Başlığı & Kapatma Butonu
                    Row(
                        modifier = Modifier.fillMaxWidth(),
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
                                    .background(RedCritical.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                                    .border(1.dp, RedCritical.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.BatteryAlert,
                                    contentDescription = null,
                                    tint = RedCritical,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Yüksek Pil Tüketenler",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Bekleme & Derin Uyku Tüketim Raporu",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        IconButton(
                            onClick = { showHighDrainAppsDialog = false },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Kapat",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Üst Özet & Etki Kartı
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Tespit Edilen Toplam Kayıp",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "~%${"%.1f".format(totalLoss)} Pil Kaybı",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Black,
                                        color = RedCritical
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "Toplam Uyandırma",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "$totalWakelocks kez",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = OrangeFlame
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Derin uyku (Doze modu) esnasında CPU'yu uyanık tutan (Wakelock) ve arka planda çalışan servisler bataryanızı tüketir.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.5.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Filtre Sekmeleri
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = highDrainDialogFilter == 0,
                            onClick = { highDrainDialogFilter = 0 },
                            label = { Text("Tümü (${highDrainStandbyApps.size})", fontWeight = FontWeight.SemiBold, fontSize = 11.5.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CyanAccent.copy(alpha = 0.2f),
                                selectedLabelColor = CyanAccent
                            )
                        )
                        FilterChip(
                            selected = highDrainDialogFilter == 1,
                            onClick = { highDrainDialogFilter = 1 },
                            label = { Text("Yüksek Risk", fontWeight = FontWeight.SemiBold, fontSize = 11.5.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = RedCritical.copy(alpha = 0.2f),
                                selectedLabelColor = RedCritical
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Uygulama Listesi
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(filteredApps.size) { index ->
                            val app = filteredApps[index]
                            val isKilled = killedStandbyPackages.contains(app.packageName)
                            StandbyHighDrainAppCard(
                                app = app,
                                isKilled = isKilled,
                                onOpenAppSettings = onOpenAppSettings,
                                onKillBackgroundApp = { pkg ->
                                    android.widget.Toast.makeText(
                                        context,
                                        "✅ ${app.appName} durduruldu. ~${app.memoryMb} MB RAM temizlendi!",
                                        android.widget.Toast.LENGTH_SHORT
                                    ).show()
                                    onKillStandbyApp(pkg)
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { showHighDrainAppsDialog = false },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CyanAccent)
                    ) {
                        Text("Tamam", fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                }
            }
        }
    }

    if (showSmartChargingDialog) {
        SmartChargingScheduleDialog(
            config = smartChargingConfig,
            analysis = smartChargingAnalysis,
            onDismiss = { showSmartChargingDialog = false },
            onSaveConfig = { newConfig ->
                onUpdateSmartChargingConfig(newConfig)
                showSmartChargingDialog = false
            },
            onApplyAiRecommendation = {
                onApplyAiChargingRecommendation()
                showSmartChargingDialog = false
            }
        )
    }

    if (showChargingOverlay) {
        ChargingAnimationOverlay(
            batteryInfo = batteryInfo,
            onDismiss = { showChargingOverlay = false }
        )
    }

    if (showChargerBenchmark) {
        ChargerBenchmarkDialog(
            batteryInfo = batteryInfo,
            onDismiss = { showChargerBenchmark = false }
        )
    }

    if (showPhoneCooler) {
        PhoneCoolerDialog(
            batteryInfo = batteryInfo,
            onDismiss = { showPhoneCooler = false }
        )
    }

    if (showBatteryCalibration) {
        BatteryCalibrationDialog(
            batteryInfo = batteryInfo,
            onDismiss = { showBatteryCalibration = false },
            onCalibrationApplied = { newCycles, calibDate ->
                calibratedCycles = newCycles
                lastCalibrationDate = calibDate
            }
        )
    }

    if (showStressTest) {
        BatteryStressTestDialog(
            batteryInfo = batteryInfo,
            onDismiss = { showStressTest = false }
        )
    }

    if (showAppThermalIndex) {
        AppThermalIndexDialog(
            onDismiss = { showAppThermalIndex = false }
        )
    }

    if (showNightGuardDialog) {
        NightChargingGuardDialog(
            onDismiss = { showNightGuardDialog = false }
        )
    }

    if (showSmartAlarmsDialog) {
        SmartAlarmSettingsDialog(
            onDismiss = { showSmartAlarmsDialog = false }
        )
    }

    if (showWeeklySummaryDialog) {
        WeeklyBatterySummaryDialog(
            onDismiss = { showWeeklySummaryDialog = false }
        )
    }

    if (showDatabaseBackupDialog) {
        DatabaseBackupRestoreDialog(
            onDismiss = { showDatabaseBackupDialog = false }
        )
    }

    if (showModularPowerFeaturesDialog) {
        ModularPowerFeaturesDialog(
            onDismissRequest = { showModularPowerFeaturesDialog = false }
        )
    }
}

@Composable
fun StandbyHighDrainAppCard(
    app: StandbyHighDrainAppItem,
    isKilled: Boolean,
    onOpenAppSettings: (String) -> Unit,
    onKillBackgroundApp: (String) -> Unit
) {
    val riskColor = when (app.riskLevel) {
        "KRİTİK" -> RedCritical
        "YÜKSEK" -> OrangeFlame
        "ORTA" -> AmberWarning
        else -> EmeraldPrimary
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isKilled) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = BorderStroke(
            1.dp,
            if (isKilled) EmeraldPrimary.copy(alpha = 0.4f) else riskColor.copy(alpha = 0.35f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f, fill = false),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AppIconView(
                        packageName = app.packageName,
                        appName = app.appName,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = app.appName,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        Text(
                            text = "${app.category} • ~${app.memoryMb} MB RAM",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 10.5.sp
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = (if (isKilled) EmeraldPrimary else riskColor).copy(alpha = 0.15f),
                    border = BorderStroke(0.8.dp, (if (isKilled) EmeraldPrimary else riskColor).copy(alpha = 0.5f))
                ) {
                    Text(
                        text = if (isKilled) "DURDURULDU" else app.riskLevel,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isKilled) EmeraldPrimary else riskColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (!isKilled && app.isKillable) {
                Button(
                    onClick = { onKillBackgroundApp(app.packageName) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = RedCritical)
                ) {
                    Text("Durdur & Uyut", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun MetricSquareCell(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(6.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(accentColor.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                    .border(1.dp, accentColor.copy(alpha = 0.4f), RoundedCornerShape(4.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
