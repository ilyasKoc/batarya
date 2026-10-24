package com.example.system

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * 🔄 Servis Canlandırma ve Bekçi Alıcısı (RestartReceiver)
 * 
 * Sistem bellek baskısı nedeniyle VoltGuardService sonlandırıldığında veya
 * AlarmManager 15 dakikalık Watchdog sinyali gönderdiğinde servisi anında yeniden başlatır.
 */
class RestartReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "RestartReceiver"
        const val ACTION_RESTART_SERVICE = "com.example.system.ACTION_RESTART_SERVICE"
        const val ACTION_WATCHDOG_PING = "com.example.system.ACTION_WATCHDOG_PING"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return

        Log.d(TAG, "RestartReceiver tetiklendi: ${intent.action}")

        if (AutoStartHelper.isBackgroundProtectionEnabled(context)) {
            Log.d(TAG, "VoltGuardService arka planda yeniden canlandırılıyor...")
            VoltGuardService.startService(context)
            // Süreklilik için watchdog alarmını yenile
            AutoStartHelper.scheduleWatchdog(context)
        }
    }
}
