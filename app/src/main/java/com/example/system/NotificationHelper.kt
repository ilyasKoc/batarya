package com.example.system

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity

object NotificationHelper {

    private const val CHANNEL_BATTERY_ID = "voltguard_battery_alerts"
    private const val CHANNEL_DATA_ID = "voltguard_data_alerts"
    private const val CHANNEL_APP_LIMIT_ID = "voltguard_app_limit_alerts"
    private const val CHANNEL_POWER_SAVING_ID = "voltguard_power_saving_alerts"
    private const val CHANNEL_SERVICE_ID = "voltguard_persistent_service"
    private const val CHANNEL_CHARGER_WAKE_ID = "voltguard_charger_wake_channel"
    private const val CHANNEL_NIGHT_GUARD_ID = "voltguard_night_guard_alerts"

    const val EXTRA_NAVIGATE_TAB = "NAVIGATE_TAB"
    const val TAB_BATTERY = 0
    const val TAB_TIMELINE = 1
    const val TAB_AI_COACH = 2
    const val TAB_MONTHLY = 3
    const val TAB_SETTINGS = 4

    private fun createActivityPendingIntent(context: Context, requestCode: Int, targetTab: Int): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_MAIN
            addCategory(Intent.CATEGORY_LAUNCHER)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(EXTRA_NAVIGATE_TAB, targetTab)
        }
        return PendingIntent.getActivity(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun initChannels(context: Context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return

                val serviceChannel = NotificationChannel(
                    CHANNEL_SERVICE_ID,
                    "VoltGuard 7/24 Kesintisiz Koruma Servisi",
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "Uygulamanın arka planda ve cihaz açılışında kesintisiz çalışmasını sağlar"
                    setShowBadge(false)
                    enableVibration(false)
                }

                val batteryChannel = NotificationChannel(
                    CHANNEL_BATTERY_ID,
                    "Batarya ve Sıcaklık Alarmları",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Batarya şarj seviyesi ve yüksek sıcaklık uyarıları verir"
                    enableVibration(true)
                }

                val dataChannel = NotificationChannel(
                    CHANNEL_DATA_ID,
                    "Veri Kotası Uyarıları",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "İnternet veri limitleri aşıldığında uyarı bildirimi gönderir"
                    enableVibration(true)
                }

                val appLimitChannel = NotificationChannel(
                    CHANNEL_APP_LIMIT_ID,
                    "Uygulama Kullanım Limiti Uyarıları",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Belirlediğiniz günlük uygulama kullanım limitleri aşıldığında uyarı gönderir"
                    enableVibration(true)
                }

                val powerSavingChannel = NotificationChannel(
                    CHANNEL_POWER_SAVING_ID,
                    "Güç Tasarrufu Hedef Bildirimleri",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Güç tasarrufu modu ile ulaşılan batarya ve süre hedeflerini bildirir"
                    enableVibration(true)
                }

                val chargerWakeChannel = NotificationChannel(
                    CHANNEL_CHARGER_WAKE_ID,
                    "Şarj Bağlantısı Anında Uyandırma Bildirimleri",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Şarj cihazı takıldığında ekranı ve uygulamayı otomatik uyandıran öncelikli bildirim"
                    enableVibration(true)
                    enableLights(true)
                    lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
                }

                val nightGuardChannel = NotificationChannel(
                    CHANNEL_NIGHT_GUARD_ID,
                    "Akıllı Gece Şarj Koruması & Damlama Bildirimleri",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Gece %80 tampon koruması ve uyanış öncesi damlama şarjı bildirimleri"
                    enableVibration(true)
                }

                notificationManager.createNotificationChannel(serviceChannel)
                notificationManager.createNotificationChannel(batteryChannel)
                notificationManager.createNotificationChannel(dataChannel)
                notificationManager.createNotificationChannel(appLimitChannel)
                notificationManager.createNotificationChannel(powerSavingChannel)
                notificationManager.createNotificationChannel(chargerWakeChannel)
                notificationManager.createNotificationChannel(nightGuardChannel)
            }
        } catch (_: Exception) {}
    }

    fun buildForegroundServiceNotification(
        context: Context,
        batteryPercent: Int = 0,
        tempCelsius: Float = 0f,
        isCharging: Boolean = false
    ): android.app.Notification {
        val pendingIntent = createActivityPendingIntent(context, 1000, TAB_BATTERY)

        val statusText = if (batteryPercent > 0) {
            val chargeStatus = if (isCharging) "⚡ Şarj Oluyor" else "🔋 Deşarj Oluyor"
            "%$batteryPercent • ${String.format(java.util.Locale.US, "%.1f", tempCelsius)}°C • $chargeStatus"
        } else {
            "7/24 Kesintisiz Batarya & Veri Koruması Aktif"
        }

        return NotificationCompat.Builder(context, CHANNEL_SERVICE_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_charging)
            .setContentTitle("VoltGuard Canlı Koruma")
            .setContentText(statusText)
            .setSubText("Arka Planda Aktif")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    fun sendFullChargeNotification(context: Context, currentLevel: Int, targetThreshold: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Bildirime tıklandığında doğrudan Batarya sekmesine yönlendir
        val pendingIntent = createActivityPendingIntent(context, 1001, TAB_BATTERY)

        val builder = NotificationCompat.Builder(context, CHANNEL_BATTERY_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_charging)
            .setContentTitle("⚡ Şarj Hedefine Ulaşıldı! (%$currentLevel)")
            .setContentText("Bataryanız %$targetThreshold seviyesine ulaştı. Aşırı şarjı önlemek için fişi çekebilirsiniz.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        notificationManager.notify(1001, builder.build())
    }

    fun sendDataLimitWarning(context: Context, networkName: String, usedMb: Long, limitMb: Long) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val percent = if (limitMb > 0) (usedMb * 100 / limitMb).toInt() else 100

        // Bildirime tıklandığında doğrudan Ayarlar / Limit sekmesine yönlendir
        val pendingIntent = createActivityPendingIntent(context, 1002, TAB_SETTINGS)

        val builder = NotificationCompat.Builder(context, CHANNEL_DATA_ID)
            .setSmallIcon(android.R.drawable.stat_notify_error)
            .setContentTitle("⚠️ $networkName Veri Limiti Aşıldı!")
            .setContentText("Toplam ${usedMb} MB / ${limitMb} MB kullanıldı (%$percent). Kotanızı kontrol ediniz.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        notificationManager.notify(1002, builder.build())
    }

    fun sendBatteryOverheatNotification(context: Context, currentTemp: Float, thresholdTemp: Float) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val pendingIntent = createActivityPendingIntent(context, 1003, TAB_BATTERY)

        val builder = NotificationCompat.Builder(context, CHANNEL_BATTERY_ID)
            .setSmallIcon(android.R.drawable.stat_notify_error)
            .setContentTitle("🔥 Aşırı Batarya Sıcaklığı Uyarısı! (${String.format(java.util.Locale.US, "%.1f", currentTemp)}°C)")
            .setContentText("Batarya sıcaklığı kritik seviye olan ${thresholdTemp.toInt()}°C üzerine çıktı. Şarjı durdurmanız ve cihazı dinlendirmeniz önerilir.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        notificationManager.notify(1003, builder.build())
    }

    fun sendAppLimitWarning(context: Context, appName: String, usedMinutes: Int, limitMinutes: Int, isExceeded: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val pendingIntent = createActivityPendingIntent(context, 1004, TAB_MONTHLY)

        val title = if (isExceeded) {
            "⏱️ Dijital Denge: $appName Limiti Aşıldı!"
        } else {
            "⏱️ Dijital Denge: $appName Süreniz Dolmak Üzere"
        }

        val text = if (isExceeded) {
            "Bugün $appName için belirlenen $limitMinutes dk limit aşıldı (Kullanım: $usedMinutes dk). Mola vermeyi unutmayın."
        } else {
            "Bugün $appName için $usedMinutes dk harcadınız. Günlük $limitMinutes dk limitine ulaşmak üzeresiniz."
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_APP_LIMIT_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        val notificationId = 2000 + (appName.hashCode() % 5000)
        notificationManager.notify(notificationId, builder.build())
    }

    fun sendPowerSavingGoalNotification(
        context: Context,
        savedPercent: Float,
        savedMah: Float,
        targetPercent: Float,
        activeDurationText: String,
        isCustomTest: Boolean = false
    ) {
        initChannels(context)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return

        // Bildirime tıklandığında Aylık Analiz / Pro Labs sekmesine yönlendir
        val pendingIntent = createActivityPendingIntent(context, 1005, TAB_MONTHLY)

        val title = if (isCustomTest) {
            "⚡ Test: Güç Tasarrufu Bildirim Sistemi Aktif!"
        } else {
            "🎉 Tebrikler! Güç Tasarruf Hedefine Ulaşıldı (Hedef: %${String.format(java.util.Locale.US, "%.0f", targetPercent)})"
        }

        val formattedPercent = String.format(java.util.Locale.US, "%.1f", savedPercent)
        val formattedMah = savedMah.toInt()

        val text = if (isCustomTest) {
            "Güç tasarrufu bildirimleriniz başarıyla yapılandırıldı. Tasarruf hedeflerine ulaştığınızda anlık uyarı alacaksınız."
        } else {
            "Bugün Güç Tasarrufu Modu sayesinde cihazınızda +%$formattedPercent ($formattedMah mAh) batarya kurtarıldı! (Toplam Tasarruf Süresi: $activeDurationText)"
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_POWER_SAVING_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_charging)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("$text\n\n💡 İpucu: Güç tasarrufu modu ekran frekansını ve arka plan senkronizasyonunu optimize ederek pil ömrünü uzatır.")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        val notificationId = if (isCustomTest) 3001 else 3000
        notificationManager.notify(notificationId, builder.build())
    }

    fun sendAutoPowerSaveNotification(
        context: Context,
        isActivated: Boolean,
        timeRange: String,
        modeTitle: String
    ) {
        initChannels(context)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
        val pendingIntent = createActivityPendingIntent(context, 1006, TAB_BATTERY)

        val title = if (isActivated) {
            "🌙 Akıllı Çizelge: $modeTitle Aktif Edildi"
        } else {
            "☀️ Akıllı Çizelge: Normal Güç Moduna Dönüldü"
        }

        val message = if (isActivated) {
            "Planlanan saat aralığında ($timeRange) bataryanızı korumak için sistem otomatik güç tasarrufuna geçti."
        } else {
            "Planlanan tasarruf aralığı tamamlandı. Cihazınız normal çalışma moduna geri alındı."
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_POWER_SAVING_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_charging)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        notificationManager.notify(3002, builder.build())
    }

    fun sendSmartChargingReminder(
        context: Context,
        batteryPercent: Int,
        hoursRemainingText: String,
        recommendedSlotText: String
    ) {
        initChannels(context)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
        val pendingIntent = createActivityPendingIntent(context, 1007, TAB_BATTERY)

        val title = "⚡ Akıllı Şarj Hatırlatıcısı: Pil Seviyesi %$batteryPercent"
        val message = "Piliniz kritik eşiğe yaklaşıyor (tahmini $hoursRemainingText). Akıllı çizelgenize göre önerilen şarj aralığı: $recommendedSlotText."

        val builder = NotificationCompat.Builder(context, CHANNEL_BATTERY_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_charging)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("$message\n\n💡 İpucu: Bataryayı %20-%85 aralığında tutmak lityum-iyon hücrelerinin ömrünü 2 katına çıkarır.")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        notificationManager.notify(3003, builder.build())
    }

    fun sendPowerConnectedNotification(
        context: Context,
        currentLevel: Int,
        plugType: String,
        wattage: Float
    ) {
        initChannels(context)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
        val pendingIntent = createActivityPendingIntent(context, 1008, TAB_BATTERY)

        val speedText = if (wattage > 0) " (${String.format(java.util.Locale.US, "%.1f", wattage)}W)" else ""
        val title = "⚡ Şarj Cihazı Takıldı • VoltGuard Aktif!"
        val message = "Şarj bağlantısı algılandı (%$currentLevel - $plugType$speedText). VoltGuard canlı izleme ekranı otomatik olarak uyandırıldı."

        val builder = NotificationCompat.Builder(context, CHANNEL_CHARGER_WAKE_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_charging)
            .setContentTitle(title)
            .setContentText("Pil: %$currentLevel • $plugType$speedText • Canlı Koruma Devrede")
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setFullScreenIntent(pendingIntent, true)
            .setContentIntent(pendingIntent)
            .addAction(android.R.drawable.ic_menu_view, "Uygulamayı Aç", pendingIntent)
            .setAutoCancel(true)

        notificationManager.notify(1008, builder.build())
    }

    /**
     * 🌙 Gece %80 Tampon Koruması Aktif Bildirimi
     */
    fun sendNightChargingBufferNotification(
        context: Context,
        currentPercent: Int,
        targetBuffer: Int,
        wakeUpTimeFormatted: String,
        isTrickleScheduled: Boolean
    ) {
        initChannels(context)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
        val pendingIntent = createActivityPendingIntent(context, 1009, TAB_BATTERY)

        val title = "🌙 Akıllı Gece Tamponu Devrede (%$currentPercent)"
        val message = if (isTrickleScheduled) {
            "Bataryanız %$targetBuffer tampon seviyesinde kilitlendi (4.05V). Sabah $wakeUpTimeFormatted uyanışınızdan önce damlama şarjı ile nazikçe %100'e tamamlanacak."
        } else {
            "Bataryanız %$targetBuffer tampon seviyesine ulaştı. Gece boyu aşırı voltaj stresi ve şişme önleniyor."
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_NIGHT_GUARD_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_charging)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("$message\n\n🛡️ Hücre Koruması: 4.35V tepe gerilim engellendi (+%28 daha uzun döngü ömrü).")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        notificationManager.notify(4001, builder.build())
    }

    /**
     * ⚡ Uyanış Saatine Göre Damlama Şarjı (Trickle Charge) Başladı Bildirimi
     */
    fun sendNightTrickleTopupNotification(
        context: Context,
        currentPercent: Int,
        wakeUpTimeFormatted: String
    ) {
        initChannels(context)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
        val pendingIntent = createActivityPendingIntent(context, 1010, TAB_BATTERY)

        val title = "⚡ Uyanış Damlama Şarjı Başladı (%$currentPercent)"
        val message = "Uyanış saatiniz ($wakeUpTimeFormatted) yaklaştığı için %80 tampon çözüldü. Batarya düşük akımla nazikçe tam şarja tamamlanıyor."

        val builder = NotificationCompat.Builder(context, CHANNEL_NIGHT_GUARD_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_charging)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        notificationManager.notify(4002, builder.build())
    }

    /**
     * ☀️ Sabah Uyanışa Hazır Bildirimi
     */
    fun sendMorningReadyNotification(
        context: Context,
        currentPercent: Int
    ) {
        initChannels(context)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
        val pendingIntent = createActivityPendingIntent(context, 1011, TAB_BATTERY)

        val title = "☀️ Günaydın! Bataryanız Hazır (%$currentPercent)"
        val message = "Cihazınız gece boyu hücre voltaj stresi yaşamadan taze kimyasal denge ile tam şarj seviyesinde hazırlandı. Güne güçlü başlayabilirsiniz."

        val builder = NotificationCompat.Builder(context, CHANNEL_NIGHT_GUARD_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_charging)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        notificationManager.notify(4003, builder.build())
    }
}
