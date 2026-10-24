package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Launch
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.AppUsageDataEntity
import com.example.data.entity.TimelineEventEntity
import com.example.system.LiveBatteryInfo
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.OrangeFlame
import com.example.ui.theme.RedCritical
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Oturum içinde aynı uygulamanın tüm activity kullanımlarını tek bir çatı altında birleştiren model.
 * Kullanıcının istediği gibi: her activity için ayrı satır açılmaz; uygulama oturumda 1 kez görünür.
 */
data class AggregatedSessionApp(
    val appName: String,
    val packageName: String,
    val firstStartTime: Long,
    val totalDurationSeconds: Int
) {
    val totalMinutes: Int get() = totalDurationSeconds / 60
    val remainingSeconds: Int get() = totalDurationSeconds % 60

    val formattedDuration: String
        get() = when {
            totalMinutes == 0 && remainingSeconds > 0 -> "$remainingSeconds saniye"
            totalMinutes > 0 && remainingSeconds == 0 -> "$totalMinutes dk"
            totalMinutes > 0 && remainingSeconds > 0 -> "$totalMinutes dk $remainingSeconds sn"
            else -> "< 1 saniye"
        }
}

/**
 * Ekran açılışından kapanışına kadar olan süreci kapsayan ağaç oturum modeli.
 */
data class TreeSession(
    val sessionId: String,
    val screenOn: TimelineEventEntity?,
    val apps: List<AggregatedSessionApp>,
    val screenOff: TimelineEventEntity?,
    val startTime: Long,
    val endTime: Long,
    val totalScreenTimeMinutes: Int
)

/**
 * Paket adını hiçbir zaman arayüzde göstermemek için temiz Türkçe isim çözümleyici.
 */
fun cleanDisplayName(appName: String, packageName: String): String {
    return com.example.system.AppNamesHelper.cleanDisplayName(appName, packageName)
}

/**
 * Ham kullanım olaylarını temiz ve dallanmış oturumlara dönüştürür.
 * 1) Ekran açıldı birden çok kez tekrar etmez, ekran kapanana kadar tek oturumda 1 kez gösterilir.
 * 2) Uygulamaların tüm activity sayfaları tek bir kayıtta toplanır (kaç dk kaç sn kaldı gösterilir).
 * 3) Engellenmiş / gizlenmiş uygulamalar elenir.
 */
fun groupEventsIntoSessions(
    events: List<TimelineEventEntity>,
    blockedPackages: Set<String>
): List<TreeSession> {
    if (events.isEmpty()) return emptyList()

    val filtered = events.filter { it.packageName !in blockedPackages }
    val sorted = filtered.sortedBy { it.timestamp }
    val sessions = mutableListOf<TreeSession>()

    var activeScreenOn: TimelineEventEntity? = null
    val currentAppMap = mutableMapOf<String, AggregatedSessionApp>()
    var sessionStartTime: Long = 0L

    fun finalizeSession(screenOffEvent: TimelineEventEntity?, endTime: Long) {
        val appList = currentAppMap.values.sortedBy { it.firstStartTime }
        val start = activeScreenOn?.timestamp ?: (appList.firstOrNull()?.firstStartTime ?: if (sessionStartTime > 0) sessionStartTime else endTime)
        val totalSecs = appList.sumOf { it.totalDurationSeconds }
        val totalMins = if (totalSecs > 0) {
            (totalSecs / 60).coerceAtLeast(if (totalSecs >= 30) 1 else 0)
        } else {
            ((endTime - start) / 60000L).toInt().coerceAtLeast(0)
        }

        if (activeScreenOn != null || appList.isNotEmpty() || screenOffEvent != null) {
            sessions.add(
                TreeSession(
                    sessionId = "session_${start}_${sessions.size}",
                    screenOn = activeScreenOn,
                    apps = appList,
                    screenOff = screenOffEvent,
                    startTime = start,
                    endTime = maxOf(start, endTime),
                    totalScreenTimeMinutes = totalMins
                )
            )
        }
        activeScreenOn = null
        currentAppMap.clear()
        sessionStartTime = 0L
    }

    for (event in sorted) {
        when (event.eventType) {
            "SCREEN_ON" -> {
                if (activeScreenOn != null || currentAppMap.isNotEmpty()) {
                    finalizeSession(null, event.timestamp)
                }
                activeScreenOn = event
                sessionStartTime = event.timestamp
            }
            "APP_USAGE" -> {
                if (activeScreenOn == null && currentAppMap.isEmpty()) {
                    sessionStartTime = event.timestamp
                }
                val durationSecs = when {
                    event.durationSeconds > 0 -> event.durationSeconds
                    event.durationMinutes > 0 -> event.durationMinutes * 60
                    else -> 10
                }

                val cleanName = cleanDisplayName(event.appName, event.packageName)
                val appKey = if (event.packageName.isNotBlank() && event.packageName != "android") {
                    event.packageName
                } else {
                    cleanName
                }

                val existing = currentAppMap[appKey]
                if (existing != null) {
                    currentAppMap[appKey] = existing.copy(
                        totalDurationSeconds = existing.totalDurationSeconds + durationSecs
                    )
                } else {
                    currentAppMap[appKey] = AggregatedSessionApp(
                        appName = cleanName,
                        packageName = event.packageName,
                        firstStartTime = event.timestamp,
                        totalDurationSeconds = durationSecs
                    )
                }
            }
            "SCREEN_OFF" -> {
                finalizeSession(event, event.timestamp)
            }
        }
    }

    if (activeScreenOn != null || currentAppMap.isNotEmpty()) {
        finalizeSession(null, System.currentTimeMillis())
    }

    return sessions.reversed()
}

/**
 * TimelineScreen: Günlük ekran açılışı/kapanışı ve uygulama kullanım oturumlarını
 * hiyerarşik ağaç yapısında veya kronolojik düz listede sunan çizelge ekranı.
 * - Ekran açıldığında başlayan ve kapandığında biten oturumları gruplar.
 * - Gizlenen/engellenen uygulamaları filtreler.
 * - Gün değiştirici (Bugün, Dün, 2 Gün Önce) ile geçmiş oturumları inceleme imkanı verir.
 */
@Composable
fun TimelineScreen(
    events: List<TimelineEventEntity>,
    selectedDayOffset: Int,
    blockedPackages: Set<String> = emptySet(),
    appUsages: List<AppUsageDataEntity> = emptyList(),
    liveBattery: LiveBatteryInfo? = null,
    showAllActions: Boolean = false,
    onToggleShowAllActions: (Boolean) -> Unit = {},
    onDayOffsetChange: (Int) -> Unit,
    onToggleBlockApp: (String) -> Unit = {},
    onClearAllBlockedApps: () -> Unit = {},
    onNavigateToSettings: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    var selectedViewMode by remember { mutableIntStateOf(0) } // 0: Ağaç Yapısı, 1: Düz Liste
    var showFilterDialog by remember { mutableStateOf(false) }
    var selectedAppDetail by remember { mutableStateOf<AppTimelineDetail?>(null) }
    var appToHideCandidate by remember { mutableStateOf<Pair<String, String>?>(null) } // Pair(packageName, appName)

    val treeSessions = remember(events, blockedPackages) {
        groupEventsIntoSessions(events, blockedPackages)
    }

    val visibleEvents = remember(events, blockedPackages) {
        events.filter { it.packageName !in blockedPackages }
    }

    val displayedTreeSessions = remember(treeSessions, showAllActions) {
        if (showAllActions) {
            treeSessions
        } else {
            treeSessions.filter { it.apps.isNotEmpty() }
        }
    }

    val displayedVisibleEvents = remember(visibleEvents, showAllActions, treeSessions) {
        if (showAllActions) {
            visibleEvents
        } else {
            val nonEmptySessionTimestamps = mutableSetOf<Long>()
            treeSessions.filter { it.apps.isNotEmpty() }.forEach { session ->
                session.screenOn?.let { nonEmptySessionTimestamps.add(it.timestamp) }
                session.screenOff?.let { nonEmptySessionTimestamps.add(it.timestamp) }
            }
            visibleEvents.filter { it.eventType == "APP_USAGE" || nonEmptySessionTimestamps.contains(it.timestamp) }
        }
    }

    val emptySessionsCount = remember(treeSessions) {
        treeSessions.count { it.apps.isEmpty() }
    }

    val screenOnCount = displayedTreeSessions.count { it.screenOn != null }
    val totalAppMinutes = displayedTreeSessions.sumOf { it.totalScreenTimeMinutes }

    val allDetectedApps = remember(events, blockedPackages) {
        val map = mutableMapOf<String, String>()
        events.filter { it.eventType == "APP_USAGE" && it.packageName.isNotBlank() }.forEach {
            map[it.packageName] = cleanDisplayName(it.appName, it.packageName)
        }
        blockedPackages.forEach { pkg ->
            if (!map.containsKey(pkg)) {
                map[pkg] = cleanDisplayName("", pkg)
            }
        }
        map.toList().sortedBy { it.second }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("timeline_screen_scroll"),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // --- 1. GÜN SEÇİCİ & FİLTRE BUTONU ---
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Kullanım Zaman Ağacı",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        FilterChip(
                            selected = selectedViewMode == 0,
                            onClick = { selectedViewMode = 0 },
                            label = { Text("Ağaç", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = EmeraldPrimary,
                                selectedLabelColor = Color.Black
                            )
                        )
                        FilterChip(
                            selected = selectedViewMode == 1,
                            onClick = { selectedViewMode = 1 },
                            label = { Text("Liste", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CyanAccent,
                                selectedLabelColor = Color.Black
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(0 to "Bugün", 1 to "Dün", 2 to "2 Gün Önce").forEach { (offset, label) ->
                        val isSelected = selectedDayOffset == offset
                        FilterChip(
                            selected = isSelected,
                            onClick = { onDayOffsetChange(offset) },
                            label = {
                                Text(
                                    text = label,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CyanAccent,
                                selectedLabelColor = Color.Black
                            ),
                            modifier = Modifier.weight(1f).testTag("day_chip_$offset")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // --- 1.1 TÜM EYLEMLER / BOŞ EKRANLARI GÖSTER SWİTCH'İ ---
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("toggle_show_all_events_card"),
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f, fill = false),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (showAllActions) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = null,
                                tint = if (showAllActions) CyanAccent else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Tüm Eylemleri Göster",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1
                                )
                                Text(
                                    text = if (showAllActions) {
                                        "Boş ekran açılışları dahil gösteriliyor"
                                    } else {
                                        if (emptySessionsCount > 0) "$emptySessionsCount boş açılış gizlendi (Uygulamasız)" else "Uygulama açılmayan boş kilitler gizli"
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (showAllActions) CyanAccent else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 10.sp,
                                    maxLines = 1
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Switch(
                            checked = showAllActions,
                            onCheckedChange = { onToggleShowAllActions(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = CyanAccent,
                                uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            modifier = Modifier.testTag("switch_show_all_actions")
                        )
                    }
                }
            }
        }

        // --- 2. KOMPAKT ÖZET METRİK ŞERİDİ ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    CompactMetric(
                        icon = Icons.Default.LockOpen,
                        label = "Açılış",
                        value = "$screenOnCount kez",
                        color = EmeraldPrimary
                    )
                    CompactMetric(
                        icon = Icons.Default.HourglassBottom,
                        label = "Aktif",
                        value = "$totalAppMinutes dk",
                        color = CyanAccent
                    )
                    CompactMetric(
                        icon = Icons.Default.Timeline,
                        label = "Oturum",
                        value = "${displayedTreeSessions.size} dal",
                        color = OrangeFlame
                    )
                    if (blockedPackages.isNotEmpty()) {
                        CompactMetric(
                            icon = Icons.Default.VisibilityOff,
                            label = "Gizlenen",
                            value = "${blockedPackages.size} app",
                            color = RedCritical
                        )
                    }
                }
            }
        }

        // --- 3. LİSTELEME VEYA BOŞ DURUM ---
        if (displayedTreeSessions.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = CyanAccent,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (emptySessionsCount > 0 && !showAllActions) "Boş Ekran Açılışları Gizlendi" else "Kayıt Bulunmuyor",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = when {
                                emptySessionsCount > 0 && !showAllActions -> {
                                    "Bu güne ait $emptySessionsCount adet kilit açma/kapama eylemi var ancak hiçbir uygulama açılmadığı için gizlendi. Görmek için yukarıdaki anahtarı açabilirsiniz."
                                }
                                blockedPackages.isNotEmpty() -> {
                                    "Bazı uygulamalar filtrelendiği için görünmüyor olabilir. Filtre menüsünden kontrol edebilirsiniz."
                                }
                                else -> {
                                    "Ekran açıldıkça ve uygulamalar kullanıldıkça ağaç yapısında listelenecektir."
                                }
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        if (emptySessionsCount > 0 && !showAllActions) {
                            Button(
                                onClick = { onToggleShowAllActions(true) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CyanAccent,
                                    contentColor = Color.Black
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Boş Eylemleri Göster ($emptySessionsCount)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        } else if (blockedPackages.isNotEmpty()) {
                            Button(
                                onClick = {
                                    onClearAllBlockedApps()
                                    Toast.makeText(context, "Tüm gizlenen uygulamalar tekrar aktif edildi.", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = EmeraldPrimary,
                                    contentColor = Color.Black
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Tüm Gizlenenleri Tekrar Aç", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        } else if (onNavigateToSettings != null) {
                            OutlinedButton(
                                onClick = onNavigateToSettings,
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Ayarlar ve İzinleri Görüntüle ⚙️", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        } else {
            if (selectedViewMode == 0) {
                itemsIndexed(displayedTreeSessions) { index, session ->
                    CompactTreeSessionCard(
                        sessionIndex = displayedTreeSessions.size - index,
                        session = session,
                        timeFormat = timeFormat,
                        onAppClick = { appName, pkg, durationSecs ->
                            selectedAppDetail = buildAppTimelineDetail(
                                appName = appName,
                                packageName = pkg,
                                sessionDurationSeconds = durationSecs,
                                eventsForDay = events,
                                allAppUsages = appUsages,
                                liveBattery = liveBattery,
                                selectedDayOffset = selectedDayOffset
                            )
                        },
                        onAppLongClick = { pkg, name ->
                            appToHideCandidate = Pair(pkg, name)
                        },
                        onHideApp = { pkg, name ->
                            appToHideCandidate = Pair(pkg, name)
                        }
                    )
                }
            } else {
                itemsIndexed(displayedVisibleEvents.sortedByDescending { it.timestamp }) { _, event ->
                    CompactFlatNodeItem(
                        event = event,
                        timeFormatted = timeFormat.format(Date(event.timestamp)),
                        onAppClick = {
                            selectedAppDetail = buildAppTimelineDetail(
                                appName = event.appName,
                                packageName = event.packageName,
                                sessionDurationSeconds = if (event.durationSeconds > 0) event.durationSeconds else event.durationMinutes * 60,
                                eventsForDay = events,
                                allAppUsages = appUsages,
                                liveBattery = liveBattery,
                                selectedDayOffset = selectedDayOffset
                            )
                        },
                        onAppLongClick = { pkg, name ->
                            appToHideCandidate = Pair(pkg, name)
                        },
                        onHideApp = {
                            val cleanName = cleanDisplayName(event.appName, event.packageName)
                            appToHideCandidate = Pair(event.packageName, cleanName)
                        }
                    )
                }
            }
        }
    }

    if (showFilterDialog) {
        AppVisibilityFilterDialog(
            allApps = allDetectedApps,
            blockedPackages = blockedPackages,
            onToggleApp = onToggleBlockApp,
            onClearAllBlocked = onClearAllBlockedApps,
            onDismiss = { showFilterDialog = false }
        )
    }

    appToHideCandidate?.let { (pkg, name) ->
        AppHideConfirmationDialog(
            appName = name,
            packageName = pkg,
            onConfirmHide = {
                onToggleBlockApp(pkg)
                Toast.makeText(
                    context,
                    "$name çizelgeden gizlendi. Ayarlar bölümünden istediğiniz zaman tekrar aktif edebilirsiniz.",
                    Toast.LENGTH_LONG
                ).show()
            },
            onViewDetail = {
                val event = events.find { it.packageName == pkg }
                val durationSecs = event?.let { if (it.durationSeconds > 0) it.durationSeconds else it.durationMinutes * 60 } ?: 60
                selectedAppDetail = buildAppTimelineDetail(
                    appName = name,
                    packageName = pkg,
                    sessionDurationSeconds = durationSecs,
                    eventsForDay = events,
                    allAppUsages = appUsages,
                    liveBattery = liveBattery,
                    selectedDayOffset = selectedDayOffset
                )
            },
            onDismiss = { appToHideCandidate = null }
        )
    }

    selectedAppDetail?.let { detail ->
        AppDailyDetailDialog(
            detail = detail,
            onDismiss = { selectedAppDetail = null }
        )
    }
}

/**
 * Kullanıcının istediği tam format:
 * - Ekran açıldı (yeşil, kilit açıldığında 1 kez)
 * - Uygulama Adı (paket adı değil!) ve Saati (örnek: WhatsApp  13:00)
 * - Kullanım süresi: dakika ve saniye (örnek: "Kullanım: 1 dk 25 sn")
 * - Her sayfa/activity için ayrı satır açmaz, tek satırda birleştirir
 * - Hızlı gizleme butonu ile istemediği uygulamayı engelleyebilir
 * - Ekran kapandı (kırmızı, ekran kilitlendiğinde 1 kez)
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CompactTreeSessionCard(
    sessionIndex: Int,
    session: TreeSession,
    timeFormat: SimpleDateFormat,
    onAppClick: (String, String, Int) -> Unit = { _, _, _ -> },
    onAppLongClick: (String, String) -> Unit = { _, _ -> },
    onHideApp: (String, String) -> Unit = { _, _ -> }
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("session_tree_$sessionIndex"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
            // --- Üst Şerit: Oturum Bilgisi ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Oturum #$sessionIndex",
                    style = MaterialTheme.typography.labelSmall,
                    color = EmeraldLight,
                    fontWeight = FontWeight.Bold
                )

                val startHourMin = timeFormat.format(Date(session.startTime))
                val endHourMin = if (session.screenOff != null) timeFormat.format(Date(session.endTime)) else "Şimdi"

                Text(
                    text = "$startHourMin ➔ $endHourMin (${session.totalScreenTimeMinutes} dk)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ==================== 1. EKRAN AÇILDI (YEŞİL, SADECE 1 KEZ) ====================
            val screenOnTime = session.screenOn?.let { Date(it.timestamp) } ?: Date(session.startTime)
            val screenOnHourMin = timeFormat.format(screenOnTime)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .drawBehind {
                        drawLine(
                            color = EmeraldPrimary.copy(alpha = 0.45f),
                            start = Offset(x = 10.dp.toPx(), y = 20.dp.toPx()),
                            end = Offset(x = 10.dp.toPx(), y = size.height),
                            strokeWidth = 2.dp.toPx()
                        )
                    }
                    .padding(bottom = 8.dp),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(EmeraldPrimary.copy(alpha = 0.2f))
                        .border(1.5.dp, EmeraldPrimary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LockOpen,
                        contentDescription = "Ekran Açıldı",
                        tint = EmeraldPrimary,
                        modifier = Modifier.size(11.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Ekran Açıldı",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldLight
                        )
                        Text(
                            text = screenOnHourMin,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = EmeraldLight
                        )
                    }
                    Text(
                        text = "Açıklama: ${session.screenOn?.details?.ifEmpty { "Kilit açıldı" } ?: "Kilit açıldı"}",
                        style = MaterialTheme.typography.labelSmall,
                        color = EmeraldLight.copy(alpha = 0.85f)
                    )
                }
            }

            // ==================== 2. UYGULAMA DALLARI (HER UYGULAMA SADECE 1 KEZ) ====================
            if (session.apps.isEmpty()) {
                val emptyLineColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .drawBehind {
                            drawLine(
                                color = emptyLineColor,
                                start = Offset(x = 10.dp.toPx(), y = 0f),
                                end = Offset(x = 10.dp.toPx(), y = size.height),
                                strokeWidth = 2.dp.toPx()
                            )
                        }
                        .padding(start = 28.dp, top = 2.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = CyanAccent,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Uygulama açılmadı (Kilit açıp kontrol edildi)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }

            session.apps.forEach { app ->
                val appHourMin = timeFormat.format(Date(app.firstStartTime))

                val appColor = when (app.appName) {
                    "YouTube" -> RedCritical
                    "WhatsApp" -> EmeraldPrimary
                    "Instagram" -> OrangeFlame
                    "Spotify" -> EmeraldPrimary
                    "Chrome" -> ElectricBlue
                    "Midas" -> EmeraldLight
                    else -> CyanAccent
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .drawBehind {
                            drawLine(
                                color = CyanAccent.copy(alpha = 0.35f),
                                start = Offset(x = 10.dp.toPx(), y = 0f),
                                end = Offset(x = 10.dp.toPx(), y = size.height),
                                strokeWidth = 2.dp.toPx()
                            )
                        }
                        .padding(bottom = 8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .padding(top = 2.dp, start = 2.dp)
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(appColor.copy(alpha = 0.25f))
                            .border(1.dp, appColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (app.appName == "YouTube") Icons.Default.PlayArrow else Icons.Default.Smartphone,
                            contentDescription = null,
                            tint = appColor,
                            modifier = Modifier.size(9.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .combinedClickable(
                                onClick = {
                                    onAppClick(app.appName, app.packageName, app.totalDurationSeconds)
                                },
                                onLongClick = {
                                    onAppLongClick(app.packageName, app.appName)
                                }
                            )
                            .testTag("session_app_${app.packageName}"),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.22f)
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f, fill = false)
                                ) {
                                    Text(
                                        text = app.appName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Text(
                                    text = appHourMin,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = CyanAccent
                                )
                            }

                            Spacer(modifier = Modifier.height(3.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Kullanım: ${app.formattedDuration}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .clickable { onHideApp(app.packageName, app.appName) }
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                        .testTag("hide_app_${app.packageName}"),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.VisibilityOff,
                                        contentDescription = "Gizle",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "Gizle",
                                        fontSize = 9.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ==================== 3. EKRAN KAPANDI (KIRMIZI, SADECE 1 KEZ) ====================
            if (session.screenOff != null) {
                val screenOffHourMin = timeFormat.format(Date(session.screenOff.timestamp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .drawBehind {
                            drawLine(
                                color = RedCritical.copy(alpha = 0.45f),
                                start = Offset(x = 10.dp.toPx(), y = 0f),
                                end = Offset(x = 10.dp.toPx(), y = 10.dp.toPx()),
                                strokeWidth = 2.dp.toPx()
                            )
                        },
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(RedCritical.copy(alpha = 0.2f))
                            .border(1.5.dp, RedCritical, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Ekran Kapandı",
                            tint = RedCritical,
                            modifier = Modifier.size(11.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Ekran Kapandı",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = RedCritical
                            )
                            Text(
                                text = screenOffHourMin,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = RedCritical
                            )
                        }
                        Text(
                            text = "Açıklama: ${session.screenOff.details.ifEmpty { "Ekran kilitlendi" }}",
                            style = MaterialTheme.typography.labelSmall,
                            color = RedCritical.copy(alpha = 0.85f)
                        )
                    }
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .padding(start = 5.dp)
                            .size(10.dp)
                            .background(EmeraldPrimary, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(15.dp))
                    Text(
                        text = "Ekran şu an açık",
                        style = MaterialTheme.typography.labelSmall,
                        color = EmeraldLight,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

/**
 * Kompakt düz liste öğesi
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CompactFlatNodeItem(
    event: TimelineEventEntity,
    timeFormatted: String,
    onAppClick: () -> Unit = {},
    onAppLongClick: (String, String) -> Unit = { _, _ -> },
    onHideApp: () -> Unit = {}
) {
    val isScreenOn = event.eventType == "SCREEN_ON"
    val isScreenOff = event.eventType == "SCREEN_OFF"

    val durationText = when {
        event.durationSeconds in 1..59 && event.durationMinutes == 0 -> "${event.durationSeconds} saniye"
        event.durationSeconds > 59 && event.durationSeconds % 60 != 0 -> "${event.durationSeconds / 60} dk ${event.durationSeconds % 60} sn"
        event.durationMinutes > 0 -> "${event.durationMinutes} dk"
        event.durationSeconds > 0 -> "${event.durationSeconds} saniye"
        else -> "< 1 dk"
    }

    val itemColor = when {
        isScreenOn -> EmeraldLight
        isScreenOff -> RedCritical
        else -> MaterialTheme.colorScheme.onSurface
    }

    val cleanName = if (isScreenOn) "Ekran Açıldı" else if (isScreenOff) "Ekran Kapandı" else cleanDisplayName(event.appName, event.packageName)

    val icon = when {
        isScreenOn -> Icons.Default.LockOpen
        isScreenOff -> Icons.Default.Lock
        cleanName == "YouTube" -> Icons.Default.PlayArrow
        else -> Icons.Default.Smartphone
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (!isScreenOn && !isScreenOff) {
                    Modifier.combinedClickable(
                        onClick = { onAppClick() },
                        onLongClick = { onAppLongClick(event.packageName, cleanName) }
                    ).testTag("flat_node_${event.packageName}")
                } else Modifier
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isScreenOn -> EmeraldPrimary.copy(alpha = 0.08f)
                isScreenOff -> RedCritical.copy(alpha = 0.08f)
                else -> MaterialTheme.colorScheme.surface
            }
        ),
        border = BorderStroke(
            1.dp,
            when {
                isScreenOn -> EmeraldPrimary.copy(alpha = 0.3f)
                isScreenOff -> RedCritical.copy(alpha = 0.3f)
                else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            isScreenOn -> EmeraldPrimary.copy(alpha = 0.2f)
                            isScreenOff -> RedCritical.copy(alpha = 0.2f)
                            else -> CyanAccent.copy(alpha = 0.15f)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = when {
                        isScreenOn -> EmeraldPrimary
                        isScreenOff -> RedCritical
                        else -> CyanAccent
                    },
                    modifier = Modifier.size(13.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        Text(
                            text = cleanName,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = itemColor
                        )
                    }
                    Text(
                        text = timeFormatted,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isScreenOn) EmeraldLight else if (isScreenOff) RedCritical else CyanAccent
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = when {
                            isScreenOn -> "Açıklama: ${event.details.ifEmpty { "Kilit açıldı" }}"
                            isScreenOff -> "Açıklama: ${event.details.ifEmpty { "Ekran kilitlendi" }}"
                            else -> "Kullanım: $durationText"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = when {
                            isScreenOn -> EmeraldLight.copy(alpha = 0.85f)
                            isScreenOff -> RedCritical.copy(alpha = 0.85f)
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )

                    if (!isScreenOn && !isScreenOff) {
                        IconButton(
                            onClick = onHideApp,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.VisibilityOff,
                                contentDescription = "Gizle",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Zaman çizelgesinde tıklanan uygulamanın günlük internet kullanımı, geçirilen süre,
 * günlük pil tüketimi ve başlatma/ayarlar aksiyonlarını taşıyan detay modeli.
 */
data class AppTimelineDetail(
    val appName: String,
    val packageName: String,
    val sessionDurationFormatted: String?,
    val totalDaySeconds: Int,
    val totalDayFormatted: String,
    val sessionCountToday: Int,
    val wifiBytes: Long,
    val mobileBytes: Long,
    val totalDataFormatted: String,
    val wifiFormatted: String,
    val mobileFormatted: String,
    val estimatedBatteryPercent: Float,
    val estimatedBatteryMah: Int,
    val batteryImpactLevel: String,
    val dayLabel: String
)

/**
 * Uygulama için günlük süre, internet verisi ve pil tüketimini hesaplar.
 */
fun buildAppTimelineDetail(
    appName: String,
    packageName: String,
    sessionDurationSeconds: Int?,
    eventsForDay: List<TimelineEventEntity>,
    allAppUsages: List<AppUsageDataEntity>,
    liveBattery: LiveBatteryInfo?,
    selectedDayOffset: Int
): AppTimelineDetail {
    val cleanName = cleanDisplayName(appName, packageName)
    val dayLabel = when (selectedDayOffset) {
        0 -> "Bugün"
        1 -> "Dün"
        2 -> "2 Gün Önce"
        else -> "$selectedDayOffset Gün Önce"
    }

    val matchingDayEvents = eventsForDay.filter {
        (packageName.isNotBlank() && it.packageName == packageName) ||
        (it.appName.isNotBlank() && it.appName.equals(appName, ignoreCase = true)) ||
        (cleanName.isNotBlank() && it.appName.equals(cleanName, ignoreCase = true))
    }

    val totalSecondsCalculated = matchingDayEvents.sumOf {
        if (it.durationSeconds > 0) it.durationSeconds else it.durationMinutes * 60
    }
    val sessionSecs = sessionDurationSeconds ?: 0
    val totalDaySecs = maxOf(totalSecondsCalculated, sessionSecs).coerceAtLeast(1)

    val dayMins = totalDaySecs / 60
    val daySecsRem = totalDaySecs % 60
    val totalDayFormatted = when {
        dayMins == 0 && daySecsRem > 0 -> "$daySecsRem saniye"
        dayMins > 0 && daySecsRem == 0 -> "$dayMins dakika"
        dayMins > 0 && daySecsRem > 0 -> "$dayMins dk $daySecsRem sn"
        else -> "< 1 saniye"
    }

    val sessionDurationFormatted = if (sessionSecs > 0) {
        val sMins = sessionSecs / 60
        val sSecs = sessionSecs % 60
        when {
            sMins == 0 -> "$sSecs sn"
            sSecs == 0 -> "$sMins dk"
            else -> "$sMins dk $sSecs sn"
        }
    } else null

    val sessionCount = maxOf(matchingDayEvents.size, if (sessionSecs > 0) 1 else 0).coerceAtLeast(1)

    // Veri Kullanımı
    val usageRecord = allAppUsages.find {
        (packageName.isNotBlank() && it.packageName == packageName) ||
        it.appName.equals(appName, ignoreCase = true) ||
        it.appName.equals(cleanName, ignoreCase = true)
    }

    val wifiBytes = usageRecord?.wifiBytes ?: 0L
    val mobileBytes = usageRecord?.mobileBytes ?: 0L
    val totalBytes = wifiBytes + mobileBytes

    fun formatBytes(bytes: Long): String {
        val kb = bytes / 1024.0
        val mb = kb / 1024.0
        val gb = mb / 1024.0
        return when {
            gb >= 1.0 -> "${String.format(Locale.US, "%.1f", gb)} GB"
            mb >= 1.0 -> "${String.format(Locale.US, "%.1f", mb)} MB"
            kb >= 1.0 -> "${String.format(Locale.US, "%.0f", kb)} KB"
            bytes > 0 -> "$bytes B"
            else -> "0 KB"
        }
    }

    val totalDataFormatted = if (totalBytes > 0) formatBytes(totalBytes) else "0 KB"
    val wifiFormatted = if (wifiBytes > 0) formatBytes(wifiBytes) else "0 KB"
    val mobileFormatted = if (mobileBytes > 0) formatBytes(mobileBytes) else "0 KB"

    // Pil Tüketimi
    val allDayAppsTotalSecs = eventsForDay.filter { it.eventType == "APP_USAGE" }
        .sumOf { if (it.durationSeconds > 0) it.durationSeconds else it.durationMinutes * 60 }
        .coerceAtLeast(totalDaySecs)
        .coerceAtLeast(1)

    val appRatio = (totalDaySecs.toFloat() / allDayAppsTotalSecs).coerceIn(0.01f, 1.0f)
    val baseDailyDrain = if ((liveBattery?.dailyDrainPercent ?: 0) > 0) {
        liveBattery!!.dailyDrainPercent.toFloat()
    } else {
        18f
    }

    val estimatedBatteryPercent = (baseDailyDrain * appRatio).coerceIn(0.1f, 95f)
    val estimatedBatteryMah = (estimatedBatteryPercent * 50).toInt().coerceAtLeast(5)
    val batteryImpactLevel = when {
        estimatedBatteryPercent >= 6.0f -> "Yüksek Tüketim ⚡⚡"
        estimatedBatteryPercent >= 2.0f -> "Dengeli Tüketim ⚡"
        else -> "Düşük / Tasarruflu 🌱"
    }

    return AppTimelineDetail(
        appName = cleanName,
        packageName = packageName,
        sessionDurationFormatted = sessionDurationFormatted,
        totalDaySeconds = totalDaySecs,
        totalDayFormatted = totalDayFormatted,
        sessionCountToday = sessionCount,
        wifiBytes = wifiBytes,
        mobileBytes = mobileBytes,
        totalDataFormatted = totalDataFormatted,
        wifiFormatted = wifiFormatted,
        mobileFormatted = mobileFormatted,
        estimatedBatteryPercent = estimatedBatteryPercent,
        estimatedBatteryMah = estimatedBatteryMah,
        batteryImpactLevel = batteryImpactLevel,
        dayLabel = dayLabel
    )
}

/**
 * Kullanıcının istediği uygulama detay diyaloğu:
 * 1) O gün ne kadar internet kullandı (Hücresel / Wi-Fi / Toplam)
 * 2) O gün uygulamada ne kadar kalındı (Toplam gün süresi + oturum süresi)
 * 3) Günlük pil tüketimi (~%X.X, ~mAh, Tüketim Seviyesi)
 * 4) Uygulamaya gitmek / açmak için doğrudan başlatıcı buton
 */
@Composable
fun AppDailyDetailDialog(
    detail: AppTimelineDetail,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(CyanAccent.copy(alpha = 0.2f))
                        .border(1.5.dp, CyanAccent, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (detail.appName == "YouTube") Icons.Default.PlayArrow else Icons.Default.Smartphone,
                        contentDescription = null,
                        tint = CyanAccent,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = detail.appName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f, fill = false),
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = CyanAccent.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, CyanAccent.copy(alpha = 0.4f))
                        ) {
                            Text(
                                text = detail.dayLabel,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = CyanAccent,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                    if (detail.packageName.isNotBlank() && detail.packageName != "android") {
                        Text(
                            text = detail.packageName,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            maxLines = 1
                        )
                    }
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("app_daily_detail_dialog_content"),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // 1. KART: O GÜN NE KADAR KALINDIĞI (KULLANIM SÜRESİ)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = CyanAccent.copy(alpha = 0.08f)
                    ),
                    border = BorderStroke(1.dp, CyanAccent.copy(alpha = 0.35f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccessTime,
                                    contentDescription = null,
                                    tint = CyanAccent,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "O Gün Kalınan Süre",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = CyanAccent
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = detail.totalDayFormatted,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.End
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        if (detail.sessionDurationFormatted != null) {
                            Text(
                                text = "• Bu oturumdaki süre: ${detail.sessionDurationFormatted}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = "• Gün içi açılış sıklığı: ${detail.sessionCountToday} kez kullanıldı",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // 2. KART: NE KADAR İNTERNET KULLANDIĞI
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = ElectricBlue.copy(alpha = 0.08f)
                    ),
                    border = BorderStroke(1.dp, ElectricBlue.copy(alpha = 0.35f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Wifi,
                                    contentDescription = null,
                                    tint = ElectricBlue,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "İnternet Veri Harcaması",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = ElectricBlue,
                                    maxLines = 1
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = detail.totalDataFormatted,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.End,
                                maxLines = 1
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CellTower,
                                    contentDescription = null,
                                    tint = EmeraldLight,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Mobil: ${detail.mobileFormatted}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Wifi,
                                    contentDescription = null,
                                    tint = CyanAccent,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Wi-Fi: ${detail.wifiFormatted}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                // 3. KART: GÜNLÜK PİL TÜKETİMİ
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = OrangeFlame.copy(alpha = 0.08f)
                    ),
                    border = BorderStroke(1.dp, OrangeFlame.copy(alpha = 0.35f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.BatteryChargingFull,
                                    contentDescription = null,
                                    tint = OrangeFlame,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Günlük Pil Tüketimi",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = OrangeFlame
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "~%${String.format(Locale.US, "%.1f", detail.estimatedBatteryPercent)}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = OrangeFlame,
                                textAlign = TextAlign.End
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Tahmini Güç: ~${detail.estimatedBatteryMah} mAh",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = OrangeFlame.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = detail.batteryImpactLevel,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = OrangeFlame,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // 4. AKSİYON: UYGULAMAYI KULLAN / GİT
                Button(
                    onClick = {
                        val pm = context.packageManager
                        val launchIntent = if (detail.packageName.isNotBlank()) pm.getLaunchIntentForPackage(detail.packageName) else null
                        if (launchIntent != null) {
                            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            try {
                                context.startActivity(launchIntent)
                                onDismiss()
                            } catch (e: Exception) {
                                Toast.makeText(context, "${detail.appName} başlatılamadı: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            try {
                                val settingsIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                    data = Uri.fromParts("package", detail.packageName, null)
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                context.startActivity(settingsIntent)
                                onDismiss()
                            } catch (_: Exception) {
                                Toast.makeText(context, "${detail.appName} başlatılamadı veya sistem bileşeni.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("btn_launch_app_from_dialog"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyanAccent,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.Launch, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Uygulamayı Aç / Kullan", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

                OutlinedButton(
                    onClick = {
                        try {
                            val settingsIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", detail.packageName, null)
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(settingsIntent)
                            onDismiss()
                        } catch (_: Exception) {
                            Toast.makeText(context, "Sistem ayarları açılamadı.", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("btn_app_settings_from_dialog"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Sistem Uygulama Ayarları", fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("btn_dismiss_app_detail")
            ) {
                Text("Kapat", fontWeight = FontWeight.Bold)
            }
        }
    )
}

/**
 * Uzun basma sonrasında kullanıcıya uygulamayı gizleme / engelleme onay penceresi.
 */
@Composable
fun AppHideConfirmationDialog(
    appName: String,
    packageName: String,
    onConfirmHide: () -> Unit,
    onViewDetail: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(OrangeFlame.copy(alpha = 0.2f))
                    .border(1.5.dp, OrangeFlame, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.VisibilityOff,
                    contentDescription = null,
                    tint = OrangeFlame,
                    modifier = Modifier.size(24.dp)
                )
            }
        },
        title = {
            Text(
                text = "$appName Gizlensin mi?",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Bu uygulamayı ($appName) zaman çizelgesinde görmek istemiyorsanız gizleyebilirsiniz.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = CyanAccent.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, CyanAccent.copy(alpha = 0.35f))
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = CyanAccent,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "💡 İstediğiniz zaman ekranın üstündeki panelden veya filtre menüsünden tek tıkla tekrar aktif edebilirsiniz.",
                            style = MaterialTheme.typography.bodySmall,
                            color = CyanAccent
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirmHide()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = OrangeFlame,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("confirm_hide_app_button")
            ) {
                Icon(Icons.Default.VisibilityOff, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Gizle (Engelle)", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedButton(
                    onClick = {
                        onDismiss()
                        onViewDetail()
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Detayları Gör", fontSize = 12.sp)
                }
                TextButton(onClick = onDismiss) {
                    Text("İptal")
                }
            }
        }
    )
}

/**
 * Görmek istenmeyen uygulamaların engellendiği ve istendiğinde tekrar açılabildiği filtreleme paneli.
 */
@Composable
fun AppVisibilityFilterDialog(
    allApps: List<Pair<String, String>>,
    blockedPackages: Set<String>,
    onToggleApp: (String) -> Unit,
    onClearAllBlocked: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }

    val filteredApps = remember(allApps, searchQuery) {
        if (searchQuery.isBlank()) allApps
        else {
            val q = searchQuery.trim().lowercase(Locale.getDefault())
            allApps.filter { (pkg, name) ->
                name.lowercase(Locale.getDefault()).contains(q) || pkg.lowercase(Locale.getDefault()).contains(q)
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = null,
                        tint = CyanAccent,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Uygulama Görünürlüğü",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Kapat", modifier = Modifier.size(18.dp))
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Zaman çizelgesinde görmek istemediğiniz uygulamaları kapatabilir, istediğinizde buradan tekrar açabilirsiniz.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Arama Kutusu
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Uygulama ara (örn: Sistem, Başlatıcı)", fontSize = 12.sp) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp), tint = CyanAccent)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(20.dp)) {
                                Icon(Icons.Default.Close, contentDescription = "Temizle", modifier = Modifier.size(14.dp))
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanAccent,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("filter_search_field")
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (filteredApps.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (searchQuery.isNotEmpty()) "Eşleşen uygulama bulunamadı." else "Henüz kayıtlı uygulama bulunmuyor.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 300.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(filteredApps) { (pkg, name) ->
                            val isVisible = pkg !in blockedPackages

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isVisible) {
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                    } else {
                                        RedCritical.copy(alpha = 0.1f)
                                    }
                                ),
                                border = BorderStroke(
                                    1.dp,
                                    if (isVisible) MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f) else RedCritical.copy(alpha = 0.35f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    if (isVisible) CyanAccent.copy(alpha = 0.15f) else RedCritical.copy(alpha = 0.15f)
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = if (isVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                                contentDescription = null,
                                                tint = if (isVisible) CyanAccent else RedCritical,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(10.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = name,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isVisible) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                                maxLines = 1
                                            )
                                            Text(
                                                text = if (isVisible) "Çizelgede Görünür (Aktif)" else "Gizlendi (Engellendi)",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (isVisible) EmeraldLight else RedCritical,
                                                maxLines = 1
                                            )
                                        }
                                    }

                                    Switch(
                                        checked = isVisible,
                                        onCheckedChange = {
                                            onToggleApp(pkg)
                                            val actionText = if (isVisible) "$name çizelgeden gizlendi" else "$name tekrar aktif edildi"
                                            Toast.makeText(context, actionText, Toast.LENGTH_SHORT).show()
                                        },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = EmeraldPrimary,
                                            checkedTrackColor = EmeraldPrimary.copy(alpha = 0.3f),
                                            uncheckedThumbColor = Color.Gray,
                                            uncheckedTrackColor = Color.DarkGray
                                        ),
                                        modifier = Modifier.testTag("toggle_app_$pkg")
                                    )
                                }
                            }
                        }
                    }
                }

                if (blockedPackages.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = {
                            onClearAllBlocked()
                            Toast.makeText(context, "Tüm gizlenen uygulamalar tekrar aktif edildi.", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EmeraldPrimary,
                            contentColor = Color.Black
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Tüm Gizlenenleri Tekrar Aç", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = CyanAccent,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Tamam", fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
fun CompactMetric(
    icon: ImageVector,
    label: String,
    value: String,
    color: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(16.dp)
        )
        Column {
            Text(
                text = value,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
