package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.NightlightRound
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VolumeUp
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
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.system.AlarmToneStyle
import com.example.system.CustomAlarmSoundHelper
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.OrangeFlame
import com.example.ui.theme.RedCritical

@Composable
fun SmartAlarmSettingsDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    var selectedTone by remember { mutableStateOf(CustomAlarmSoundHelper.getSelectedTone(context)) }
    var isPlayingPreview by remember { mutableStateOf(false) }

    var charge80Enabled by remember { mutableStateOf(CustomAlarmSoundHelper.isCharge80AlertEnabled(context)) }
    var charge100Enabled by remember { mutableStateOf(CustomAlarmSoundHelper.isCharge100AlertEnabled(context)) }
    var overheatEnabled by remember { mutableStateOf(CustomAlarmSoundHelper.isOverheatAlertEnabled(context)) }
    var overheatTemp by remember { mutableFloatStateOf(CustomAlarmSoundHelper.getOverheatThresholdTemp(context)) }

    var lowBatteryEnabled by remember { mutableStateOf(CustomAlarmSoundHelper.isLowBatteryAlertEnabled(context)) }
    var lowBatteryThreshold by remember { mutableIntStateOf(CustomAlarmSoundHelper.getLowBatteryThreshold(context)) }
    var autoUltraSaver by remember { mutableStateOf(CustomAlarmSoundHelper.isAutoUltraSaverEnabled(context)) }

    var quietHoursEnabled by remember { mutableStateOf(CustomAlarmSoundHelper.isQuietHoursEnabled(context)) }

    DisposableEffect(Unit) {
        onDispose {
            CustomAlarmSoundHelper.stopAlarmTone()
        }
    }

    Dialog(
        onDismissRequest = {
            CustomAlarmSoundHelper.stopAlarmTone()
            onDismiss()
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .widthIn(max = 540.dp)
                .testTag("smart_alarm_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
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
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(CyanAccent, ElectricBlue)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Akıllı Sesli Alarmlar",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Şarj limitleri ve termal koruma ikazları",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        }
                    }
                    IconButton(
                        onClick = {
                            CustomAlarmSoundHelper.stopAlarmTone()
                            onDismiss()
                        },
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

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // 1. Ton Seçimi ve Canlı Önizleme
                    item {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.GraphicEq,
                                            contentDescription = null,
                                            tint = CyanAccent,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Text(
                                            text = "Alarm Melodisi & Tonu",
                                            fontWeight = FontWeight.SemiBold,
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                    }

                                    Button(
                                        onClick = {
                                            if (isPlayingPreview) {
                                                CustomAlarmSoundHelper.stopAlarmTone()
                                                isPlayingPreview = false
                                            } else {
                                                isPlayingPreview = true
                                                CustomAlarmSoundHelper.playAlarmTone(context, selectedTone, 2500L)
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isPlayingPreview) RedCritical else CyanAccent,
                                            contentColor = Color.Black
                                        ),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.height(34.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isPlayingPreview) Icons.Default.Stop else Icons.Default.PlayArrow,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (isPlayingPreview) "Durdur" else "Önizle",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    AlarmToneStyle.entries.forEach { tone ->
                                        val isSelected = tone == selectedTone
                                        Surface(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    selectedTone = tone
                                                    CustomAlarmSoundHelper.setSelectedTone(context, tone)
                                                    CustomAlarmSoundHelper.playAlarmTone(context, tone, 1500L)
                                                },
                                            shape = RoundedCornerShape(10.dp),
                                            color = if (isSelected) CyanAccent.copy(alpha = 0.15f) else Color.Transparent,
                                            border = BorderStroke(
                                                width = if (isSelected) 1.5.dp else 1.dp,
                                                color = if (isSelected) CyanAccent else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                                            )
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(10.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = tone.title,
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                        color = if (isSelected) CyanAccent else MaterialTheme.colorScheme.onSurface,
                                                        fontSize = 14.sp
                                                    )
                                                    Text(
                                                        text = tone.description,
                                                        fontSize = 11.sp,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                                if (isSelected) {
                                                    Icon(
                                                        imageVector = Icons.Default.CheckCircle,
                                                        contentDescription = null,
                                                        tint = CyanAccent,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 2. Şarj Uyarıları (%80 Sağlık & %100 Dolum)
                    item {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.BatteryChargingFull,
                                        contentDescription = null,
                                        tint = EmeraldPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = "Şarj Limiti Uyarısı",
                                        fontWeight = FontWeight.SemiBold,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // %80 Batarya Koruma
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "%80 Batarya Sağlık Uyarısı",
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            text = "Li-ion hücre yaşlanmasını önlemek için %80 dolumda sesle uyarır.",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Switch(
                                        checked = charge80Enabled,
                                        onCheckedChange = {
                                            charge80Enabled = it
                                            CustomAlarmSoundHelper.setCharge80AlertEnabled(context, it)
                                        },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = EmeraldPrimary,
                                            checkedTrackColor = EmeraldPrimary.copy(alpha = 0.3f)
                                        )
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // %100 Tam Dolum
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "%100 Tam Şarj Alarmı",
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            text = "Pil tam dolduğunda şarjdan çekmeniz için bildirim ve alarm çalar.",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Switch(
                                        checked = charge100Enabled,
                                        onCheckedChange = {
                                            charge100Enabled = it
                                            CustomAlarmSoundHelper.setCharge100AlertEnabled(context, it)
                                        },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = EmeraldPrimary,
                                            checkedTrackColor = EmeraldPrimary.copy(alpha = 0.3f)
                                        )
                                    )
                                }
                            }
                        }
                    }

                    // 3. Termal Aşırı Isınma Siren Alarmı
                    item {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.LocalFireDepartment,
                                            contentDescription = null,
                                            tint = OrangeFlame,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Column {
                                            Text(
                                                text = "Aşırı Isınma Siren Alarmı",
                                                fontWeight = FontWeight.SemiBold,
                                                style = MaterialTheme.typography.bodyLarge
                                            )
                                            Text(
                                                text = "Eşik: ${overheatTemp.toInt()}°C",
                                                fontSize = 12.sp,
                                                color = OrangeFlame,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                    Switch(
                                        checked = overheatEnabled,
                                        onCheckedChange = {
                                            overheatEnabled = it
                                            CustomAlarmSoundHelper.setOverheatAlertEnabled(context, it)
                                        },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = OrangeFlame,
                                            checkedTrackColor = OrangeFlame.copy(alpha = 0.3f)
                                        )
                                    )
                                }

                                AnimatedVisibility(visible = overheatEnabled) {
                                    Column(modifier = Modifier.padding(top = 8.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            listOf(40f, 42f, 45f, 48f).forEach { temp ->
                                                FilterChip(
                                                    selected = overheatTemp.toInt() == temp.toInt(),
                                                    onClick = {
                                                        overheatTemp = temp
                                                        CustomAlarmSoundHelper.setOverheatThresholdTemp(context, temp)
                                                    },
                                                    label = { Text("${temp.toInt()}°C", fontSize = 12.sp) },
                                                    colors = FilterChipDefaults.filterChipColors(
                                                        selectedContainerColor = OrangeFlame.copy(alpha = 0.2f),
                                                        selectedLabelColor = OrangeFlame
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 4. Düşük Pil Uyarısı & Otomatik Ultra Tasarruf
                    item {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.BatteryAlert,
                                            contentDescription = null,
                                            tint = RedCritical,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Column {
                                            Text(
                                                text = "Düşük Pil Uyarısı",
                                                fontWeight = FontWeight.SemiBold,
                                                style = MaterialTheme.typography.bodyLarge
                                            )
                                            Text(
                                                text = "Kritik Eşik: %$lowBatteryThreshold",
                                                fontSize = 12.sp,
                                                color = RedCritical,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                    Switch(
                                        checked = lowBatteryEnabled,
                                        onCheckedChange = {
                                            lowBatteryEnabled = it
                                            CustomAlarmSoundHelper.setLowBatteryAlertEnabled(context, it)
                                        },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = RedCritical,
                                            checkedTrackColor = RedCritical.copy(alpha = 0.3f)
                                        )
                                    )
                                }

                                AnimatedVisibility(visible = lowBatteryEnabled) {
                                    Column(modifier = Modifier.padding(top = 10.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            listOf(10, 15, 20, 25).forEach { threshold ->
                                                FilterChip(
                                                    selected = lowBatteryThreshold == threshold,
                                                    onClick = {
                                                        lowBatteryThreshold = threshold
                                                        CustomAlarmSoundHelper.setLowBatteryThreshold(context, threshold)
                                                    },
                                                    label = { Text("%$threshold", fontSize = 12.sp) },
                                                    colors = FilterChipDefaults.filterChipColors(
                                                        selectedContainerColor = RedCritical.copy(alpha = 0.2f),
                                                        selectedLabelColor = RedCritical
                                                    )
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))

                                        // Otomatik Ultra Tasarruf Tetikleyicisi
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = "Otomatik Ultra Tasarruf",
                                                    fontWeight = FontWeight.Medium,
                                                    fontSize = 13.sp
                                                )
                                                Text(
                                                    text = "%$lowBatteryThreshold seviyesine inildiğinde Ultra Güç Modunu otomatik devreye sok.",
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            Switch(
                                                checked = autoUltraSaver,
                                                onCheckedChange = {
                                                    autoUltraSaver = it
                                                    CustomAlarmSoundHelper.setAutoUltraSaverEnabled(context, it)
                                                },
                                                colors = SwitchDefaults.colors(
                                                    checkedThumbColor = RedCritical,
                                                    checkedTrackColor = RedCritical.copy(alpha = 0.3f)
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 5. Sessiz Saatler / Gece İstisnası
                    item {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.NightlightRound,
                                        contentDescription = null,
                                        tint = CyanAccent,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Column {
                                        Text(
                                            text = "Gece Sessiz Saatler",
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            text = "23:00 - 07:00 arası kritik olmayan sesli alarmları sessize alır.",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Switch(
                                    checked = quietHoursEnabled,
                                    onCheckedChange = {
                                        quietHoursEnabled = it
                                        CustomAlarmSoundHelper.setQuietHoursEnabled(context, it)
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = CyanAccent,
                                        checkedTrackColor = CyanAccent.copy(alpha = 0.3f)
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Bottom Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            CustomAlarmSoundHelper.playAlarmTone(context, selectedTone, 3000L)
                            Toast.makeText(context, "${selectedTone.title} alarm sesi test ediliyor...", Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Alarm, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Test Çal", fontSize = 13.sp)
                    }

                    Button(
                        onClick = {
                            CustomAlarmSoundHelper.stopAlarmTone()
                            Toast.makeText(context, "Alarm ayarları kaydedildi.", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyanAccent,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                    ) {
                        Text("Tamam", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}
