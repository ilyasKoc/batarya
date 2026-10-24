package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.OrangeFlame
import com.example.ui.theme.RedCritical

/**
 * 📈 Çizgi Grafik Veri Noktası Modeli
 */
data class ChartPoint(val label: String, val value: Float)

/**
 * 📊 Çiftli Sütun Grafik Veri Noktası Modeli (Örn: Wi-Fi ve Mobil Veri)
 */
data class DualBarPoint(val label: String, val value1: Float, val value2: Float)

/**
 * 🍩 Simit (Donut) Dilim Modeli
 */
data class DonutSlice(val label: String, val value: Float, val color: Color)

/**
 * 📈 Batarya Geçmiş Doluluk Çizgi Grafiği (BatteryHistoryLineChart)
 * 
 * Batarya yüzdesinin zaman içindeki değişimini yumuşak eğriler (Bézier Curve)
 * ve alt gradyan dolgusuyla animasyonlu olarak çizer.
 * 
 * @param points Çizilecek zaman ve yüzde noktaları
 * @param modifier Düzenleyici
 * @param lineColor Eğri çizgi rengi
 */
@Composable
fun BatteryHistoryLineChart(
    points: List<ChartPoint>,
    modifier: Modifier = Modifier,
    lineColor: Color = EmeraldPrimary
) {
    if (points.isEmpty()) return

    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(points) {
        animProgress.snapTo(0f)
        animProgress.animateTo(1f, animationSpec = tween(700))
    }

    val maxVal = 100f
    val minVal = 0f

    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .padding(horizontal = 8.dp)
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                val width = size.width
                val height = size.height
                val bottomPadding = 30f
                val chartHeight = height - bottomPadding

                // Kılavuz çizgileri (0%, 25%, 50%, 75%, 100%)
                val guideLines = listOf(0f, 25f, 50f, 75f, 100f)
                for (lvl in guideLines) {
                    val y = chartHeight - (lvl / maxVal) * chartHeight
                    drawLine(
                        color = Color.White.copy(alpha = 0.08f),
                        start = Offset(0f, y),
                        end = Offset(width, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                if (points.size < 2) return@Canvas

                val stepX = width / (points.size - 1)
                val path = Path()
                val fillPath = Path()

                points.forEachIndexed { i, pt ->
                    val x = i * stepX
                    val y = chartHeight - (pt.value.coerceIn(minVal, maxVal) / maxVal) * chartHeight * animProgress.value

                    if (i == 0) {
                        path.moveTo(x, y)
                        fillPath.moveTo(x, chartHeight)
                        fillPath.lineTo(x, y)
                    } else {
                        val prevX = (i - 1) * stepX
                        val prevY = chartHeight - (points[i - 1].value.coerceIn(minVal, maxVal) / maxVal) * chartHeight * animProgress.value
                        val cX1 = prevX + (x - prevX) / 2
                        val cY1 = prevY
                        val cX2 = prevX + (x - prevX) / 2
                        val cY2 = y
                        path.cubicTo(cX1, cY1, cX2, cY2, x, y)
                        fillPath.cubicTo(cX1, cY1, cX2, cY2, x, y)
                    }

                    if (i == points.size - 1) {
                        fillPath.lineTo(x, chartHeight)
                        fillPath.close()
                    }
                }

                // Eğri altı ışıma gradyanı
                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            lineColor.copy(alpha = 0.35f),
                            lineColor.copy(alpha = 0.02f)
                        ),
                        startY = 0f,
                        endY = chartHeight
                    )
                )

                // Ana çizgi
                drawPath(
                    path = path,
                    color = lineColor,
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )

                // Veri noktası dairesel vurguları
                points.forEachIndexed { i, pt ->
                    val x = i * stepX
                    val y = chartHeight - (pt.value.coerceIn(minVal, maxVal) / maxVal) * chartHeight * animProgress.value
                    if (i % 3 == 0 || i == points.size - 1) {
                        drawCircle(
                            color = Color(0xFF0F172A),
                            radius = 5.dp.toPx(),
                            center = Offset(x, y)
                        )
                        drawCircle(
                            color = lineColor,
                            radius = 3.5.dp.toPx(),
                            center = Offset(x, y)
                        )
                    }
                }
            }
        }

        // Alt zaman etiketleri
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val labelIndices = listOf(0, points.size / 4, points.size / 2, (3 * points.size) / 4, points.size - 1)
            labelIndices.distinct().forEach { idx ->
                if (idx < points.size) {
                    Text(
                        text = points[idx].label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

/**
 * 📊 Çift Sütunlu Veri Kullanım Grafiği (DataUsageDualBarChart)
 * 
 * Wi-Fi ve Mobil veri tüketimlerini yan yana karşılaştırmalı sütunlarla görselleştirir.
 */
@Composable
fun DataUsageDualBarChart(
    items: List<DualBarPoint>,
    modifier: Modifier = Modifier,
    bar1Color: Color = CyanAccent,
    bar2Color: Color = EmeraldPrimary,
    bar1Name: String = "Wi-Fi",
    bar2Name: String = "Mobil"
) {
    if (items.isEmpty()) return

    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(items) {
        animProgress.snapTo(0f)
        animProgress.animateTo(1f, animationSpec = tween(650))
    }

    val maxVal = items.maxOfOrNull { maxOf(it.value1, it.value2) }?.coerceAtLeast(1f) ?: 10f

    Column(modifier = modifier.fillMaxWidth()) {
        // Gösterge (Lejant)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(10.dp).background(bar1Color, CircleShape))
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = bar1Name, style = MaterialTheme.typography.labelMedium)
            Spacer(modifier = Modifier.width(16.dp))
            Box(modifier = Modifier.size(10.dp).background(bar2Color, CircleShape))
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = bar2Name, style = MaterialTheme.typography.labelMedium)
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(170.dp)
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                val width = size.width
                val height = size.height
                val bottomLabelHeight = 24f
                val chartHeight = height - bottomLabelHeight

                val slotWidth = width / items.size
                val barWidth = (slotWidth * 0.32f).coerceAtMost(22.dp.toPx())
                val spaceBetweenBars = 3.dp.toPx()

                items.forEachIndexed { i, item ->
                    val centerX = i * slotWidth + slotWidth / 2

                    val h1 = (item.value1 / maxVal) * chartHeight * animProgress.value
                    val h2 = (item.value2 / maxVal) * chartHeight * animProgress.value

                    val x1 = centerX - barWidth - spaceBetweenBars / 2
                    val x2 = centerX + spaceBetweenBars / 2

                    // Bar 1 (Wi-Fi)
                    drawRoundRect(
                        color = bar1Color,
                        topLeft = Offset(x1, chartHeight - h1),
                        size = Size(barWidth, h1),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
                    )

                    // Bar 2 (Mobil Veri)
                    drawRoundRect(
                        color = bar2Color,
                        topLeft = Offset(x2, chartHeight - h2),
                        size = Size(barWidth, h2),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
                    )
                }
            }
        }

        // X Ekseni etiketleri
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            items.forEach {
                Text(
                    text = it.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * 🍩 Uygulama Kategorisi Dağılım Grafiği (AppUsageDonutChart)
 * 
 * Sosyal Medya, Oyun, Video, Ofis vb. kategorilerin toplam süre içindeki payını
 * dairesel pasta/simit grafik üzerinde renkli dilimlerle gösterir.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AppUsageDonutChart(
    slices: List<DonutSlice>,
    modifier: Modifier = Modifier,
    centerTitle: String = "Toplam",
    centerSubtitle: String = ""
) {
    if (slices.isEmpty()) return

    val total = slices.sumOf { it.value.toDouble() }.toFloat().coerceAtLeast(0.01f)
    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(slices) {
        animProgress.snapTo(0f)
        animProgress.animateTo(1f, animationSpec = tween(800))
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(190.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(180.dp)) {
                val strokeWidth = 24.dp.toPx()
                val diameter = size.minDimension - strokeWidth
                val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)
                val arcSize = Size(diameter, diameter)

                var startAngle = -90f
                slices.forEach { slice ->
                    val sweepAngle = (slice.value / total) * 360f * animProgress.value
                    if (sweepAngle > 0.5f) {
                        drawArc(
                            color = slice.color,
                            startAngle = startAngle,
                            sweepAngle = sweepAngle - 1.5f,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }
                    startAngle += (slice.value / total) * 360f
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = centerTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (centerSubtitle.isNotEmpty()) {
                    Text(
                        text = centerSubtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Dilim Lejantı
        FlowRow(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            slices.forEach { slice ->
                val pct = ((slice.value / total) * 100).toInt()
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(slice.color, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "${slice.label} (%$pct)",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}

/**
 * 📅 Aylık Ekran Süresi Çubuk Grafiği (MonthlyScreenTimeBarChart)
 * 
 * Son 30 günün ekran süresi eğilimini ve günlük ortalama çizgisini gösterir.
 */
@Composable
fun MonthlyScreenTimeBarChart(
    dailyHours: List<Pair<Int, Float>>, // Gün 1..30 -> Saat
    modifier: Modifier = Modifier,
    averageHours: Float = 4.5f
) {
    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(dailyHours) {
        animProgress.snapTo(0f)
        animProgress.animateTo(1f, animationSpec = tween(700))
    }

    val maxVal = maxOf(dailyHours.maxOfOrNull { it.second } ?: 8f, averageHours + 2f)

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            ) {
                Text(
                    text = "Ekran Süresi Analizi",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Son 30 günlük kullanım dağılımı",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Box(
                modifier = Modifier
                    .background(OrangeFlame.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                    .border(BorderStroke(1.dp, OrangeFlame.copy(alpha = 0.35f)), RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 5.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(12.dp, 3.dp)
                            .background(OrangeFlame, RoundedCornerShape(1.5.dp))
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "Ort. ${"%.1f".format(averageHours)} sa",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = OrangeFlame,
                        maxLines = 1
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                val width = size.width
                val height = size.height

                // Ortalama kılavuz çizgisi
                val avgY = height - (averageHours / maxVal) * height
                drawLine(
                    color = OrangeFlame.copy(alpha = 0.8f),
                    start = Offset(0f, avgY),
                    end = Offset(width, avgY),
                    strokeWidth = 1.5.dp.toPx()
                )

                if (dailyHours.isEmpty()) return@Canvas
                val barSlot = width / dailyHours.size
                val barW = (barSlot * 0.7f).coerceAtMost(12.dp.toPx())

                dailyHours.forEachIndexed { i, pair ->
                    val h = (pair.second / maxVal) * height * animProgress.value
                    val x = i * barSlot + (barSlot - barW) / 2
                    val color = if (pair.second >= averageHours) CyanAccent else EmeraldPrimary.copy(alpha = 0.75f)

                    drawRoundRect(
                        color = color,
                        topLeft = Offset(x, height - h),
                        size = Size(barW, h),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 6.dp, end = 6.dp, top = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "1. Gün", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = "15. Gün", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = "30. Gün", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
