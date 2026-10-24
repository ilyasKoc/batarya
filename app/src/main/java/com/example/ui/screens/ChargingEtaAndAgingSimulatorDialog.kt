package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.TrendingUp
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
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.system.LiveBatteryInfo
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.OrangeFlame
import com.example.ui.theme.PurpleNeon
import com.example.ui.theme.RedCritical
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun ChargingEtaAndAgingSimulatorDialog(
    batteryInfo: LiveBatteryInfo,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Şarj Bitiş Saati (ETA), 1: 1 Yıllık Hücre Simülatörü
    var targetPercent by remember { mutableFloatStateOf(80f) }
    var selectedSimulationMonth by remember { mutableIntStateOf(12) } // 1 - 12 ay

    val currentPercent = batteryInfo.percentage
    val isCharging = batteryInfo.isCharging
    val wattage = if (batteryInfo.chargingWattage > 0) batteryInfo.chargingWattage else 18.0f
    val capacityMah = batteryInfo.batteryCapacityMah

    // Charging Speed calculation (approx % per minute)
    val percentPerMinute = when {
        wattage >= 45f -> 1.4f
        wattage >= 25f -> 1.0f
        wattage >= 18f -> 0.75f
        wattage >= 10f -> 0.5f
        else -> 0.35f
    }

    // Calculate Minutes to 80% and 100%
    val minutesTo80 = if (currentPercent < 80) {
        ((80 - currentPercent) / percentPerMinute).toInt().coerceAtLeast(1)
    } else 0

    val minutesTo100 = if (currentPercent < 100) {
        val part1 = if (currentPercent < 80) (80 - currentPercent) / percentPerMinute else 0f
        val part2 = (100 - maxOf(currentPercent, 80)) / (percentPerMinute * 0.45f) // Trickle charging after 80%
        (part1 + part2).toInt().coerceAtLeast(2)
    } else 0

    val minutesToCustom = if (currentPercent < targetPercent) {
        val target = targetPercent.toInt()
        if (target <= 80) {
            ((target - currentPercent) / percentPerMinute).toInt().coerceAtLeast(1)
        } else {
            val part1 = if (currentPercent < 80) (80 - currentPercent) / percentPerMinute else 0f
            val part2 = (target - maxOf(currentPercent, 80)) / (percentPerMinute * 0.45f)
            (part1 + part2).toInt().coerceAtLeast(2)
        }
    } else 0

    fun calculateClockTime(addMinutes: Int): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.MINUTE, addMinutes)
        return SimpleDateFormat("HH:mm", Locale.getDefault()).format(cal.time)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .widthIn(max = 560.dp)
                .fillMaxHeight(0.92f)
                .clip(RoundedCornerShape(24.dp))
                .testTag("charging_eta_simulator_dialog"),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.2.dp, CyanAccent.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
            ) {
                // Header
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
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(CyanAccent, EmeraldPrimary)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Şarj Saati & Ömür Simülatörü",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Akıllı ETA Saati & 1 Yıllık Kapasite Projeksiyonu",
                                style = MaterialTheme.typography.bodySmall,
                                color = CyanAccent,
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

                Spacer(modifier = Modifier.height(12.dp))

                // Segmented Tab Selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        label = { Text("⏱️ Bitiş Saati (ETA)", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, maxLines = 1) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CyanAccent.copy(alpha = 0.2f),
                            selectedLabelColor = CyanAccent
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        label = { Text("📈 1 Yıllık Ömür", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, maxLines = 1) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = EmeraldPrimary.copy(alpha = 0.2f),
                            selectedLabelColor = EmeraldPrimary
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Scrollable Content
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (selectedTab == 0) {
                        // --- TAB 0: KESİN SAATLİ ŞARJ TAHMİNİ (ETA) ---
                        item {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                                border = BorderStroke(1.dp, CyanAccent.copy(alpha = 0.35f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text("Mevcut Pil Seviyesi", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text(
                                                text = "%$currentPercent",
                                                style = MaterialTheme.typography.headlineSmall,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = if (currentPercent >= 80) EmeraldPrimary else CyanAccent
                                            )
                                        }

                                        Column(horizontalAlignment = Alignment.End) {
                                            Text("Canlı Şarj Gücü", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text(
                                                text = "${String.format(Locale.US, "%.1f", wattage)} W",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = CyanAccent
                                            )
                                            Text(
                                                text = if (isCharging) "⚡ ${batteryInfo.plugType}" else "Prize Takılı Değil",
                                                fontSize = 10.sp,
                                                color = if (isCharging) EmeraldLight else OrangeFlame
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // 2 Milestones Cards: %80 (Sağlıklı) & %100 (Tam Dolu)
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // %80 Card
                                Surface(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(14.dp),
                                    color = EmeraldPrimary.copy(alpha = 0.12f),
                                    border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.45f))
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("Hedef: %80", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = EmeraldPrimary)
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = EmeraldPrimary.copy(alpha = 0.2f)
                                            ) {
                                                Text("Önerilen", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = EmeraldPrimary, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = if (currentPercent >= 80) "Ulaşıldı ✓" else calculateClockTime(minutesTo80),
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = EmeraldPrimary
                                        )
                                        Text(
                                            text = if (currentPercent >= 80) "Hücreler korumada" else "Kalan: ~$minutesTo80 dakika",
                                            fontSize = 10.5.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                // %100 Card
                                Surface(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(14.dp),
                                    color = CyanAccent.copy(alpha = 0.1f),
                                    border = BorderStroke(1.dp, CyanAccent.copy(alpha = 0.4f))
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text("Hedef: %100", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = CyanAccent)
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = if (currentPercent >= 100) "Tam Dolu ✓" else calculateClockTime(minutesTo100),
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = CyanAccent
                                        )
                                        Text(
                                            text = if (currentPercent >= 100) "Şarj tamamlandı" else "Kalan: ~$minutesTo100 dakika",
                                            fontSize = 10.5.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }

                        // Custom Target Slider
                        item {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Özel Şarj Hedefi:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                        Text(
                                            text = "%${targetPercent.toInt()} (${calculateClockTime(minutesToCustom)})",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = PurpleNeon
                                        )
                                    }
                                    Slider(
                                        value = targetPercent,
                                        onValueChange = { targetPercent = it },
                                        valueRange = 50f..100f,
                                        steps = 9,
                                        colors = SliderDefaults.colors(
                                            thumbColor = PurpleNeon,
                                            activeTrackColor = PurpleNeon
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Text(
                                        text = if (targetPercent.toInt() <= currentPercent) "Seçilen seviyeye zaten ulaşıldı." else "Hedef seviyeye yaklaşık $minutesToCustom dakika sonra ulaşılacaktır.",
                                        fontSize = 10.5.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    } else {
                        // --- TAB 1: 1 YILLIK HÜCRE ÖMRÜ SİMÜLATÖRÜ ---
                        item {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = EmeraldPrimary.copy(alpha = 0.1f),
                                border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.45f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.TrendingUp, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "12 Aylık Batarya Aşınma Projeksiyonu",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = EmeraldPrimary
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Lityum-iyon piller her gün %100'e kadar doldurulup gece prizde tutulduğunda yılda ~%16 kapasite kaybeder. VoltGuard %80 Akıllı Gece Kalkanı kullanıldığında bu kayıp sadece ~%5 ile sınırlı kalır.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 11.sp,
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                        }

                        // Month Selector Slider
                        item {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Simülasyon Süresi:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                        Text(
                                            text = "$selectedSimulationMonth Ay Sonra (~${selectedSimulationMonth * 28} Gün)",
                                            fontWeight = FontWeight.Bold,
                                            color = CyanAccent,
                                            fontSize = 12.5.sp
                                        )
                                    }
                                    Slider(
                                        value = selectedSimulationMonth.toFloat(),
                                        onValueChange = { selectedSimulationMonth = it.toInt() },
                                        valueRange = 1f..12f,
                                        steps = 10,
                                        colors = SliderDefaults.colors(
                                            thumbColor = CyanAccent,
                                            activeTrackColor = CyanAccent
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }

                        // Simulation Comparison Cards (VoltGuard vs Standard)
                        item {
                            val standardLossPercent = (selectedSimulationMonth * 1.35f)
                            val standardHealth = (100f - standardLossPercent).coerceAtLeast(70f)
                            val standardCapacity = (capacityMah * (standardHealth / 100f)).toInt()

                            val voltguardLossPercent = (selectedSimulationMonth * 0.42f)
                            val voltguardHealth = (100f - voltguardLossPercent).coerceAtLeast(92f)
                            val voltguardCapacity = (capacityMah * (voltguardHealth / 100f)).toInt()

                            val savedCapacityMah = voltguardCapacity - standardCapacity

                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                // VoltGuard Smart Mode
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = EmeraldPrimary.copy(alpha = 0.12f),
                                    border = BorderStroke(1.2.dp, EmeraldPrimary.copy(alpha = 0.6f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("🛡️ VoltGuard Korumalı Kullanım", fontWeight = FontWeight.Bold, color = EmeraldPrimary, fontSize = 12.5.sp)
                                            Text("%${String.format(Locale.US, "%.1f", voltguardHealth)} Sağlık", fontWeight = FontWeight.ExtraBold, color = EmeraldPrimary, fontSize = 14.sp)
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Kalan Kapasite: $voltguardCapacity mAh • Kayıp: -${String.format(Locale.US, "%.1f", voltguardLossPercent)}%",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        LinearProgressIndicator(
                                            progress = { voltguardHealth / 100f },
                                            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                                            color = EmeraldPrimary,
                                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                                        )
                                    }
                                }

                                // Standard Unprotected Charging
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = RedCritical.copy(alpha = 0.08f),
                                    border = BorderStroke(1.dp, RedCritical.copy(alpha = 0.35f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("⚠️ Standart Kontrolsüz Şarj", fontWeight = FontWeight.Bold, color = RedCritical, fontSize = 12.5.sp)
                                            Text("%${String.format(Locale.US, "%.1f", standardHealth)} Sağlık", fontWeight = FontWeight.Bold, color = RedCritical, fontSize = 13.5.sp)
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Kalan Kapasite: $standardCapacity mAh • Kayıp: -${String.format(Locale.US, "%.1f", standardLossPercent)}%",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        LinearProgressIndicator(
                                            progress = { standardHealth / 100f },
                                            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                                            color = RedCritical,
                                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                                        )
                                    }
                                }

                                // Total Gain Summary
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = CyanAccent.copy(alpha = 0.12f),
                                    border = BorderStroke(1.dp, CyanAccent.copy(alpha = 0.4f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "$selectedSimulationMonth ayda net kazanç: +$savedCapacityMah mAh pil kapasitesi ve +1.8 yıl ekstra batarya ömrü.",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = CyanAccent
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Bottom Action
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyanAccent,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Tamam", fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
                }
            }
        }
    }
}
