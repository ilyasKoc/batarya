package com.example.system

import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings

/**
 * ⚡ Canlı Akım Yüzen Katman Yöneticisi (LiveCurrentOverlayManager)
 * 
 * Ekranın sol alt köşesinde saniyelik anlık batarya akımını (+/- mA) şeffaf olarak
 * gösteren sistem penceresi (Overlay Service) yaşam döngüsünü ve izin kontrollerini yönetir.
 */
object LiveCurrentOverlayManager {

    /**
     * Yüzen katman özelliğini açar veya kapatır.
     * İzin verilmişse servisi başlatır, aksi halde servisi durdurur.
     */
    fun setEnabled(context: Context, enabled: Boolean) {
        CustomAlarmSoundHelper.setLiveCurrentOverlayEnabled(context, enabled)
        if (enabled) {
            startOverlayServiceIfPermitted(context)
        } else {
            stopOverlayService(context)
        }
    }

    /**
     * Canlı akım göstergesinin kullanıcı ayarlarında açık olup olmadığını kontrol eder.
     */
    fun isEnabled(context: Context): Boolean {
        return CustomAlarmSoundHelper.isLiveCurrentOverlayEnabled(context)
    }

    /**
     * "Diğer uygulamaların üzerinde göster" (SYSTEM_ALERT_WINDOW) izni varsa servisi başlatır.
     */
    fun startOverlayServiceIfPermitted(context: Context) {
        if (Settings.canDrawOverlays(context)) {
            try {
                val intent = Intent(context, LiveCurrentOverlayService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (_: Exception) {}
        }
    }

    /**
     * Yüzen katman servisini güvenle durdurur.
     */
    fun stopOverlayService(context: Context) {
        try {
            val intent = Intent(context, LiveCurrentOverlayService::class.java)
            context.stopService(intent)
        } catch (_: Exception) {}
    }

    /**
     * Uygulama açılışında veya ayar değişiminde katman durumunu senkronize eder.
     */
    fun syncState(context: Context) {
        if (isEnabled(context)) {
            startOverlayServiceIfPermitted(context)
        } else {
            stopOverlayService(context)
        }
    }
}
