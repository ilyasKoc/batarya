package com.example.ui.screens

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.NightlightRound
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.system.LiveBatteryInfo
import com.example.system.NightChargingGuardHelper
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.OrangeFlame
import com.example.ui.theme.PurpleNeon
import com.example.ui.theme.RedCritical
import com.example.viewmodel.NightDrainReport

/**
 * BatteryAlarmsTab: Şarj dolum alarmları, aşırı sıcaklık/ısınma koruma eşikleri
 * ve gece deşarj koruyucusu (Night Guard) ayarlarını içeren sekme.
 */
@Composable
fun BatteryAlarmsTab(
    batteryInfo: LiveBatteryInfo,
    fullChargeAlarmEnabled: Boolean,
    fullChargeTargetPercent: Int,
    onToggleAlarm: (Boolean) -> Unit,
    onSetAlarmTarget: (Int) -> Unit,
    highTempAlarmEnabled: Boolean,
    highTempThresholdCelsius: Float,
    onToggleHighTempAlarm: (Boolean) -> Unit,
    onSetHighTempThreshold: (Float) -> Unit,
    onTestHighTempAlarm: () -> Unit,
    nightDrainReport: NightDrainReport,
    onOpenNightGuard: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tempColor = when {
        batteryInfo.temperatureCelsius >= highTempThresholdCelsius -> RedCritical
        batteryInfo.temperatureCelsius >= 38.0f -> OrangeFlame
        else -> EmeraldPrimary
    }

    val tempStatusText = when {
        batteryInfo.temperatureCelsius >= highTempThresholdCelsius -> "🔥 KRİTİK AŞIRI ISINMA"
        batteryInfo.temperatureCelsius >= 38.0f -> "⚠️ Yüksek Sıcaklık"
        else -> "✓ İdeal Çalışma Sıcaklığı"
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("tab_content_alarms_guard"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- 1. ŞARJ DOLUM ALARMI ---
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "ŞARJ DOLUM ALARMI",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = CyanAccent,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                )

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("charge_alarm_card"),
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
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
                                Icon(
                                    imageVector = Icons.Default.NotificationsActive,
                                    contentDescription = null,
                                    tint = CyanAccent,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Şarj Dolum Bildirimi",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Hedef seviyede sesli uyarı verir",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Switch(
                                checked = fullChargeAlarmEnabled,
                                onCheckedChange = onToggleAlarm,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = CyanAccent,
                                    checkedTrackColor = CyanAccent.copy(alpha = 0.4f)
                                ),
                                modifier = Modifier.testTag("charge_alarm_switch")
                            )
                        }

                        AnimatedVisibility(visible = fullChargeAlarmEnabled) {
                            Column(modifier = Modifier.padding(top = 14.dp)) {
                                Text(
                                    text = "Hedef Seviye Eşiği:",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    listOf(80, 85, 90, 100).forEach { target ->
                                        val isTargetSelected = fullChargeTargetPercent == target
                                        Surface(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable { onSetAlarmTarget(target) },
                                            shape = RoundedCornerShape(6.dp),
                                            color = if (isTargetSelected) CyanAccent.copy(alpha = 0.2f)
                                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                            border = BorderStroke(
                                                width = if (isTargetSelected) 1.5.dp else 1.dp,
                                                color = if (isTargetSelected) CyanAccent else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                                            )
                                        ) {
                                            Box(
                                                modifier = Modifier.padding(vertical = 8.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "%$target",
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontWeight = if (isTargetSelected) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (isTargetSelected) CyanAccent else MaterialTheme.colorScheme.onSurface
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
        }

        // --- 2. PİL SAĞLIĞI & ŞARJ SICAKLIĞI KORUMASI ---
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "PİL SAĞLIĞI & ŞARJ SICAKLIĞI ALARMI",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = OrangeFlame,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                )

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("thermal_protection_card"),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.5.dp, if (batteryInfo.temperatureCelsius >= highTempThresholdCelsius) RedCritical else MaterialTheme.colorScheme.outline.copy(alpha = 0.8f))
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
                                        .background(tempColor.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                        .border(1.dp, tempColor.copy(alpha = 0.4f), RoundedCornerShape(6.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocalFireDepartment,
                                        contentDescription = null,
                                        tint = tempColor,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Aşırı Isınma Koruması",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Şarjda sıcaklık aşımında alarm çalar",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Switch(
                                checked = highTempAlarmEnabled,
                                onCheckedChange = onToggleHighTempAlarm,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = OrangeFlame,
                                    checkedTrackColor = OrangeFlame.copy(alpha = 0.4f)
                                ),
                                modifier = Modifier.testTag("temp_alarm_switch")
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(tempColor.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                                .border(1.dp, tempColor.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Thermostat,
                                    contentDescription = null,
                                    tint = tempColor,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        text = "Anlık Şarj / Pil Sıcaklığı",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = tempStatusText,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = tempColor
                                    )
                                }
                            }

                            Text(
                                text = "${"%.1f".format(batteryInfo.temperatureCelsius)}°C",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = tempColor
                            )
                        }

                        AnimatedVisibility(visible = highTempAlarmEnabled) {
                            Column(modifier = Modifier.padding(top = 14.dp)) {
                                Text(
                                    text = "Kritik Alarm Eşiği (Varsayılan 43°C):",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    listOf(40.0f, 42.0f, 43.0f, 45.0f).forEach { threshold ->
                                        val isSelected = (highTempThresholdCelsius == threshold)
                                        Surface(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable { onSetHighTempThreshold(threshold) },
                                            shape = RoundedCornerShape(6.dp),
                                            color = if (isSelected) OrangeFlame.copy(alpha = 0.2f)
                                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                            border = BorderStroke(
                                                width = if (isSelected) 1.5.dp else 1.dp,
                                                color = if (isSelected) OrangeFlame else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                                            )
                                        ) {
                                            Box(
                                                modifier = Modifier.padding(vertical = 8.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "${threshold.toInt()}°C",
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (isSelected) OrangeFlame else MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                OutlinedButton(
                                    onClick = onTestHighTempAlarm,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(6.dp),
                                    border = BorderStroke(1.dp, OrangeFlame.copy(alpha = 0.6f))
                                ) {
                                    Icon(imageVector = Icons.Default.WarningAmber, contentDescription = null, tint = OrangeFlame)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Sıcaklık Aşımı Uyarısını Test Et",
                                        fontWeight = FontWeight.Bold,
                                        color = OrangeFlame
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- 3. AKILLI GECE & UYKU ŞARJ KORUMASI ---
        item {
            val context = LocalContext.current
            val nightStatus = remember(batteryInfo.percentage, batteryInfo.isCharging) {
                NightChargingGuardHelper.evaluateNightGuardStatus(
                    context = context,
                    batteryPercent = batteryInfo.percentage,
                    isCharging = batteryInfo.isCharging
                )
            }

            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "AKILLI GECE & UYKU ŞARJ KORUMASI",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = PurpleNeon,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                )

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenNightGuard() }
                        .testTag("night_charging_guard_card"),
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.5.dp, PurpleNeon.copy(alpha = 0.5f))
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
                                        .size(42.dp)
                                        .background(PurpleNeon.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                                        .border(1.2.dp, PurpleNeon.copy(alpha = 0.45f), RoundedCornerShape(10.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Bedtime,
                                        contentDescription = null,
                                        tint = PurpleNeon,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Gece Şarj Tamponu",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.weight(1f, fill = false)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = EmeraldPrimary.copy(alpha = 0.15f),
                                            modifier = Modifier.align(Alignment.CenterVertically)
                                        ) {
                                            Text(
                                                text = "%${nightStatus.currentBufferPercent} TAMPON",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = EmeraldPrimary,
                                                fontSize = 9.sp,
                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = "Uyanışa göre damlama şarjı ile %100 hücre stresi önleme",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = PurpleNeon
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Canlı Durum ve Bilgi Çubukları
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                color = PurpleNeon.copy(alpha = 0.12f),
                                border = BorderStroke(1.dp, PurpleNeon.copy(alpha = 0.3f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "🌙 ${nightStatus.timeRangeFormatted}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = PurpleNeon,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                color = CyanAccent.copy(alpha = 0.12f),
                                border = BorderStroke(1.dp, CyanAccent.copy(alpha = 0.3f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "⚡ 07:00 Uyanış Damlaması",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = CyanAccent,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Sağlık Özeti
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(EmeraldPrimary.copy(alpha = 0.08f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🛡️ Hücre Voltaj Koruması (4.05V)",
                                style = MaterialTheme.typography.labelSmall,
                                color = EmeraldPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "+%28 Döngü Ömrü",
                                style = MaterialTheme.typography.labelSmall,
                                color = EmeraldPrimary,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }
        }

        // --- 4. GECE / UYKU MODU TÜKETİM RAPORU ---
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "GECE / UYKU MODU TÜKETİM RAPORU",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = ElectricBlue,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                )

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("night_drain_report_card"),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.8f))
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
                                        .background(ElectricBlue.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                        .border(1.dp, ElectricBlue.copy(alpha = 0.4f), RoundedCornerShape(6.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.NightlightRound,
                                        contentDescription = null,
                                        tint = ElectricBlue,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Gece Uyku Tüketim Karnesi",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${nightDrainReport.startHour}:00 - 0${nightDrainReport.endHour}:00 (Ekran Kapalı)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = EmeraldPrimary.copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.4f))
                            ) {
                                Text(
                                    text = "-%${nightDrainReport.batteryDropPercent}",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldPrimary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = "Saatlik Tüketim",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "%${"%.1f".format(nightDrainReport.estimatedDrainPerHour)}/sa",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = ElectricBlue
                                    )
                                }
                            }

                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = "Arka Plan Uyandırma",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "${nightDrainReport.backgroundWakeups} kez",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = OrangeFlame
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Gece Arka Planda Tüketen Uygulamalar:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        nightDrainReport.stealthApps.forEach { stealth ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = stealth.appName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = stealth.details,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Text(
                                    text = "~%${"%.1f".format(stealth.estimatedDrainPercent)}",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = AmberWarning
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                                .padding(10.dp)
                        ) {
                            Text(
                                text = "💡 ${nightDrainReport.healthAdvice}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
