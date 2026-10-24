package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DataUsage
import androidx.compose.material.icons.filled.DisplaySettings
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Rule
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.entity.DataLimitConfigEntity
import com.example.system.AlarmToneStyle
import com.example.system.CustomAlarmSoundHelper
import com.example.system.LiveCurrentOverlayManager
import com.example.system.RealUsageStatsHelper
import com.example.system.SystemShortcuts
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.OrangeFlame
import com.example.ui.theme.PurpleNeon
import com.example.ui.theme.RedCritical

/**
 * SettingsTab: Ayarlar ekranının 5 ana modülü
 * 1. SYSTEM_PERMISSIONS: Android sistem izinleri, arka plan optimizasyonu ve servis durumu
 * 2. BATTERY_ALARM: Şarj ve sıcaklık alarmları, canlı akım göstergesi ve zil sesleri
 * 3. PRIVACY_APPS: Zaman çizelgesi uygulama gizleme ve kara liste yönetimi
 * 4. DATA_BACKUP: Wi-Fi ve Mobil veri limitleri, Room veritabanı JSON yedekleme/geri yükleme
 * 5. ABOUT_GUIDE: Uygulama sürümü, batarya kılavuzu ve teknik telemetri açıklamaları
 */
enum class SettingsTab(
    val title: String,
    val icon: ImageVector,
    val testTag: String
) {
    SYSTEM_PERMISSIONS("İzinler & Sistem", Icons.Default.Security, "tab_settings_permissions"),
    BATTERY_ALARM("Batarya & Alarm", Icons.Default.BatteryChargingFull, "tab_settings_battery_alarm"),
    PRIVACY_APPS("Gizlilik & Uygulama", Icons.Default.VisibilityOff, "tab_settings_privacy"),
    DATA_BACKUP("Veri & Yedekleme", Icons.Default.Storage, "tab_settings_data_backup"),
    ABOUT_GUIDE("Hakkında & Kılavuz", Icons.Default.Info, "tab_settings_about")
}

/**
 * SettingsScreen: VoltGuard'ın tüm konfigürasyon, izin, alarm, gizlilik ve yedekleme ayarlarını barındıran ekran.
 */
@Composable
fun SettingsScreen(
    blockedPackages: Set<String>,
    allDetectedApps: List<Pair<String, String>>,
    onToggleBlockApp: (String) -> Unit,
    onClearAllBlockedApps: () -> Unit,
    fullChargeAlarmEnabled: Boolean,
    fullChargeTargetPercent: Int,
    onToggleAlarm: (Boolean) -> Unit,
    onSetAlarmTarget: (Int) -> Unit,
    dataLimitConfig: DataLimitConfigEntity,
    onUpdateLimits: (mobileMb: Long, wifiMb: Long, threshold: Int, alertEnabled: Boolean) -> Unit,
    isLiveCurrentOverlayEnabled: Boolean = false,
    onToggleLiveCurrentOverlay: (Boolean) -> Unit = {}
) {
    val context = LocalContext.current

    // Usage access permission state
    var hasUsagePermission by remember { mutableStateOf(RealUsageStatsHelper.hasUsageAccess(context)) }

    // Overlay & Exact Alarm permission states
    var hasOverlayPermission by remember { mutableStateOf(SystemShortcuts.canDrawOverlays(context)) }
    var hasExactAlarmPermission by remember { mutableStateOf(SystemShortcuts.canScheduleExactAlarms(context)) }

    // Background & Autostart state
    var isBackgroundProtectionEnabled by remember {
        mutableStateOf(com.example.system.AutoStartHelper.isBackgroundProtectionEnabled(context))
    }
    var isBootStartEnabled by remember {
        mutableStateOf(com.example.system.AutoStartHelper.isBootStartEnabled(context))
    }
    var isAutoWakeOnChargerEnabled by remember {
        mutableStateOf(com.example.system.AutoStartHelper.isAutoWakeOnChargerEnabled(context))
    }
    var isKeepAliveLockEnabled by remember {
        mutableStateOf(com.example.system.AutoStartHelper.isKeepAliveLockEnabled(context))
    }
    var isIgnoringBatteryOptimizations by remember {
        mutableStateOf(com.example.system.AutoStartHelper.isIgnoringBatteryOptimizations(context))
    }

    // Custom Alarm Tone & Sound state
    var selectedTone by remember {
        mutableStateOf(CustomAlarmSoundHelper.getSelectedTone(context))
    }
    var lowBatteryAlertEnabled by remember {
        mutableStateOf(CustomAlarmSoundHelper.isLowBatteryAlertEnabled(context))
    }
    var lowBatteryThreshold by remember {
        mutableIntStateOf(CustomAlarmSoundHelper.getLowBatteryThreshold(context))
    }
    var autoUltraSaverEnabled by remember {
        mutableStateOf(CustomAlarmSoundHelper.isAutoUltraSaverEnabled(context))
    }
    var isAmoledBlack by remember {
        mutableStateOf(CustomAlarmSoundHelper.isAmoledTrueBlackEnabled(context))
    }
    var isFahrenheit by remember {
        mutableStateOf(CustomAlarmSoundHelper.isFahrenheit(context))
    }
    var showNightGuardSettingsDialog by remember {
        mutableStateOf(false)
    }
    var showSmartAlarmSettingsDialog by remember {
        mutableStateOf(false)
    }
    var showWeeklyBatterySummaryDialog by remember {
        mutableStateOf(false)
    }
    var showDatabaseBackupDialog by remember {
        mutableStateOf(false)
    }
    var showAboutGuideDialog by remember {
        mutableStateOf(false)
    }
    var showModularPowerFeaturesDialog by remember {
        mutableStateOf(false)
    }

    // Notification permission state
    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        )
    }

    val notificationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotificationPermission = isGranted
        if (isGranted) {
            Toast.makeText(context, "Bildirim izni onaylandı", Toast.LENGTH_SHORT).show()
        }
    }

    // Check permissions on enter
    LaunchedEffect(Unit) {
        hasUsagePermission = RealUsageStatsHelper.hasUsageAccess(context)
        hasOverlayPermission = SystemShortcuts.canDrawOverlays(context)
        hasExactAlarmPermission = SystemShortcuts.canScheduleExactAlarms(context)
        isIgnoringBatteryOptimizations = com.example.system.AutoStartHelper.isIgnoringBatteryOptimizations(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            hasNotificationPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    var showAppFilterDialog by remember { mutableStateOf(false) }
    var showQuotaEditDialog by remember { mutableStateOf(false) }

    var selectedTab by remember { mutableStateOf(SettingsTab.SYSTEM_PERMISSIONS) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("settings_screen_root")
    ) {
        // --- 1. PINNED HEADER ---
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(CyanAccent.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        tint = CyanAccent,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Uygulama & İzin Ayarları",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Özelliklere göre kategorize edilmiş ayarlar ve izinler",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // --- 2. PINNED SCROLLABLE TAB ROW ---
        ScrollableTabRow(
            selectedTabIndex = selectedTab.ordinal,
            edgePadding = 12.dp,
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
            contentColor = CyanAccent,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTab.ordinal]),
                    color = CyanAccent
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("settings_tab_row")
        ) {
            SettingsTab.values().forEach { tab ->
                val isSelected = selectedTab == tab
                Tab(
                    selected = isSelected,
                    onClick = { selectedTab = tab },
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

        // --- 3. SEÇİLİ SEKME İÇERİĞİ ---
        Crossfade(
            targetState = selectedTab,
            label = "settings_tab_crossfade",
            modifier = Modifier.weight(1f)
        ) { currentTab ->
            when (currentTab) {
                // ==========================================
                // SEKME 1: SİSTEM İZİNLERİ & ARKA PLAN KORUMASI
                // ==========================================
                SettingsTab.SYSTEM_PERMISSIONS -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("settings_system_scroll"),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // --- 1.1. MODÜLER PİL VE ÖZELLİK YÖNETİCİSİ (AÇIK / KAPALI ANAHTARLARI) ---
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showModularPowerFeaturesDialog = true }
                    .testTag("card_modular_power_features"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF14241C)
                ),
                border = BorderStroke(1.5.dp, EmeraldPrimary.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(EmeraldPrimary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = EmeraldPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Pil Tüketen Özellikleri Yönet",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldLight
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = EmeraldPrimary.copy(alpha = 0.25f)
                            ) {
                                Text(
                                    text = "YENİ",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldLight,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "İstemediğiniz özellikleri kapatıp telefonun ısınmasını ve şarj tüketimini sıfırlayın. Sadece lazım olduğunda açın.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.8f),
                            lineHeight = 16.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { showModularPowerFeaturesDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text("Ayarla", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        // --- 2. SİSTEM İZİNLERİ VE ERİŞİM HAKLARI ---
        item {
            SettingsSectionHeader(title = "İzinler ve Sistem Erişimi", icon = Icons.Default.Security)
        }

        // 2a. Kullanım Erişimi İzni (Usage Access)
        item {
            PermissionCard(
                title = "Kullanım Erişimi İzni",
                description = "Uygulamaların ekran süresi, veri kullanımı ve zaman çizelgesi oturumlarının kaydedilmesi için gereklidir.",
                isGranted = hasUsagePermission,
                grantedLabel = "Erişim İzni Aktif",
                deniedLabel = "İzin Gerekli",
                actionLabel = "İzni Yönet",
                onActionClick = {
                    SystemShortcuts.openUsageAccessSettings(context)
                },
                testTag = "settings_usage_access_card"
            )
        }

        // 2b. Bildirim İzni (Notifications)
        item {
            PermissionCard(
                title = "Bildirim İzni",
                description = "Tam şarj alarmları, şarj bağlantı bildirimleri ve veri kotası aşım uyarılarının anında iletilmesi için gereklidir.",
                isGranted = hasNotificationPermission,
                grantedLabel = "Bildirimler Açık",
                deniedLabel = "Bildirim İzni Yok",
                actionLabel = "İzin İste",
                onActionClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        SystemShortcuts.openNotificationSettings(context)
                    }
                },
                testTag = "settings_notification_card"
            )
        }

        // 2c. Diğer Uygulamaların Üzerinde Görüntüleme & Şarjda Ekranı Açma İzni
        item {
            PermissionCard(
                title = "Diğer Uygulamaların Üzerinde Görünme & Ekranı Uyandırma",
                description = "Telefon kapalıyken veya başka bir uygulamadayken şarj takıldığında VoltGuard'ın ekranı açıp otomatik olarak öne gelebilmesi için gereklidir.",
                isGranted = hasOverlayPermission,
                grantedLabel = "Üstte Görünme Aktif",
                deniedLabel = "İzin Gerekli",
                actionLabel = "İzni Aç / Yönet",
                onActionClick = {
                    SystemShortcuts.openOverlaySettings(context)
                },
                testTag = "settings_overlay_card"
            )
        }

        // 2d. Hassas Alarmlar ve Hatırlatıcılar İzni
        item {
            PermissionCard(
                title = "Hassas Alarmlar & Zamanlayıcı İzni",
                description = "Pil seviyesi hedefleri, yüksek sıcaklık alarmları ve arka plan bekçi sisteminin (Watchdog) tam vaktinde uyanması için gereklidir.",
                isGranted = hasExactAlarmPermission,
                grantedLabel = "Alarm İzni Aktif",
                deniedLabel = "İzin Gerekli",
                actionLabel = "Alarm İznini Yönet",
                onActionClick = {
                    SystemShortcuts.openExactAlarmSettings(context)
                },
                testTag = "settings_exact_alarm_card"
            )
        }

        // 2e. Pil Optimizasyonu Muafiyeti İzni
        item {
            PermissionCard(
                title = "Pil Tasarrufu Muafiyeti (Arka Planda Uyutulmama)",
                description = "Android işletim sisteminin veya cihaz üreticisinin uygulamayı arka planda zorla uyutmasını engeller; 7/24 kesintisiz takibi garantiler.",
                isGranted = isIgnoringBatteryOptimizations,
                grantedLabel = "Pil Muafiyeti Aktif",
                deniedLabel = "Kısıtlama Var (Önerilir)",
                actionLabel = "Muafiyet Ver",
                onActionClick = {
                    com.example.system.AutoStartHelper.requestIgnoreBatteryOptimizations(context)
                },
                testTag = "settings_battery_optimization_card"
            )
        }

        // 2c. 7/24 Kesintisiz Arka Plan & Otomatik Başlatma (Auto-Start & Keep-Alive)
        item {
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("settings_autostart_protection_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
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
                                    .size(38.dp)
                                    .background(EmeraldPrimary.copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PowerSettingsNew,
                                    contentDescription = null,
                                    tint = EmeraldPrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "7/24 Kesintisiz Koruma & Otomatik Başlatma",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (isBackgroundProtectionEnabled) "Aktif • Sürekli Arka Planda Çalışıyor" else "Devre Dışı",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isBackgroundProtectionEnabled) EmeraldPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Switch(
                            checked = isBackgroundProtectionEnabled,
                            onCheckedChange = { checked ->
                                isBackgroundProtectionEnabled = checked
                                com.example.system.AutoStartHelper.setBackgroundProtectionEnabled(context, checked)
                                Toast.makeText(
                                    context,
                                    if (checked) "7/24 Arka Plan Koruması Başlatıldı" else "Arka Plan Koruması Durduruldu",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = EmeraldPrimary,
                                checkedTrackColor = EmeraldPrimary.copy(alpha = 0.35f)
                            ),
                            modifier = Modifier.testTag("toggle_background_protection_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Uygulama kapatılsa veya cihaz yeniden başlatılsa bile koruma otomatik olarak yeniden başlar ve alarm / tüketim takibini kesintisiz sürdürür.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Boot auto-start toggle
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                Text(
                                    text = "Cihaz Açılışında Otomatik Başlat",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Telefon her açıldığında veya yeniden başlatıldığında VoltGuard servisini kendiliğinden ayağa kaldırır.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = isBootStartEnabled,
                                onCheckedChange = { checked ->
                                    isBootStartEnabled = checked
                                    com.example.system.AutoStartHelper.setBootStartEnabled(context, checked)
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = CyanAccent,
                                    checkedTrackColor = CyanAccent.copy(alpha = 0.35f)
                                ),
                                modifier = Modifier.testTag("toggle_boot_autostart_switch")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Charger auto-wake toggle
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.BatteryChargingFull,
                                        contentDescription = null,
                                        tint = EmeraldPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Şarja Takınca Otomatik Aktifleş",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Uygulama kapalı olsa bile şarj kablosu takıldığı anda otomatik uyanır ve korumayı başlatır.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = isAutoWakeOnChargerEnabled,
                                onCheckedChange = { checked ->
                                    isAutoWakeOnChargerEnabled = checked
                                    com.example.system.AutoStartHelper.setAutoWakeOnChargerEnabled(context, checked)
                                    Toast.makeText(
                                        context,
                                        if (checked) "Şarj takıldığında otomatik aktifleşme devrede" else "Şarjda otomatik uyanma kapatıldı",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = EmeraldPrimary,
                                    checkedTrackColor = EmeraldPrimary.copy(alpha = 0.35f)
                                ),
                                modifier = Modifier.testTag("toggle_charger_autowake_switch")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Unkillable Keep-Alive Protection status
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = CyanAccent.copy(alpha = 0.08f),
                        border = BorderStroke(1.dp, CyanAccent.copy(alpha = 0.35f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Security,
                                        contentDescription = null,
                                        tint = CyanAccent,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Kapatılamaz Kesintisiz Koruma Kalkanı",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = CyanAccent
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Son uygulamalardan kapatılsa bile bekçi sistemi (Watchdog) 1 saniye içinde servisi geri canlandırır.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = isKeepAliveLockEnabled,
                                onCheckedChange = { checked ->
                                    isKeepAliveLockEnabled = checked
                                    com.example.system.AutoStartHelper.setKeepAliveLockEnabled(context, checked)
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = CyanAccent,
                                    checkedTrackColor = CyanAccent.copy(alpha = 0.35f)
                                ),
                                modifier = Modifier.testTag("toggle_keepalive_lock_switch")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Battery optimization exemption card / button
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isIgnoringBatteryOptimizations) EmeraldPrimary.copy(alpha = 0.1f) else OrangeFlame.copy(alpha = 0.1f),
                        border = BorderStroke(
                            1.dp,
                            if (isIgnoringBatteryOptimizations) EmeraldPrimary.copy(alpha = 0.4f) else OrangeFlame.copy(alpha = 0.4f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (isIgnoringBatteryOptimizations) Icons.Default.CheckCircle else Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = if (isIgnoringBatteryOptimizations) EmeraldPrimary else OrangeFlame,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isIgnoringBatteryOptimizations) "Pil Tasarrufu Muafiyeti: Aktif" else "Pil Kısıtlamasını Kaldır",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isIgnoringBatteryOptimizations) EmeraldPrimary else OrangeFlame
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (isIgnoringBatteryOptimizations)
                                        "İşletim sistemi VoltGuard'ı arka planda kısıtlamayacak şekilde ayarlandı."
                                    else
                                        "Android'in arka planda uygulamayı zorla uyutmasını engellemek için muafiyet verin.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            if (!isIgnoringBatteryOptimizations) {
                                OutlinedButton(
                                    onClick = {
                                        com.example.system.AutoStartHelper.requestIgnoreBatteryOptimizations(context)
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = OrangeFlame),
                                    border = BorderStroke(1.dp, OrangeFlame),
                                    modifier = Modifier.testTag("request_battery_exemption_btn")
                                ) {
                                    Text(text = "Muaf Tut", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // OEM Autostart permission shortcut (Xiaomi, Huawei, Oppo, Samsung, etc.)
                    OutlinedButton(
                        onClick = {
                            val opened = com.example.system.AutoStartHelper.openAutoStartSettings(context)
                            if (!opened) {
                                Toast.makeText(context, "Uygulama ayarlarından 'Otomatik Başlatma' iznini kontrol edebilirsiniz.", Toast.LENGTH_LONG).show()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("open_oem_autostart_btn"),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CyanAccent),
                        border = BorderStroke(1.dp, CyanAccent.copy(alpha = 0.6f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.OpenInNew,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = CyanAccent
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Üretici Otomatik Başlatma Ayarlarını Aç",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // 2d. Sistem Kısayolları (Pil Tasarrufu, Ağ ve Ekran)
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth().testTag("settings_system_shortcuts_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Sistem Kısayolları",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Cihazınızın yerel sistem menülerine hızlıca ulaşın:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    ShortcutRow(
                        title = "Sistem Güç Tasarrufu Ayarları",
                        subtitle = "Pil tasarruf modu ve arka plan kısıtlamaları",
                        icon = Icons.Default.BatteryAlert,
                        iconTint = EmeraldPrimary,
                        onClick = { SystemShortcuts.openBatterySaverSettings(context) }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    ShortcutRow(
                        title = "Ağ ve Mobil Veri Ayarları",
                        subtitle = "Veri dolaşımı ve Wi-Fi yönetimi",
                        icon = Icons.Default.Wifi,
                        iconTint = CyanAccent,
                        onClick = { SystemShortcuts.openDataUsageSettings(context) }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    ShortcutRow(
                        title = "Ekran ve Parlaklık Ayarları",
                        subtitle = "Ekran zaman aşımı ve parlaklık kontrolleri",
                        icon = Icons.Default.DisplaySettings,
                        iconTint = OrangeFlame,
                        onClick = { SystemShortcuts.openDisplaySettings(context) }
                    )
                }
            }
        }

                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }

                // ==========================================
                // SEKME 3: ZAMAN ÇİZELGESİ & UYGULAMA GİZLİLİĞİ
                // ==========================================
                SettingsTab.PRIVACY_APPS -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("settings_privacy_scroll"),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // --- 3. ZAMAN ÇİZELGESİ VE UYGULAMA GİZLİLİĞİ AYARLARI ---
                        item {
                            SettingsSectionHeader(title = "Zaman Çizelgesi & Uygulama Gizleme", icon = Icons.Default.Timeline)
                        }

        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth().testTag("settings_timeline_privacy_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f).padding(end = 8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(
                                        if (blockedPackages.isNotEmpty()) OrangeFlame.copy(alpha = 0.15f) else CyanAccent.copy(alpha = 0.15f),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (blockedPackages.isNotEmpty()) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = null,
                                    tint = if (blockedPackages.isNotEmpty()) OrangeFlame else CyanAccent,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Uygulama Çizelgede Gizle / Göster",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (blockedPackages.isNotEmpty()) {
                                        "${blockedPackages.size} uygulama zaman çizelgesinden gizlendi"
                                    } else {
                                        "Tüm uygulamalar zaman çizelgesinde aktif"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (blockedPackages.isNotEmpty()) OrangeFlame else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (blockedPackages.isNotEmpty()) OrangeFlame.copy(alpha = 0.15f) else CyanAccent.copy(alpha = 0.12f),
                            border = BorderStroke(
                                1.dp,
                                if (blockedPackages.isNotEmpty()) OrangeFlame.copy(alpha = 0.4f) else CyanAccent.copy(alpha = 0.3f)
                            )
                        ) {
                            Text(
                                text = "${blockedPackages.size} Gizli",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (blockedPackages.isNotEmpty()) OrangeFlame else CyanAccent,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Zaman çizelgesinde görmek istemediğiniz sistem veya arka plan uygulamalarını buradan seçip gizleyebilir, istediğinizde tek tıkla tekrar açabilirsiniz.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )

                    // Eğer gizlenmiş uygulamalar varsa hızlı chip listesi göster
                    if (blockedPackages.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Şu Anda Gizli Olanlar (Açmak için dokunun):",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = OrangeFlame
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        val blockedList = allDetectedApps.filter { it.first in blockedPackages }
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            blockedList.take(6).forEach { (pkg, name) ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = OrangeFlame.copy(alpha = 0.08f),
                                    border = BorderStroke(1.dp, OrangeFlame.copy(alpha = 0.3f)),
                                    modifier = Modifier.fillMaxWidth().clickable {
                                        onToggleBlockApp(pkg)
                                        Toast.makeText(context, "$name tekrar aktif edildi", Toast.LENGTH_SHORT).show()
                                    }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                Icons.Default.VisibilityOff,
                                                contentDescription = null,
                                                tint = OrangeFlame,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = name,
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }

                                        Text(
                                            text = "Aç ✕",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = EmeraldPrimary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                            if (blockedList.size > 6) {
                                Text(
                                    text = "+ ${blockedList.size - 6} uygulama daha...",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { showAppFilterDialog = true },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CyanAccent,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Uygulama Seç / Yönet", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        if (blockedPackages.isNotEmpty()) {
                            OutlinedButton(
                                onClick = {
                                    onClearAllBlockedApps()
                                    Toast.makeText(context, "Tüm gizlenen uygulamalar tekrar aktif edildi.", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Tümünü Göster", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

                        // Güvenlik & Gizlilik Bilgisi Kartı
                        item {
                            ElevatedCard(
                                modifier = Modifier.fillMaxWidth().testTag("settings_privacy_info_card_tab3"),
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.elevatedCardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                                )
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Security,
                                            contentDescription = null,
                                            tint = EmeraldPrimary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "VoltGuard Güvenlik & Gizlilik",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "• Tüm pil olayları, kullanım süreleri ve veri kayıtları %100 yerel Room veritabanında saklanır.\n• Hiçbir kişisel veri veya kullanım geçmişi harici bir sunucuya iletilmez.\n• Kullanım erişimi izni yalnızca sistemin sağladığı istatistikleri cihaz üzerinde okumak için kullanılır.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        lineHeight = 18.sp
                                    )
                                }
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }

                // ==========================================
                // SEKME 2: BATARYA & ŞARJ ALARMI
                // ==========================================
                SettingsTab.BATTERY_ALARM -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("settings_battery_alarm_scroll"),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // --- 4. BATARYA & ŞARJ ALARMI AYARLARI ---
                        item {
                            SettingsSectionHeader(title = "Batarya & Şarj Alarmı", icon = Icons.Default.BatteryChargingFull)
                        }

                        // --- 4.0. EKRAN İÇİ CANLI AKIM GÖSTERGESİ (SOL ALT KÖŞE - YEŞİL/KIRMIZI) ---
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("card_live_current_overlay_setting"),
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                ),
                                border = BorderStroke(
                                    1.2.dp,
                                    if (isLiveCurrentOverlayEnabled) EmeraldPrimary.copy(alpha = 0.6f) else Color.Transparent
                                )
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f).padding(end = 8.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(38.dp)
                                                    .background(
                                                        if (isLiveCurrentOverlayEnabled) EmeraldPrimary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                                                        CircleShape
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Bolt,
                                                    contentDescription = null,
                                                    tint = if (isLiveCurrentOverlayEnabled) EmeraldPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Text(
                                                    text = "Ekran İçi Canlı Akım Göstergesi",
                                                    style = MaterialTheme.typography.titleSmall,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = "Telefonun sol alt köşesinde saniye saniye anlık akım takibi",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }

                                        Switch(
                                            checked = isLiveCurrentOverlayEnabled,
                                            onCheckedChange = { checked ->
                                                onToggleLiveCurrentOverlay(checked)
                                                if (checked && !hasOverlayPermission) {
                                                    Toast.makeText(
                                                        context,
                                                        "Uygulama içinde sol altta aktif! Diğer uygulamalarda da görmek için 'Üstte Görünme' iznini açabilirsiniz.",
                                                        Toast.LENGTH_LONG
                                                    ).show()
                                                } else if (checked) {
                                                    Toast.makeText(context, "Ekran içi canlı akım göstergesi aktif!", Toast.LENGTH_SHORT).show()
                                                } else {
                                                    Toast.makeText(context, "Canlı akım göstergesi kapatıldı", Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                            colors = SwitchDefaults.colors(
                                                checkedThumbColor = Color.Black,
                                                checkedTrackColor = EmeraldPrimary
                                            )
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Bilgi ve Canlı Önizleme Kutusu (Arkaplan olmadan saf rakamlar gösterildiğini açıklar)
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(
                                                        text = "Deşarj (Harcama): ",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                    Text(
                                                        text = "-380 mA",
                                                        style = MaterialTheme.typography.labelMedium,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        color = Color(0xFF00E676),
                                                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                                    )
                                                }

                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(
                                                        text = "Şarjda: ",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                    Text(
                                                        text = "+1850 mA",
                                                        style = MaterialTheme.typography.labelMedium,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        color = Color(0xFFFF334B),
                                                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "• KESİNLİKLE arkaplanı yoktur (tamamen şeffaf), sol alt köşede saniye saniye güncellenir.",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    if (isLiveCurrentOverlayEnabled && !hasOverlayPermission) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        OutlinedButton(
                                            onClick = {
                                                SystemShortcuts.openOverlaySettings(context)
                                            },
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Diğer Uygulamaların Üzerinde de Göster (İzin Ver)", fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }

        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth().testTag("settings_battery_alarm_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                            Text(
                                text = "Tam Şarj Alarmı",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Pil belirlenen seviyeye ulaştığında sesli ve titreşimli uyarı gönderir.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = fullChargeAlarmEnabled,
                            onCheckedChange = onToggleAlarm,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.Black,
                                checkedTrackColor = EmeraldPrimary
                            )
                        )
                    }

                    AnimatedVisibility(visible = fullChargeAlarmEnabled) {
                        Column(modifier = Modifier.padding(top = 14.dp)) {
                            Text(
                                text = "Hedef Şarj Yüzdesi: %$fullChargeTargetPercent",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = EmeraldPrimary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf(80, 85, 90, 95, 100).forEach { target ->
                                    val isSelected = fullChargeTargetPercent == target
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { onSetAlarmTarget(target) },
                                        label = { Text("%$target", fontSize = 11.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = EmeraldPrimary,
                                            selectedLabelColor = Color.Black
                                        ),
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- 4.2. ÖZEL ALARM SESİ VE MELODİ SEÇİCİ ---
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth().testTag("settings_custom_alarm_tone_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(PurpleNeon.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = null,
                                tint = PurpleNeon,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Alarm Sesi & Melodi Seçimi",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Tam şarj ve kritik sıcaklık uyarı tonu",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    AlarmToneStyle.entries.forEach { tone ->
                        val isSelected = selectedTone == tone
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) PurpleNeon.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            border = BorderStroke(1.dp, if (isSelected) PurpleNeon.copy(alpha = 0.5f) else Color.Transparent),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    selectedTone = tone
                                    CustomAlarmSoundHelper.setSelectedTone(context, tone)
                                    CustomAlarmSoundHelper.playAlarmTone(context, tone)
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                    Text(
                                        text = tone.title,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) PurpleNeon else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = tone.description,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        CustomAlarmSoundHelper.playAlarmTone(context, tone)
                                    },
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(PurpleNeon.copy(alpha = 0.2f), CircleShape)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Dinle",
                                        tint = PurpleNeon,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = { showSmartAlarmSettingsDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PurpleNeon,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().height(38.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Gelişmiş Alarm & Ses Menüsü (Detaylı Ayarla)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // --- 4.3. DÜŞÜK PİL UYARISI & OTOMATİK ULTRA TASARRUF ---
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth().testTag("settings_low_battery_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                            Text(
                                text = "Düşük Pil Uyarısı & Otomatik Tasarruf",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Pil %$lowBatteryThreshold seviyesine indiğinde uyarır ve Ultra Tasarrufa geçer.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = lowBatteryAlertEnabled,
                            onCheckedChange = { enabled ->
                                lowBatteryAlertEnabled = enabled
                                CustomAlarmSoundHelper.setLowBatteryAlertEnabled(context, enabled)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.Black,
                                checkedTrackColor = OrangeFlame
                            )
                        )
                    }

                    AnimatedVisibility(visible = lowBatteryAlertEnabled) {
                        Column(modifier = Modifier.padding(top = 14.dp)) {
                            Text(
                                text = "Uyarı Eşiği:",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = OrangeFlame
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(10, 15, 20, 25).forEach { thresh ->
                                    val isSelected = lowBatteryThreshold == thresh
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = {
                                            lowBatteryThreshold = thresh
                                            CustomAlarmSoundHelper.setLowBatteryThreshold(context, thresh)
                                        },
                                        label = { Text("%$thresh", fontSize = 11.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = OrangeFlame,
                                            selectedLabelColor = Color.Black
                                        ),
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                    Text(
                                        text = "Otomatik Ultra Güç Tasarrufu",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "Eşik aşıldığında pil modunu otomatik Ultra Tasarrufa geçirir.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Switch(
                                    checked = autoUltraSaverEnabled,
                                    onCheckedChange = { checked ->
                                        autoUltraSaverEnabled = checked
                                        CustomAlarmSoundHelper.setAutoUltraSaverEnabled(context, checked)
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = OrangeFlame,
                                        checkedTrackColor = OrangeFlame.copy(alpha = 0.35f)
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- 4.4. AMOLED SAF SİYAH (TRUE BLACK) TEMA ---
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth().testTag("settings_amoled_theme_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f).padding(end = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(CyanAccent.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.DarkMode,
                                contentDescription = null,
                                tint = CyanAccent,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "AMOLED Saf Siyah (True Black)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "OLED piksellerini kapatarak %10 ekstra pil tasarrufu sağlar",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Switch(
                        checked = isAmoledBlack,
                        onCheckedChange = { checked ->
                            isAmoledBlack = checked
                            CustomAlarmSoundHelper.setAmoledTrueBlackEnabled(context, checked)
                            Toast.makeText(
                                context,
                                if (checked) "AMOLED Saf Siyah Modu Aktif" else "Standart Karanlık Mod Aktif",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = CyanAccent,
                            checkedTrackColor = CyanAccent.copy(alpha = 0.35f)
                        )
                    )
                }
            }
        }

        // --- 4.4b. SICAKLIK BİRİMİ SEÇİMİ (°C / °F) ---
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth().testTag("settings_temp_unit_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f, fill = false).padding(end = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(OrangeFlame.copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Tune,
                                    contentDescription = null,
                                    tint = OrangeFlame,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Sıcaklık Birimi (°C / °F)",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                                Text(
                                    text = if (isFahrenheit) "Fahrenheit (°F) seçili" else "Santigrat (°C) seçili",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                            }
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FilterChip(
                                selected = !isFahrenheit,
                                onClick = {
                                    isFahrenheit = false
                                    CustomAlarmSoundHelper.setFahrenheit(context, false)
                                    Toast.makeText(context, "Sıcaklık birimi: Santigrat (°C)", Toast.LENGTH_SHORT).show()
                                },
                                label = { Text("°C", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = OrangeFlame,
                                    selectedLabelColor = Color.Black
                                )
                            )

                            FilterChip(
                                selected = isFahrenheit,
                                onClick = {
                                    isFahrenheit = true
                                    CustomAlarmSoundHelper.setFahrenheit(context, true)
                                    Toast.makeText(context, "Sıcaklık birimi: Fahrenheit (°F)", Toast.LENGTH_SHORT).show()
                                },
                                label = { Text("°F", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = OrangeFlame,
                                    selectedLabelColor = Color.Black
                                )
                            )
                        }
                    }
                }
            }
        }

        // --- 4.5. AKILLI GECE ŞARJ KORUMASI (NIGHT GUARD) ---
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth().testTag("settings_night_guard_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f).padding(end = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(PurpleNeon.copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Bedtime,
                                    contentDescription = null,
                                    tint = PurpleNeon,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Akıllı Gece Şarj Koruması",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Gece Şarj Tamponu (%80) & Uyanış Damlama Zamanlayıcısı",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Button(
                            onClick = { showNightGuardSettingsDialog = true },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PurpleNeon),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("Yapılandır", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }

                // ==========================================
                // SEKME 4: VERİ KULLANIM KOTASI & YEDEKLEME
                // ==========================================
                SettingsTab.DATA_BACKUP -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("settings_data_backup_scroll"),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // --- 5. VERİ KULLANIM KOTASI & LİMİT AYARLARI ---
                        item {
                            SettingsSectionHeader(title = "Veri Kotası & Uyarılar", icon = Icons.Default.DataUsage)
                        }

        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth().testTag("settings_data_limits_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                            Text(
                                text = "Kota Aşım Uyarıları",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Mobil: ${dataLimitConfig.mobileLimitMb} MB | Wi-Fi: ${dataLimitConfig.wifiLimitMb} MB (%${dataLimitConfig.warningThresholdPercent} uyarısı)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = dataLimitConfig.isAlertEnabled,
                            onCheckedChange = { enabled ->
                                onUpdateLimits(
                                    dataLimitConfig.mobileLimitMb,
                                    dataLimitConfig.wifiLimitMb,
                                    dataLimitConfig.warningThresholdPercent,
                                    enabled
                                )
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.Black,
                                checkedTrackColor = CyanAccent
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { showQuotaEditDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyanAccent,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Rule, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Limitleri Düzenle", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // --- 6. VERİTABANI, YEDEKLEME VE HAFTALIK RAPORLAR ---
        item {
            SettingsSectionHeader(title = "Veritabanı, Yedekleme & Raporlar", icon = Icons.Default.Storage)
        }

        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth().testTag("settings_database_backup_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(PurpleNeon.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Storage,
                                contentDescription = null,
                                tint = PurpleNeon,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "JSON Yedekleme & Geri Yükleme",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Tüm log ve ayarları dışa aktarın veya geri yükleyin",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { showWeeklyBatterySummaryDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = EmeraldPrimary,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).height(40.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Analytics, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Haftalık Özet", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { showDatabaseBackupDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PurpleNeon,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).height(40.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Storage, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("JSON Yedek", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }

                // ==========================================
                // SEKME 5: HAKKINDA & KULLANMA KILAVUZU
                // ==========================================
                SettingsTab.ABOUT_GUIDE -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("settings_about_scroll"),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // --- 7. HAKKINDA & KULLANMA KILAVUZU (ÖZELLİKLER REHBERİ) ---
                        item {
                            SettingsSectionHeader(title = "Hakkında & Kullanma Kılavuzu", icon = Icons.Default.Info)
                        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("settings_about_guide_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(1.2.dp, CyanAccent.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f, fill = false)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        androidx.compose.ui.graphics.Brush.linearGradient(
                                            listOf(CyanAccent, EmeraldPrimary)
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "VoltGuard Battery Manager",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "Sürüm 2.6.0 Pro • Kullanım Kılavuzu & Özellikler",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = CyanAccent,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = EmeraldPrimary.copy(alpha = 0.18f)
                        ) {
                            Text(
                                text = "Pro Aktif",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldPrimary,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "VoltGuard, pil ömrünüzü uzatmak için canlı donanım telemetrisi, akıllı şarj koruması, hücre stres testi ve yapay zeka analizlerini bir arada sunar. Tüm özelliklerin nasıl çalıştığını ve nasıl erişileceğini öğrenmek için kılavuzu açabilirsiniz.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 17.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // 4 Core Feature Quick Summary Pills
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            color = CyanAccent.copy(alpha = 0.1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.Bolt, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("Telemetri", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = CyanAccent)
                            }
                        }

                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            color = PurpleNeon.copy(alpha = 0.1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.Science, contentDescription = null, tint = PurpleNeon, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("Hücre Testi", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = PurpleNeon)
                            }
                        }

                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            color = EmeraldPrimary.copy(alpha = 0.1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.Bedtime, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("Gece Kalkanı", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                            }
                        }

                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            color = OrangeFlame.copy(alpha = 0.1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = OrangeFlame, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("AI Koç", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = OrangeFlame)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = { showAboutGuideDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("open_about_user_guide_btn"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyanAccent,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MenuBook,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Kullanma Kılavuzu & Özellikleri İncele",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        // --- 8. GÜVENLİK VE GİZLİLİK BİLGİSİ ---
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth().testTag("settings_privacy_info_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = EmeraldPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "VoltGuard Güvenlik & Gizlilik",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "• Tüm pil olayları, kullanım süreleri ve veri kayıtları %100 yerel Room veritabanında saklanır.\n• Hiçbir kişisel veri veya kullanım geçmişi harici bir sunucuya iletilmez.\n• Kullanım erişimi izni yalnızca sistemin sağladığı istatistikleri cihaz üzerinde okumak için kullanılır.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )
                }
            }
        }

                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            }
        }
    }

    // --- HAKKINDA & KULLANMA KILAVUZU DİYALOĞU ---
    if (showAboutGuideDialog) {
        AboutUserGuideDialog(
            onDismiss = { showAboutGuideDialog = false }
        )
    }

    // --- MODÜLER PİL VE ÖZELLİK YÖNETİCİSİ DİYALOĞU ---
    if (showModularPowerFeaturesDialog) {
        ModularPowerFeaturesDialog(
            onDismissRequest = { showModularPowerFeaturesDialog = false }
        )
    }

    // --- UYGULAMA FİLTRE DİYALOĞU ---
    if (showAppFilterDialog) {
        AppVisibilityFilterDialog(
            allApps = allDetectedApps,
            blockedPackages = blockedPackages,
            onToggleApp = onToggleBlockApp,
            onClearAllBlocked = onClearAllBlockedApps,
            onDismiss = { showAppFilterDialog = false }
        )
    }

    // --- KOTA LİMİT DÜZENLEME DİYALOĞU ---
    if (showQuotaEditDialog) {
        QuotaEditDialog(
            currentMobileMb = dataLimitConfig.mobileLimitMb,
            currentWifiMb = dataLimitConfig.wifiLimitMb,
            currentThreshold = dataLimitConfig.warningThresholdPercent,
            currentAlertEnabled = dataLimitConfig.isAlertEnabled,
            onDismiss = { showQuotaEditDialog = false },
            onSave = { mob, wif, thr, alert ->
                onUpdateLimits(mob, wif, thr, alert)
                showQuotaEditDialog = false
                Toast.makeText(context, "Kota limitleri kaydedildi", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // --- AKILLI GECE ŞARJ KORUMASI DİYALOĞU ---
    if (showNightGuardSettingsDialog) {
        NightChargingGuardDialog(
            onDismiss = { showNightGuardSettingsDialog = false }
        )
    }

    // --- AKILLI ALARM AYARLARI DİYALOĞU (Option 3) ---
    if (showSmartAlarmSettingsDialog) {
        SmartAlarmSettingsDialog(
            onDismiss = { showSmartAlarmSettingsDialog = false }
        )
    }

    // --- HAFTALIK PİL ÖZETİ DİYALOĞU (Option 5 part 1) ---
    if (showWeeklyBatterySummaryDialog) {
        WeeklyBatterySummaryDialog(
            onDismiss = { showWeeklyBatterySummaryDialog = false }
        )
    }

    // --- VERİTABANI YEDEKLEME & GERİ YÜKLEME DİYALOĞU (Option 5 part 2) ---
    if (showDatabaseBackupDialog) {
        DatabaseBackupRestoreDialog(
            onDismiss = { showDatabaseBackupDialog = false }
        )
    }
}

@Composable
private fun SettingsSectionHeader(title: String, icon: ImageVector) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 6.dp, bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = CyanAccent,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun PermissionCard(
    title: String,
    description: String,
    isGranted: Boolean,
    grantedLabel: String,
    deniedLabel: String,
    actionLabel: String,
    onActionClick: () -> Unit,
    testTag: String
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth().testTag(testTag),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isGranted) EmeraldPrimary.copy(alpha = 0.15f) else OrangeFlame.copy(alpha = 0.15f),
                    border = BorderStroke(
                        1.dp,
                        if (isGranted) EmeraldPrimary.copy(alpha = 0.4f) else OrangeFlame.copy(alpha = 0.4f)
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Icon(
                            imageVector = if (isGranted) Icons.Default.CheckCircle else Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (isGranted) EmeraldPrimary else OrangeFlame,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = if (isGranted) grantedLabel else deniedLabel,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isGranted) EmeraldPrimary else OrangeFlame,
                            maxLines = 1
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onActionClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isGranted) MaterialTheme.colorScheme.surfaceVariant else CyanAccent,
                    contentColor = if (isGranted) MaterialTheme.colorScheme.onSurface else Color.Black
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.OpenInNew,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = actionLabel,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun ShortcutRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(iconTint.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Default.OpenInNew,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun QuotaEditDialog(
    currentMobileMb: Long,
    currentWifiMb: Long,
    currentThreshold: Int,
    currentAlertEnabled: Boolean,
    onDismiss: () -> Unit,
    onSave: (Long, Long, Int, Boolean) -> Unit
) {
    var mobileText by remember { mutableStateOf(currentMobileMb.toString()) }
    var wifiText by remember { mutableStateOf(currentWifiMb.toString()) }
    var threshold by remember { mutableIntStateOf(currentThreshold) }
    var alertEnabled by remember { mutableStateOf(currentAlertEnabled) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Veri Kotası ve Uyarı Ayarları", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = mobileText,
                    onValueChange = { mobileText = it.filter { c -> c.isDigit() } },
                    label = { Text("Mobil Veri Limiti (MB)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = wifiText,
                    onValueChange = { wifiText = it.filter { c -> c.isDigit() } },
                    label = { Text("Wi-Fi Limiti (MB)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Text(
                    text = "Uyarı Eşiği: %$threshold",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(70, 80, 90).forEach { th ->
                        val isSel = threshold == th
                        FilterChip(
                            selected = isSel,
                            onClick = { threshold = th },
                            label = { Text("%$th") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CyanAccent,
                                selectedLabelColor = Color.Black
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Kota Uyarıları", style = MaterialTheme.typography.bodyMedium)
                    Switch(
                        checked = alertEnabled,
                        onCheckedChange = { alertEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = CyanAccent
                        )
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val m = mobileText.toLongOrNull() ?: currentMobileMb
                    val w = wifiText.toLongOrNull() ?: currentWifiMb
                    onSave(m, w, threshold, alertEnabled)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = CyanAccent,
                    contentColor = Color.Black
                )
            ) {
                Text("Kaydet", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("İptal")
            }
        }
    )
}
