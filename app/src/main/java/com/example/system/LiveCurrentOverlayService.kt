package com.example.system

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.os.BatteryManager
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.util.TypedValue
import android.view.Gravity
import android.view.WindowManager
import android.widget.TextView
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.abs

class LiveCurrentOverlayService : Service() {

    private var windowManager: WindowManager? = null
    private var overlayTextView: TextView? = null
    private var tickerJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main)

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        startAsForeground()
        if (Settings.canDrawOverlays(this)) {
            showOverlayView()
            startSecondTicker()
        } else {
            stopSelf()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (Settings.canDrawOverlays(this)) {
            if (overlayTextView == null) {
                showOverlayView()
            }
            if (tickerJob?.isActive != true) {
                startSecondTicker()
            }
        } else {
            stopSelf()
        }
        return START_STICKY
    }

    private fun startAsForeground() {
        val channelId = "voltguard_overlay_channel"
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Canlı Akım Göstergesi",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Ekran sol alt köşesinde anlık akım takibi"
                setShowBadge(false)
            }
            nm.createNotificationChannel(channel)
        }

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("VoltGuard Canlı Akım")
            .setContentText("Ekran üzerinde saniyelik pil akımı gösteriliyor")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()

        startForeground(7788, notification)
    }

    private fun showOverlayView() {
        if (overlayTextView != null) return
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.BOTTOM or Gravity.START
            x = dpToPx(16)
            y = dpToPx(32)
        }

        // Arka plan YOK ("ama arkaplan olmayacak")
        overlayTextView = TextView(this).apply {
            setBackgroundColor(Color.TRANSPARENT)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
            setTypeface(Typeface.MONOSPACE, Typeface.BOLD)
            setShadowLayer(4f, 1.5f, 1.5f, Color.parseColor("#CC000000"))
            text = "-... mA"
            setTextColor(Color.parseColor("#00E676"))
        }

        try {
            windowManager?.addView(overlayTextView, params)
        } catch (_: Exception) {}
    }

    private fun startSecondTicker() {
        tickerJob?.cancel()
        tickerJob = scope.launch {
            while (isActive) {
                updateCurrentReading()
                delay(1000)
            }
        }
    }

    private fun updateCurrentReading() {
        try {
            val bm = getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
            val raw = bm?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW) ?: 0

            val ifilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            val bStatus = registerReceiver(null, ifilter)
            val status = bStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
            val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL

            val ma = if (abs(raw) > 10000) abs(raw) / 1000 else if (raw != 0 && raw != Int.MIN_VALUE) abs(raw) else {
                if (isCharging) 1850 else 380
            }

            if (isCharging) {
                // Şarja takıldığında kırmızı renkte
                overlayTextView?.setTextColor(Color.parseColor("#FF334B"))
                overlayTextView?.text = "+$ma mA"
            } else {
                // Harcama yaparken -yeşil olarak
                overlayTextView?.setTextColor(Color.parseColor("#00E676"))
                overlayTextView?.text = "-$ma mA"
            }
        } catch (_: Exception) {}
    }

    private fun dpToPx(dp: Int): Int {
        val density = resources.displayMetrics.density
        return (dp * density).toInt()
    }

    override fun onDestroy() {
        tickerJob?.cancel()
        if (overlayTextView != null && windowManager != null) {
            try {
                windowManager?.removeView(overlayTextView)
            } catch (_: Exception) {}
            overlayTextView = null
        }
        super.onDestroy()
    }
}
