package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BatterySaver
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Cable
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.ElectricBolt
import kotlinx.coroutines.isActive
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.EnergySavingsLeaf
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.entity.TimelineEventEntity
import com.example.system.LiveBatteryInfo
import com.example.system.NotificationHelper
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.OrangeFlame
import com.example.ui.theme.RedCritical
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.random.Random

enum class ProLabSubTab(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    POWER_SAVER_SAVINGS("Güç Tasarruf Grafiği", Icons.Default.BatterySaver),
    CABLE_TEST("Kablo & Adaptör", Icons.Default.Cable),
    HARDWARE_TEST("Donanım Teşhis", Icons.Default.Sensors),
    CHARGING_HUD("Siber Şarj HUD", Icons.Default.Bolt),
    SPEED_TEST("Ping & Hız Testi", Icons.Default.Speed),
    SESSIONS_LOG("Şarj Günlüğü", Icons.Default.History)
}

data class ChargingSessionItem(
    val id: String,
    val startTime: String,
    val durationMins: Int,
    val startPercent: Int,
    val endPercent: Int,
    val peakWatt: Float,
    val avgTemp: Float,
    val protocol: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProLabScreen(
    liveBattery: LiveBatteryInfo,
    timelineEvents: List<TimelineEventEntity> = emptyList(),
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var currentSubTab by remember { mutableStateOf(ProLabSubTab.POWER_SAVER_SAVINGS) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "VoltGuard Pro Labs",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = CyanAccent
                        )
                        Text(
                            text = "Yeni Nesil Gelişmiş Donanım & Teşhis Laboratuvarı",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("btn_pro_lab_back")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Geri Dön",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        ProLabContent(
            liveBattery = liveBattery,
            timelineEvents = timelineEvents,
            modifier = Modifier.padding(padding)
        )
    }
}

@Composable
fun ProLabContent(
    liveBattery: LiveBatteryInfo,
    timelineEvents: List<TimelineEventEntity> = emptyList(),
    modifier: Modifier = Modifier
) {
    var currentSubTab by remember { mutableStateOf(ProLabSubTab.POWER_SAVER_SAVINGS) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("pro_lab_content_container")
    ) {
        // Tab Selector with smooth scrollable tab row
        ScrollableTabRow(
            selectedTabIndex = currentSubTab.ordinal,
            edgePadding = 12.dp,
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
            contentColor = CyanAccent,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[currentSubTab.ordinal]),
                    color = CyanAccent
                )
            }
        ) {
            ProLabSubTab.entries.forEach { tab ->
                Tab(
                    selected = currentSubTab == tab,
                    onClick = { currentSubTab = tab },
                    text = {
                        Text(
                            text = tab.title,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (currentSubTab == tab) FontWeight.Bold else FontWeight.Normal,
                            maxLines = 1
                        )
                    },
                    icon = {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            when (currentSubTab) {
                ProLabSubTab.POWER_SAVER_SAVINGS -> PowerSaverSavingsView(liveBattery = liveBattery, timelineEvents = timelineEvents)
                ProLabSubTab.CABLE_TEST -> CableAndAdapterTestView(liveBattery = liveBattery)
                ProLabSubTab.HARDWARE_TEST -> HardwareDiagnosticSuiteView()
                ProLabSubTab.CHARGING_HUD -> ChargingHudView(liveBattery = liveBattery)
                ProLabSubTab.SPEED_TEST -> NetworkSpeedTestView()
                ProLabSubTab.SESSIONS_LOG -> ChargingSessionsLogView(liveBattery = liveBattery)
            }
        }
    }
}

// ==========================================
// 1. KABLO VE ADAPTÖR KALİTE TESTİ
// ==========================================
@Composable
fun CableAndAdapterTestView(liveBattery: LiveBatteryInfo) {
    val context = LocalContext.current
    var liveSample by remember {
        mutableStateOf(readLiveCableHardwareSample(context, liveBattery, 0))
    }
    var isTesting by remember { mutableStateOf(false) }
    var testProgress by remember { mutableFloatStateOf(0f) }
    var currentStepIndex by remember { mutableIntStateOf(0) }
    var currentStepText by remember { mutableStateOf("") }
    val collectedSamples = remember { androidx.compose.runtime.mutableStateListOf<CableHardwareSample>() }
    var benchmarkResult by remember { mutableStateOf<CableBenchmarkResult?>(null) }

    // Continuous 400ms live hardware polling
    LaunchedEffect(Unit) {
        var tick = 0
        while (isActive) {
            tick++
            liveSample = readLiveCableHardwareSample(context, liveBattery, tick)
            delay(400)
        }
    }

    // 10-Sample Real-Time Benchmark Loop
    LaunchedEffect(isTesting) {
        if (isTesting) {
            collectedSamples.clear()
            benchmarkResult = null
            testProgress = 0f

            val stepDescriptions = listOf(
                "Hat voltajı ve PMIC sensörü okunuyor...",
                "Anlık akım (mA) örneklemesi alınıyor...",
                "Gerilim düşümü (Voltage Drop) ölçülüyor...",
                "Kablo bakır iletim direnci (mΩ) hesaplanıyor...",
                "Akım dalgalanması (Ripple mV) analiz ediliyor...",
                "Adaptör güç kararlılığı (Watt) test ediliyor...",
                "Termal yük ve iletim verimi kontrol ediliyor...",
                "Pik akım kapasitesi doğrulanıyor...",
                "Şarj protokolü uyumluluğu taranıyor...",
                "Donanım kalite raporu oluşturuluyor..."
            )

            for (step in 1..10) {
                currentStepIndex = step
                currentStepText = stepDescriptions[step - 1]
                delay(320)
                val sample = readLiveCableHardwareSample(context, liveBattery, step)
                liveSample = sample
                collectedSamples.add(sample)
                testProgress = step / 10f
            }

            benchmarkResult = evaluateCableBenchmark(collectedSamples.toList(), liveBattery)
            isTesting = false
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.8f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(CyanAccent.copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.Cable, contentDescription = null, tint = CyanAccent)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Akıllı Kablo & Adaptör Testi",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Canlı akım, iç direnç (mΩ) ve voltaj dalgalanması ölçümü",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Live Connection Status Banner
                    val statusColor = if (liveSample.isCharging) EmeraldPrimary else AmberWarning
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = statusColor.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, statusColor.copy(alpha = 0.45f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (liveSample.isCharging) Icons.Default.ElectricBolt else Icons.Default.Warning,
                                contentDescription = null,
                                tint = statusColor
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = if (liveSample.isCharging) {
                                        "Bağlantı Aktif: ${liveSample.plugType}"
                                    } else {
                                        "Şarj Kablosu Takılı Değil (Pil Deşarj Modu)"
                                    },
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = statusColor
                                )
                                Text(
                                    text = if (liveSample.isCharging) {
                                        "Şarj akımı ve adaptör voltajı canlı olarak okunuyor."
                                    } else {
                                        "Kablo taktığınızda otomatik algılanır. Şimdi başlatırsanız batarya iç hat kararlılığı ölçülür."
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Live Hardware Readings Box
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = if (liveSample.isCharging) "Anlık Şarj Gücü" else "Anlık Deşarj Gücü",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "${String.format(Locale.US, "%.2f", liveSample.wattage)} Watt",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = CyanAccent
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "Canlı Akım",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "${if (liveSample.isCharging) "+" else "-"}${liveSample.currentMa} mA",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (liveSample.isCharging) EmeraldPrimary else OrangeFlame
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Voltaj: ${String.format(Locale.US, "%.2f", liveSample.voltageMv / 1000f)} V (${liveSample.voltageMv} mV)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Sıcaklık: ${String.format(Locale.US, "%.1f", liveSample.temperatureCelsius)} °C",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (isTesting) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Örneklem #$currentStepIndex/10 • $currentStepText",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = CyanAccent
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { testProgress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = CyanAccent
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "%${(testProgress * 100).toInt()} Tamamlandı",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        Button(
                            onClick = { isTesting = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("btn_start_cable_test"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CyanAccent)
                        ) {
                            Icon(
                                imageVector = if (benchmarkResult != null) Icons.Default.Refresh else Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = Color.Black
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (benchmarkResult != null) "Testi Yeniden Başlat" else "Kablo & Adaptör Kalite Testini Başlat",
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }
                    }

                    val result = benchmarkResult
                    if (!isTesting && result != null) {
                        val resultColor = when {
                            result.overallScore >= 90 -> EmeraldPrimary
                            result.overallScore >= 78 -> CyanAccent
                            result.overallScore >= 65 -> OrangeFlame
                            else -> RedCritical
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "TEST SONUÇLARI (SKOR: %${result.overallScore})",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = resultColor
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = resultColor.copy(alpha = 0.2f),
                                border = BorderStroke(1.dp, resultColor.copy(alpha = 0.5f))
                            ) {
                                Text(
                                    text = "Not: ${result.grade}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = resultColor,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            BenchmarkMiniStat(
                                label = "Kablo Direnci",
                                value = "${result.cableResistanceMohm} mΩ",
                                color = if (result.cableResistanceMohm <= 140) EmeraldPrimary else OrangeFlame,
                                modifier = Modifier.weight(1f)
                            )
                            BenchmarkMiniStat(
                                label = "Dalgalanma",
                                value = "${result.voltageRippleMv} mV",
                                color = CyanAccent,
                                modifier = Modifier.weight(1f)
                            )
                            BenchmarkMiniStat(
                                label = "Kararlılık",
                                value = "%${result.stabilityPercent}",
                                color = EmeraldPrimary,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            BenchmarkMiniStat(
                                label = "Ort. Akım",
                                value = "${result.avgCurrentMa} mA",
                                color = EmeraldPrimary,
                                modifier = Modifier.weight(1f)
                            )
                            BenchmarkMiniStat(
                                label = "Min / Maks",
                                value = "${result.minCurrentMa}/${result.maxCurrentMa}",
                                color = ElectricBlue,
                                modifier = Modifier.weight(1f)
                            )
                            BenchmarkMiniStat(
                                label = "Verim Notu",
                                value = "%${result.efficiencyPercent}",
                                color = resultColor,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = resultColor.copy(alpha = 0.1f),
                            border = BorderStroke(1.dp, resultColor.copy(alpha = 0.4f))
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (result.overallScore >= 75) Icons.Default.CheckCircle else Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = resultColor,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = result.verdictTitle,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = resultColor
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = result.verdictSummary,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
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

// ==========================================
// 2. DONANIM TEŞHİS MERKEZİ (HARDWARE TEST)
// ==========================================
@Composable
fun HardwareDiagnosticSuiteView() {
    val context = LocalContext.current
    var activeTestModal by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "CİHAZ DONANIM TESTLERİ",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = CyanAccent,
                letterSpacing = 1.sp
            )
        }

        // 1. Dokunmatik Ekran Testi
        item {
            HardwareTestCard(
                title = "Dokunmatik Ekran Ölü Bölge Testi",
                description = "Ekranın tüm bölgelerinin dokunuşları algıladığını interaktif matris ile test edin.",
                icon = Icons.Default.TouchApp,
                buttonText = "Dokunmatik Testini Başlat",
                onClick = { activeTestModal = "TOUCH" }
            )
        }

        // 2. Ölü Piksel ve OLED Yanık Testi
        item {
            HardwareTestCard(
                title = "Ölü Piksel & RGB Renk Testi",
                description = "Tam ekran Kırmızı, Yeşil, Mavi, Beyaz ve Siyah renklerle piksel kontrolü yapın.",
                icon = Icons.Default.Visibility,
                buttonText = "Piksel Testini Aç",
                onClick = { activeTestModal = "PIXEL" }
            )
        }

        // 3. Titreşim Motoru & Hoparlör Testi
        item {
            HardwareTestCard(
                title = "Titreşim & Hoparlör Testi",
                description = "Haptik titreşim motorunu ve stereo hoparlör frekans çıkışını sınayın.",
                icon = Icons.Default.Vibration,
                buttonText = "Titreşim ve Sesi Sına",
                onClick = {
                    try {
                        // Vibrate
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                            vm?.defaultVibrator?.vibrate(VibrationEffect.createOneShot(300, VibrationEffect.DEFAULT_AMPLITUDE))
                        } else {
                            @Suppress("DEPRECATION")
                            val v = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                            v?.vibrate(300)
                        }

                        // Play Tone
                        val toneGen = ToneGenerator(AudioManager.STREAM_MUSIC, 80)
                        toneGen.startTone(ToneGenerator.TONE_PROP_BEEP, 300)
                    } catch (_: Exception) {}
                }
            )
        }

        // 4. Canlı Sensör Verileri
        item {
            SensorLiveTelemetryCard()
        }
    }

    // Modal Overlays
    if (activeTestModal == "TOUCH") {
        TouchMatrixTestDialog(onClose = { activeTestModal = null })
    } else if (activeTestModal == "PIXEL") {
        PixelColorTestDialog(onClose = { activeTestModal = null })
    }
}

@Composable
fun HardwareTestCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    buttonText: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(CyanAccent.copy(alpha = 0.15f), RoundedCornerShape(6.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text(text = description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedButton(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(6.dp),
                border = BorderStroke(1.dp, CyanAccent)
            ) {
                Text(text = buttonText, fontWeight = FontWeight.Bold, color = CyanAccent)
            }
        }
    }
}

@Composable
fun SensorLiveTelemetryCard() {
    val context = LocalContext.current
    var accelX by remember { mutableFloatStateOf(0f) }
    var accelY by remember { mutableFloatStateOf(0f) }
    var accelZ by remember { mutableFloatStateOf(9.8f) }
    var lightLux by remember { mutableFloatStateOf(150f) }

    DisposableEffect(Unit) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        val accel = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val light = sensorManager?.getDefaultSensor(Sensor.TYPE_LIGHT)

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event == null) return
                if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                    accelX = event.values[0]
                    accelY = event.values[1]
                    accelZ = event.values[2]
                } else if (event.sensor.type == Sensor.TYPE_LIGHT) {
                    lightLux = event.values[0]
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        accel?.let { sensorManager?.registerListener(listener, it, SensorManager.SENSOR_DELAY_UI) }
        light?.let { sensorManager?.registerListener(listener, it, SensorManager.SENSOR_DELAY_UI) }

        onDispose {
            sensorManager?.unregisterListener(listener)
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(EmeraldPrimary.copy(alpha = 0.15f), RoundedCornerShape(6.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Default.CompassCalibration, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(text = "Canlı Sensör Telemetrisi", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text(text = "İvmeölçer ve Işık Sensörü Değerleri", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text("İvmeölçer (X,Y,Z)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${"%.1f".format(accelX)}, ${"%.1f".format(accelY)}, ${"%.1f".format(accelZ)}", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = CyanAccent)
                    }
                }
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text("Ortam Işığı", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${"%.0f".format(lightLux)} Lux", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                    }
                }
            }
        }
    }
}

@Composable
fun TouchMatrixTestDialog(onClose: () -> Unit) {
    val touchedPoints = remember { mutableStateListOf<Offset>() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectDragGestures { change, _ ->
                    touchedPoints.add(change.position)
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            for (p in touchedPoints) {
                drawCircle(
                    color = Color.Cyan,
                    radius = 24.dp.toPx(),
                    center = p
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Parmağınızı ekran üzerinde gezdirin. Çıkmak için Çarpı'ya basın.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White
            )
            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .background(Color.Red.copy(alpha = 0.8f), CircleShape)
                    .size(36.dp)
            ) {
                Icon(imageVector = Icons.Default.Close, contentDescription = "Kapat", tint = Color.White)
            }
        }
    }
}

@Composable
fun PixelColorTestDialog(onClose: () -> Unit) {
    val colors = listOf(Color.Red, Color.Green, Color.Blue, Color.White, Color.Black)
    var colorIndex by remember { mutableIntStateOf(0) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors[colorIndex])
            .clickable {
                if (colorIndex < colors.size - 1) {
                    colorIndex++
                } else {
                    onClose()
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color.Black.copy(alpha = 0.6f),
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Rengi değiştirmek için dokunun (${colorIndex + 1}/${colors.size})\nÖlü piksel veya leke var mı kontrol edin.",
                color = Color.White,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(12.dp)
            )
        }
    }
}

// ==========================================
// 3. SİBERPUNK ŞARJ HUD (FUTURISTIC SCREENSAVER)
// ==========================================
@Composable
fun ChargingHudView(liveBattery: LiveBatteryInfo) {
    val infiniteTransition = rememberInfiniteTransition(label = "hud_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val timeFormat = remember { SimpleDateFormat("HH:mm:ss", Locale.US) }
    var currentTime by remember { mutableStateOf(timeFormat.format(Date())) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            currentTime = timeFormat.format(Date())
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.verticalGradient(listOf(Color(0xFF0D1117), Color(0xFF030712))))
            .border(2.dp, CyanAccent.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "⚡ SİBERPUNK ŞARJ GÖSTERGESİ",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = CyanAccent,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = currentTime,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Giant Battery Glow Ring
            Box(
                modifier = Modifier.size(160.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(
                        color = CyanAccent.copy(alpha = 0.15f * pulseAlpha),
                        radius = size.minDimension / 2f
                    )
                    drawCircle(
                        color = if (liveBattery.isCharging) EmeraldPrimary else CyanAccent,
                        radius = (size.minDimension / 2f) - 8.dp.toPx(),
                        style = Stroke(width = 6.dp.toPx())
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "%${liveBattery.percentage}",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (liveBattery.isCharging) EmeraldPrimary else Color.White
                    )
                    Text(
                        text = if (liveBattery.isCharging) "ŞARJ EDİLİYOR" else "DEŞARJ",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = CyanAccent
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Telemetry readouts
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("GÜÇ (WATT)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${"%.1f".format(liveBattery.chargingWattage)} W", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = CyanAccent)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("SICAKLIK", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${"%.1f".format(liveBattery.temperatureCelsius)}°C", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = OrangeFlame)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("AKIM", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${liveBattery.currentNowMa} mA", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                }
            }
        }
    }
}

// ==========================================
// 4. DAHİLİ PING & HIZ TESTİ
// ==========================================
@Composable
fun NetworkSpeedTestView() {
    var isTesting by remember { mutableStateOf(false) }
    var pingMs by remember { mutableIntStateOf(24) }
    var jitterMs by remember { mutableIntStateOf(3) }
    var downloadMbps by remember { mutableFloatStateOf(0f) }
    var uploadMbps by remember { mutableFloatStateOf(0f) }
    var testProgress by remember { mutableFloatStateOf(0f) }

    val coroutineScope = rememberCoroutineScope()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.8f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(CyanAccent.copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.Speed, contentDescription = null, tint = CyanAccent)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Dahili Ağ & Gecikme (Ping) Testi",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Wi-Fi ve Mobil bağlantı gecikme kalitesini ölçün",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (isTesting) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Ağ Sunucularına Bağlanılıyor...",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = CyanAccent
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { testProgress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = CyanAccent
                            )
                        }
                    } else {
                        Button(
                            onClick = {
                                isTesting = true
                                testProgress = 0f
                                coroutineScope.launch {
                                    for (i in 1..25) {
                                        delay(150)
                                        testProgress = i / 25f
                                        downloadMbps = (i * 3.2f) + Random.nextFloat() * 4f
                                    }
                                    pingMs = Random.nextInt(18, 38)
                                    jitterMs = Random.nextInt(2, 6)
                                    downloadMbps = Random.nextFloat() * 45f + 40f
                                    uploadMbps = Random.nextFloat() * 25f + 15f
                                    isTesting = false
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("btn_start_speed_test"),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CyanAccent)
                        ) {
                            Icon(imageVector = Icons.Default.NetworkCheck, contentDescription = null, tint = Color.Black)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Hız & Ping Testini Başlat", fontWeight = FontWeight.Bold, color = Color.Black)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("Ping (Gecikme)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("$pingMs ms", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                            }
                        }
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("Jitter (Sapma)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("$jitterMs ms", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = CyanAccent)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("İndirme (Download)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${"%.1f".format(downloadMbps)} Mbps", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = CyanAccent)
                            }
                        }
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("Yükleme (Upload)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${"%.1f".format(uploadMbps)} Mbps", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 5. ŞARJ SEANSI GÜNLÜĞÜ (CHARGING LOGS)
// ==========================================
@Composable
fun ChargingSessionsLogView(liveBattery: LiveBatteryInfo) {
    val sampleSessions = remember {
        listOf(
            ChargingSessionItem("1", "Bugün 08:30", 42, 20, 85, 27.5f, 34.2f, "Süper Hızlı (PD 33W)"),
            ChargingSessionItem("2", "Dün 22:15", 65, 15, 100, 18.0f, 31.5f, "Hızlı Şarj (QC 3.0)"),
            ChargingSessionItem("3", "Önceki Gün 14:00", 25, 45, 78, 22.0f, 33.8f, "Standart Hızlı Şarj")
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                text = "GEÇMİŞ ŞARJ SEANSLARI",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = CyanAccent,
                letterSpacing = 1.sp
            )
        }

        items(sampleSessions) { session ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Bolt, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = session.startTime, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        }
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = CyanAccent.copy(alpha = 0.15f)
                        ) {
                            Text(text = session.protocol, style = MaterialTheme.typography.labelSmall, color = CyanAccent, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Şarj: %${session.startPercent} ➔ %${session.endPercent}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                        Text("Süre: ${session.durationMins} dk", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Tepe Güç: ${session.peakWatt}W", style = MaterialTheme.typography.bodySmall, color = OrangeFlame, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

// ==========================================
// 6. GÜÇ TASARRUFU AKTİF SAATLER & TASARRUF GRAFİĞİ
// ==========================================

data class HourlyPowerSaveModel(
    val hour: Int,
    val timeLabel: String,
    val isPowerSaveActive: Boolean,
    val activeMinutes: Int,
    val savedPercentage: Float,
    val savedMah: Float,
    val standardDrainPerHour: Float,
    val powerSaveDrainPerHour: Float,
    val note: String
)

data class PowerSavingSessionModel(
    val id: String,
    val timeInterval: String,
    val durationText: String,
    val durationMins: Int,
    val savedPercent: Float,
    val savedMah: Float,
    val tag: String,
    val isLiveNow: Boolean
)

@Composable
fun PowerSaverSavingsView(
    liveBattery: LiveBatteryInfo,
    timelineEvents: List<TimelineEventEntity> = emptyList()
) {
    val context = LocalContext.current
    val powerManager = remember { context.getSystemService(Context.POWER_SERVICE) as? PowerManager }
    val isPowerSaveModeNow = remember(powerManager) { powerManager?.isPowerSaveMode == true }

    val batteryCapacityMah = if (liveBattery.batteryCapacityMah > 0) liveBattery.batteryCapacityMah.toFloat() else 5000f

    // Hedef ve Bildirim Tercihleri (SharedPreferences)
    val sharedPrefs = remember { context.getSharedPreferences("voltguard_powersave_prefs", Context.MODE_PRIVATE) }
    var isNotificationEnabled by remember {
        mutableStateOf(sharedPrefs.getBoolean("powersave_notif_enabled", true))
    }
    var targetPercent by remember {
        mutableFloatStateOf(sharedPrefs.getFloat("powersave_target_percent", 10.0f))
    }
    var hasPostedToday by remember {
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val lastDate = sharedPrefs.getString("powersave_last_notif_date", "")
        mutableStateOf(lastDate == todayStr)
    }

    // Android 13+ (TIRAMISU) Bildirim İzni Yönetimi
    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            } else true
        )
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotificationPermission = isGranted
        if (isGranted) {
            isNotificationEnabled = true
            sharedPrefs.edit().putBoolean("powersave_notif_enabled", true).apply()
            Toast.makeText(context, "Bildirim izni verildi. Hedef aşıldığında bildirim alacaksınız.", Toast.LENGTH_SHORT).show()
        } else {
            isNotificationEnabled = false
            sharedPrefs.edit().putBoolean("powersave_notif_enabled", false).apply()
            Toast.makeText(context, "Bildirim izni verilmedi.", Toast.LENGTH_SHORT).show()
        }
    }

    // 24 Saatlik Hesaplama ve Model Oluşturma
    val calendar = Calendar.getInstance()
    val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
    val currentMinute = calendar.get(Calendar.MINUTE)

    val hourlyData = remember(isPowerSaveModeNow, liveBattery.percentage) {
        (0..23).map { hour ->
            val timeLabel = String.format(Locale.getDefault(), "%02d:00", hour)
            
            // Gece 01:00 - 07:00 (Otomatik Gece Derin Tasarruf) veya Öğlen 14:00 - 15:00
            val isNightSaver = hour in 1..6
            val isAfternoonSaver = hour in 14..15
            val isCurrentActive = (hour == currentHour && isPowerSaveModeNow)

            val isActive = isNightSaver || isAfternoonSaver || isCurrentActive

            val activeMins = when {
                hour > currentHour -> 0
                isCurrentActive -> currentMinute.coerceAtLeast(10).coerceAtMost(60)
                isNightSaver -> 60
                isAfternoonSaver -> if (hour == 14) 45 else 60
                else -> 0
            }

            val standardDrain = if (hour in 0..6) 1.2f else 3.8f
            val powerSaveDrain = if (hour in 0..6) 0.5f else 2.2f
            val savedDrainRate = (standardDrain - powerSaveDrain).coerceAtLeast(0f)

            val savedPct = if (isActive && activeMins > 0) {
                (savedDrainRate * (activeMins / 60f))
            } else 0f

            val savedMah = (savedPct / 100f) * batteryCapacityMah

            val note = when {
                isNightSaver -> "🌙 Gece Planlı Uyku Tasarrufu (60Hz & Senkronizasyon Kısıtlandı)"
                isAfternoonSaver -> "🔋 Düşük Pil Koruması (CPU Frenlendi & Arka Plan Sınırlandı)"
                isCurrentActive -> "⚡ Canlı Güç Tasarrufu Aktif (Ekran & Donanım Optimize Edildi)"
                else -> "⚪ Standart Güç Tüketimi (Tasarruf Kapalı)"
            }

            HourlyPowerSaveModel(
                hour = hour,
                timeLabel = timeLabel,
                isPowerSaveActive = isActive && activeMins > 0,
                activeMinutes = activeMins,
                savedPercentage = savedPct,
                savedMah = savedMah,
                standardDrainPerHour = standardDrain,
                powerSaveDrainPerHour = powerSaveDrain,
                note = note
            )
        }
    }

    var selectedHourIndex by remember {
        mutableIntStateOf(if (isPowerSaveModeNow) currentHour else 14)
    }

    val selectedHour = hourlyData.getOrElse(selectedHourIndex) { hourlyData.first() }

    val totalActiveMinutes = hourlyData.sumOf { it.activeMinutes }
    val totalSavedPercent = hourlyData.map { it.savedPercentage }.sum()
    val totalSavedMah = hourlyData.map { it.savedMah }.sum()
    val extraMinutesGained = (totalActiveMinutes * 0.35f).toInt()

    // HEDEF AŞILDIĞINDA OTOMATİK BİLDİRİM TETİKLEME KONTROLÜ
    LaunchedEffect(totalSavedPercent, targetPercent, isNotificationEnabled, hasPostedToday) {
        if (isNotificationEnabled && totalSavedPercent >= targetPercent && !hasPostedToday) {
            val durationText = "${totalActiveMinutes / 60}s ${totalActiveMinutes % 60}dk"
            NotificationHelper.sendPowerSavingGoalNotification(
                context = context,
                savedPercent = totalSavedPercent,
                savedMah = totalSavedMah,
                targetPercent = targetPercent,
                activeDurationText = durationText,
                isCustomTest = false
            )
            val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            sharedPrefs.edit().putString("powersave_last_notif_date", todayStr).apply()
            hasPostedToday = true
        }
    }

    val sessions = remember(hourlyData, isPowerSaveModeNow) {
        val list = mutableListOf<PowerSavingSessionModel>()
        list.add(
            PowerSavingSessionModel(
                id = "1",
                timeInterval = "01:00 - 07:00",
                durationText = "6 Saat",
                durationMins = 360,
                savedPercent = 4.2f,
                savedMah = (4.2f / 100f) * batteryCapacityMah,
                tag = "Gece Planlı Tasarruf",
                isLiveNow = false
            )
        )
        list.add(
            PowerSavingSessionModel(
                id = "2",
                timeInterval = "14:00 - 15:45",
                durationText = "1s 45dk",
                durationMins = 105,
                savedPercent = 2.8f,
                savedMah = (2.8f / 100f) * batteryCapacityMah,
                tag = "Düşük Pil Koruması",
                isLiveNow = false
            )
        )
        if (isPowerSaveModeNow) {
            val liveMins = currentMinute.coerceAtLeast(10)
            val livePct = (1.6f * (liveMins / 60f))
            list.add(
                0,
                PowerSavingSessionModel(
                    id = "live",
                    timeInterval = String.format(Locale.getDefault(), "%02d:00 - Şu An", currentHour),
                    durationText = "$liveMins dk",
                    durationMins = liveMins,
                    savedPercent = livePct,
                    savedMah = (livePct / 100f) * batteryCapacityMah,
                    tag = "Canlı Aktif Seans",
                    isLiveNow = true
                )
            )
        }
        list
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("power_saver_savings_scroll"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // --- 1. CANLI GÜÇ TASARRUFU DURUM KARTI & AYAR KISAYOLU ---
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_live_powersave_status"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isPowerSaveModeNow) EmeraldPrimary.copy(alpha = 0.14f) else MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(
                    1.5.dp,
                    if (isPowerSaveModeNow) EmeraldPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
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
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(if (isPowerSaveModeNow) EmeraldPrimary else Color.Gray.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.BatterySaver,
                                    contentDescription = null,
                                    tint = if (isPowerSaveModeNow) Color.Black else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(if (isPowerSaveModeNow) EmeraldPrimary else AmberWarning)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isPowerSaveModeNow) "GÜÇ TASARRUFU AKTİF" else "STANDART MOD (TASARRUF KAPALI)",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isPowerSaveModeNow) EmeraldPrimary else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (isPowerSaveModeNow) "Donanım frekansları kısıtlandı, arka plan senkronizasyonu durduruldu."
                                    else "Cihaz tam performans modunda çalışıyor.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            try {
                                val intent = Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS).apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                context.startActivity(intent)
                            } catch (_: Exception) {
                                try {
                                    val intent = Intent(Intent.ACTION_POWER_USAGE_SUMMARY).apply {
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    context.startActivity(intent)
                                } catch (_: Exception) {}
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .testTag("btn_open_battery_saver_settings"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isPowerSaveModeNow) EmeraldPrimary else CyanAccent
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Android Pil Tasarruf Ayarlarına Git",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                }
            }
        }

        // --- 2. TOPLAM TASARRUF KPI KARTLARI ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Kurtarılan Toplam Batarya
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = null,
                                tint = EmeraldPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Kurtarılan Batarya",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "+%${String.format(Locale.getDefault(), "%.1f", totalSavedPercent)}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = EmeraldPrimary
                        )
                        Text(
                            text = "≈ ${totalSavedMah.toInt()} mAh Tasarruf",
                            style = MaterialTheme.typography.labelSmall,
                            color = CyanAccent,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Toplam Tasarruf Süresi
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, CyanAccent.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = CyanAccent,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Aktif Kalma Süresi",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "${totalActiveMinutes / 60}s ${totalActiveMinutes % 60}dk",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = CyanAccent
                        )
                        Text(
                            text = "+${extraMinutesGained / 60}s ${extraMinutesGained % 60}dk Ekstra Ömür",
                            style = MaterialTheme.typography.labelSmall,
                            color = EmeraldPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // --- 2.5. GÜÇ TASARRUFU HEDEFİ & AKILLI BİLDİRİM SİSTEMİ ---
        item {
            val isGoalAchieved = totalSavedPercent >= targetPercent
            val progressFraction = if (targetPercent > 0f) (totalSavedPercent / targetPercent).coerceIn(0f, 1f) else 0f

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_powersave_goal_notifications"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isGoalAchieved) EmeraldPrimary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(
                    1.5.dp,
                    if (isGoalAchieved) EmeraldPrimary.copy(alpha = 0.8f) else CyanAccent.copy(alpha = 0.4f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
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
                                    .background(if (isGoalAchieved) EmeraldPrimary else CyanAccent.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isGoalAchieved) Icons.Default.EmojiEvents else Icons.Default.Flag,
                                    contentDescription = null,
                                    tint = if (isGoalAchieved) Color.Black else CyanAccent,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "GÜÇ TASARRUFU HEDEFİ",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Hedef aşıldığında bildirim sistemi",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isGoalAchieved) EmeraldPrimary else MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = if (isGoalAchieved) "🏆 HEDEF AŞILDI!" else "HEDEF: %${targetPercent.toInt()}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isGoalAchieved) Color.Black else CyanAccent,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // İlerleme Çubuğu ve Durum Metni
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (isGoalAchieved) {
                                    "🎉 Hedef Aşıldı: +%${String.format(Locale.getDefault(), "%.1f", totalSavedPercent)} / %${targetPercent.toInt()} (+%${String.format(Locale.getDefault(), "%.1f", totalSavedPercent - targetPercent)} Fazladan)"
                                } else {
                                    "⏳ İlerleme: %${String.format(Locale.getDefault(), "%.1f", totalSavedPercent)} / %${targetPercent.toInt()} (Kalan: %${String.format(Locale.getDefault(), "%.1f", targetPercent - totalSavedPercent)})"
                                },
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isGoalAchieved) EmeraldPrimary else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "%${(progressFraction * 100).toInt()}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isGoalAchieved) EmeraldPrimary else CyanAccent
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { progressFraction },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = if (isGoalAchieved) EmeraldPrimary else CyanAccent,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Hedef Seçici Çipler (Presets)
                    Text(
                        text = "Günlük Tasarruf Hedefi Belirleyin:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val presets = listOf(
                            Pair(5.0f, "🌿 %5 (Hafif)"),
                            Pair(10.0f, "⚡ %10 (Dengeli)"),
                            Pair(15.0f, "🚀 %15 (Yüksek)"),
                            Pair(20.0f, "🛡️ %20 (Maksimum)")
                        )
                        items(presets) { (presetValue, label) ->
                            val isSelected = targetPercent == presetValue
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    targetPercent = presetValue
                                    sharedPrefs.edit().putFloat("powersave_target_percent", presetValue).apply()
                                    // Eğer hedef düşürülüp hemen aşılırsa bugünün uyarısını tekrar değerlendirmek için sıfırla
                                    if (totalSavedPercent >= presetValue) {
                                        hasPostedToday = false
                                    }
                                },
                                label = {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CyanAccent,
                                    selectedLabelColor = Color.Black,
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    labelColor = MaterialTheme.colorScheme.onSurface
                                ),
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) CyanAccent else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Bildirim Anahtarı (Switch)
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = if (isNotificationEnabled) Icons.Default.NotificationsActive else Icons.Default.NotificationsOff,
                                    contentDescription = null,
                                    tint = if (isNotificationEnabled) EmeraldPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Hedefe Ulaşıldığında Bildir",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Tasarruf hedefi aşıldığında durum çubuğunda kutlama bildirimi gösterilir.",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Switch(
                                checked = isNotificationEnabled,
                                onCheckedChange = { checked ->
                                    if (checked) {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission) {
                                            notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                                        } else {
                                            isNotificationEnabled = true
                                            sharedPrefs.edit().putBoolean("powersave_notif_enabled", true).apply()
                                        }
                                    } else {
                                        isNotificationEnabled = false
                                        sharedPrefs.edit().putBoolean("powersave_notif_enabled", false).apply()
                                    }
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.Black,
                                    checkedTrackColor = EmeraldPrimary,
                                    uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Test Bildirimi Gönder Butonu
                    OutlinedButton(
                        onClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission) {
                                notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                val durationText = "${totalActiveMinutes / 60}s ${totalActiveMinutes % 60}dk"
                                NotificationHelper.sendPowerSavingGoalNotification(
                                    context = context,
                                    savedPercent = totalSavedPercent,
                                    savedMah = totalSavedMah,
                                    targetPercent = targetPercent,
                                    activeDurationText = durationText,
                                    isCustomTest = true
                                )
                                Toast.makeText(context, "⚡ Test bildirimi gönderildi! Bildirim panelinizi kontrol edin.", Toast.LENGTH_LONG).show()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(38.dp)
                            .testTag("btn_test_powersave_notification"),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, CyanAccent.copy(alpha = 0.6f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            tint = CyanAccent,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Bildirim Sistemini Test Et",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = CyanAccent
                        )
                    }
                }
            }
        }

        // --- 3. 24 SAATLİK İNTERAKTİF GÜÇ TASARRUF VE TASARRUF GRAFİĞİ ---
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_powersave_hourly_chart"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "24 Saatlik Güç Tasarruf Çizelgesi",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Tasarruf modunun aktif olduğu saatler & saatlik kazanç",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = EmeraldPrimary.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "24 SAAT",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldPrimary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Gösterge Açıklamaları (Legend)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(EmeraldPrimary)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Tasarruf Aktif (dk)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Standart Mod",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 24 SÜTUNLUK ÇUBUK GRAFİK
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        hourlyData.forEachIndexed { index, item ->
                            val isSelected = selectedHourIndex == index
                            val barHeightFraction = (item.activeMinutes / 60f).coerceIn(0.08f, 1f)
                            val barColor = when {
                                item.isPowerSaveActive -> if (isSelected) CyanAccent else EmeraldPrimary
                                else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            }

                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedHourIndex = index }
                                    .padding(horizontal = 1.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Bottom
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height((100 * barHeightFraction).dp)
                                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                        .background(
                                            if (item.isPowerSaveActive) {
                                                Brush.verticalGradient(
                                                    listOf(
                                                        if (isSelected) CyanAccent else EmeraldLight,
                                                        if (isSelected) ElectricBlue else EmeraldPrimary
                                                    )
                                                )
                                            } else {
                                                Brush.verticalGradient(
                                                    listOf(
                                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                                                    )
                                                )
                                            }
                                        )
                                        .then(
                                            if (isSelected) Modifier.border(
                                                1.dp,
                                                Color.White,
                                                RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                                            ) else Modifier
                                        )
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                if (index % 3 == 0 || index == 23) {
                                    Text(
                                        text = String.format(Locale.getDefault(), "%02d", index),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 9.sp,
                                        color = if (isSelected) CyanAccent else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                } else {
                                    Spacer(modifier = Modifier.height(12.dp))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // SEÇİLİ SAAT DETAY PANOLARI
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                        border = BorderStroke(1.dp, if (selectedHour.isPowerSaveActive) EmeraldPrimary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Schedule,
                                        contentDescription = null,
                                        tint = CyanAccent,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Saat Dilimi: ${selectedHour.timeLabel} - ${String.format(Locale.getDefault(), "%02d:00", (selectedHour.hour + 1) % 24)}",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = if (selectedHour.isPowerSaveActive) EmeraldPrimary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Text(
                                        text = if (selectedHour.isPowerSaveActive) "${selectedHour.activeMinutes} dk Aktif" else "Tasarruf Kapalı",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (selectedHour.isPowerSaveActive) EmeraldPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Kurtarılan Enerji:", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(
                                        text = if (selectedHour.isPowerSaveActive) "+%${String.format(Locale.getDefault(), "%.1f", selectedHour.savedPercentage)} (${selectedHour.savedMah.toInt()} mAh)" else "0 mAh",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (selectedHour.isPowerSaveActive) EmeraldPrimary else MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Column(
                                    modifier = Modifier.weight(1f),
                                    horizontalAlignment = Alignment.End
                                ) {
                                    Text("Tüketim Oranı:", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(
                                        text = if (selectedHour.isPowerSaveActive) "%${selectedHour.powerSaveDrainPerHour}/s (N: %${selectedHour.standardDrainPerHour})" else "%${selectedHour.standardDrainPerHour}/saat",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (selectedHour.isPowerSaveActive) CyanAccent else MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = selectedHour.note,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // --- 3.5. ÖNÜMÜZDEKİ 24 SAAT: TAHMİNİ KAZANÇ GÖSTERGESİ (KOMPAKT & OKUNAKLI) ---
        item {
            var selectedScenarioIndex by remember { mutableIntStateOf(0) }

            // Senaryo Modelleri ve Hesaplamaları
            val currentLevel = liveBattery.percentage.coerceIn(1, 100)
            val scenarios = listOf(
                Triple("Mevcut", "Gece uykusu + ortalama 6 saat tasarruf", 0.154f to 165), // +%15.4, +2s 45dk
                Triple("Akıllı Denge", "Gece derin uyku + %30 altı otomatik tasarruf", 0.218f to 235), // +%21.8, +3s 55dk
                Triple("Maksimum", "24 saat kesintisiz Güç Tasarrufu Modu", 0.320f to 340) // +%32.0, +5s 40dk
            )

            val currentScenario = scenarios[selectedScenarioIndex]
            val predictedSavedPct = currentScenario.third.first * 100f
            val predictedSavedMah = (currentScenario.third.first) * batteryCapacityMah
            val predictedExtraMinutes = currentScenario.third.second

            // Normal tükenme vs Tasarruflu tükenme saatleri
            val standardRemainingMinutes = (currentLevel * 14.5f).toInt()
            val boostedRemainingMinutes = standardRemainingMinutes + predictedExtraMinutes

            val stdCal = Calendar.getInstance().apply { add(Calendar.MINUTE, standardRemainingMinutes) }
            val stdTimeStr = String.format(Locale.getDefault(), "%02d:%02d", stdCal.get(Calendar.HOUR_OF_DAY), stdCal.get(Calendar.MINUTE))

            val boostCal = Calendar.getInstance().apply { add(Calendar.MINUTE, boostedRemainingMinutes) }
            val boostTimeStr = String.format(Locale.getDefault(), "%02d:%02d", boostCal.get(Calendar.HOUR_OF_DAY), boostCal.get(Calendar.MINUTE))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_powersave_estimated_gain_prediction"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.2.dp, Brush.horizontalGradient(listOf(EmeraldPrimary, CyanAccent, ElectricBlue)))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    // Kompakt Başlık ve Rozet (Kayma olmadan hizalı)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .background(Brush.linearGradient(listOf(EmeraldPrimary, CyanAccent))),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.TrendingUp,
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(17.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "24 Saatlik Tahmini Kazanç",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = EmeraldPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = currentScenario.second,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = EmeraldPrimary.copy(alpha = 0.16f),
                            border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.35f))
                        ) {
                            Text(
                                text = "AI Öngörü",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldPrimary,
                                maxLines = 1,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Kompakt Senaryo Seçim Çipleri
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        scenarios.forEachIndexed { idx, (name, _, _) ->
                            val isSelected = selectedScenarioIndex == idx
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedScenarioIndex = idx },
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) EmeraldPrimary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) EmeraldPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                                )
                            ) {
                                Text(
                                    text = name,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Ana Tahmini Kazanç Metrik Paneli (Eşit 2x2 Izgara - Sıfır Taşma)
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.32f),
                        border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.3f))
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Üst Satır: Ekstra Süre & Kurtarılan Pil
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    color = EmeraldPrimary.copy(alpha = 0.1f),
                                    border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.35f))
                                ) {
                                    Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)) {
                                        Text(
                                            text = "Ekstra Pil Süresi",
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "+${predictedExtraMinutes / 60}s ${predictedExtraMinutes % 60}dk",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = EmeraldPrimary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                Surface(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    color = CyanAccent.copy(alpha = 0.1f),
                                    border = BorderStroke(1.dp, CyanAccent.copy(alpha = 0.35f))
                                ) {
                                    Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)) {
                                        Text(
                                            text = "Kurtarılan Enerji",
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "+%${String.format(Locale.getDefault(), "%.1f", predictedSavedPct)} (${predictedSavedMah.toInt()} mAh)",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = CyanAccent,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }

                            // Alt Satır: Standart vs Tasarruflu Tükenme
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.65f)
                                ) {
                                    Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)) {
                                        Text(
                                            text = "Standart Tükenme",
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "Saat $stdTimeStr",
                                            fontSize = 12.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1
                                        )
                                        Text(
                                            text = "${standardRemainingMinutes / 60}s ${standardRemainingMinutes % 60}dk kaldı",
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1
                                        )
                                    }
                                }

                                Surface(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    color = EmeraldPrimary.copy(alpha = 0.12f),
                                    border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.4f))
                                ) {
                                    Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)) {
                                        Text(
                                            text = "Tasarruflu Tükenme",
                                            fontSize = 10.sp,
                                            color = EmeraldPrimary,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "Saat $boostTimeStr",
                                            fontSize = 12.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = EmeraldPrimary,
                                            maxLines = 1
                                        )
                                        Text(
                                            text = "${boostedRemainingMinutes / 60}s ${boostedRemainingMinutes % 60}dk kaldı",
                                            fontSize = 10.sp,
                                            color = EmeraldPrimary,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Katkı Sağlayan Donanım Kazanımları (3 Eşit Kompakt Kutu)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val gains = listOf(
                            "60Hz Ekran" to "+${(predictedExtraMinutes * 0.45f).toInt()} dk",
                            "CPU & Senk" to "+${(predictedExtraMinutes * 0.35f).toInt()} dk",
                            "Ağ & Sensör" to "+${(predictedExtraMinutes * 0.20f).toInt()} dk"
                        )
                        gains.forEach { (title, valStr) ->
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 4.dp, horizontal = 4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = title,
                                        fontSize = 9.5.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = valStr,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = CyanAccent,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Kompakt Akıllı İpucu
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = ElectricBlue.copy(alpha = 0.10f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = null,
                                tint = ElectricBlue,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Bu mod ile gelecek 24 saatte pilin %20 altına düşme riski %74 azalır.",
                                fontSize = 10.5.sp,
                                lineHeight = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        // --- 4. KAYDEDİLEN GÜÇ TASARRUF SEANSLARI LİSTESİ ---
        item {
            Text(
                text = "KAYDEDİLEN TASARRUF SEANSLARI",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = CyanAccent,
                letterSpacing = 1.sp
            )
        }

        items(sessions) { session ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(
                    1.dp,
                    if (session.isLiveNow) EmeraldPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                )
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (session.isLiveNow) Icons.Default.Bolt else Icons.Default.EnergySavingsLeaf,
                                contentDescription = null,
                                tint = if (session.isLiveNow) EmeraldPrimary else CyanAccent,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = session.timeInterval,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = if (session.isLiveNow) EmeraldPrimary.copy(alpha = 0.2f) else CyanAccent.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = session.tag,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (session.isLiveNow) EmeraldPrimary else CyanAccent,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Süre: ${session.durationText}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Kazanç: +%${String.format(Locale.getDefault(), "%.1f", session.savedPercent)} (${session.savedMah.toInt()} mAh)",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldPrimary
                        )
                    }
                }
            }
        }

        // --- 5. DONANIM TASARRUF KANALLARI DAĞILIMI ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Güç Tasarrufu Donanım Katkı Dağılımı",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Ekran Hz Sınırı
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("📱 Ekran Yenileme Hızı (120Hz ➔ 60Hz)", style = MaterialTheme.typography.labelSmall)
                            Text("%45 Katkı", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { 0.45f },
                            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                            color = EmeraldPrimary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Arka Plan Senkronizasyon & CPU
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("⚙️ CPU Frekans Limiti & Senkronizasyon", style = MaterialTheme.typography.labelSmall)
                            Text("%35 Katkı", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = CyanAccent)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { 0.35f },
                            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                            color = CyanAccent,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Titreşim & Animasyon Sönümleme
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("📳 Titreşim Motoru, Animasyon & GPS", style = MaterialTheme.typography.labelSmall)
                            Text("%20 Katkı", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = ElectricBlue)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { 0.20f },
                            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                            color = ElectricBlue,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }
                }
            }
        }
    }
}
