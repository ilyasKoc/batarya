package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
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
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.BatteryStd
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiTethering
import androidx.compose.material3.Badge
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.system.AppStandbyBucketItem
import com.example.system.FullSystemTelemetry
import com.example.system.MemoryTelemetry
import com.example.system.NetworkTelemetry
import com.example.system.PowerTelemetry
import com.example.system.SystemTelemetryHelper
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.BlueLight
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.OrangeFlame
import com.example.ui.theme.RedCritical
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class TelemetrySection(val title: String, val icon: ImageVector) {
    ALL("Tümü", Icons.Default.Analytics),
    POWER("Güç & Akım", Icons.Default.ElectricBolt),
    MEMORY("RAM & Çekirdek", Icons.Default.Memory),
    NETWORK("Ağ & Radyo", Icons.Default.Wifi),
    STANDBY("Standby Kovaları", Icons.Default.Security)
}

@Composable
fun TelemetryScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var telemetryData by remember { mutableStateOf(SystemTelemetryHelper.collectTelemetry(context)) }
    var selectedSection by remember { mutableStateOf(TelemetrySection.ALL) }
    var lastUpdatedTime by remember { mutableStateOf(SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())) }
    var isAutoRefreshEnabled by remember { mutableStateOf(true) }

    // Periodic live telemetry poller (every 5 seconds in background)
    LaunchedEffect(isAutoRefreshEnabled) {
        while (isAutoRefreshEnabled) {
            val freshData = withContext(Dispatchers.IO) {
                SystemTelemetryHelper.collectTelemetry(context)
            }
            telemetryData = freshData
            lastUpdatedTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
            delay(5000)
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("telemetry_screen_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Header Card with Live Status & Manual Refresh
        item {
            TelemetryHeaderCard(
                lastUpdated = lastUpdatedTime,
                isLive = isAutoRefreshEnabled,
                onToggleLive = { isAutoRefreshEnabled = !isAutoRefreshEnabled },
                onManualRefresh = {
                    telemetryData = SystemTelemetryHelper.collectTelemetry(context)
                    lastUpdatedTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
                }
            )
        }

        // 2. Section Selector Chips
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(TelemetrySection.values()) { section ->
                    val isSelected = selectedSection == section
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedSection = section },
                        label = {
                            Text(
                                text = section.title,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = section.icon,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (isSelected) EmeraldPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = EmeraldPrimary.copy(alpha = 0.18f),
                            selectedLabelColor = EmeraldPrimary
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) EmeraldPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        )
                    )
                }
            }
        }

        // 3. Power & Electricity Telemetry
        if (selectedSection == TelemetrySection.ALL || selectedSection == TelemetrySection.POWER) {
            item {
                SectionTitle(
                    title = "⚡ Donanım Güç & Elektrik Telemetrisi",
                    subtitle = "Anlık akım, voltaj, Watt hesaplaması ve termal kısılma durumu"
                )
            }
            item {
                PowerTelemetryCard(power = telemetryData.power)
            }
        }

        // 4. Memory & Kernel Internals Telemetry
        if (selectedSection == TelemetrySection.ALL || selectedSection == TelemetrySection.MEMORY) {
            item {
                SectionTitle(
                    title = "🧠 Bellek Mimarisi & Çekirdek (RAM Internals)",
                    subtitle = "Native Heap, ART Dalvik, GPU belleği ve LMK eşik telemetrisi"
                )
            }
            item {
                MemoryTelemetryCard(memory = telemetryData.memory)
            }
        }

        // 5. Network & Radio Frequency Telemetry
        if (selectedSection == TelemetrySection.ALL || selectedSection == TelemetrySection.NETWORK) {
            item {
                SectionTitle(
                    title = "📡 Gelişmiş Ağ Protokol & Radyo Sinyali",
                    subtitle = "Frekans bandı (2.4/5/6 GHz), Wi-Fi standardı, RSSI dBm ve IP paketleri"
                )
            }
            item {
                NetworkTelemetryCard(network = telemetryData.network)
            }
        }

        // 6. Standby Buckets
        if (selectedSection == TelemetrySection.ALL || selectedSection == TelemetrySection.STANDBY) {
            item {
                SectionTitle(
                    title = "⚙️ Android Standby Buckets (Güç Öncelikleri)",
                    subtitle = "İşletim sisteminin uygulamalara atadığı arka plan kısıtlama sınıfları"
                )
            }
            item {
                StandbyBucketsOverviewCard(buckets = telemetryData.standbyBuckets)
            }
            items(telemetryData.standbyBuckets) { bucketItem ->
                AppStandbyItemCard(bucketItem = bucketItem)
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SectionTitle(title: String, subtitle: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun TelemetryHeaderCard(
    lastUpdated: String,
    isLive: Boolean,
    onToggleLive: () -> Unit,
    onManualRefresh: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("telemetry_header_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(
                            Brush.linearGradient(listOf(CyanAccent.copy(alpha = 0.2f), EmeraldPrimary.copy(alpha = 0.2f))),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Analytics,
                        contentDescription = null,
                        tint = CyanAccent,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Teknik Donanım Telemetrisi",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isLive) EmeraldPrimary.copy(alpha = 0.15f) else AmberWarning.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = if (isLive) "CANLI (3s)" else "DURAKLATILDI",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isLive) EmeraldPrimary else AmberWarning,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = "Son Güncelleme: $lastUpdated",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row {
                IconButton(onClick = onToggleLive) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = "Canlı Yayın",
                        tint = if (isLive) EmeraldPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onManualRefresh) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Yenile",
                        tint = CyanAccent
                    )
                }
            }
        }
    }
}

@Composable
private fun PowerTelemetryCard(power: PowerTelemetry) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("telemetry_power_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Power Formula Hero Box
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "HESAPLANAN GERÇEK GÜÇ (P = V × I)",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = CyanAccent
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "${power.powerWatts}",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (power.isCharging) EmeraldPrimary else OrangeFlame
                            )
                            Text(
                                text = " Watt",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 3.dp, start = 4.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "(${power.powerMilliwatts} mW)",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 3.dp)
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .background(
                                if (power.isCharging) EmeraldPrimary.copy(alpha = 0.15f) else OrangeFlame.copy(alpha = 0.15f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (power.isCharging) Icons.Default.Bolt else Icons.Default.ElectricBolt,
                            contentDescription = null,
                            tint = if (power.isCharging) EmeraldPrimary else OrangeFlame,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Grid Metrics
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricItemBox(
                    title = "Anlık Akım (Now)",
                    value = "${if (power.instantCurrentMa > 0) "+" else ""}${power.instantCurrentMa} mA",
                    icon = Icons.Default.Speed,
                    tint = if (power.instantCurrentMa >= 0) EmeraldPrimary else CyanAccent,
                    modifier = Modifier.weight(1f)
                )
                MetricItemBox(
                    title = "Ortalama Akım (Avg)",
                    value = "${if (power.averageCurrentMa > 0) "+" else ""}${power.averageCurrentMa} mA",
                    icon = Icons.Default.Timeline,
                    tint = ElectricBlue,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricItemBox(
                    title = "Voltaj Düzeyi",
                    value = "${power.voltageMv} mV (${String.format(Locale.US, "%.2f", power.voltageMv / 1000f)} V)",
                    icon = Icons.Default.ElectricBolt,
                    tint = AmberWarning,
                    modifier = Modifier.weight(1f)
                )
                MetricItemBox(
                    title = "Coulomb Sayacı",
                    value = "${power.chargeCounterMah} mAh",
                    icon = Icons.Default.BatteryChargingFull,
                    tint = EmeraldLight,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Thermal Status Bar
            val thermalColor = when (power.thermalSeverityLevel) {
                0 -> EmeraldPrimary
                1 -> BlueLight
                2 -> AmberWarning
                3 -> OrangeFlame
                else -> RedCritical
            }

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = thermalColor.copy(alpha = 0.1f),
                border = BorderStroke(1.dp, thermalColor.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Thermostat,
                        contentDescription = null,
                        tint = thermalColor,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Termal Durum & CPU Kısılma (Throttling)",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = thermalColor
                        )
                        Text(
                            text = power.thermalStatus,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Extra Info Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Kimya: ${power.batteryTechnology}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Sağlık: ${power.batteryHealth}",
                    style = MaterialTheme.typography.bodySmall,
                    color = EmeraldPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = power.chargePlugType,
                    style = MaterialTheme.typography.bodySmall,
                    color = CyanAccent,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun MemoryTelemetryCard(memory: MemoryTelemetry) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("telemetry_memory_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // RAM Progress Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Fiziksel RAM Kullanımı",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "%${memory.ramUsagePercent} (${memory.usedRamMb} MB / ${memory.totalRamMb} MB)",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = if (memory.ramUsagePercent > 85) RedCritical else CyanAccent
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { (memory.ramUsagePercent / 100f).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape),
                color = if (memory.ramUsagePercent > 85) RedCritical else CyanAccent,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                strokeCap = StrokeCap.Round
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Sub-division of RAM (Heap breakdown)
            Text(
                text = "RAM Bölümleme & Yığın Dökümü (PSS / Dalvik / Native):",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MemorySubBox(label = "Native C++ Heap", value = "${memory.nativeHeapMb} MB", color = ElectricBlue, modifier = Modifier.weight(1f))
                MemorySubBox(label = "Dalvik / ART VM", value = "${memory.dalvikHeapMb} MB", color = EmeraldPrimary, modifier = Modifier.weight(1f))
                MemorySubBox(label = "GPU / Graphics", value = "${memory.graphicsMemoryMb} MB", color = OrangeFlame, modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(12.dp))

            // LMK & System State Row
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "LMK Tetiklenme Eşiği:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${memory.thresholdMb} MB Boş RAM",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Cihaz Çalışma Süresi (Uptime):",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${memory.uptimeHours} Saat",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = CyanAccent
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Deep Sleep / Doze Durumu:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (memory.isDeviceIdleMode) "Derin Uykuda (Doze)" else "Aktif / Uyanık",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = if (memory.isDeviceIdleMode) EmeraldPrimary else AmberWarning
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MemorySubBox(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.4f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 10.sp,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun NetworkTelemetryCard(network: NetworkTelemetry) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("telemetry_network_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(CyanAccent.copy(alpha = 0.15f), CircleShape),
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
                        Text(
                            text = network.networkType,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (network.isConnected) "İnternet Erişimi Aktif" else "Bağlantı Yok",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (network.isConnected) EmeraldPrimary else RedCritical,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = CyanAccent.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, CyanAccent.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = network.wifiFrequencyBand,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = CyanAccent,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Wi-Fi RF Parameters Grid
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricItemBox(
                    title = "Bağlantı Hızı (Link)",
                    value = "${network.wifiLinkSpeedMbps} Mbps",
                    icon = Icons.Default.Speed,
                    tint = EmeraldPrimary,
                    modifier = Modifier.weight(1f)
                )
                MetricItemBox(
                    title = "Sinyal Gücü (RSSI)",
                    value = "${network.wifiRssiDbm} dBm (%${network.wifiSignalPercent})",
                    icon = Icons.Default.WifiTethering,
                    tint = CyanGlow,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricItemBox(
                    title = "Wi-Fi Standardı",
                    value = network.wifiStandard,
                    icon = Icons.Default.Security,
                    tint = ElectricBlue,
                    modifier = Modifier.weight(1f)
                )
                MetricItemBox(
                    title = "Yerel IP Adresi",
                    value = network.ipAddress,
                    icon = Icons.Default.Info,
                    tint = AmberWarning,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // IP Packets Stats
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Soket & IP Paket Telemetrisi (TrafficStats):",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Alınan (Rx) Paket Sayısı:", style = MaterialTheme.typography.bodySmall)
                        Text(text = "${String.format(Locale.US, "%,d", network.totalRxPackets)} paket", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Gönderilen (Tx) Paket Sayısı:", style = MaterialTheme.typography.bodySmall)
                        Text(text = "${String.format(Locale.US, "%,d", network.totalTxPackets)} paket", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Ortalama Paket Boyutu:", style = MaterialTheme.typography.bodySmall)
                        Text(text = "${network.avgPacketSizeBytes} Bayt / Paket", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = CyanAccent)
                    }
                }
            }
        }
    }
}

@Composable
private fun StandbyBucketsOverviewCard(buckets: List<AppStandbyBucketItem>) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("telemetry_standby_overview_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Android Standby Buckets Nedir?",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Android 9 (Pie) ve sonrasında sistem, uygulamaları kullanım sıklığına göre 5 kovaya ayırır. Nadir kullanılan uygulamaların arka plan işleri, alarmları ve internet erişimleri sınırlandırılarak batarya korunur.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AppStandbyItemCard(bucketItem: AppStandbyBucketItem) {
    val (badgeColor, badgeTextColor) = when (bucketItem.bucketCode) {
        10 -> Pair(EmeraldPrimary.copy(alpha = 0.15f), EmeraldPrimary) // ACTIVE
        20 -> Pair(CyanAccent.copy(alpha = 0.15f), CyanAccent) // WORKING_SET
        30 -> Pair(BlueLight.copy(alpha = 0.15f), ElectricBlue) // FREQUENT
        40 -> Pair(AmberWarning.copy(alpha = 0.15f), OrangeFlame) // RARE
        else -> Pair(RedCritical.copy(alpha = 0.15f), RedCritical) // RESTRICTED
    }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("app_standby_${bucketItem.packageName}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                    Text(
                        text = bucketItem.appName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Text(
                        text = bucketItem.packageName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp,
                        maxLines = 1
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = badgeColor
                ) {
                    Text(
                        text = bucketItem.restrictionLevel,
                        style = MaterialTheme.typography.labelSmall,
                        color = badgeTextColor,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Kova: ${bucketItem.bucketName}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = badgeTextColor
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "• ${bucketItem.bucketDescription}",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2
                    )
                }
            }
        }
    }
}

@Composable
private fun MetricItemBox(
    title: String,
    value: String,
    icon: ImageVector,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
        }
    }
}
