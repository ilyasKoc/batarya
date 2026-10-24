package com.example.system

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * 🚀 Cihaz Yeniden Başlatma & Açılış Alıcısı (BootReceiver)
 * 
 * Telefon kapatılıp açıldığında (BOOT_COMPLETED) veya uygulama güncellendiğinde
 * (MY_PACKAGE_REPLACED), VoltGuard arka plan koruma servisini ve bekçi (watchdog)
 * alarmını otomatik olarak yeniden başlatır.
 */
class BootReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "BootReceiver"
    }

    /**
     * Sistem açılış veya paket güncelleme intent'i geldiğinde tetiklenir.
     * Kullanıcı otomatik başlatmayı (AutoStart) açmışsa servisi ayağa kaldırır.
     */
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return

        Log.d(TAG, "BootReceiver tetiklendi: ${intent.action}")

        val bootActions = listOf(
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            "android.intent.action.QUICKBOOT_POWERON",
            "com.htc.intent.action.QUICKBOOT_POWERON",
            Intent.ACTION_REBOOT
        )

        if (bootActions.contains(intent.action) || intent.action == Intent.ACTION_BOOT_COMPLETED) {
            if (AutoStartHelper.isBootStartEnabled(context) && AutoStartHelper.isBackgroundProtectionEnabled(context)) {
                Log.d(TAG, "Cihaz açılışında VoltGuardService ve Watchdog başlatılıyor...")
                VoltGuardService.startService(context)
                AutoStartHelper.scheduleWatchdog(context)
            }
        }
    }
}
