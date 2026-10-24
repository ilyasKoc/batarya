package com.example.system

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.util.Log

/**
 * 📱 Cihazda Yüklü Uygulama Modeli (DeviceAppItem)
 * 
 * @property packageName Uygulamanın benzersiz paket adı (Örn: com.instagram.android)
 * @property appName Uygulamanın temiz ve okunaklı adı (Örn: Instagram)
 * @property category Uygulama kategorisi (Sosyal Medya, Oyun, Üretkenlik vb.)
 * @property isSystemApp Sistem / ROM uygulaması olup olmadığı
 * @property isLaunchable Kullanıcı tarafından ana ekrandan açılabilir olup olmadığı
 * @property firstInstallTime İlk yükleme zaman damgası
 * @property lastUpdateTime Son güncelleme zaman damgası
 */
data class DeviceAppItem(
    val packageName: String,
    val appName: String,
    val category: String,
    val isSystemApp: Boolean,
    val isLaunchable: Boolean = true,
    val firstInstallTime: Long = 0L,
    val lastUpdateTime: Long = 0L
)

/**
 * 🔍 Yüklü Uygulamaları Tarayıcı (DeviceAppScanner)
 * 
 * Cihazda bulunan tüm kullanıcı ve sistem uygulamalarını tarar, launcher aktivitelerini
 * tespit eder ve kategorilerine göre sınıflandırarak listeler.
 */
object DeviceAppScanner {

    /**
     * Cihazdaki tüm yüklü uygulamaları tarar ve sıralı liste halinde döndürür.
     * Kullanıcı uygulamaları ve başlatılabilir uygulamalar önceliklendirilir.
     * 
     * @param context Android context
     * @return Taranmış ve filtrelenmiş DeviceAppItem listesi
     */
    fun scanInstalledApps(context: Context): List<DeviceAppItem> {
        val pm = context.packageManager
        val appsMap = mutableMapOf<String, DeviceAppItem>()

        // 1. Ana ekranda / Uygulama çekmecesinde görünen başlatılabilir uygulamaları sorgula
        try {
            val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }
            val resolveInfos = pm.queryIntentActivities(mainIntent, 0)
            for (info in resolveInfos) {
                val pkg = info.activityInfo.packageName
                if (pkg == context.packageName) continue

                val label = try {
                    info.loadLabel(pm).toString()
                } catch (e: Exception) {
                    ""
                }

                val appInfo = try {
                    pm.getApplicationInfo(pkg, 0)
                } catch (e: Exception) {
                    null
                }

                val isSystem = if (appInfo != null) {
                    (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0 &&
                    (appInfo.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == 0
                } else false

                val cleanName = AppNamesHelper.cleanDisplayName(label, pkg)
                val category = AppNamesHelper.detectCategoryForPackage(pkg)

                appsMap[pkg] = DeviceAppItem(
                    packageName = pkg,
                    appName = cleanName,
                    category = category,
                    isSystemApp = isSystem,
                    isLaunchable = true
                )
            }
        } catch (e: Exception) {
            Log.e("DeviceAppScanner", "Başlatıcı uygulamaları taranırken hata", e)
        }

        // 2. Arka plan servisleri ve sistem paketleri dahil tüm yüklü uygulamaları sorgula
        try {
            val installedApps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
            for (app in installedApps) {
                val pkg = app.packageName
                if (pkg == context.packageName) continue
                if (pkg == "android" || pkg.startsWith("com.android.internal")) continue

                val isSystem = (app.flags and ApplicationInfo.FLAG_SYSTEM) != 0 &&
                               (app.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == 0

                val existing = appsMap[pkg]
                if (existing == null) {
                    val label = try {
                        app.loadLabel(pm).toString()
                    } catch (e: Exception) {
                        ""
                    }
                    val cleanName = AppNamesHelper.cleanDisplayName(label, pkg)
                    val category = AppNamesHelper.detectCategoryForPackage(pkg)

                    appsMap[pkg] = DeviceAppItem(
                        packageName = pkg,
                        appName = cleanName,
                        category = category,
                        isSystemApp = isSystem,
                        isLaunchable = false
                    )
                }
            }
        } catch (e: Exception) {
            Log.e("DeviceAppScanner", "Yüklü paketler taranırken hata", e)
        }

        // 3. Paket kurulum ve güncelleme zaman damgalarını ekle
        try {
            val installedPackages = pm.getInstalledPackages(0)
            for (pkgInfo in installedPackages) {
                val pkg = pkgInfo.packageName
                val item = appsMap[pkg]
                if (item != null) {
                    appsMap[pkg] = item.copy(
                        firstInstallTime = pkgInfo.firstInstallTime,
                        lastUpdateTime = pkgInfo.lastUpdateTime
                    )
                }
            }
        } catch (e: Exception) {
            Log.e("DeviceAppScanner", "Paket tarihleri zenginleştirilirken hata", e)
        }

        // Sıralama: Önce kullanıcı uygulamaları, sonra sistem uygulamaları, ardından alfabetik
        return appsMap.values.sortedWith(
            compareBy<DeviceAppItem> { it.isSystemApp }
                .thenByDescending { it.isLaunchable }
                .thenBy { it.appName.lowercase() }
        )
    }

    /**
     * Belirtilen paketin uygulama simgesini döndürür.
     */
    fun getAppIcon(context: Context, packageName: String): Drawable? {
        return try {
            context.packageManager.getApplicationIcon(packageName)
        } catch (_: Exception) {
            null
        }
    }
}
