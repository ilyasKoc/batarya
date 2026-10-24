package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
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

data class AppThermalItem(
    val appName: String,
    val packageName: String,
    val tempDeltaCelsius: Float,
    val powerWatts: Float,
    val category: String,
    val icon: ImageVector,
    val riskLevel: String, // "KRİTİK", "YÜKSEK", "ORTA", "DÜŞÜK"
    val heatSource: String, // CPU, GPU, Sensör, Modem vb.
    val recommendation: String
)

@Composable
fun AppThermalIndexDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val cooledApps = remember { mutableStateListOf<String>() }
    var selectedFilter by remember { mutableIntStateOf(0) } // 0 = Tümü, 1 = Yüksek/Kritik, 2 = Oyun & Medya

    val thermalApps = remember {
        listOf(
            AppThermalItem(
                appName = "Kamera & 4K Video",
                packageName = "com.android.camera",
                tempDeltaCelsius = 5.2f,
                powerWatts = 4.8f,
                category = "Sensör & Video",
                icon = Icons.Default.CameraAlt,
                riskLevel = "KRİTİK",
                heatSource = "Kamera Sensörü + ISP + GPU",
                recommendation = "4K 60FPS video kaydı ve yapay zeka sahne işleme sensörde en yüksek ısıyı üretir."
            ),
            AppThermalItem(
                appName = "3D Oyun Motoru",
                packageName = "com.epicgames.portal",
                tempDeltaCelsius = 4.6f,
                powerWatts = 4.2f,
                category = "Oyun / Grafik",
                icon = Icons.Default.Gamepad,
                riskLevel = "YÜKSEK",
                heatSource = "GPU Çekirdekleri & Yüksek FPS",
                recommendation = "Ağır 3D grafik motorları grafik yongasını %90+ yük altında tutarak gövdeyi ısıtır."
            ),
            AppThermalItem(
                appName = "Instagram & Video Akışı",
                packageName = "com.instagram.android",
                tempDeltaCelsius = 3.1f,
                powerWatts = 2.6f,
                category = "Sosyal Medya",
                icon = Icons.Default.Share,
                riskLevel = "ORTA",
                heatSource = "Video Kod Çözücü & 5G Ağ",
                recommendation = "Sürekli kısa video oynatma ve önbellekleme işlemciyi ılık seviyede tutar."
            ),
            AppThermalItem(
                appName = "YouTube Video Akışı",
                packageName = "com.google.android.youtube",
                tempDeltaCelsius = 2.2f,
                powerWatts = 1.9f,
                category = "Video & Medya",
                icon = Icons.Default.PlayCircle,
                riskLevel = "DÜŞÜK",
                heatSource = "Ekran Parlaklığı & Wi-Fi",
                recommendation = "Donanımsal video çözücü sayesinde ısı artışı makul ve kontrollü düzeydedir."
            ),
            AppThermalItem(
                appName = "Chrome Web Tarayıcı",
                packageName = "com.android.chrome",
                tempDeltaCelsius = 1.7f,
                powerWatts = 1.4f,
                category = "Tarayıcı & Web",
                icon = Icons.Default.Public,
                riskLevel = "DÜŞÜK",
                heatSource = "V8 JavaScript & Sekmeler",
                recommendation = "Çok sayıda açık sekme ve animasyonlu scriptler arka planda hafif ısı üretir."
            )
        )
    }

    val filteredApps = remember(thermalApps, selectedFilter) {
        when (selectedFilter) {
            1 -> thermalApps.filter { it.riskLevel == "KRİTİK" || it.riskLevel == "YÜKSEK" }
            2 -> thermalApps.filter { it.category.contains("Oyun") || it.category.contains("Video") || it.category.contains("Medya") }
            else -> thermalApps
        }
    }

    fun coolAllApps() {
        thermalApps.forEach { app ->
            if (!cooledApps.contains(app.packageName)) {
                cooledApps.add(app.packageName)
            }
        }
        Toast.makeText(context, "✓ Tüm uygulamaların termal sınırları optimize edildi!", Toast.LENGTH_SHORT).show()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .widthIn(max = 560.dp)
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
                .testTag("app_thermal_index_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            border = BorderStroke(1.2.dp, OrangeFlame.copy(alpha = 0.55f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // 1. Header (İkon, Başlık ve Kapat Butonu)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(OrangeFlame, RedCritical)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Uygulama Isı Endeksi",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Donanım Isı Katkısı & Termal Harita",
                                style = MaterialTheme.typography.bodySmall,
                                color = OrangeFlame,
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

                Spacer(modifier = Modifier.height(10.dp))

                // 2. Üst Açıklama & Durum Kartı (Net & Anlaşılır)
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    border = BorderStroke(1.dp, OrangeFlame.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "Termal Isı Endeksi Nedir?",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = OrangeFlame
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Uygulamaların CPU, GPU ve kamera sensörlerini çalıştırırken bataryaya eklediği net sıcaklık payını (+°C) gösterir.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // En Çok Isıtan
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.75f),
                                border = BorderStroke(0.8.dp, RedCritical.copy(alpha = 0.35f))
                            ) {
                                Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)) {
                                    Text("En Yüksek Isı Yükü", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("Kamera (+5.2°C)", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = RedCritical, maxLines = 1)
                                }
                            }
                            // Soğutma Durumu
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.75f),
                                border = BorderStroke(0.8.dp, EmeraldPrimary.copy(alpha = 0.35f))
                            ) {
                                Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)) {
                                    Text("Termal Sınır Durumu", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("${cooledApps.size}/${thermalApps.size} Optimize", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = EmeraldPrimary, maxLines = 1)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 3. Filtreleme Çipleri
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = selectedFilter == 0,
                        onClick = { selectedFilter = 0 },
                        label = { Text("Tümü (${thermalApps.size})", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = OrangeFlame.copy(alpha = 0.2f),
                            selectedLabelColor = OrangeFlame
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = selectedFilter == 1,
                        onClick = { selectedFilter = 1 },
                        label = { Text("Kritik / Yüksek", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = RedCritical.copy(alpha = 0.2f),
                            selectedLabelColor = RedCritical
                        ),
                        modifier = Modifier.weight(1.1f)
                    )
                    FilterChip(
                        selected = selectedFilter == 2,
                        onClick = { selectedFilter = 2 },
                        label = { Text("Oyun & Medya", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CyanAccent.copy(alpha = 0.2f),
                            selectedLabelColor = CyanAccent
                        ),
                        modifier = Modifier.weight(1.1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 4. Uygulama Listesi (weight(1f) ile her boyuttaki ekrana tam uyumlu, taşmayan liste)
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredApps) { item ->
                        val isCooled = cooledApps.contains(item.packageName)
                        val badgeColor = when (item.riskLevel) {
                            "KRİTİK" -> RedCritical
                            "YÜKSEK" -> OrangeFlame
                            "ORTA" -> AmberWarning
                            else -> EmeraldPrimary
                        }
                        val progressFraction = (item.tempDeltaCelsius / 6.0f).coerceIn(0.1f, 1f)

                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (isCooled) EmeraldPrimary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            border = BorderStroke(1.dp, if (isCooled) EmeraldPrimary.copy(alpha = 0.45f) else badgeColor.copy(alpha = 0.35f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                // Üst Satır: İkon + İsim + Sıcaklık Rozeti
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
                                                .background(badgeColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = item.icon,
                                                contentDescription = null,
                                                tint = badgeColor,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = item.appName,
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = "${item.category} • ~${item.powerWatts}W Güç",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontSize = 10.5.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    // Sıcaklık Rozeti
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (isCooled) EmeraldPrimary.copy(alpha = 0.18f) else badgeColor.copy(alpha = 0.18f),
                                        border = BorderStroke(0.8.dp, if (isCooled) EmeraldPrimary.copy(alpha = 0.4f) else badgeColor.copy(alpha = 0.4f))
                                    ) {
                                        val isFahrenheit = com.example.system.CustomAlarmSoundHelper.isFahrenheit(context)
                                        val deltaStr = if (isFahrenheit) {
                                            "+${String.format(java.util.Locale.US, "%.1f", item.tempDeltaCelsius * 1.8f)}°F"
                                        } else {
                                            "+${item.tempDeltaCelsius}°C"
                                        }
                                        Text(
                                            text = if (isCooled) "Optimize Edildi ✓" else "$deltaStr Isı",
                                            fontWeight = FontWeight.ExtraBold,
                                            color = if (isCooled) EmeraldPrimary else badgeColor,
                                            fontSize = 11.sp,
                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Termal Isı Çubuğu (Görsel Isı Seviyesi)
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "Isı Odağı: ${item.heatSource}",
                                            fontSize = 10.sp,
                                            color = if (isCooled) EmeraldLight else OrangeFlame,
                                            fontWeight = FontWeight.SemiBold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f, fill = false)
                                        )
                                        Text(
                                            text = item.riskLevel,
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = badgeColor
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(3.dp))
                                    LinearProgressIndicator(
                                        progress = { if (isCooled) 0.15f else progressFraction },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(5.dp)
                                            .clip(RoundedCornerShape(3.dp)),
                                        color = if (isCooled) EmeraldPrimary else badgeColor,
                                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                // Tavsiye Kutucuğu
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "💡 ${item.recommendation}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 10.5.sp,
                                        lineHeight = 15.sp,
                                        modifier = Modifier.padding(6.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // TERMAL LİMİT UYGULA BUTONU VEYA AKTİF DURUM ROZETİ (Geniş, Tam Görünür, Asla Kesilmeyen Tasarım)
                                if (!isCooled) {
                                    Button(
                                        onClick = {
                                            cooledApps.add(item.packageName)
                                            Toast.makeText(context, "${item.appName} termal sınırı aktif edildi. Isı düşürülüyor...", Toast.LENGTH_SHORT).show()
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = CyanAccent.copy(alpha = 0.15f),
                                            contentColor = CyanAccent
                                        ),
                                        border = BorderStroke(1.2.dp, CyanAccent.copy(alpha = 0.8f)),
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .heightIn(min = 38.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.AcUnit,
                                                contentDescription = null,
                                                tint = CyanAccent,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "Termal Limit Uygula & Soğut",
                                                fontSize = 12.sp,
                                                color = CyanAccent,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                } else {
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = EmeraldPrimary.copy(alpha = 0.12f),
                                        border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.45f)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = EmeraldPrimary,
                                                modifier = Modifier.size(15.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "Termal Sınır Devrede (Maksimum Isı Engellendi)",
                                                fontSize = 11.5.sp,
                                                color = EmeraldPrimary,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 5. Alt Butonlar (Tümünü Soğut & Kapat) - Daima Ekranda Sabit ve Görünür
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (cooledApps.size < thermalApps.size) {
                        OutlinedButton(
                            onClick = { coolAllApps() },
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.2.dp, OrangeFlame),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AcUnit,
                                contentDescription = null,
                                tint = OrangeFlame,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Tümünü Soğut",
                                color = OrangeFlame,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = OrangeFlame,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                    ) {
                        Text(
                            text = "Tamam",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp
                        )
                    }
                }
            }
        }
    }
}
