package com.example.system

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import java.util.Locale

/**
 * 📱 Ana Ekran Batarya Widget Sağlayıcısı (VoltGuardWidgetProvider)
 * 
 * Android ana ekranında anlık şarj durumu, sıcaklık, tahmini kalan süre ve şarj gücünü
 * (Watt) dinamik olarak günceller ve dokunulduğunda VoltGuard uygulamasını açar.
 */
class VoltGuardWidgetProvider : AppWidgetProvider() {

    /**
     * Periyodik aralıklarla veya sistem isteğiyle widget'ların güncellenmesini sağlar.
     */
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    companion object {
        /**
         * Ana ekrandaki tüm VoltGuard widget örneklerini anında canlı verilerle yeniler.
         */
        fun updateAllWidgets(context: Context) {
            try {
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val thisWidget = ComponentName(context, VoltGuardWidgetProvider::class.java)
                val allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)
                for (widgetId in allWidgetIds) {
                    updateAppWidget(context, appWidgetManager, widgetId)
                }
            } catch (_: Exception) {}
        }

        /**
         * Tek bir widget örneğinin görsel öğelerini (RemoteViews) hazırlar ve sisteme bağlar.
         */
        private fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            try {
                val battery = BatteryMonitor.getImmediateBatteryInfo(context)
                val views = RemoteViews(context.packageName, R.layout.widget_voltguard)

                // Pil yüzdesi ve sıcaklığı ata
                views.setTextViewText(R.id.widget_battery_percent, "%${battery.percentage}")
                views.setTextViewText(R.id.widget_temp, "${String.format(Locale.US, "%.1f", battery.temperatureCelsius)}°C")
                views.setTextViewText(R.id.widget_status, battery.status)

                val remainingText = if (battery.isCharging) {
                    "Doluma: ${String.format(Locale.US, "%.1f", battery.estimatedHoursRemaining)} sa"
                } else {
                    "Kalan: ${String.format(Locale.US, "%.1f", battery.estimatedHoursRemaining)} sa"
                }
                views.setTextViewText(R.id.widget_remaining, remainingText)

                if (battery.isCharging && battery.chargingWattage > 0) {
                    views.setViewVisibility(R.id.widget_power_watt, View.VISIBLE)
                    views.setTextViewText(
                        R.id.widget_power_watt,
                        "Güç: ${String.format(Locale.US, "%.1f", battery.chargingWattage)}W • ${battery.chargeSpeedCategory}"
                    )
                } else {
                    views.setViewVisibility(R.id.widget_power_watt, View.GONE)
                }

                // Widget'a dokunulduğunda uygulamayı açacak PendingIntent
                val intent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                val pendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)

                appWidgetManager.updateAppWidget(appWidgetId, views)
            } catch (_: Exception) {}
        }
    }
}
