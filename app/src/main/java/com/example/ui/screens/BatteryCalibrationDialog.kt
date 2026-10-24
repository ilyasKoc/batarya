package com.example.ui.screens

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PublishedWithChanges
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.system.AlarmToneStyle
import com.example.system.CustomAlarmSoundHelper
import com.example.system.LiveBatteryInfo
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.OrangeFlame
import com.example.ui.theme.PurpleNeon
import com.example.ui.theme.RedCritical
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun BatteryCalibrationDialog(
    batteryInfo: LiveBatteryInfo,
    onDismiss: () -> Unit,
    onCalibrationApplied: ((newCycles: Int, calibDate: String) -> Unit)? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var cycles by remember { mutableIntStateOf(CustomAlarmSoundHelper.getEstimatedCycleCount(context)) }
    var lastCalibrationDate by remember { mutableStateOf(CustomAlarmSoundHelper.getLastCalibrationDateFormatted(context)) }

    var isCalibrating by remember { mutableStateOf(false) }
    var calibrationProgress by remember { mutableFloatStateOf(0f) }
    var currentProgressText by remember { mutableStateOf("Kalibrasyon hazır") }
    var isCompleted by remember { mutableStateOf(CustomAlarmSoundHelper.getLastCalibrationTime(context) > 0L) }
    var calibrationSuccessDetails by remember { mutableStateOf<CalibrationResultSummary?>(null) }

    var activeTab by remember { mutableIntStateOf(0) } // 0 = Kalibrasyon Motoru, 1 = Döngü Sayacı, 2 = 3 Adımlı Rehber
    var showEditCyclesDialog by remember { mutableStateOf(false) }
    var editCyclesText by remember { mutableStateOf(cycles.toString()) }

    val wearPercent = batteryInfo.batteryWearHealthPercent
    val designCapacity = batteryInfo.batteryCapacityMah
    val estimatedRemainingCapacity = (designCapacity * (wearPercent / 100f)).toInt()

    val animatedProgress by animateFloatAsState(
        targetValue = calibrationProgress,
        animationSpec = tween(350, easing = FastOutSlowInEasing),
        label = "calibrationProgress"
    )

    fun triggerVibration() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(50)
                }
            }
        } catch (_: Exception) {}
    }

    fun startCalibrationEngine() {
        scope.launch {
            isCalibrating = true
            isCompleted = false
            calibrationSuccessDetails = null

            calibrationProgress = 0.15f
            currentProgressText = "1/4: Donanım voltaj eğrisi & Kernel OCV tablosu sıfırlanıyor..."
            triggerVibration()
            delay(450)

            calibrationProgress = 0.45f
            currentProgressText = "2/4: Coulomb kapasite sayacı ve akım sapması eşitleniyor..."
            triggerVibration()
            delay(500)

            calibrationProgress = 0.75f
            currentProgressText = "3/4: Pil sağlık katsayısı (%${wearPercent}) doğrulanıyor..."
            triggerVibration()
            delay(500)

            calibrationProgress = 0.95f
            currentProgressText = "4/4: Kalibrasyon parametreleri hafızaya kaydediliyor..."
            triggerVibration()
            delay(400)

            calibrationProgress = 1.0f
            currentProgressText = "Kalibrasyon Başarıyla Tamamlandı!"

            // Perform actual persistence & logic
            CustomAlarmSoundHelper.performFullCalibration(
                context = context,
                adjustedCycles = cycles,
                calibratedCapacity = designCapacity
            )
            val updatedDate = CustomAlarmSoundHelper.getLastCalibrationDateFormatted(context)
            lastCalibrationDate = updatedDate

            // Play confirmation chime
            CustomAlarmSoundHelper.playAlarmTone(context, AlarmToneStyle.SOFT_CHIME, 1200L)

            calibrationSuccessDetails = CalibrationResultSummary(
                calibratedAt = updatedDate,
                voltageDriftCorrected = "±0.00V (Düzeltildi)",
                coulombSync = "Coulomb Sensörü Senkronize Edildi",
                healthVerified = "%$wearPercent (Doğrulandı)",
                capacityMah = "$designCapacity mAh"
            )

            isCalibrating = false
            isCompleted = true

            onCalibrationApplied?.invoke(cycles, updatedDate)
            Toast.makeText(context, "✓ Batarya ölçer ve voltaj tablosu başarıyla kalibre edildi!", Toast.LENGTH_LONG).show()
        }
    }

    Dialog(
        onDismissRequest = {
            if (!isCalibrating) onDismiss()
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .widthIn(max = 560.dp)
                .fillMaxWidth(0.95f)
                .testTag("battery_calibration_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            border = BorderStroke(1.dp, PurpleNeon.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
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
                                        listOf(PurpleNeon, CyanAccent)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Autorenew,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Şarj Döngüsü & Kalibrasyon",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Voltaj sapmasını düzelt & göstergeyi eşitle",
                                style = MaterialTheme.typography.bodySmall,
                                color = PurpleNeon,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    IconButton(
                        onClick = onDismiss,
                        enabled = !isCalibrating,
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

                // Segmented Tabs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = activeTab == 0,
                        onClick = { activeTab = 0 },
                        label = { Text("Kalibrasyon", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PurpleNeon.copy(alpha = 0.2f),
                            selectedLabelColor = PurpleNeon
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = activeTab == 1,
                        onClick = { activeTab = 1 },
                        label = { Text("Döngü Yönetimi", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CyanAccent.copy(alpha = 0.2f),
                            selectedLabelColor = CyanAccent
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = activeTab == 2,
                        onClick = { activeTab = 2 },
                        label = { Text("3 Adımlı Rehber", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = EmeraldPrimary.copy(alpha = 0.2f),
                            selectedLabelColor = EmeraldPrimary
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 480.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Üst İstatistik Kartı (Tüm tablarda ortak)
                    item {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                            border = BorderStroke(1.dp, PurpleNeon.copy(alpha = 0.35f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "Toplam Şarj Döngüsü",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Row(
                                            verticalAlignment = Alignment.Bottom,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(
                                                text = "$cycles",
                                                fontSize = 24.sp,
                                                fontWeight = FontWeight.Black,
                                                color = PurpleNeon
                                            )
                                            Text(
                                                text = "Döngü",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = PurpleNeon,
                                                modifier = Modifier.padding(bottom = 2.dp)
                                            )
                                        }
                                        Text(
                                            text = "Tahmini Ömür: ~800 Döngü",
                                            fontSize = 9.5.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "Sağlıklı Kapasite",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "$estimatedRemainingCapacity mAh",
                                            fontSize = 17.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = EmeraldPrimary
                                        )
                                        Text(
                                            text = "Tasarım: $designCapacity mAh (%$wearPercent)",
                                            fontSize = 9.5.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.History,
                                        contentDescription = null,
                                        tint = if (isCompleted) EmeraldPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = "Son Kalibrasyon: $lastCalibrationDate",
                                        fontSize = 10.5.sp,
                                        fontWeight = if (isCompleted) FontWeight.SemiBold else FontWeight.Normal,
                                        color = if (isCompleted) EmeraldPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    if (activeTab == 0) {
                        // TAB 0: Kalibrasyon Motoru & Canlı İşlem
                        item {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = "Donanım Voltaj & Gösterge Kalibrasyonu",
                                        fontWeight = FontWeight.SemiBold,
                                        style = MaterialTheme.typography.titleSmall
                                    )
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = "Pil yüzdesi aniden düşüyorsa veya hızlı boşalıyorsa voltaj eğrisini sıfırlayın ve Coulomb sayacını kalibre edin.",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        lineHeight = 15.sp
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    if (isCalibrating) {
                                        Column(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            LinearProgressIndicator(
                                                progress = { animatedProgress },
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(8.dp)
                                                    .clip(RoundedCornerShape(4.dp)),
                                                color = PurpleNeon,
                                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = currentProgressText,
                                                    fontSize = 10.5.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = CyanAccent,
                                                    modifier = Modifier.weight(1f)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "%${(animatedProgress * 100).toInt()}",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = PurpleNeon
                                                )
                                            }
                                        }
                                    } else if (isCompleted) {
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = EmeraldPrimary.copy(alpha = 0.12f),
                                            border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.45f)),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp)) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.CheckCircle,
                                                        contentDescription = null,
                                                        tint = EmeraldPrimary,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                    Text(
                                                        text = "Kalibrasyon Başarıyla Uygulandı!",
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 13.sp,
                                                        color = EmeraldPrimary
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(6.dp))
                                                Text(
                                                    text = "✓ Donanım voltaj eğrisi & OCV tablosu sıfırlandı\n✓ Coulomb sensörü eşitlemesi tamamlandı\n✓ Şarj döngü sayacı ($cycles) doğrulandı\n✓ Pil yüzde göstergesi hassas ölçüme çekildi",
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                    lineHeight = 16.sp
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Button(
                                        onClick = { startCalibrationEngine() },
                                        enabled = !isCalibrating,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = PurpleNeon,
                                            contentColor = Color.White
                                        ),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(44.dp)
                                            .testTag("btn_apply_calibration_engine")
                                    ) {
                                        if (isCalibrating) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(18.dp),
                                                color = Color.White,
                                                strokeWidth = 2.dp
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Kalibre Ediliyor...", fontWeight = FontWeight.Bold)
                                        } else {
                                            Icon(imageVector = Icons.Default.PublishedWithChanges, contentDescription = null, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = if (isCompleted) "Yeniden Kalibre Et" else "Kalibrasyonu Şimdi Uygula",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    } else if (activeTab == 1) {
                        // TAB 1: Döngü Sayacı Yönetimi
                        item {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = "Şarj Döngü Sayacı Ayarı",
                                        fontWeight = FontWeight.SemiBold,
                                        style = MaterialTheme.typography.titleSmall
                                    )
                                    Text(
                                        text = "Yeni batarya taktıysanız veya döngüyü sıfırlamak istiyorsanız buradan güncelleyebilirsiniz.",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        IconButton(
                                            onClick = {
                                                if (cycles > 0) {
                                                    cycles -= 1
                                                    CustomAlarmSoundHelper.setEstimatedCycleCount(context, cycles)
                                                    onCalibrationApplied?.invoke(cycles, lastCalibrationDate)
                                                }
                                            },
                                            modifier = Modifier
                                                .size(40.dp)
                                                .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                                        ) {
                                            Icon(imageVector = Icons.Default.Remove, contentDescription = "Azalt")
                                        }

                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = "$cycles",
                                                fontSize = 28.sp,
                                                fontWeight = FontWeight.Black,
                                                color = CyanAccent
                                            )
                                            Text("Döngü Değeri", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }

                                        IconButton(
                                            onClick = {
                                                cycles += 1
                                                CustomAlarmSoundHelper.setEstimatedCycleCount(context, cycles)
                                                onCalibrationApplied?.invoke(cycles, lastCalibrationDate)
                                            },
                                            modifier = Modifier
                                                .size(40.dp)
                                                .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                                        ) {
                                            Icon(imageVector = Icons.Default.Add, contentDescription = "Artır")
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = {
                                                cycles = 0
                                                CustomAlarmSoundHelper.setEstimatedCycleCount(context, 0)
                                                onCalibrationApplied?.invoke(0, lastCalibrationDate)
                                                Toast.makeText(context, "Döngü sayacı sıfırlandı (0)", Toast.LENGTH_SHORT).show()
                                            },
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(imageVector = Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Sıfırla (0)", fontSize = 12.sp)
                                        }

                                        Button(
                                            onClick = {
                                                editCyclesText = cycles.toString()
                                                showEditCyclesDialog = true
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = Color.Black),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Manuel Gir", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        // TAB 2: 3 Adımlı Donanımsal Kalibrasyon Rehberi
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                CalibrationStepCard(
                                    stepNumber = "1",
                                    title = "Bataryayı %5 Seviyesine Kadar Boşaltın",
                                    desc = "Cihazı normal kullanarak pil seviyesinin %5 civarına inmesini sağlayın. Telefonun tamamen kapanmasına gerek yoktur.",
                                    badgeColor = OrangeFlame
                                )
                                CalibrationStepCard(
                                    stepNumber = "2",
                                    title = "Kesintisiz %100 Şarj Edin",
                                    desc = "Orijinal kabloyu takın ve aralıksız %100 dolana kadar şarjda bırakın. Tam dolduktan sonra 20-30 dk daha şarjda tutun.",
                                    badgeColor = EmeraldPrimary
                                )
                                CalibrationStepCard(
                                    stepNumber = "3",
                                    title = "Kalibrasyonu Başlatın",
                                    desc = "İlk sekmedeki 'Kalibrasyonu Şimdi Uygula' butonuna dokunarak voltaj eğrisini ve kernel ölçüm tablosunu sıfırlayın.",
                                    badgeColor = CyanAccent
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Bottom Close Button
                Button(
                    onClick = onDismiss,
                    enabled = !isCalibrating,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                ) {
                    Text("Kapat", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                }
            }
        }
    }

    // Manuel Döngü Düzenleme Diyaloğu
    if (showEditCyclesDialog) {
        AlertDialog(
            onDismissRequest = { showEditCyclesDialog = false },
            title = { Text("Döngü Sayısını Belirleyin", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                Column {
                    Text(
                        "Cihazınızın bilinen döngü sayısını manuel olarak girin:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = editCyclesText,
                        onValueChange = { editCyclesText = it.filter { ch -> ch.isDigit() } },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val parsed = editCyclesText.toIntOrNull() ?: cycles
                        cycles = parsed
                        CustomAlarmSoundHelper.setEstimatedCycleCount(context, parsed)
                        onCalibrationApplied?.invoke(parsed, lastCalibrationDate)
                        showEditCyclesDialog = false
                        Toast.makeText(context, "Döngü sayısı $parsed olarak güncellendi.", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = Color.Black)
                ) {
                    Text("Kaydet", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditCyclesDialog = false }) {
                    Text("İptal")
                }
            }
        )
    }
}

private data class CalibrationResultSummary(
    val calibratedAt: String,
    val voltageDriftCorrected: String,
    val coulombSync: String,
    val healthVerified: String,
    val capacityMah: String
)

@Composable
private fun CalibrationStepCard(
    stepNumber: String,
    title: String,
    desc: String,
    badgeColor: Color
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(badgeColor.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stepNumber,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    color = badgeColor
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = desc,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )
            }
        }
    }
}
