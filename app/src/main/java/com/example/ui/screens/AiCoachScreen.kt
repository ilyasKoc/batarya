package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.heightIn
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.Dialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.entity.AppTimeLimitEntity
import com.example.system.AppNamesHelper
import com.example.system.DeviceAppItem
import com.example.system.DeviceAppScanner
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.GreenSuccess
import com.example.ui.theme.OrangeFlame
import com.example.ui.theme.PurpleNeon
import com.example.ui.theme.RedCritical
import com.example.viewmodel.MainViewModel
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.platform.testTag
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Bolt
import com.example.system.AppIconCache
import com.example.ui.theme.EmeraldPrimary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray

@Composable
fun AppIconView(
    packageName: String,
    appName: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var iconBitmap by remember(packageName) { mutableStateOf<ImageBitmap?>(null) }

    LaunchedEffect(packageName) {
        val cached = AppIconCache.getIcon(context, packageName)
        iconBitmap = cached
    }

    val currentBitmap = iconBitmap
    if (currentBitmap != null) {
        Image(
            bitmap = currentBitmap,
            contentDescription = appName,
            modifier = modifier
        )
    } else {
        Box(
            modifier = modifier
                .clip(CircleShape)
                .background(OrangeFlame.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = appName.take(1).uppercase(),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = OrangeFlame
            )
        }
    }
}

/**
 * AiCoachScreen: Gemini yapay zeka destekli pil enerji optimizasyon koçu,
 * uygulama kullanım kısıtlamaları ve dijital sağlık rehberi ekranı.
 */
@Composable
fun AiCoachScreen(
    viewModel: MainViewModel,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    val aiReport by viewModel.aiCoachReport.collectAsState()
    val isGenerating by viewModel.isGeneratingAiReport.collectAsState()
    val aiEnergyReport by viewModel.aiEnergyReport.collectAsState()
    val isGeneratingEnergyReport by viewModel.isGeneratingEnergyReport.collectAsState()
    val appLimits by viewModel.appTimeLimits.collectAsState()
    val timelineEvents by viewModel.timelineEvents.collectAsState()
    val currentPowerMode by viewModel.powerMode.collectAsState()
    val optimizationMessage by viewModel.optimizationMessage.collectAsState()

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var selectedAiSubTab by remember { mutableIntStateOf(0) } // 0 = AI Enerji Raporu, 1 = Alışkanlık Koçu
    var showAddLimitDialog by remember { mutableStateOf(false) }
    var appSearchQuery by remember { mutableStateOf("") }
    var selectedAppCandidate by remember { mutableStateOf<DeviceAppItem?>(null) }
    var customMinutesText by remember { mutableStateOf("45") }
    var selectedFilterCategory by remember { mutableStateOf("Tümü") }

    // Live real device scanned apps
    var scannedDeviceApps by remember { mutableStateOf<List<DeviceAppItem>>(emptyList()) }
    var isScanningDevice by remember { mutableStateOf(false) }

    fun triggerDeviceScan() {
        isScanningDevice = true
        coroutineScope.launch {
            val apps = withContext(Dispatchers.IO) {
                DeviceAppScanner.scanInstalledApps(context)
            }
            scannedDeviceApps = apps
            isScanningDevice = false
        }
    }

    LaunchedEffect(showAddLimitDialog) {
        if (showAddLimitDialog && scannedDeviceApps.isEmpty()) {
            triggerDeviceScan()
        }
    }

    // Calculate today's used minutes per app
    val appUsageMinutesMap = remember(timelineEvents) {
        val map = mutableMapOf<String, Int>()
        for (event in timelineEvents) {
            if (event.eventType == "APP_USAGE" && event.dayOffset == 0) {
                val mins = if (event.durationMinutes > 0) event.durationMinutes else (event.durationSeconds / 60).coerceAtLeast(1)
                map[event.packageName] = (map[event.packageName] ?: 0) + mins
            }
        }
        map
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
    ) {
        // --- TOP SEGMENTED AI TAB BAR (AI ENERJİ RAPORU / ALIŞKANLIK KOÇU) ---
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .testTag("ai_coach_top_tabs"),
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Tab 0: AI Enerji Raporu (Pil Tasarrufu)
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { selectedAiSubTab = 0 }
                        .testTag("subtab_ai_energy_report"),
                    shape = RoundedCornerShape(8.dp),
                    color = if (selectedAiSubTab == 0) EmeraldPrimary.copy(alpha = 0.2f) else Color.Transparent,
                    border = BorderStroke(
                        1.dp,
                        if (selectedAiSubTab == 0) EmeraldPrimary else Color.Transparent
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 9.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ElectricBolt,
                            contentDescription = null,
                            tint = if (selectedAiSubTab == 0) EmeraldPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(17.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "AI Enerji Raporu",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = if (selectedAiSubTab == 0) FontWeight.Bold else FontWeight.Medium,
                            color = if (selectedAiSubTab == 0) EmeraldPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Tab 1: Alışkanlık Koçu
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { selectedAiSubTab = 1 }
                        .testTag("subtab_ai_habit_coach"),
                    shape = RoundedCornerShape(8.dp),
                    color = if (selectedAiSubTab == 1) PurpleNeon.copy(alpha = 0.2f) else Color.Transparent,
                    border = BorderStroke(
                        1.dp,
                        if (selectedAiSubTab == 1) PurpleNeon else Color.Transparent
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 9.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = null,
                            tint = if (selectedAiSubTab == 1) PurpleNeon else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(17.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Alışkanlık Koçu",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = if (selectedAiSubTab == 1) FontWeight.Bold else FontWeight.Medium,
                            color = if (selectedAiSubTab == 1) PurpleNeon else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        if (selectedAiSubTab == 0) {
            AiEnergyReportTab(
                report = aiEnergyReport,
                isGenerating = isGeneratingEnergyReport,
                currentPowerMode = currentPowerMode,
                optimizationMessage = optimizationMessage,
                onClearOptimizationMessage = { viewModel.clearOptimizationMessage() },
                onRefreshReport = { viewModel.triggerAiEnergyReport(forceRefresh = true) },
                onApplyQuickAction = { action ->
                    if (action.actionType == "APP_LIMITS") {
                        selectedAiSubTab = 1
                        showAddLimitDialog = true
                    } else {
                        viewModel.applyEnergyQuickAction(action)
                    }
                },
                onOpenAppSettings = { viewModel.openAppSystemSettings(it) },
                modifier = Modifier.weight(1f)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 90.dp)
            ) {
        // --- 1. AI HABIT COACH HERO CARD ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(PurpleNeon.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = PurpleNeon,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f, fill = false)) {
                                Text(
                                    text = "Gemini AI Alışkanlık Koçu",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "Günlük Alışkanlık Analizi",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = { viewModel.triggerAiHabitAnalysis() },
                            enabled = !isGenerating,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PurpleNeon),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            if (isGenerating) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(14.dp),
                                    strokeWidth = 2.dp,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Analiz...", style = MaterialTheme.typography.labelSmall)
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Analiz Et",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    val score = aiReport?.score ?: 85
                    val personaTitle = aiReport?.personaTitle ?: "Dengeli Dijital Kaşif 🧘"
                    val summary = aiReport?.summary ?: "Bugünkü telefon kullanımınız, kilit açma sıklığınız ve uyku düzeniniz analiz ediliyor."

                    // Score Gauge & Persona
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.size(80.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                progress = { 1f },
                                modifier = Modifier.fillMaxSize(),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                strokeWidth = 8.dp
                            )
                            CircularProgressIndicator(
                                progress = { (score.toFloat() / 100f).coerceIn(0f, 1f) },
                                modifier = Modifier.fillMaxSize(),
                                color = if (score >= 80) GreenSuccess else if (score >= 60) AmberWarning else RedCritical,
                                strokeWidth = 8.dp,
                                strokeCap = StrokeCap.Round
                            )
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "$score",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Skor",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = CyanAccent.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = personaTitle,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = CyanAccent,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = summary,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }
        }

        // --- 2. STRENGTHS & RISKS CARDS ---
        item {
            val strengths = remember(aiReport?.strengthsJson) {
                parseJsonList(aiReport?.strengthsJson, listOf("Gece ekran kullanımını erken sınırladınız", "Günün çoğunda odak seviyeniz yüksekti"))
            }
            val risks = remember(aiReport?.risksJson) {
                parseJsonList(aiReport?.risksJson, listOf("Günün ilk saatinde hızlı kilit açma refleksi", "Sosyal medyada 40 dk üzeri kesintisiz kalış"))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Strengths
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = GreenSuccess, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Güçlü Yönler", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = GreenSuccess)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        strengths.forEach { s ->
                            Text(
                                text = "• $s",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 15.sp,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }
                    }
                }

                // Risks
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = AmberWarning, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Dikkat Noktaları", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = AmberWarning)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        risks.forEach { r ->
                            Text(
                                text = "• $r",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 15.sp,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        // --- 3. 3-STEP ACTION PLAN ---
        item {
            val suggestions = remember(aiReport?.suggestionsJson) {
                parseJsonList(aiReport?.suggestionsJson, listOf(
                    "Sosyal medya uygulamaları için günlük 45 dk süre limiti koyun.",
                    "Öğleden sonra 25 dakikalık bir Pomodoro seansı başlatın.",
                    "Saat 23:00'ten sonra telefon bildirimlerini sessize alın."
                ))
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Psychology, contentDescription = null, tint = CyanAccent)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "AI Eylem & Gelişim Planı",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    suggestions.forEachIndexed { index, tip ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .clip(CircleShape)
                                    .background(CyanAccent.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${index + 1}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = CyanAccent
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = tip,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 18.sp,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        // --- 4. APP TIME LIMITS (DIGITAL DETOX) SECTION ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.HourglassBottom, contentDescription = null, tint = OrangeFlame)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Uygulama Süre Limitleri (Detoks)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(onClick = { showAddLimitDialog = true }) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Limit Ekle", tint = OrangeFlame)
                }
            }
        }

        // Quick Preset Add Bar
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Hızlı Limit Belirle (Popüler Uygulamalar):",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            Triple("com.instagram.android", "Instagram", 45),
                            Triple("com.google.android.youtube", "YouTube", 60),
                            Triple("com.zhiliaoapp.musically", "TikTok", 30),
                            Triple("com.twitter.android", "X (Twitter)", 30)
                        ).forEach { (pkg, name, defaultMins) ->
                            val alreadyAdded = appLimits.any { it.packageName == pkg }
                            OutlinedButton(
                                onClick = {
                                    viewModel.setAppTimeLimit(pkg, name, defaultMins)
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, if (alreadyAdded) GreenSuccess.copy(alpha = 0.5f) else CyanAccent.copy(alpha = 0.4f)),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = name,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (alreadyAdded) GreenSuccess else CyanAccent,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = if (alreadyAdded) "Aktif" else "$defaultMins dk",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (appLimits.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Henüz süre limiti belirlenmiş bir uygulama yok. Yukarıdaki hızlı butonları kullanarak veya '+' ile özel limit ekleyebilirsiniz.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        } else {
            items(appLimits) { limit ->
                val usedMins = appUsageMinutesMap[limit.packageName] ?: 0
                val progress = if (limit.limitMinutes > 0) (usedMins.toFloat() / limit.limitMinutes).coerceIn(0f, 1f) else 0f
                val isExceeded = usedMins >= limit.limitMinutes && limit.isEnabled

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isExceeded) RedCritical.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (isExceeded) RedCritical.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.size(44.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                progress = { 1f },
                                modifier = Modifier.fillMaxSize(),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                strokeWidth = 5.dp
                            )
                            CircularProgressIndicator(
                                progress = { progress },
                                modifier = Modifier.fillMaxSize(),
                                color = if (isExceeded) RedCritical else OrangeFlame,
                                strokeWidth = 5.dp,
                                strokeCap = StrokeCap.Round
                            )
                            Text(
                                text = "${(progress * 100).toInt()}%",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isExceeded) RedCritical else OrangeFlame
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = limit.appName,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                if (isExceeded) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = RedCritical.copy(alpha = 0.2f)
                                    ) {
                                        Text(
                                            text = "Aşıldı!",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = RedCritical,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Hedef: ${limit.limitMinutes} dk • Bugün: $usedMins dk",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Switch(
                                checked = limit.isEnabled,
                                onCheckedChange = { viewModel.toggleAppTimeLimit(limit.packageName, it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = OrangeFlame,
                                    checkedTrackColor = OrangeFlame.copy(alpha = 0.4f)
                                )
                            )
                            IconButton(onClick = { viewModel.deleteAppTimeLimit(limit.packageName) }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Limiti Sil",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    } // close else { LazyColumn }
    } // close Column

    if (showAddLimitDialog) {
        val filteredApps = remember(appSearchQuery, selectedFilterCategory, scannedDeviceApps, appUsageMinutesMap) {
            var list = scannedDeviceApps

            // Category filter
            list = when (selectedFilterCategory) {
                "Yüklenenler" -> list.filter { !it.isSystemApp }
                "Bugün Kullanılanlar" -> list.filter { (appUsageMinutesMap[it.packageName] ?: 0) > 0 }
                "Sosyal & İletişim" -> list.filter { it.category in listOf("Sosyal Medya", "Mesajlaşma & İletişim") }
                "Medya & Video" -> list.filter { it.category in listOf("Video & Eğlence", "Müzik & Ses") }
                "Oyunlar" -> list.filter { it.category == "Oyun" }
                "Sistem" -> list.filter { it.isSystemApp }
                else -> list
            }

            // Search query filter
            if (appSearchQuery.isNotBlank()) {
                val q = appSearchQuery.trim()
                list = list.filter {
                    it.appName.contains(q, ignoreCase = true) ||
                    it.packageName.contains(q, ignoreCase = true) ||
                    it.category.contains(q, ignoreCase = true)
                }
            }

            list
        }

        Dialog(
            onDismissRequest = {
                showAddLimitDialog = false
                appSearchQuery = ""
                selectedAppCandidate = null
            },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .fillMaxHeight(0.90f)
                    .testTag("dialog_select_app_limit"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF131720)),
                border = BorderStroke(1.2.dp, OrangeFlame.copy(alpha = 0.35f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp)
                ) {
                    // --- 1. HEADER (NO SHIFTING / NO OVERLAP) ---
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
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(OrangeFlame.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PhoneAndroid,
                                    contentDescription = null,
                                    tint = OrangeFlame,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Uygulama Süre Sınırı",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = if (isScanningDevice) "Cihaz taranıyor..." else "${filteredApps.size} uygulama listelendi",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isScanningDevice) OrangeFlame else Color.White.copy(alpha = 0.65f),
                                    fontSize = 11.5.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { triggerDeviceScan() },
                                enabled = !isScanningDevice,
                                modifier = Modifier.size(34.dp)
                            ) {
                                if (isScanningDevice) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp,
                                        color = OrangeFlame
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Yeniden Tara",
                                        tint = OrangeFlame,
                                        modifier = Modifier.size(19.dp)
                                    )
                                }
                            }
                            IconButton(
                                onClick = {
                                    showAddLimitDialog = false
                                    appSearchQuery = ""
                                    selectedAppCandidate = null
                                },
                                modifier = Modifier.size(34.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Kapat",
                                    tint = Color.White.copy(alpha = 0.7f),
                                    modifier = Modifier.size(19.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // --- 2. SEARCH BAR ---
                    OutlinedTextField(
                        value = appSearchQuery,
                        onValueChange = { appSearchQuery = it },
                        placeholder = { Text("Telefonda ara (TikTok, Instagram, Oyun)...", fontSize = 12.sp, color = Color.White.copy(alpha = 0.5f)) },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = OrangeFlame, modifier = Modifier.size(16.dp))
                        },
                        trailingIcon = {
                            if (appSearchQuery.isNotEmpty()) {
                                IconButton(onClick = { appSearchQuery = "" }, modifier = Modifier.size(22.dp)) {
                                    Icon(imageVector = Icons.Default.Close, contentDescription = "Temizle", tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(14.dp))
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 44.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // --- 3. CATEGORY CHIPS ---
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val categories = listOf(
                            "Tümü",
                            "Yüklenenler",
                            "Bugün Kullanılanlar",
                            "Sosyal & İletişim",
                            "Medya & Video",
                            "Oyunlar",
                            "Sistem"
                        )
                        items(categories) { cat ->
                            val isSelected = selectedFilterCategory == cat
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedFilterCategory = cat },
                                label = { Text(cat, fontSize = 10.5.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = OrangeFlame.copy(alpha = 0.25f),
                                    selectedLabelColor = OrangeFlame,
                                    containerColor = Color(0xFF1A202C)
                                ),
                                border = BorderStroke(1.dp, if (isSelected) OrangeFlame else Color.White.copy(alpha = 0.1f)),
                                modifier = Modifier.height(28.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // --- 4. MAXIMIZED APP LIST CONTAINER ---
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF161C26)),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
                    ) {
                        if (isScanningDevice && scannedDeviceApps.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    CircularProgressIndicator(modifier = Modifier.size(28.dp), color = OrangeFlame)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Cihaz uygulamaları taranıyor...",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        } else if (filteredApps.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = null,
                                        tint = Color.White.copy(alpha = 0.4f),
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Eşleşen uygulama bulunamadı.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 6.dp, vertical = 6.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                items(filteredApps, key = { it.packageName }) { item ->
                                    val isSelected = selectedAppCandidate?.packageName == item.packageName
                                    val existingLimit = appLimits.firstOrNull { it.packageName == item.packageName }
                                    val todayMins = appUsageMinutesMap[item.packageName] ?: 0

                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                selectedAppCandidate = item
                                                if (existingLimit != null) {
                                                    customMinutesText = existingLimit.limitMinutes.toString()
                                                }
                                            },
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (isSelected) OrangeFlame.copy(alpha = 0.22f) else Color(0xFF1E2533),
                                        border = BorderStroke(
                                            1.dp,
                                            if (isSelected) OrangeFlame else Color.White.copy(alpha = 0.05f)
                                        )
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 8.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // App Icon
                                            AppIconView(
                                                packageName = item.packageName,
                                                appName = item.appName,
                                                modifier = Modifier
                                                    .size(32.dp)
                                                    .clip(RoundedCornerShape(7.dp))
                                            )

                                            Spacer(modifier = Modifier.width(8.dp))

                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = item.appName,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                                    fontSize = 13.sp,
                                                    color = if (isSelected) OrangeFlame else Color.White,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    Text(
                                                        text = item.category,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = Color.White.copy(alpha = 0.5f),
                                                        fontSize = 10.5.sp
                                                    )
                                                    if (todayMins > 0) {
                                                        Text(
                                                            text = "• Bugün: $todayMins dk",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = OrangeFlame,
                                                            fontSize = 10.5.sp,
                                                            fontWeight = FontWeight.SemiBold
                                                        )
                                                    }
                                                }
                                            }

                                            if (isSelected) {
                                                Icon(
                                                    imageVector = Icons.Default.CheckCircle,
                                                    contentDescription = "Seçildi",
                                                    tint = OrangeFlame,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            } else if (existingLimit != null) {
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = GreenSuccess.copy(alpha = 0.2f)
                                                ) {
                                                    Text(
                                                        text = "${existingLimit.limitMinutes} dk limit",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = GreenSuccess,
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

                    Spacer(modifier = Modifier.height(8.dp))

                    // --- 5. SELECTED APP STRIP ---
                    val candidate = selectedAppCandidate
                    if (candidate != null) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            color = OrangeFlame.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, OrangeFlame.copy(alpha = 0.35f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AppIconView(
                                    packageName = candidate.packageName,
                                    appName = candidate.appName,
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(RoundedCornerShape(5.dp))
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Seçilen: ${candidate.appName}",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = OrangeFlame,
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "Süre: ${customMinutesText.ifBlank { "0" }} dk",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 11.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                    }

                    // --- 6. QUICK MINUTE PILLS ---
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf(15, 30, 45, 60, 90, 120).forEach { mins ->
                            val isSelected = customMinutesText == mins.toString()
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { customMinutesText = mins.toString() },
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) OrangeFlame.copy(alpha = 0.28f) else Color(0xFF1E2533),
                                border = BorderStroke(1.dp, if (isSelected) OrangeFlame else Color.White.copy(alpha = 0.08f))
                            ) {
                                Box(modifier = Modifier.padding(vertical = 5.dp), contentAlignment = Alignment.Center) {
                                    Text(
                                        text = if (mins >= 60 && mins % 60 == 0) "${mins / 60} sa" else "$mins dk",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) OrangeFlame else Color.White.copy(alpha = 0.85f),
                                        fontSize = 10.5.sp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // --- 7. INPUT & SAVE ROW ---
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = customMinutesText,
                            onValueChange = { customMinutesText = it.filter { ch -> ch.isDigit() } },
                            placeholder = { Text("Dakika", fontSize = 11.sp) },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .width(95.dp)
                                .heightIn(min = 42.dp)
                        )

                        val isFormValid = selectedAppCandidate != null && (customMinutesText.toIntOrNull() ?: 0) > 0
                        Button(
                            onClick = {
                                val c = selectedAppCandidate ?: return@Button
                                val mins = customMinutesText.toIntOrNull() ?: 45
                                viewModel.setAppTimeLimit(c.packageName, c.appName, mins)
                                showAddLimitDialog = false
                                appSearchQuery = ""
                                selectedAppCandidate = null
                            },
                            enabled = isFormValid,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = OrangeFlame),
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (selectedAppCandidate == null) "Uygulama Seçin" else "Limiti Kaydet (${customMinutesText.ifBlank { "0" }} dk)",
                                fontWeight = FontWeight.Bold,
                                color = Color.Black,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun parseJsonList(jsonStr: String?, fallback: List<String>): List<String> {
    if (jsonStr.isNullOrBlank()) return fallback
    return try {
        val arr = JSONArray(jsonStr)
        val list = mutableListOf<String>()
        for (i in 0 until arr.length()) {
            list.add(arr.getString(i))
        }
        if (list.isNotEmpty()) list else fallback
    } catch (_: Exception) {
        fallback
    }
}
