package com.example.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.ScreenSearchDesktop
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
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
import androidx.compose.runtime.mutableStateOf
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
import androidx.compose.ui.window.DialogProperties
import com.example.system.PowerFeatureConfigHelper
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.OrangeFlame
import com.example.ui.theme.PurpleNeon
import com.example.ui.theme.RedCritical

enum class BatteryImpact(val label: String, val color: Color, val containerColor: Color) {
    HIGH("Yüksek Pil & Isı", RedCritical, Color(0xFF330A0A)),
    MEDIUM("Orta Pil Etkisi", OrangeFlame, Color(0xFF33200A)),
    LOW("Düşük / Hafif", EmeraldPrimary, Color(0xFF0A2B18)),
    NEGLIGIBLE("Sıfır / Minimum", CyanAccent, Color(0xFF06222B))
}

@Composable
fun ModularPowerFeaturesDialog(
    onDismissRequest: () -> Unit
) {
    val context = LocalContext.current

    var isServiceEnabled by remember {
        mutableStateOf(PowerFeatureConfigHelper.isServiceEnabled(context))
    }
    var isWatchdogEnabled by remember {
        mutableStateOf(PowerFeatureConfigHelper.isWatchdogEnabled(context))
    }
    var isLiveTelemetryPollingEnabled by remember {
        mutableStateOf(PowerFeatureConfigHelper.isLiveTelemetryPollingEnabled(context))
    }
    var isScreenUsageTrackingEnabled by remember {
        mutableStateOf(PowerFeatureConfigHelper.isScreenUsageTrackingEnabled(context))
    }
    var isWidgetSyncEnabled by remember {
        mutableStateOf(PowerFeatureConfigHelper.isWidgetSyncEnabled(context))
    }
    var isSmartChargingAutomationEnabled by remember {
        mutableStateOf(PowerFeatureConfigHelper.isSmartChargingAutomationEnabled(context))
    }
    var isThermalAndFullAlarmsEnabled by remember {
        mutableStateOf(PowerFeatureConfigHelper.isThermalAndFullAlarmsEnabled(context))
    }
    var isNightGuardEnabled by remember {
        mutableStateOf(PowerFeatureConfigHelper.isNightGuardEnabled(context))
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .fillMaxWidth(0.96f)
            .fillMaxHeight(0.92f)
            .testTag("modular_power_features_dialog"),
        title = {
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
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(EmeraldPrimary.copy(alpha = 0.3f), CyanAccent.copy(alpha = 0.2f))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Özellik Güç Yöneticisi",
                            tint = EmeraldPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Pil & Isı Özellik Yöneticisi",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "İstediğiniz özellikleri açın / kapatın",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.6f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                IconButton(
                    onClick = onDismissRequest,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Kapat",
                        tint = Color.White.copy(alpha = 0.7f)
                    )
                }
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                // Hızlı Ön Ayarlar (Presets)
                item {
                    Text(
                        text = "⚡ HIZLI MOD SEÇİMİ",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = CyanAccent,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // 1. Sıfır Arka Plan / Ultra Eko
                        OutlinedButton(
                            onClick = {
                                PowerFeatureConfigHelper.applyZeroDrainEcoPreset(context)
                                isServiceEnabled = false
                                isWatchdogEnabled = false
                                isLiveTelemetryPollingEnabled = false
                                isScreenUsageTrackingEnabled = false
                                isWidgetSyncEnabled = false
                                isSmartChargingAutomationEnabled = false
                                isThermalAndFullAlarmsEnabled = false
                                isNightGuardEnabled = false
                                Toast.makeText(context, "🌿 Sıfır Arka Plan Tüketim Modu devrede!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp)
                                .testTag("btn_preset_eco"),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.5f)),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (!isServiceEnabled && !isWatchdogEnabled && !isLiveTelemetryPollingEnabled)
                                    EmeraldPrimary.copy(alpha = 0.2f) else Color.Transparent
                            ),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Eco, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Ultra Eko", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = EmeraldLight)
                                }
                                Text("%0 Arka Plan", fontSize = 10.sp, color = Color.White.copy(alpha = 0.6f))
                            }
                        }

                        // 2. Dengeli Mod
                        OutlinedButton(
                            onClick = {
                                PowerFeatureConfigHelper.applyBalancedPreset(context)
                                isServiceEnabled = true
                                isWatchdogEnabled = false
                                isLiveTelemetryPollingEnabled = false
                                isScreenUsageTrackingEnabled = true
                                isWidgetSyncEnabled = true
                                isSmartChargingAutomationEnabled = true
                                isThermalAndFullAlarmsEnabled = true
                                isNightGuardEnabled = true
                                Toast.makeText(context, "⚖️ Dengeli Güç Tasarrufu Modu devrede!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp)
                                .testTag("btn_preset_balanced"),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, CyanAccent.copy(alpha = 0.5f)),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (isServiceEnabled && !isWatchdogEnabled && !isLiveTelemetryPollingEnabled)
                                    CyanAccent.copy(alpha = 0.2f) else Color.Transparent
                            ),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Speed, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Dengeli", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = CyanAccent)
                                }
                                Text("Optimum Pil", fontSize = 10.sp, color = Color.White.copy(alpha = 0.6f))
                            }
                        }

                        // 3. Tam Güç / Pro Mod
                        OutlinedButton(
                            onClick = {
                                PowerFeatureConfigHelper.applyFullMonitoringPreset(context)
                                isServiceEnabled = true
                                isWatchdogEnabled = true
                                isLiveTelemetryPollingEnabled = true
                                isScreenUsageTrackingEnabled = true
                                isWidgetSyncEnabled = true
                                isSmartChargingAutomationEnabled = true
                                isThermalAndFullAlarmsEnabled = true
                                isNightGuardEnabled = true
                                Toast.makeText(context, "⚡ Tüm İzleme & Koruma Özellikleri Açıldı!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp)
                                .testTag("btn_preset_full"),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, PurpleNeon.copy(alpha = 0.5f)),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (isServiceEnabled && isWatchdogEnabled && isLiveTelemetryPollingEnabled)
                                    PurpleNeon.copy(alpha = 0.2f) else Color.Transparent
                            ),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Bolt, contentDescription = null, tint = PurpleNeon, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Pro İzleme", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = PurpleNeon)
                                }
                                Text("Tüm Özellikler", fontSize = 10.sp, color = Color.White.copy(alpha = 0.6f))
                            }
                        }
                    }
                }

                // Bilgilendirme Kutusu
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF14241B)),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = EmeraldPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Kapatmak istediğiniz özelliklerin anahtarını kapatabilirsiniz. İhtiyaç duyduğunuzda sadece o özelliği aktif edin.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.85f),
                                lineHeight = 16.sp
                            )
                        }
                    }
                }

                // MODÜLER ÖZELLİK LİSTESİ
                item {
                    Text(
                        text = "🛠️ ÖZEL ÖZELLİK ANAHTARLARI",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.7f),
                        letterSpacing = 1.sp
                    )
                }

                // 1. Canlı Donanım & CPU Frekans Taraması (En Yüksek Isınma & Pil Tüketeni)
                item {
                    FeatureToggleCard(
                        title = "Canlı CPU Frekans & Donanım Taraması",
                        description = "Açıkken her 12 saniyede bir CPU çekirdek hızlarını, RAM ve ağ paketlerini okur. Kapatıldığında telefon ısınmaz, sadece manuel basıldığında veri çeker.",
                        icon = Icons.Default.Memory,
                        iconTint = if (isLiveTelemetryPollingEnabled) RedCritical else Color.Gray,
                        impact = BatteryImpact.HIGH,
                        checked = isLiveTelemetryPollingEnabled,
                        onCheckedChange = { checked ->
                            isLiveTelemetryPollingEnabled = checked
                            PowerFeatureConfigHelper.setLiveTelemetryPollingEnabled(context, checked)
                        },
                        tag = "toggle_live_telemetry"
                    )
                }

                // 2. Watchdog / Uykudan Uyandırma Koruması
                item {
                    FeatureToggleCard(
                        title = "Watchdog & Derin Uykudan Uyandırma",
                        description = "Açıkken Android sisteminin uygulamayı durdurmasını engeller. Kapatıldığında telefonun Derin Uykuya (Deep Sleep) geçmesine izin verir ve şarj tasarrufu sağlar.",
                        icon = Icons.Default.Warning,
                        iconTint = if (isWatchdogEnabled) RedCritical else Color.Gray,
                        impact = BatteryImpact.HIGH,
                        checked = isWatchdogEnabled,
                        onCheckedChange = { checked ->
                            isWatchdogEnabled = checked
                            PowerFeatureConfigHelper.setWatchdogEnabled(context, checked)
                        },
                        tag = "toggle_watchdog"
                    )
                }

                // 3. Arka Plan Ön Plan Servisi & Bildirim
                item {
                    FeatureToggleCard(
                        title = "Arka Plan Servisi & Canlı Bildirim",
                        description = "Açıkken bildirim çubuğunda canlı pil yüzdesi, sıcaklık ve şarj hızı gösterilir. Kapatıldığında uygulama arkada tamamen durur (%0 tüketim).",
                        icon = Icons.Default.Notifications,
                        iconTint = if (isServiceEnabled) OrangeFlame else Color.Gray,
                        impact = BatteryImpact.MEDIUM,
                        checked = isServiceEnabled,
                        onCheckedChange = { checked ->
                            isServiceEnabled = checked
                            PowerFeatureConfigHelper.setServiceEnabled(context, checked)
                        },
                        tag = "toggle_bg_service"
                    )
                }

                // 4. Şarj & Aşırı Sıcaklık Siren Alarmları
                item {
                    FeatureToggleCard(
                        title = "Şarj Limiti (%80/%100) & Sıcaklık Alarmları",
                        description = "Batarya %80 veya %100 olduğunda ve sıcaklık kritik seviyeye ulaştığında sesli alarm çalar.",
                        icon = Icons.Default.Alarm,
                        iconTint = if (isThermalAndFullAlarmsEnabled) CyanAccent else Color.Gray,
                        impact = BatteryImpact.LOW,
                        checked = isThermalAndFullAlarmsEnabled,
                        onCheckedChange = { checked ->
                            isThermalAndFullAlarmsEnabled = checked
                            PowerFeatureConfigHelper.setThermalAndFullAlarmsEnabled(context, checked)
                        },
                        tag = "toggle_alarms"
                    )
                }

                // 5. Ekran Açık/Kapalı Süre & Pil Tüketim Kaydı
                item {
                    FeatureToggleCard(
                        title = "Ekran Süresi & Günlük Pil Analizi",
                        description = "Ekranın ne kadar süre açık kaldığını ve saatlik pil düşüş grafiğini kaydeder.",
                        icon = Icons.Default.ScreenSearchDesktop,
                        iconTint = if (isScreenUsageTrackingEnabled) EmeraldPrimary else Color.Gray,
                        impact = BatteryImpact.LOW,
                        checked = isScreenUsageTrackingEnabled,
                        onCheckedChange = { checked ->
                            isScreenUsageTrackingEnabled = checked
                            PowerFeatureConfigHelper.setScreenUsageTrackingEnabled(context, checked)
                        },
                        tag = "toggle_screen_tracking"
                    )
                }

                // 6. Ana Ekran Widget Canlı Senkronizasyonu
                item {
                    FeatureToggleCard(
                        title = "Ana Ekran Widget Canlı Eşitlemesi",
                        description = "Ana ekranınızdaki VoltGuard widget'ının pil değiştikçe anlık güncellenmesini sağlar.",
                        icon = Icons.Default.Widgets,
                        iconTint = if (isWidgetSyncEnabled) CyanAccent else Color.Gray,
                        impact = BatteryImpact.LOW,
                        checked = isWidgetSyncEnabled,
                        onCheckedChange = { checked ->
                            isWidgetSyncEnabled = checked
                            PowerFeatureConfigHelper.setWidgetSyncEnabled(context, checked)
                        },
                        tag = "toggle_widget_sync"
                    )
                }

                // 7. Akıllı Şarj Çizelgesi & Otomatik Tasarruf
                item {
                    FeatureToggleCard(
                        title = "Akıllı Şarj & Otomatik Tasarruf Çizelgesi",
                        description = "Gece belirlenen saatlerde telefonunuzu otomatik olarak Ultra Güç Tasarrufu moduna alır.",
                        icon = Icons.Default.Schedule,
                        iconTint = if (isSmartChargingAutomationEnabled) EmeraldLight else Color.Gray,
                        impact = BatteryImpact.NEGLIGIBLE,
                        checked = isSmartChargingAutomationEnabled,
                        onCheckedChange = { checked ->
                            isSmartChargingAutomationEnabled = checked
                            PowerFeatureConfigHelper.setSmartChargingAutomationEnabled(context, checked)
                        },
                        tag = "toggle_smart_schedule"
                    )
                }

                // 8. Gece Uyku Koruması & Gizli Uygulama Taraması
                item {
                    FeatureToggleCard(
                        title = "Gece Uyku & Gizli Sızıntı Koruması",
                        description = "Siz uyurken arkada gizlice veri çeken veya şarj bitiren uygulamaları tespit eder.",
                        icon = Icons.Default.Bedtime,
                        iconTint = if (isNightGuardEnabled) PurpleNeon else Color.Gray,
                        impact = BatteryImpact.NEGLIGIBLE,
                        checked = isNightGuardEnabled,
                        onCheckedChange = { checked ->
                            isNightGuardEnabled = checked
                            PowerFeatureConfigHelper.setNightGuardEnabled(context, checked)
                        },
                        tag = "toggle_night_guard"
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    Toast.makeText(context, "✅ Güç tercihleri kaydedildi ve uygulandı.", Toast.LENGTH_SHORT).show()
                    onDismissRequest()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("btn_save_power_features"),
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.Black)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Tamamla & Uygula", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        containerColor = Color(0xFF13171F),
        shape = RoundedCornerShape(24.dp)
    )
}

@Composable
private fun FeatureToggleCard(
    title: String,
    description: String,
    icon: ImageVector,
    iconTint: Color,
    impact: BatteryImpact,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    tag: String
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (checked) Color(0xFF1B2230) else Color(0xFF151922)
        ),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            1.dp,
            if (checked) iconTint.copy(alpha = 0.35f) else Color.White.copy(alpha = 0.08f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
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
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(iconTint.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconTint,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (checked) Color.White else Color.White.copy(alpha = 0.6f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Switch(
                    checked = checked,
                    onCheckedChange = onCheckedChange,
                    modifier = Modifier.testTag(tag),
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = iconTint,
                        uncheckedThumbColor = Color.LightGray,
                        uncheckedTrackColor = Color.DarkGray
                    )
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.65f),
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Pil Tüketim Etiketi (Battery Impact Badge)
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = impact.containerColor,
                border = BorderStroke(0.5.dp, impact.color.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(impact.color)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = impact.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = impact.color,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}
