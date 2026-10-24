package com.example

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.BatteryStd
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.DataUsage
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.system.LiveCurrentOverlayManager
import com.example.system.SystemShortcuts
import com.example.ui.screens.AiCoachScreen
import com.example.ui.screens.BatteryScreen
import com.example.ui.screens.DataUsageScreen
import com.example.ui.screens.MonthlyAnalysisScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TelemetryScreen
import com.example.ui.screens.TimelineScreen
import com.example.ui.components.LiveCurrentInScreenOverlay
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.MainViewModel
import androidx.compose.material.icons.filled.AutoAwesome

/**
 * VoltGuard Ana Gezinme Sekmeleri (MainTab)
 * - BATTERY: Canlı pil telemetrisi, güç modları, araçlar ve alarm paneli
 * - TIMELINE: Günlük pil ve ekran kullanım olayları zaman çizelgesi
 * - AI_COACH: Gemini destekli yapay zeka pil koçu ve enerji tasarruf önerileri
 * - MONTHLY: Donanım telemetrisi, kullanım karnesi, Ağ & Standby ve Pro Labs
 * - SETTINGS: Sistem izinleri, veri kotaları, alarmlar ve yedekleme ayarları
 */
enum class MainTab(val title: String, val icon: ImageVector, val tag: String) {
    BATTERY("Batarya", Icons.Default.BatteryChargingFull, "nav_tab_battery"),
    TIMELINE("Çizelge", Icons.Default.Timeline, "nav_tab_timeline"),
    AI_COACH("AI Koç", Icons.Default.AutoAwesome, "nav_tab_ai_coach"),
    MONTHLY("Analiz", Icons.Default.Analytics, "nav_tab_monthly"),
    SETTINGS("Ayarlar", Icons.Default.Settings, "nav_tab_settings")
}

/**
 * MainActivity: VoltGuard uygulamasının ana giriş noktası.
 * - Edge-to-Edge ekran yerleşimi ve kilit ekranı üzerinde uyanma desteği sağlar.
 * - Bildirimlerden gelen sekme yönlendirmelerini (handleIntent) yönetir.
 * - Canlı akım göstergesi senkronizasyonunu ve ekran açılma/kapanma yayın alıcılarını koordine eder.
 */
class MainActivity : ComponentActivity() {

    private val targetTabState = mutableStateOf<Int?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Şarj takıldığında ekranı açma ve kilit ekranı üzerinde güvenle gösterme desteği
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as? android.app.KeyguardManager
            keyguardManager?.requestDismissKeyguard(this, null)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                android.view.WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                android.view.WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                android.view.WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }

        handleIntent(intent)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                VoltGuardApp(targetTabFromIntent = targetTabState.value)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    /**
     * Bildirimlerden gelen doğrudan sekme yönlendirme intent'lerini işler.
     */
    private fun handleIntent(intent: Intent?) {
        val targetTab = intent?.getIntExtra(com.example.system.NotificationHelper.EXTRA_NAVIGATE_TAB, -1) ?: -1
        if (targetTab in 0..4) {
            targetTabState.value = targetTab
        }
    }
}

/**
 * VoltGuardApp: Tüm uygulama ekranlarını, üst durum çubuğunu, alt gezinme menüsünü
 * ve tekil canlı akım göstergesini birleştiren ana Composable yapıdır.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoltGuardApp(
    viewModel: MainViewModel = viewModel(),
    targetTabFromIntent: Int? = null
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }

    LaunchedEffect(targetTabFromIntent) {
        if (targetTabFromIntent != null && targetTabFromIntent in 0..4) {
            selectedTab = targetTabFromIntent
        }
    }

    // Request POST_NOTIFICATIONS on Android 13+
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { /* Permission response handled */ }

    LaunchedEffect(Unit) {
        if (com.example.system.AutoStartHelper.isBackgroundProtectionEnabled(context)) {
            com.example.system.VoltGuardService.startService(context)
            com.example.system.AutoStartHelper.scheduleWatchdog(context)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    DisposableEffect(context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(c: Context?, intent: Intent?) {
                when (intent?.action) {
                    Intent.ACTION_SCREEN_ON -> viewModel.recordScreenEvent("SCREEN_ON", "Ekran açıldı")
                    Intent.ACTION_SCREEN_OFF -> viewModel.recordScreenEvent("SCREEN_OFF", "Ekran kapandı")
                    Intent.ACTION_POWER_CONNECTED -> viewModel.onPowerStateChanged(true)
                    Intent.ACTION_POWER_DISCONNECTED -> viewModel.onPowerStateChanged(false)
                }
            }
        }
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_POWER_CONNECTED)
            addAction(Intent.ACTION_POWER_DISCONNECTED)
        }
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                ContextCompat.registerReceiver(
                    context,
                    receiver,
                    filter,
                    ContextCompat.RECEIVER_EXPORTED
                )
            } else {
                context.registerReceiver(receiver, filter)
            }
        } catch (_: Exception) {
            try {
                context.registerReceiver(receiver, filter)
            } catch (_: Exception) {}
        }
        onDispose {
            try {
                context.unregisterReceiver(receiver)
            } catch (_: Exception) {}
        }
    }

    LaunchedEffect(selectedTab) {
        viewModel.refreshRealData()
    }

    val liveBattery by viewModel.liveBatteryInfo.collectAsStateWithLifecycle()
    val snapshots by viewModel.batterySnapshots.collectAsStateWithLifecycle()
    val timelineEvents by viewModel.timelineEvents.collectAsStateWithLifecycle()
    val selectedDayOffset by viewModel.selectedDayOffset.collectAsStateWithLifecycle()
    val appUsages by viewModel.appUsageRecords.collectAsStateWithLifecycle()
    val selectedPeriod by viewModel.selectedPeriod.collectAsStateWithLifecycle()
    val dataLimitConfig by viewModel.dataLimitConfig.collectAsStateWithLifecycle()
    val powerMode by viewModel.powerMode.collectAsStateWithLifecycle()
    val chargeAlarmEnabled by viewModel.fullChargeAlarmEnabled.collectAsStateWithLifecycle()
    val chargeAlarmTarget by viewModel.fullChargeTargetPercent.collectAsStateWithLifecycle()
    val highTempAlarmEnabled by viewModel.highTempAlarmEnabled.collectAsStateWithLifecycle()
    val highTempThresholdCelsius by viewModel.highTempThresholdCelsius.collectAsStateWithLifecycle()
    val nightDrainReport by viewModel.nightDrainReport.collectAsStateWithLifecycle()
    val categoryBreakdown by viewModel.categoryBreakdown.collectAsStateWithLifecycle()
    val appTimeLimits by viewModel.appTimeLimits.collectAsStateWithLifecycle()
    val backgroundApps by viewModel.backgroundApps.collectAsStateWithLifecycle()
    val isCleaningMemory by viewModel.isCleaningMemory.collectAsStateWithLifecycle()
    val optimizationMessage by viewModel.optimizationMessage.collectAsStateWithLifecycle()
    val blockedPackages by viewModel.blockedPackages.collectAsStateWithLifecycle()
    val showAllTimelineActions by viewModel.showAllTimelineActions.collectAsStateWithLifecycle()
    val screenStats by viewModel.screenBatteryStats.collectAsStateWithLifecycle()
    val standbyAnalysis by viewModel.standbyDrainAnalysis.collectAsStateWithLifecycle()
    val killedStandbyPackages by viewModel.killedStandbyPackages.collectAsStateWithLifecycle()
    val isLiveCurrentOverlayEnabled by viewModel.liveCurrentOverlayEnabled.collectAsStateWithLifecycle()

    val lifecycleOwner = LocalLifecycleOwner.current
    var hasOverlayPermission by remember {
        mutableStateOf(SystemShortcuts.canDrawOverlays(context))
    }

    DisposableEffect(lifecycleOwner, context, isLiveCurrentOverlayEnabled) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasOverlayPermission = SystemShortcuts.canDrawOverlays(context)
                LiveCurrentOverlayManager.syncState(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(EmeraldPrimary.copy(alpha = 0.2f))
                                .border(1.5.dp, EmeraldPrimary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ElectricBolt,
                                contentDescription = null,
                                tint = EmeraldPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "VoltGuard",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    // Live Battery Quick Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(EmeraldPrimary.copy(alpha = 0.15f))
                            .border(1.dp, EmeraldPrimary.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                            .clickable { selectedTab = 0 }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("topbar_battery_badge")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (liveBattery.isCharging) Icons.Default.Bolt else Icons.Default.BatteryStd,
                                contentDescription = null,
                                tint = EmeraldPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "%${liveBattery.percentage}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldPrimary
                            )
                        }
                    }

                    // Direct Settings Action
                    IconButton(
                        onClick = { selectedTab = 4 },
                        modifier = Modifier.testTag("topbar_settings_action")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Ayarlar",
                            tint = CyanAccent
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                modifier = Modifier.testTag("main_bottom_nav")
            ) {
                MainTab.values().forEachIndexed { index, tab ->
                    val isSelected = selectedTab == index
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedTab = index },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.title
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                maxLines = 1
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.Black,
                            selectedTextColor = CyanAccent,
                            indicatorColor = CyanAccent,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.testTag(tab.tag)
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> BatteryScreen(
                    batteryInfo = liveBattery,
                    snapshots = snapshots,
                    powerMode = powerMode,
                    screenStats = screenStats,
                    onResetUnplugged = { viewModel.resetUnpluggedTimeToNow() },
                    onPowerModeChange = { viewModel.setPowerMode(it) },
                    fullChargeAlarmEnabled = chargeAlarmEnabled,
                    fullChargeTargetPercent = chargeAlarmTarget,
                    onToggleAlarm = { viewModel.toggleChargeAlarm(it) },
                    onSetAlarmTarget = { viewModel.setFullChargeTargetPercent(it) },
                    highTempAlarmEnabled = highTempAlarmEnabled,
                    highTempThresholdCelsius = highTempThresholdCelsius,
                    onToggleHighTempAlarm = { viewModel.toggleHighTempAlarm(it) },
                    onSetHighTempThreshold = { viewModel.setHighTempThreshold(it) },
                    onTestHighTempAlarm = { viewModel.testHighTempAlarm() },
                    nightDrainReport = nightDrainReport,
                    backgroundApps = backgroundApps,
                    isCleaningMemory = isCleaningMemory,
                    optimizationMessage = optimizationMessage,
                    onRefreshBackgroundApps = { viewModel.refreshBackgroundApps() },
                    onKillBackgroundApp = { viewModel.killBackgroundApp(it) },
                    onKillAllBackgroundApps = { viewModel.killAllBackgroundApps() },
                    onClearOptimizationMessage = { viewModel.clearOptimizationMessage() },
                    onOpenAppSettings = { viewModel.openAppSystemSettings(it) },
                    appUsages = appUsages,
                    dataLimitConfig = dataLimitConfig,
                    selectedPeriod = selectedPeriod,
                    onPeriodChange = { viewModel.setSelectedPeriod(it) },
                    onUpdateLimits = { mob, wif, thr, alert -> viewModel.updateDataLimits(mob, wif, thr, alert) },
                    standbyAnalysis = standbyAnalysis,
                    onExportDiagnosticReport = { viewModel.exportDiagnosticReport(context) },
                    onExportCsvReport = { viewModel.exportCsvReport(context) },
                    killedStandbyPackages = killedStandbyPackages,
                    onKillStandbyApp = { viewModel.killStandbyApp(it) },
                    onKillAllStandbyApps = { viewModel.killAllStandbyApps(it) },
                    onResetStandbyOptimization = { viewModel.resetStandbyOptimization() },
                    onNavigateToAiReport = { selectedTab = 2 }
                )
                1 -> TimelineScreen(
                    events = timelineEvents,
                    selectedDayOffset = selectedDayOffset,
                    blockedPackages = blockedPackages,
                    appUsages = appUsages,
                    liveBattery = liveBattery,
                    showAllActions = showAllTimelineActions,
                    onToggleShowAllActions = { viewModel.setShowAllTimelineActions(it) },
                    onDayOffsetChange = { viewModel.setSelectedDayOffset(it) },
                    onToggleBlockApp = { pkg -> viewModel.toggleBlockApp(pkg) },
                    onClearAllBlockedApps = { viewModel.clearAllBlockedApps() },
                    onNavigateToSettings = { selectedTab = 4 }
                )
                2 -> AiCoachScreen(viewModel = viewModel)
                3 -> {
                    val telemetry by viewModel.systemTelemetry.collectAsStateWithLifecycle()
                    MonthlyAnalysisScreen(
                        monthlyUsages = appUsages,
                        liveBattery = liveBattery,
                        timelineEvents = timelineEvents,
                        categoryBreakdown = categoryBreakdown,
                        appTimeLimits = appTimeLimits,
                        systemTelemetry = telemetry,
                        onRefreshTelemetry = { viewModel.refreshTelemetry() },
                        onSetAppTimeLimit = { pkg, name, limit -> viewModel.setAppTimeLimit(pkg, name, limit) },
                        onToggleAppLimit = { pkg, isEnabled -> viewModel.toggleAppTimeLimit(pkg, isEnabled) },
                        onDeleteAppLimit = { pkg -> viewModel.deleteAppTimeLimit(pkg) }
                    )
                }
                4 -> {
                    val allDetectedApps = remember(timelineEvents, appUsages, blockedPackages) {
                        val map = mutableMapOf<String, String>()
                        timelineEvents.filter { it.packageName.isNotBlank() && it.packageName != "android" }.forEach {
                            map[it.packageName] = if (it.appName.isNotBlank()) it.appName else it.packageName
                        }
                        appUsages.filter { it.packageName.isNotBlank() && it.packageName != "android" }.forEach {
                            map[it.packageName] = if (it.appName.isNotBlank()) it.appName else it.packageName
                        }
                        blockedPackages.forEach { pkg ->
                            if (!map.containsKey(pkg)) {
                                map[pkg] = pkg
                            }
                        }
                        map.toList().sortedBy { it.second }
                    }

                    SettingsScreen(
                        blockedPackages = blockedPackages,
                        allDetectedApps = allDetectedApps,
                        onToggleBlockApp = { pkg -> viewModel.toggleBlockApp(pkg) },
                        onClearAllBlockedApps = { viewModel.clearAllBlockedApps() },
                        fullChargeAlarmEnabled = chargeAlarmEnabled,
                        fullChargeTargetPercent = chargeAlarmTarget,
                        onToggleAlarm = { viewModel.toggleChargeAlarm(it) },
                        onSetAlarmTarget = { viewModel.setFullChargeTargetPercent(it) },
                        dataLimitConfig = dataLimitConfig,
                        onUpdateLimits = { mob, wif, thr, alert ->
                            viewModel.updateDataLimits(mob, wif, thr, alert)
                        },
                        isLiveCurrentOverlayEnabled = isLiveCurrentOverlayEnabled,
                        onToggleLiveCurrentOverlay = { viewModel.setLiveCurrentOverlayEnabled(it) }
                    )
                }
            }

            // Ekran içi anlık akım göstergesi:
            // SADECE 1 TANE gösterilmelidir!
            // Eğer sistem "Üstte Görünme" (Overlay) izni verilmişse, LiveCurrentOverlayService
            // WindowManager aracılığıyla zaten ekranın sol alt köşesinde (uygulama açıkken de arkaplandayken de)
            // tek bir canlı akım göstergesi çizmektedir.
            // Bu nedenle uygulama açıkken 2 tane olmaması için, Compose içi bileşen
            // SADECE sistem overlay izni henüz verilmemişse (uygulama içi fallback) gösterilir.
            if (isLiveCurrentOverlayEnabled && !hasOverlayPermission) {
                LiveCurrentInScreenOverlay(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 16.dp, bottom = 32.dp)
                )
            }
        }
    }
}
