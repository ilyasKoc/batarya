package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.OrangeFlame
import com.example.ui.theme.PurpleNeon

@Composable
fun BatteryToolsTab(
    calibratedCycles: Int,
    lastCalibrationDate: String,
    onOpenChargingOverlay: () -> Unit,
    onOpenChargerBenchmark: () -> Unit,
    onOpenPhoneCooler: () -> Unit,
    onOpenBatteryCalibration: () -> Unit,
    onOpenStressTest: () -> Unit,
    onOpenAppThermalIndex: () -> Unit,
    onOpenSmartAlarms: () -> Unit,
    onOpenNightGuard: () -> Unit = {},
    onOpenWeeklySummary: () -> Unit,
    onOpenDatabaseBackup: () -> Unit,
    onExportDiagnosticReport: () -> Unit,
    onExportCsvReport: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("tab_content_hardware_tools"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- 1. GELİŞMİŞ DONANIM & ŞARJ ARAÇLARI ---
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "GELİŞMİŞ DONANIM & ŞARJ ARAÇLARI",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = PurpleNeon,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                )

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("advanced_hardware_tools_card"),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.5.dp, PurpleNeon.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Tool 1: Canlı Şarj Ekranı
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onOpenChargingOverlay() }
                                    .testTag("tool_btn_charging_overlay"),
                                shape = RoundedCornerShape(10.dp),
                                color = CyanAccent.copy(alpha = 0.1f),
                                border = BorderStroke(1.dp, CyanAccent.copy(alpha = 0.45f))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .background(CyanAccent.copy(alpha = 0.2f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Bolt,
                                            contentDescription = null,
                                            tint = CyanAccent,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Canlı Şarj Ekranı",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = CyanAccent,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = "Neon animasyon & anlık güç",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 10.sp
                                    )
                                }
                            }

                            // Tool 2: Kablo & Adaptör Testi
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onOpenChargerBenchmark() }
                                    .testTag("tool_btn_charger_benchmark"),
                                shape = RoundedCornerShape(10.dp),
                                color = EmeraldPrimary.copy(alpha = 0.1f),
                                border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.45f))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .background(EmeraldPrimary.copy(alpha = 0.2f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Speed,
                                            contentDescription = null,
                                            tint = EmeraldPrimary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Kablo & Şarj Testi",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = EmeraldPrimary,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = "Direnç ve watt benchmarkı",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Tool 3: Batarya Soğutucu
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onOpenPhoneCooler() }
                                    .testTag("tool_btn_phone_cooler"),
                                shape = RoundedCornerShape(10.dp),
                                color = OrangeFlame.copy(alpha = 0.1f),
                                border = BorderStroke(1.dp, OrangeFlame.copy(alpha = 0.45f))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .background(OrangeFlame.copy(alpha = 0.2f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.LocalFireDepartment,
                                            contentDescription = null,
                                            tint = OrangeFlame,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Batarya Soğutucu",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = OrangeFlame,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = "Termal işlemci rahatlatma",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 10.sp
                                    )
                                }
                            }

                            // Tool 4: Döngü & Kalibrasyon
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onOpenBatteryCalibration() }
                                    .testTag("tool_btn_calibration"),
                                shape = RoundedCornerShape(10.dp),
                                color = PurpleNeon.copy(alpha = 0.1f),
                                border = BorderStroke(1.dp, PurpleNeon.copy(alpha = 0.45f))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .background(PurpleNeon.copy(alpha = 0.2f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Refresh,
                                            contentDescription = null,
                                            tint = PurpleNeon,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Döngü & Kalibrasyon",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = PurpleNeon,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = "$calibratedCycles Döngü • ${if (lastCalibrationDate.contains("Henüz")) "Kalibre Et" else "Kalibre Edildi ✓"}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (lastCalibrationDate.contains("Henüz")) MaterialTheme.colorScheme.onSurfaceVariant else EmeraldPrimary,
                                        fontSize = 10.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Tool 5: Hücre & Stres Testi
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onOpenStressTest() }
                                    .testTag("tool_btn_stress_test"),
                                shape = RoundedCornerShape(10.dp),
                                color = PurpleNeon.copy(alpha = 0.1f),
                                border = BorderStroke(1.dp, PurpleNeon.copy(alpha = 0.45f))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .background(PurpleNeon.copy(alpha = 0.2f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Science,
                                            contentDescription = null,
                                            tint = PurpleNeon,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Hücre & Stres Testi",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = PurpleNeon,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = "30s voltaj sarkması & sag",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 10.sp
                                    )
                                }
                            }

                            // Tool 6: Uygulama Isı Endeksi
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onOpenAppThermalIndex() }
                                    .testTag("tool_btn_thermal_index"),
                                shape = RoundedCornerShape(10.dp),
                                color = OrangeFlame.copy(alpha = 0.1f),
                                border = BorderStroke(1.dp, OrangeFlame.copy(alpha = 0.45f))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .background(OrangeFlame.copy(alpha = 0.2f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Thermostat,
                                            contentDescription = null,
                                            tint = OrangeFlame,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Uygulama Isı Endeksi",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = OrangeFlame,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = "Uygulama bazlı ısı haritası",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Tool 7: Akıllı Sesli Alarmlar
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onOpenSmartAlarms() }
                                    .testTag("tool_btn_smart_alarms"),
                                shape = RoundedCornerShape(10.dp),
                                color = CyanAccent.copy(alpha = 0.1f),
                                border = BorderStroke(1.dp, CyanAccent.copy(alpha = 0.45f))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .background(CyanAccent.copy(alpha = 0.2f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.VolumeUp,
                                            contentDescription = null,
                                            tint = CyanAccent,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Akıllı Sesli Alarmlar",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = CyanAccent,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = "Siren/Radar & %80 limit",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 10.sp
                                    )
                                }
                            }

                            // Tool 8: Haftalık Pil Özeti
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onOpenWeeklySummary() }
                                    .testTag("tool_btn_weekly_summary"),
                                shape = RoundedCornerShape(10.dp),
                                color = EmeraldPrimary.copy(alpha = 0.1f),
                                border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.45f))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .background(EmeraldPrimary.copy(alpha = 0.2f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Analytics,
                                            contentDescription = null,
                                            tint = EmeraldPrimary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Haftalık Pil Özeti",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = EmeraldPrimary,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = "7 günlük sağlık & tüketim",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Tool: Akıllı Gece & Uzun Ömür Şarj Koruması
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOpenNightGuard() }
                                .testTag("tool_btn_night_guard"),
                            shape = RoundedCornerShape(10.dp),
                            color = PurpleNeon.copy(alpha = 0.12f),
                            border = BorderStroke(1.2.dp, PurpleNeon.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .background(PurpleNeon.copy(alpha = 0.25f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Bedtime,
                                            contentDescription = null,
                                            tint = PurpleNeon,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "Gece Şarj Tamponu (%80)",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = PurpleNeon,
                                                fontSize = 13.sp
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = EmeraldPrimary.copy(alpha = 0.2f)
                                            ) {
                                                Text(
                                                    text = "DAMLAMA AKTİF",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = EmeraldPrimary,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    fontSize = 8.5.sp,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                )
                                            }
                                        }
                                        Text(
                                            text = "Uyanış saatine göre optimize damlama şarjı & hücre koruması",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 10.sp
                                        )
                                    }
                                }

                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = PurpleNeon
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Tool 9: Veritabanı & Yedekleme
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOpenDatabaseBackup() }
                                .testTag("tool_btn_database_backup"),
                            shape = RoundedCornerShape(10.dp),
                            color = PurpleNeon.copy(alpha = 0.08f),
                            border = BorderStroke(1.dp, PurpleNeon.copy(alpha = 0.4f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .background(PurpleNeon.copy(alpha = 0.2f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Storage,
                                            contentDescription = null,
                                            tint = PurpleNeon,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = "Veritabanı & JSON Yedekleme",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = PurpleNeon,
                                            fontSize = 13.sp
                                        )
                                        Text(
                                            text = "Pil loglarını dışa aktar, geri yükle veya otomatik yedekle",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = PurpleNeon,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- 2. PROFESYONEL RAPOR VE VERİ DIŞA AKTARMA ---
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "PROFESYONEL RAPOR & VERİ PAYLAŞIMI",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = CyanAccent,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                )

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("report_export_card"),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.8f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Tanı ve Teşhis Raporları",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Pil sağlığı, tüketim logları ve telemetri dökümünü metin veya CSV formatında paylaşın.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = onExportDiagnosticReport,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("btn_export_diagnostic_report"),
                                shape = RoundedCornerShape(6.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = CyanAccent)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Teşhis Raporu",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black,
                                    maxLines = 1
                                )
                            }

                            OutlinedButton(
                                onClick = onExportCsvReport,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("btn_export_csv_report"),
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(1.dp, CyanAccent)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FileDownload,
                                    contentDescription = null,
                                    tint = CyanAccent,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "CSV Logları",
                                    fontWeight = FontWeight.Bold,
                                    color = CyanAccent,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
