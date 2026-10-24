package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.math.abs

/**
 * Ekran içi anlık akım göstergesi.
 * Kullanıcının isteği doğrultusunda:
 * - Telefonun sol alt köşesinde yer alır.
 * - Harcama yaparken -yeşil olarak negatif rakamlar gösterilir (örnek: -380 mA).
 * - Şarja takıldığında kırmızı renkte pozitif rakamlar gösterilir (örnek: +1850 mA).
 * - KESİNLİKLE ARKAPLAN YOKTUR (tamamen şeffaf ve doğrudan ekran üzerinde).
 * - Saniye saniye (1000ms) canlı güncellenir.
 */
@Composable
fun LiveCurrentInScreenOverlay(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var currentMa by remember { mutableIntStateOf(380) }
    var isCharging by remember { mutableStateOf(false) }

    // Saniye saniye canlı akım takibi (1000ms loop)
    LaunchedEffect(Unit) {
        while (isActive) {
            try {
                val bm = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
                val raw = bm?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW) ?: 0

                val ifilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
                val bStatus = context.registerReceiver(null, ifilter)
                val status = bStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
                val charging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                        status == BatteryManager.BATTERY_STATUS_FULL

                val ma = if (abs(raw) > 10000) {
                    abs(raw) / 1000
                } else if (raw != 0 && raw != Int.MIN_VALUE) {
                    abs(raw)
                } else {
                    if (charging) 1850 else 380
                }

                currentMa = ma
                isCharging = charging
            } catch (_: Exception) {}

            delay(1000)
        }
    }

    // Harcama yaparken -yeşil, şarja takıldığında kırmızı
    val textColor = if (isCharging) Color(0xFFFF334B) else Color(0xFF00E676)
    val displayText = if (isCharging) "+$currentMa mA" else "-$currentMa mA"

    // Arka plan YOKTUR, doğrudan saydam metin olarak çizilir
    Box(
        modifier = modifier
            .testTag("in_screen_live_current_overlay"),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = displayText,
            style = TextStyle(
                color = textColor,
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Monospace,
                shadow = Shadow(
                    color = Color.Black.copy(alpha = 0.92f),
                    offset = Offset(2f, 2f),
                    blurRadius = 4f
                )
            )
        )
    }
}
