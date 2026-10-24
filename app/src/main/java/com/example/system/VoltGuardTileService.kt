package com.example.system

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.example.MainActivity

/**
 * 🎛️ Android Hızlı Ayarlar (Quick Settings) Kutucuk Servisi (VoltGuardTileService)
 * 
 * Bildirim panelindeki hızlı kutucuğa dokunulduğunda tek dokunuşla RAM temizliği ve
 * arka plan batarya optimizasyonu yapar; anlık pil yüzdesi ve sıcaklığı dinamik gösterir.
 */
@RequiresApi(Build.VERSION_CODES.N)
class VoltGuardTileService : TileService() {

    /**
     * Hızlı ayarlar paneli açıldığında kutucuğun üzerindeki pil durumu ve yazıyı günceller.
     */
    override fun onStartListening() {
        super.onStartListening()
        updateTileState()
    }

    /**
     * Kullanıcı hızlı ayar kutucuğuna dokunduğunda tetiklenir:
     * - Arka plan işlemlerini sonlandırır ve RAM boşaltır.
     * - Bildirim çubuğunu günceller ve Toast bildirimi gösterir.
     */
    @SuppressLint("StartActivityAndCollapseDeprecated")
    override fun onClick() {
        super.onClick()
        try {
            // Hızlı RAM ve batarya optimizasyonunu çalıştır
            val runningApps = BackgroundAppsHelper.getRunningBackgroundApps(applicationContext)
            BackgroundAppsHelper.killAllBackgroundProcesses(applicationContext, runningApps)

            val battery = BatteryMonitor.getImmediateBatteryInfo(applicationContext)
            val tile = qsTile ?: return

            tile.state = Tile.STATE_ACTIVE
            tile.label = "VoltGuard"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                tile.subtitle = "%${battery.percentage} • Optimize Edildi ⚡"
            }
            tile.updateTile()

            Toast.makeText(
                applicationContext,
                "⚡ VoltGuard: Bellek temizlendi ve arka plan optimize edildi! (%${battery.percentage} - ${String.format(java.util.Locale.US, "%.1f", battery.temperatureCelsius)}°C)",
                Toast.LENGTH_SHORT
            ).show()

            // Sürekli bildirimi güncelle
            val notification = NotificationHelper.buildForegroundServiceNotification(
                applicationContext,
                battery.percentage,
                battery.temperatureCelsius,
                battery.isCharging
            )
            val nm = getSystemService(NOTIFICATION_SERVICE) as? android.app.NotificationManager
            nm?.notify(VoltGuardService.NOTIFICATION_ID, notification)
        } catch (e: Exception) {
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                val pendingIntent = android.app.PendingIntent.getActivity(
                    this,
                    0,
                    intent,
                    android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
                )
                startActivityAndCollapse(pendingIntent)
            } else {
                @Suppress("DEPRECATION")
                startActivityAndCollapse(intent)
            }
        }
    }

    /**
     * Kutucuk durumunu ve başlığını anlık pil verisine göre tazeler.
     */
    private fun updateTileState() {
        try {
            val battery = BatteryMonitor.getImmediateBatteryInfo(applicationContext)
            val tile = qsTile ?: return

            tile.state = Tile.STATE_INACTIVE
            tile.label = "VoltGuard"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val statusText = if (battery.isCharging) "⚡ %${battery.percentage} (Şarj)" else "🔋 %${battery.percentage} (${String.format(java.util.Locale.US, "%.0f", battery.temperatureCelsius)}°C)"
                tile.subtitle = statusText
            }
            tile.updateTile()
        } catch (_: Exception) {}
    }
}
