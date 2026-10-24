package com.example.system

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast

/**
 * SystemShortcuts: Android sistem ayarlarına (Pil Tasarrufu, Ekran, Ağ & İnternet,
 * Bildirimler, Üstte Gösterme İzni, Kesin Alarm Yetkisi ve Kullanım Erişimi)
 * güvenli yönlendirme ve çok katmanlı hata yakalama (fallback) sağlayan yardımcı nesne.
 */
object SystemShortcuts {

    /**
     * Uygulamanın diğer uygulamaların üzerinde çizim yapma (Overlay / Canlı Akım Göstergesi) iznine
     * sahip olup olmadığını kontrol eder.
     * @param context Android context.
     * @return İzin verilmişse true, aksi halde false.
     */
    fun canDrawOverlays(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(context)
        } else {
            true
        }
    }

    /**
     * Kullanıcıyı 'Diğer uygulamaların üzerinde görüntüleme' (Overlay) izin sayfasına yönlendirir.
     * Cihaz/ROM uyumsuzluklarına karşı alternatif intent yedekleriyle asla çökmez.
     * @param context Android context.
     */
    fun openOverlaySettings(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
                    data = Uri.parse("package:${context.packageName}")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            } catch (_: Throwable) {
                try {
                    val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                } catch (_: Throwable) {
                    openAppSettings(context)
                }
            }
        } else {
            openAppSettings(context)
        }
    }

    /**
     * Android 12+ (API 31+) cihazlarda kesin alarm (Exact Alarm) kurma yetkisinin olup olmadığını doğrular.
     * @param context Android context.
     * @return Yetki varsa true, aksi halde false.
     */
    fun canScheduleExactAlarms(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
            alarmManager?.canScheduleExactAlarms() ?: true
        } else {
            true
        }
    }

    /**
     * Kullanıcıyı sistemin Kesin Alarm (Exact Alarm) izin sayfasına yönlendirir.
     * @param context Android context.
     */
    fun openExactAlarmSettings(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    data = Uri.parse("package:${context.packageName}")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            } catch (_: Throwable) {
                openAppSettings(context)
            }
        } else {
            openAppSettings(context)
        }
    }

    /**
     * Cihazın yerel Pil Tasarrufu (Battery Saver) ve Güç Tüketimi ayarlarına yönlendirir.
     * @param context Android context.
     */
    fun openBatterySaverSettings(context: Context) {
        val intents = listOf(
            Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS),
            Intent(Intent.ACTION_POWER_USAGE_SUMMARY),
            Intent(Settings.ACTION_SETTINGS)
        )
        for (intent in intents) {
            try {
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
                return
            } catch (_: Throwable) {}
        }
        try {
            openAppSettings(context)
        } catch (_: Throwable) {
            Toast.makeText(context, "Sistem Pil Ayarları açılamadı", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Cihazın Ağ, Wi-Fi ve Mobil Veri ayarlarına güvenli bir şekilde yönlendirir.
     * (Eski ACTION_DATA_ROAMING_SETTINGS çökmesini önleyen kademeli fallback mimarisi).
     * @param context Android context.
     */
    fun openDataUsageSettings(context: Context) {
        val intents = listOf(
            Intent(Settings.ACTION_WIRELESS_SETTINGS),
            Intent(Settings.ACTION_DATA_USAGE_SETTINGS),
            Intent(Settings.ACTION_NETWORK_OPERATOR_SETTINGS),
            Intent(Settings.ACTION_WIFI_SETTINGS),
            Intent(Settings.ACTION_SETTINGS)
        )
        for (intent in intents) {
            try {
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
                return
            } catch (_: Throwable) {}
        }
        try {
            openAppSettings(context)
        } catch (_: Throwable) {
            Toast.makeText(context, "Ağ Ayarları açılamadı", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Android UsageStatsManager üzerinden ekran ve uygulama kullanım verilerini okuyabilmek için
     * 'Kullanım Erişimi' (Usage Access) izin sayfasına yönlendirir.
     * @param context Android context.
     */
    fun openUsageAccessSettings(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (_: Throwable) {
            try {
                val appDetails = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.parse("package:${context.packageName}")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(appDetails)
            } catch (_: Throwable) {
                Toast.makeText(context, "Kullanım erişimi ayarları açılamadı", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Cihazın Ekran Parlaklığı, Uyku Süresi ve Yenileme Hızı ayarlarına yönlendirir.
     * @param context Android context.
     */
    fun openDisplaySettings(context: Context) {
        val intents = listOf(
            Intent(Settings.ACTION_DISPLAY_SETTINGS),
            Intent(Settings.ACTION_SETTINGS)
        )
        for (intent in intents) {
            try {
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
                return
            } catch (_: Throwable) {}
        }
        try {
            openAppSettings(context)
        } catch (_: Throwable) {
            Toast.makeText(context, "Ekran ayarları açılamadı", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Uygulamanın bildirim izinleri ve bildirim kanalları sayfasına yönlendirir.
     * @param context Android context.
     */
    fun openNotificationSettings(context: Context) {
        try {
            val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                    putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
            } else {
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.parse("package:${context.packageName}")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
            }
            context.startActivity(intent)
        } catch (_: Throwable) {
            openAppSettings(context)
        }
    }

    /**
     * Uygulamanın genel Android sistem uygulama detayları (Depolama, İzinler, Pil İyileştirmesi) sayfasına yönlendirir.
     * @param context Android context.
     */
    fun openAppSettings(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:${context.packageName}")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (_: Throwable) {
            Toast.makeText(context, "Uygulama ayarları açılamadı", Toast.LENGTH_SHORT).show()
        }
    }
}
