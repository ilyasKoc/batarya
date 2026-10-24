package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.system.LiveBatteryInfo
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.OrangeFlame
import com.example.ui.theme.PurpleNeon
import java.util.Locale

@Composable
fun ChargingAnimationOverlay(
    batteryInfo: LiveBatteryInfo,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        val infiniteTransition = rememberInfiniteTransition(label = "charging_waves")

        // Pulsating glow animation
        val pulseScale by infiniteTransition.animateFloat(
            initialValue = 0.96f,
            targetValue = 1.04f,
            animationSpec = infiniteRepeatable(
                animation = tween(1400, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulse_scale"
        )

        val rotationAngle by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(7000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "rotation_glow"
        )

        val wattage = if (batteryInfo.chargingWattage > 0) batteryInfo.chargingWattage else 18.5f
        val currentMa = if (batteryInfo.currentNowMa > 0) batteryInfo.currentNowMa else 2850
        val voltage = if (batteryInfo.voltageMv > 0) batteryInfo.voltageMv / 1000f else 4.15f

        // Charging stage determination
        val stageIndex = when {
            batteryInfo.percentage < 60 -> 1
            batteryInfo.percentage < 85 -> 2
            else -> 3
        }

        val (stageTitle, stageColor, stageDesc, stageBadge) = when (stageIndex) {
            1 -> Quadruple(
                "Maksimum Hızlı Akım Evresi",
                CyanAccent,
                "Lityum hücrelere maksimum amperaj aktarılıyor (%0-%60)",
                "1. Evre • Hızlı Akım"
            )
            2 -> Quadruple(
                "Sabit Voltaj & Dengeleme Evresi",
                EmeraldPrimary,
                "Hücre voltajı dengeleniyor, aşırı ısınma önleniyor (%60-%85)",
                "2. Evre • Dengeleme"
            )
            else -> Quadruple(
                "Akıllı Damlama & Hücre Koruma",
                PurpleNeon,
                "Hücre doyumunu korumak için akım azaltıldı (%85-%100)",
                "3. Evre • Damlama"
            )
        }

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF070B11))
                .statusBarsPadding()
                .navigationBarsPadding()
                .testTag("charging_animation_overlay"),
            contentAlignment = Alignment.Center
        ) {
            val availableHeight = maxHeight
            val availableWidth = maxWidth

            // Responsive sizing based on device height
            val isSmallScreen = availableHeight < 640.dp
            val isMediumScreen = availableHeight in 640.dp..760.dp

            val circleBoxSize = when {
                isSmallScreen -> 150.dp
                isMediumScreen -> 185.dp
                else -> 210.dp
            }
            val outerRingSize = circleBoxSize * 0.96f
            val arcSize = circleBoxSize * 0.82f

            val horizontalPad = if (availableWidth < 360.dp) 12.dp else 16.dp
            val verticalPad = if (isSmallScreen) 8.dp else 14.dp

            // Background Ambient Glow
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2f, size.height * 0.32f)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            stageColor.copy(alpha = 0.14f),
                            Color.Transparent
                        ),
                        center = center,
                        radius = size.width * 0.75f
                    ),
                    center = center,
                    radius = size.width * 0.75f
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = horizontalPad, vertical = verticalPad)
                    .widthIn(max = 520.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(if (isSmallScreen) 8.dp else 12.dp)
            ) {
                // 1. Top Bar with Plug Type & Close Button
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
                                .size(34.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(stageColor.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ElectricBolt,
                                contentDescription = null,
                                tint = stageColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Canlı Donanım Şarjı",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${batteryInfo.plugType} • ${batteryInfo.chargeSpeedCategory}",
                                style = MaterialTheme.typography.labelSmall,
                                color = stageColor,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color.White.copy(alpha = 0.12f), CircleShape)
                            .testTag("close_charging_overlay_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Kapat",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // 2. Center Dynamic Animation Ring
                Box(
                    modifier = Modifier
                        .size(circleBoxSize)
                        .padding(vertical = if (isSmallScreen) 2.dp else 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Outer Pulsating Neon Ring
                    Canvas(modifier = Modifier.size(outerRingSize * pulseScale)) {
                        drawCircle(
                            brush = Brush.sweepGradient(
                                listOf(
                                    stageColor.copy(alpha = 0.15f),
                                    CyanAccent.copy(alpha = 0.7f),
                                    stageColor,
                                    PurpleNeon.copy(alpha = 0.35f),
                                    stageColor.copy(alpha = 0.15f)
                                )
                            ),
                            style = Stroke(width = if (isSmallScreen) 3.5.dp.toPx() else 4.5.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }

                    // Middle Progress Arc
                    Canvas(modifier = Modifier.size(arcSize)) {
                        val sweep = (batteryInfo.percentage.coerceIn(0, 100) / 100f) * 360f
                        drawArc(
                            color = Color.White.copy(alpha = 0.08f),
                            startAngle = -90f,
                            sweepAngle = 360f,
                            useCenter = false,
                            style = Stroke(width = if (isSmallScreen) 6.5.dp.toPx() else 8.dp.toPx())
                        )
                        drawArc(
                            brush = Brush.sweepGradient(
                                listOf(
                                    stageColor,
                                    CyanAccent,
                                    EmeraldPrimary,
                                    stageColor
                                )
                            ),
                            startAngle = -90f,
                            sweepAngle = sweep,
                            useCenter = false,
                            style = Stroke(width = if (isSmallScreen) 6.5.dp.toPx() else 8.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }

                    // Center Numerical Content
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = null,
                            tint = stageColor,
                            modifier = Modifier.size(if (isSmallScreen) 20.dp else 26.dp)
                        )
                        Text(
                            text = "%${batteryInfo.percentage}",
                            fontSize = if (isSmallScreen) 30.sp else 38.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            lineHeight = if (isSmallScreen) 32.sp else 40.sp
                        )
                        Text(
                            text = String.format(Locale.US, "%.1f W", wattage),
                            fontSize = if (isSmallScreen) 13.sp else 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyanAccent
                        )
                        Text(
                            text = "${currentMa} mA",
                            fontSize = 10.5.sp,
                            color = Color.White.copy(alpha = 0.75f),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // 3. Stage Card (Akıllı Donanım Şarjı / Akıllı Evre) - Sabit, Kaymayan Yapı
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color.White.copy(alpha = 0.06f),
                    border = BorderStroke(1.dp, stageColor.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        // Header of Stage Card
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = stageTitle,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = stageColor,
                                modifier = Modifier.weight(1f, fill = false),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = stageColor.copy(alpha = 0.2f),
                                border = BorderStroke(0.8.dp, stageColor.copy(alpha = 0.4f))
                            ) {
                                Text(
                                    text = stageBadge,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = stageColor,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // 3-Step Visual Stage Stepper
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            StageStepBar(step = 1, currentStep = stageIndex, activeColor = CyanAccent, label = "%0-%60 Hızlı", modifier = Modifier.weight(1f))
                            StageStepBar(step = 2, currentStep = stageIndex, activeColor = EmeraldPrimary, label = "%60-%85 Denge", modifier = Modifier.weight(1f))
                            StageStepBar(step = 3, currentStep = stageIndex, activeColor = PurpleNeon, label = "%85-%100 Koruma", modifier = Modifier.weight(1f))
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = stageDesc,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                }

                // 4. Real-time Hardware Metrics Grid (Okunabilir, Net)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MetricMiniBox(
                        title = "Sıcaklık",
                        value = "${String.format(Locale.US, "%.1f", batteryInfo.temperatureCelsius)}°C",
                        icon = Icons.Default.Thermostat,
                        tint = if (batteryInfo.temperatureCelsius > 38f) OrangeFlame else EmeraldPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    MetricMiniBox(
                        title = "Voltaj",
                        value = "${String.format(Locale.US, "%.2f", voltage)} V",
                        icon = Icons.Default.Speed,
                        tint = CyanAccent,
                        modifier = Modifier.weight(1f)
                    )
                    MetricMiniBox(
                        title = "Tahmini Süre",
                        value = "${(batteryInfo.estimatedHoursRemaining * 60).toInt().coerceAtLeast(5)} dk",
                        icon = Icons.Default.Timer,
                        tint = PurpleNeon,
                        modifier = Modifier.weight(1f)
                    )
                }

                // 5. Action / Return Button (Her Ekranda Net Görünür)
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("charging_return_home_btn"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyanAccent,
                        contentColor = Color.Black
                    )
                ) {
                    Text(
                        text = "Ana Ekrana Dön",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

@Composable
private fun StageStepBar(
    step: Int,
    currentStep: Int,
    activeColor: Color,
    label: String,
    modifier: Modifier = Modifier
) {
    val isActive = step == currentStep
    val isPassed = step < currentStep

    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(
                    when {
                        isActive -> activeColor
                        isPassed -> EmeraldPrimary.copy(alpha = 0.6f)
                        else -> Color.White.copy(alpha = 0.12f)
                    }
                )
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 9.sp,
            color = if (isActive) activeColor else Color.White.copy(alpha = 0.5f),
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
            maxLines = 1
        )
    }
}

@Composable
private fun MetricMiniBox(
    title: String,
    value: String,
    icon: ImageVector,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color.White.copy(alpha = 0.06f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = value,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = title,
                fontSize = 9.5.sp,
                color = Color.White.copy(alpha = 0.65f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
