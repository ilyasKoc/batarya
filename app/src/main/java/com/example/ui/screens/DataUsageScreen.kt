package com.example.ui.screens

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
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Rule
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.AppUsageDataEntity
import com.example.data.entity.DataLimitConfigEntity
import com.example.system.SystemShortcuts
import com.example.ui.components.AppUsageDonutChart
import com.example.ui.components.DataUsageDualBarChart
import com.example.ui.components.DonutSlice
import com.example.ui.components.DualBarPoint
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.OrangeFlame
import com.example.ui.theme.RedCritical

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataUsageScreen(
    appUsages: List<AppUsageDataEntity>,
    dataLimitConfig: DataLimitConfigEntity,
    selectedPeriod: String,
    onPeriodChange: (String) -> Unit,
    onUpdateLimits: (Long, Long, Int, Boolean) -> Unit
) {
    val context = LocalContext.current
    var selectedSubTab by remember { mutableIntStateOf(0) }
    var showEditLimitDialog by remember { mutableStateOf(false) }

    val totalWifiBytes = appUsages.sumOf { it.wifiBytes }
    val totalMobileBytes = appUsages.sumOf { it.mobileBytes }
    val totalWifiMb = totalWifiBytes / (1024 * 1024)
    val totalMobileMb = totalMobileBytes / (1024 * 1024)

    val periodLabel = when (selectedPeriod) {
        "DAILY" -> "Bugün (Gece 00:00'dan İtibaren)"
        "WEEKLY" -> "Bu Hafta (Pazartesi'den İtibaren)"
        else -> "Bu Ay (Ayın 1'inden İtibaren)"
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("data_usage_screen_scroll"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- PERIOD SELECTOR ---
        item {
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
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CyanAccent,
                            selectedLabelColor = Color.Black
                        ),
                        modifier = Modifier.weight(1f).testTag("period_chip_$periodKey")
                    )
                }
            }
        }

        // --- NETWORK OVERVIEW TOTAL CARDS ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // WiFi Card
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = CyanAccent.copy(alpha = 0.12f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(imageVector = Icons.Default.Wifi, contentDescription = null, tint = CyanAccent)
                            Box(
                                modifier = Modifier
                                    .background(CyanAccent.copy(alpha = 0.18f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = periodLabel,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CyanAccent,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = formatDataSize(totalWifiBytes),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Wi-Fi Harcaması",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Mobile Data Card
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = EmeraldPrimary.copy(alpha = 0.12f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(imageVector = Icons.Default.CellTower, contentDescription = null, tint = EmeraldPrimary)
                            Box(
                                modifier = Modifier
                                    .background(EmeraldPrimary.copy(alpha = 0.18f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = periodLabel,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = EmeraldPrimary,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = formatDataSize(totalMobileBytes),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Mobil Veri Harcaması",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // --- SUB TABS ROW ---
        item {
            PrimaryTabRow(
                selectedTabIndex = selectedSubTab,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .testTag("data_sub_tabs")
            ) {
                Tab(
                    selected = selectedSubTab == 0,
                    onClick = { selectedSubTab = 0 },
                    text = { Text("Uygulamalar", style = MaterialTheme.typography.labelMedium, maxLines = 1) },
                    icon = { Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = selectedSubTab == 1,
                    onClick = { selectedSubTab = 1 },
                    text = { Text("Limit Ayarları", style = MaterialTheme.typography.labelMedium, maxLines = 1) },
                    icon = { Icon(Icons.Default.Rule, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = selectedSubTab == 2,
                    onClick = { selectedSubTab = 2 },
                    text = { Text("Grafikler", style = MaterialTheme.typography.labelMedium, maxLines = 1) },
                    icon = { Icon(Icons.Default.BarChart, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
            }
        }

        // --- SUB TAB 0: UYGULAMA BAZINDA VERİ & EKRAN SÜRESİ ---
        if (selectedSubTab == 0) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Uygulama Tüketimi ($periodLabel)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f, fill = false),
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${appUsages.size} Uygulama",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (appUsages.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp).fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = CyanAccent,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Kayıtlı Uygulama Tüketimi Yok",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Temsili örnek veriler temizlenmiştir. Cihazınızdaki uygulamaların gerçek veri ve ekran süresi kullanımını görüntülemek için Kullanım Erişimi iznini etkinleştirebilirsiniz.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = {
                                    SystemShortcuts.openUsageAccessSettings(context)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = Color.Black),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Kullanım İznini Etkinleştir ⚙️")
                            }
                        }
                    }
                }
            } else {
                items(appUsages) { app ->
                    AppUsageRowCard(app = app)
                }
            }
        }

        // --- SUB TAB 1: VERİ KULLANIM LİMİTLERİ & AŞIM UYARI SİSTEMİ ---
        if (selectedSubTab == 1) {
            val mobileUsedMb = totalMobileMb
            val mobileLimit = dataLimitConfig.mobileLimitMb
            val mobilePercent = if (mobileLimit > 0) ((mobileUsedMb * 100) / mobileLimit).toInt() else 0
            val isMobileOverLimit = mobilePercent >= dataLimitConfig.warningThresholdPercent

            val wifiUsedMb = totalWifiMb
            val wifiLimit = dataLimitConfig.wifiLimitMb
            val wifiPercent = if (wifiLimit > 0) ((wifiUsedMb * 100) / wifiLimit).toInt() else 0
            val isWifiOverLimit = wifiPercent >= dataLimitConfig.warningThresholdPercent

            // Over Limit Alert Banner
            if (isMobileOverLimit || isWifiOverLimit) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().testTag("quota_warning_banner"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = RedCritical.copy(alpha = 0.15f)
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, RedCritical)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = RedCritical,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "DİKKAT: Veri Limiti Eşiği Aşıldı!",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = RedCritical
                                )
                                Text(
                                    text = "Belirlenen %${dataLimitConfig.warningThresholdPercent} kotasını aştınız. Ekstra ücretlendirmeleri önlemek için mobil verinizi sınırlayabilirsiniz.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            // Mobile Limit Card
            item {
                QuotaLimitCard(
                    title = "Mobil Veri Kotası",
                    usedMb = mobileUsedMb,
                    limitMb = mobileLimit,
                    accentColor = EmeraldPrimary,
                    isExceeded = isMobileOverLimit,
                    thresholdPercent = dataLimitConfig.warningThresholdPercent
                )
            }

            // Wi-Fi Limit Card
            item {
                QuotaLimitCard(
                    title = "Wi-Fi Veri Kotası",
                    usedMb = wifiUsedMb,
                    limitMb = wifiLimit,
                    accentColor = CyanAccent,
                    isExceeded = isWifiOverLimit,
                    thresholdPercent = dataLimitConfig.warningThresholdPercent
                )
            }

            // Action Buttons
            item {
                Button(
                    onClick = { showEditLimitDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_limits_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = null, tint = Color.Black)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Limitleri Değiştir", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }

        // --- SUB TAB 2: AYRINTILI GÖRSEL RAPORLAR ---
        if (selectedSubTab == 2) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("chart_comparison_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "Wi-Fi vs Mobil Veri Karşılaştırması",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Haftalık tüketim karşılaştırma grafiği",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        val realComparisonPoints = if (totalWifiBytes > 0 || totalMobileBytes > 0) {
                            val wifiGb = (totalWifiBytes / (1024f * 1024 * 1024))
                            val mobGb = (totalMobileBytes / (1024f * 1024 * 1024))
                            listOf(
                                DualBarPoint("Aktif", wifiGb, mobGb)
                            )
                        } else {
                            emptyList()
                        }

                        if (realComparisonPoints.isNotEmpty()) {
                            DataUsageDualBarChart(items = realComparisonPoints)
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Henüz karşılaştırma yapacak veri bulunmuyor.\nCihazınızda veri tüketildikçe grafik otomatik oluşturulacaktır.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("chart_donut_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "Uygulama Payı Dağılımı",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "En çok veri tüketen uygulamaların yüzdesi",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        val colors = listOf(CyanAccent, EmeraldPrimary, OrangeFlame, ElectricBlue, AmberWarning, RedCritical)
                        val slices = appUsages.take(5).mapIndexed { i, app ->
                            val totalMb = ((app.wifiBytes + app.mobileBytes) / (1024 * 1024)).toFloat()
                            DonutSlice(
                                label = app.appName,
                                value = totalMb,
                                color = colors[i % colors.size]
                            )
                        }

                        if (slices.isNotEmpty() && (totalWifiBytes + totalMobileBytes) > 0) {
                            AppUsageDonutChart(
                                slices = slices,
                                centerTitle = formatDataSize(totalWifiBytes + totalMobileBytes),
                                centerSubtitle = "Toplam Veri"
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Pasta grafiği için kayıtlı uygulama verisi bulunmuyor.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Edit Quota Dialog
    if (showEditLimitDialog) {
        EditQuotaDialog(
            currentMobileMb = dataLimitConfig.mobileLimitMb,
            currentWifiMb = dataLimitConfig.wifiLimitMb,
            currentThreshold = dataLimitConfig.warningThresholdPercent,
            onDismiss = { showEditLimitDialog = false },
            onSave = { mobMb, wifMb, thr ->
                onUpdateLimits(mobMb, wifMb, thr, true)
                showEditLimitDialog = false
            }
        )
    }
}

@Composable
fun AppUsageRowCard(app: AppUsageDataEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val displayName = com.example.system.AppNamesHelper.cleanDisplayName(app.appName, app.packageName)

            // App Avatar Icon
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(
                        when (displayName) {
                            "YouTube" -> RedCritical.copy(alpha = 0.2f)
                            "WhatsApp" -> EmeraldPrimary.copy(alpha = 0.2f)
                            "Instagram" -> OrangeFlame.copy(alpha = 0.2f)
                            "Spotify" -> EmeraldPrimary.copy(alpha = 0.2f)
                            "Chrome" -> CyanAccent.copy(alpha = 0.2f)
                            "Midas" -> EmeraldPrimary.copy(alpha = 0.25f)
                            else -> ElectricBlue.copy(alpha = 0.2f)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = displayName.take(1),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // App name and screen time
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = displayName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Ekran: ${formatMinutes(app.screenTimeMinutes)} • ${app.category}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Wi-Fi & Mobile data usage split
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = formatDataSize(app.wifiBytes + app.mobileBytes),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = CyanAccent
                )
                Row {
                    Text(
                        text = "W: ${formatDataSize(app.wifiBytes)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = " | M: ${formatDataSize(app.mobileBytes)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = EmeraldPrimary
                    )
                }
            }
        }
    }
}

@Composable
fun QuotaLimitCard(
    title: String,
    usedMb: Long,
    limitMb: Long,
    accentColor: Color,
    isExceeded: Boolean,
    thresholdPercent: Int
) {
    val fraction = if (limitMb > 0) (usedMb.toFloat() / limitMb.toFloat()).coerceIn(0f, 1f) else 0f
    val percent = (fraction * 100).toInt()
    val remainingMb = (limitMb - usedMb).coerceAtLeast(0)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Box(
                    modifier = Modifier
                        .background(
                            if (isExceeded) RedCritical.copy(alpha = 0.2f) else accentColor.copy(alpha = 0.15f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isExceeded) "%$percent Aşıldı!" else "%$percent Kullanıldı",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isExceeded) RedCritical else accentColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Progress Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(Color.White.copy(alpha = 0.1f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction)
                        .height(10.dp)
                        .background(if (isExceeded) RedCritical else accentColor)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Harcanan: ${usedMb} MB",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Kalan: ${remainingMb} MB",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = if (remainingMb == 0L) RedCritical else MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Kota Limiti: ${limitMb} MB (Uyarı: %$thresholdPercent)",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
fun EditQuotaDialog(
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
        title = { Text("Veri Kotası Limitlerini Belirle") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Aylık/Dönemsel kotalarınızı ve uyarı eşiğinizi tanımlayın.",
                    style = MaterialTheme.typography.bodySmall
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
                    label = { Text("Uyarı Eşiği (%)") },
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
                }
            ) {
                Text("Kaydet")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("İptal")
            }
        }
    )
}

fun formatDataSize(bytes: Long): String {
    val kb = bytes / 1024.0
    val mb = kb / 1024.0
    val gb = mb / 1024.0

    return when {
        gb >= 1.0 -> "${"%.1f".format(gb)} GB"
        mb >= 1.0 -> "${"%.1f".format(mb)} MB"
        kb >= 1.0 -> "${"%.0f".format(kb)} KB"
        else -> "$bytes B"
    }
}

fun formatMinutes(minutes: Int): String {
    val h = minutes / 60
    val m = minutes % 60
    return if (h > 0) "${h}s ${m}dk" else "${m}dk"
}
