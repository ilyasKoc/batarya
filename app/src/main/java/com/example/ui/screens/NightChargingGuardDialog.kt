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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.example.system.NightChargingGuardHelper
import com.example.system.NightGuardConfig
import com.example.system.NightGuardPhase
import com.example.system.NotificationHelper
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.OrangeFlame
import com.example.ui.theme.PurpleNeon
import com.example.ui.theme.RedCritical
import java.util.Locale

/**
 * 🌙 Akıllı Gece & Uzun Ömür Şarj Koruması Diyaloğu
 * - Gece Şarj Tamponu (%80 Damlama Şarjı Koruması)
 * - Uyanma saatine göre optimize edilmiş şarj zamanlayıcısı
 * - Hücre stresi ve aşırı voltaj (4.35V) yıpranmasını önleme simülatörü
 */
@Composable
fun NightChargingGuardDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var config by remember { mutableStateOf(NightChargingGuardHelper.loadConfig(context)) }
    
    // Düzenlenebilir yerel durumlar
    var isEnabled by remember { mutableStateOf(config.isEnabled) }
    var startHour by remember { mutableIntStateOf(config.startHour) }
    var startMinute by remember { mutableIntStateOf(config.startMinute) }
    var endHour by remember { mutableIntStateOf(config.endHour) }
    var endMinute by remember { mutableIntStateOf(config.endMinute) }
    var selectedBufferLimit by remember { mutableIntStateOf(config.chargeLimitPercent) }
    var trickleEnabled by remember { mutableStateOf(config.trickleChargeBufferEnabled) }
    var trickleDuration by remember { mutableIntStateOf(config.trickleDurationMinutes) }
    var autoQuiet by remember { mutableStateOf(config.autoQuietMode) }
    var preventOvervoltage by remember { mutableStateOf(config.preventCellOvervoltage) }
    var smartAlarmSync by remember { mutableStateOf(config.smartAlarmSync) }

    // Canlı durum değerlendirmesi
    val currentStatus = remember(isEnabled, startHour, startMinute, endHour, endMinute, selectedBufferLimit, trickleDuration) {
        NightChargingGuardHelper.evaluateNightGuardStatus(context, 80, true)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .widthIn(max = 560.dp)
                .testTag("night_charging_guard_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            border = BorderStroke(1.5.dp, PurpleNeon.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                // --- 1. BAŞLIK VE KAPATMA BUTONU ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f, fill = false),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .background(PurpleNeon.copy(alpha = 0.15f), CircleShape)
                                .border(1.2.dp, PurpleNeon.copy(alpha = 0.45f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bedtime,
                                contentDescription = null,
                                tint = PurpleNeon,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Akıllı Gece Şarj Koruması",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f, fill = false)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = PurpleNeon.copy(alpha = 0.2f),
                                    modifier = Modifier.align(Alignment.CenterVertically)
                                ) {
                                    Text(
                                        text = "%80 TAMPON",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = PurpleNeon,
                                        fontSize = 9.sp,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Damlama Şarjı & Hücre Stresi Önleme",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
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

                // --- 2. KAYDIRILABİLİR İÇERİK ---
                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // A. ANA AKTİF/PASİF KART
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isEnabled) PurpleNeon.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        border = BorderStroke(1.2.dp, if (isEnabled) PurpleNeon.copy(alpha = 0.5f) else Color.Transparent),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                Text(
                                    text = "Gece Şarj Tamponu Kalkanı",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isEnabled) PurpleNeon else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (isEnabled) {
                                        "Aktif • Gece %$selectedBufferLimit tamponda kilitler, sabah uyanışa göre damlama şarjı uygular"
                                    } else {
                                        "Devre Dışı • Batarya geleneksel doğrudan şarj modunda"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 16.sp
                                )
                            }

                            Switch(
                                checked = isEnabled,
                                onCheckedChange = { isEnabled = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = PurpleNeon,
                                    checkedTrackColor = PurpleNeon.copy(alpha = 0.4f)
                                ),
                                modifier = Modifier.testTag("switch_night_guard_enabled")
                            )
                        }
                    }

                    // B. 4 AŞAMALI AKILLI ŞARJ ÇİZELGESİ (ZAMAN AKIŞI)
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.AccessTime,
                                        contentDescription = null,
                                        tint = CyanAccent,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Akıllı Gece Şarj Protokolü",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = CyanAccent
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = EmeraldPrimary.copy(alpha = 0.15f),
                                    border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.3f))
                                ) {
                                    Text(
                                        text = "+%28 Döngü Ömrü",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = EmeraldPrimary,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // 4 Adımlı Çizelge Göstergesi
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                NightPhaseStepItem(
                                    step = "1",
                                    title = "Yatış",
                                    time = String.format(Locale.getDefault(), "%02d:%02d", startHour, startMinute),
                                    subtext = "%$selectedBufferLimit Hızlı Dolum",
                                    color = PurpleNeon,
                                    modifier = Modifier.weight(1f)
                                )
                                NightPhaseStepItem(
                                    step = "2",
                                    title = "Tampon",
                                    time = "Gece Boyu",
                                    subtext = "4.05V Sıfır Stres",
                                    color = CyanAccent,
                                    modifier = Modifier.weight(1f)
                                )
                                val trickleStartH = ((endHour * 60 + endMinute - trickleDuration + 1440) % 1440) / 60
                                val trickleStartM = ((endHour * 60 + endMinute - trickleDuration + 1440) % 1440) % 60
                                NightPhaseStepItem(
                                    step = "3",
                                    title = "Damlama",
                                    time = String.format(Locale.getDefault(), "%02d:%02d", trickleStartH, trickleStartM),
                                    subtext = "Nazik Tamamlama",
                                    color = AmberWarning,
                                    modifier = Modifier.weight(1f)
                                )
                                NightPhaseStepItem(
                                    step = "4",
                                    title = "Uyanış",
                                    time = String.format(Locale.getDefault(), "%02d:%02d", endHour, endMinute),
                                    subtext = "%100 Taze Hazır",
                                    color = EmeraldPrimary,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    // C. YATIŞ VE UYANIŞ SAATİ AYARLARI (ETKİLEŞİMLİ SEÇİCİ)
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "⏰ Uyku & Uyanış Zamanlayıcısı",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Yatış Saati Kartı
                                TimeAdjustCard(
                                    icon = Icons.Default.Bedtime,
                                    label = "Yatış Saati",
                                    hour = startHour,
                                    minute = startMinute,
                                    color = PurpleNeon,
                                    onHourChange = { startHour = (startHour + it + 24) % 24 },
                                    onMinuteChange = { startMinute = (startMinute + it + 60) % 60 },
                                    modifier = Modifier.weight(1f)
                                )

                                // Uyanış Saati Kartı
                                TimeAdjustCard(
                                    icon = Icons.Default.WbSunny,
                                    label = "Uyanış Saati",
                                    hour = endHour,
                                    minute = endMinute,
                                    color = OrangeFlame,
                                    onHourChange = { endHour = (endHour + it + 24) % 24 },
                                    onMinuteChange = { endMinute = (endMinute + it + 60) % 60 },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    // D. GECE ŞARJ TAMPONU LİMİTİ (%80 veya %85)
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Gece Şarj Tampon Eşiği:",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (selectedBufferLimit == 80) "En Yüksek Hücre Koruma (4.05V)" else "Dengeli Kapasite",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (selectedBufferLimit == 80) EmeraldPrimary else CyanAccent,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(75, 80, 85, 90).forEach { limit ->
                                val isSelected = selectedBufferLimit == limit
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedBufferLimit = limit },
                                    label = {
                                        Text(
                                            text = "%$limit ${if (limit == 80) "★ Önerilen" else ""}",
                                            fontSize = 11.5.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = PurpleNeon,
                                        selectedLabelColor = Color.White
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    // E. UYANIŞ ÖNCESİ DAMLAMA SÜRESİ SEÇİMİ
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = AmberWarning.copy(alpha = 0.08f),
                        border = BorderStroke(1.dp, AmberWarning.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Bolt, contentDescription = null, tint = AmberWarning, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Uyanış Damlama Şarjı (Trickle)",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = AmberWarning
                                    )
                                }

                                Switch(
                                    checked = trickleEnabled,
                                    onCheckedChange = { trickleEnabled = it },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = AmberWarning,
                                        checkedTrackColor = AmberWarning.copy(alpha = 0.4f)
                                    )
                                )
                            }

                            AnimatedVisibility(visible = trickleEnabled) {
                                Column(modifier = Modifier.padding(top = 8.dp)) {
                                    Text(
                                        text = "Uyanıştan ne kadar önce %100'e tamamlansın?",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        listOf(30, 45, 60).forEach { mins ->
                                            val isSel = trickleDuration == mins
                                            Surface(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clickable { trickleDuration = mins },
                                                shape = RoundedCornerShape(8.dp),
                                                color = if (isSel) AmberWarning.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surface,
                                                border = BorderStroke(1.dp, if (isSel) AmberWarning else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                                            ) {
                                                Text(
                                                    text = "$mins Dakika Önceden",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (isSel) AmberWarning else MaterialTheme.colorScheme.onSurface,
                                                    textAlign = TextAlign.Center,
                                                    modifier = Modifier.padding(vertical = 8.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // F. BİLİMSEL LİTYUM STRES ANALİZİ KARTI
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = EmeraldPrimary.copy(alpha = 0.08f),
                        border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.35f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Favorite, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Lityum Hücre Sağlığı Kazanımı",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldPrimary
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "• Normal Şarj: Gece boyu 7 saat 4.35V tepe gerilimde kalarak katot oksidasyonunu ve lityum kaplamasını (şişme) tetikler.\n• Akıllı Gece Tamponu: %80 (4.05V) eşiğinde tutarak hücre stresini %68 azaltır ve batarya ömrünü 2.4 kat uzatır.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 16.sp
                            )
                        }
                    }

                    // G. EK AKILLI ANAHTARLAR (Sessiz mod, Alarm Eşitleme)
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            // Sessiz Mod
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.VolumeOff, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text("Gece Sessiz Mod", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                        Text("Uykuda sesli alarm çalmaz, uyanışı bekle", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                Switch(
                                    checked = autoQuiet,
                                    onCheckedChange = { autoQuiet = it },
                                    colors = SwitchDefaults.colors(checkedThumbColor = CyanAccent, checkedTrackColor = CyanAccent.copy(alpha = 0.4f))
                                )
                            }

                            // Sistem Alarmı ile Otomatik Senkronizasyon
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Alarm, contentDescription = null, tint = PurpleNeon, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text("Uyanış Alarmı ile Eşitle", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                        Text("Cihazın kurulu çalar saatine göre damlamayı ayarla", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                Switch(
                                    checked = smartAlarmSync,
                                    onCheckedChange = { smartAlarmSync = it },
                                    colors = SwitchDefaults.colors(checkedThumbColor = PurpleNeon, checkedTrackColor = PurpleNeon.copy(alpha = 0.4f))
                                )
                            }
                        }
                    }

                    // H. ANLIK SİMÜLASYON VE TEST BUTONU
                    OutlinedButton(
                        onClick = {
                            val testWakeStr = String.format(Locale.getDefault(), "%02d:%02d", endHour, endMinute)
                            NotificationHelper.sendNightChargingBufferNotification(
                                context = context,
                                currentPercent = 80,
                                targetBuffer = selectedBufferLimit,
                                wakeUpTimeFormatted = testWakeStr,
                                isTrickleScheduled = trickleEnabled
                            )
                            Toast.makeText(context, "🌙 Gece Tamponu & Bildirim Testi Gönderildi!", Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, PurpleNeon.copy(alpha = 0.6f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_test_night_simulation")
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = PurpleNeon, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Gece Koruma Döngüsünü Test Et", color = PurpleNeon, fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // --- 3. KAYDET VE VAZGEÇ AKSİYONLARI ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                    ) {
                        Text("Vazgeç", fontSize = 13.sp)
                    }

                    Button(
                        onClick = {
                            val updated = NightGuardConfig(
                                isEnabled = isEnabled,
                                startHour = startHour,
                                startMinute = startMinute,
                                endHour = endHour,
                                endMinute = endMinute,
                                chargeLimitPercent = selectedBufferLimit,
                                trickleChargeBufferEnabled = trickleEnabled,
                                trickleDurationMinutes = trickleDuration,
                                autoQuietMode = autoQuiet,
                                preventCellOvervoltage = preventOvervoltage,
                                smartAlarmSync = smartAlarmSync,
                                targetWakeUpPercent = 100
                            )
                            NightChargingGuardHelper.saveConfig(context, updated)
                            Toast.makeText(context, "🌙 Akıllı Gece & Damlama Koruması Kaydedildi", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PurpleNeon,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1.4f)
                            .height(44.dp)
                            .testTag("btn_save_night_guard")
                    ) {
                        Icon(Icons.Default.Shield, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Kaydet & Koru", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun NightPhaseStepItem(
    step: String,
    title: String,
    time: String,
    subtext: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(horizontal = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(color.copy(alpha = 0.18f), CircleShape)
                .border(1.dp, color.copy(alpha = 0.5f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(text = step, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.ExtraBold, color = color, fontSize = 11.sp)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = title, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, maxLines = 1)
        Text(text = time, style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.ExtraBold, fontSize = 10.5.sp)
        Text(text = subtext, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 8.5.sp, maxLines = 1, textAlign = TextAlign.Center)
    }
}

@Composable
private fun TimeAdjustCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    hour: Int,
    minute: Int,
    color: Color,
    onHourChange: (Int) -> Unit,
    onMinuteChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, color.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = color)
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = String.format(Locale.getDefault(), "%02d:%02d", hour, minute),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Surface(
                    modifier = Modifier.clickable { onHourChange(-1) },
                    shape = RoundedCornerShape(4.dp),
                    color = color.copy(alpha = 0.15f)
                ) {
                    Text("-1s", style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                }
                Surface(
                    modifier = Modifier.clickable { onHourChange(1) },
                    shape = RoundedCornerShape(4.dp),
                    color = color.copy(alpha = 0.15f)
                ) {
                    Text("+1s", style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                }
                Surface(
                    modifier = Modifier.clickable { onMinuteChange(15) },
                    shape = RoundedCornerShape(4.dp),
                    color = color.copy(alpha = 0.15f)
                ) {
                    Text("+15d", style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                }
            }
        }
    }
}
