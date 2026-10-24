package com.example.system

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import java.util.Locale

object AppNamesHelper {

    private val GENERIC_NAMES = setOf(
        "app", "apps", "application", "uygulama", "android", "mobile", "client", "dev",
        "release", "main", "phone", "lite", "pro", "free", "beta", "null", "unknown", "system"
    )

    private val GENERIC_PACKAGE_SEGMENTS = setOf(
        "com", "org", "net", "io", "co", "tr", "gov", "edu", "app", "apps", "android",
        "mobile", "client", "dev", "release", "main", "phone", "lite", "pro", "free",
        "beta", "ui", "subesi", "kapida", "b2c", "prod", "google"
    )

    val KNOWN_APPS = mapOf(
        // Yatırım & Borsa & Finans
        "com.getmidas.app" to "Midas",
        "com.midas.app" to "Midas",
        "com.getmidas.android" to "Midas",
        "com.getmidas" to "Midas",
        "com.papara.app" to "Papara",
        "com.papara" to "Papara",
        "com.binance.dev" to "Binance",
        "com.trbinance.app" to "Binance TR",
        "com.btcturk.pro" to "BtcTurk | PRO",
        "com.btcturk" to "BtcTurk",
        "com.paribu" to "Paribu",
        "com.garanti.cepsubesi" to "Garanti BBVA",
        "com.akbank.android.apps.akbank_direkt" to "Akbank",
        "com.finansbank.enpara" to "Enpara.com",
        "com.enpara.app" to "Enpara.com",
        "com.qnbfinansbank.digital" to "QNB Finansbank",
        "com.ykb.android" to "Yapı Kredi",
        "com.yapikredi.mobile" to "Yapı Kredi",
        "com.vakifbank.mobile" to "VakıfBank",
        "com.ziraat.ziraatmobil" to "Ziraat Mobil",
        "com.isbank.iscep" to "İşCep",
        "com.teb" to "TEB",
        "com.denizbank.mobildenz" to "MobilDeniz",
        "com.kuveytturk.mobil" to "Kuveyt Türk",
        "com.albarakaturk.mobil" to "Albaraka Türk",
        "com.halkbank.sube" to "Halkbank",
        "com.tosla.app" to "Tosla",
        "com.paycell" to "Paycell",
        "com.nays" to "Nays",
        "com.hadi.app" to "Hadi",
        "com.ininal" to "İninal",
        "com.foreks.mobile.android" to "Foreks",
        "com.matriksdata.matriksmobile" to "Matriks",
        "com.tradingview.tradingviewapp" to "TradingView",

        // Alışveriş & Yemek
        "com.trendyol.trendyol" to "Trendyol",
        "com.hepsiburada" to "Hepsiburada",
        "com.pozitron.hepsiburada" to "Hepsiburada",
        "com.amazon.mShop.android.shopping" to "Amazon",
        "com.sahibinden" to "Sahibinden",
        "com.yemeksepeti.android" to "Yemeksepeti",
        "com.getir" to "Getir",
        "com.migros.sanalmarket" to "Migros Sanal Market",
        "com.sokmarket" to "Şok Market",
        "com.a101.kapida" to "A101 Kapıda",
        "com.bim.bimcell" to "BİM",
        "com.dolap.android" to "Dolap",
        "com.gardrops" to "Gardrops",

        // Sosyal & İletişim & Eğlence
        "com.whatsapp" to "WhatsApp",
        "com.whatsapp.w4b" to "WhatsApp Business",
        "org.telegram.messenger" to "Telegram",
        "org.telegram.messenger.web" to "Telegram",
        "com.instagram.android" to "Instagram",
        "com.twitter.android" to "X",
        "com.x.android" to "X",
        "com.facebook.katana" to "Facebook",
        "com.facebook.orca" to "Messenger",
        "com.facebook.lite" to "Facebook Lite",
        "com.zhiliaoapp.musically" to "TikTok",
        "com.ss.android.ugc.trill" to "TikTok",
        "com.google.android.youtube" to "YouTube",
        "com.google.android.apps.youtube.music" to "YouTube Music",
        "com.spotify.music" to "Spotify",
        "com.netflix.mediaclient" to "Netflix",
        "com.discord" to "Discord",
        "com.reddit.frontpage" to "Reddit",
        "com.pinterest" to "Pinterest",
        "com.snapchat.android" to "Snapchat",
        "com.linkedin.android" to "LinkedIn",
        "com.twitch.android.app" to "Twitch",
        "com.ubercab" to "Uber",
        "com.bitaksi.mop" to "BiTaksi",
        "com.marti.scooter" to "Martı",

        // Sistem & Google Servisleri
        "android" to "Sistem",
        "com.android.systemui" to "Sistem Arayüzü",
        "com.google.android.apps.messaging" to "Mesajlar",
        "com.android.mms" to "Mesajlar",
        "com.google.android.dialer" to "Telefon",
        "com.samsung.android.dialer" to "Telefon",
        "com.android.dialer" to "Telefon",
        "com.google.android.apps.photos" to "Fotoğraflar",
        "com.sec.android.gallery3d" to "Galeri",
        "com.android.chrome" to "Chrome",
        "com.google.android.gm" to "Gmail",
        "com.google.android.apps.maps" to "Google Haritalar",
        "com.android.vending" to "Google Play",
        "com.google.android.googlequicksearchbox" to "Google",
        "com.android.settings" to "Ayarlar",
        "com.sec.android.app.launcher" to "Sistem Başlatıcı (One UI)",
        "com.google.android.apps.nexuslauncher" to "Sistem Başlatıcı (Pixel)",
        "com.android.launcher" to "Sistem Başlatıcı",
        "com.android.launcher3" to "Sistem Başlatıcı",
        "com.miui.home" to "Sistem Başlatıcı (MIUI)",
        "com.huawei.android.launcher" to "Sistem Başlatıcı (Huawei)",
        "com.oppo.launcher" to "Sistem Başlatıcı (ColorOS)",
        "com.oneplus.launcher" to "Sistem Başlatıcı (OxygenOS)",
        "com.transsion.launcher" to "Sistem Başlatıcı",
        "com.teslacoilsw.launcher" to "Nova Launcher",
        "tr.gov.turkiye.edevletkapisi" to "e-Devlet",
        "com.turkcell.hesabim" to "Turkcell",
        "com.vodafone.yanimda" to "Vodafone Yanımda",
        "com.ttnet.onlinemerkez" to "Türk Telekom",
        "com.turktelekom.onlineislemler" to "Türk Telekom"
    )

    fun isGeneric(name: String?): Boolean {
        if (name.isNullOrBlank()) return true
        val clean = name.trim().lowercase(Locale.ROOT)
        return clean in GENERIC_NAMES || clean.contains(".")
    }

    /**
     * Paket adından veya mevcut appName değerinden arayüzde gösterilecek
     * en doğru, okunaklı uygulama adını üretir. Asla "app" veya "uygulama" döndürmez.
     */
    fun cleanDisplayName(appName: String, packageName: String): String {
        // 1. Bilinen eşleşmeler
        KNOWN_APPS[packageName]?.let { return it }

        // 2. Özel isim içeren paket kontrolleri (Midas, Papara, Binance vb.)
        val pkgLower = packageName.lowercase(Locale.ROOT)
        when {
            pkgLower.contains("midas") -> return "Midas"
            pkgLower.contains("papara") -> return "Papara"
            pkgLower.contains("binance") -> return if (pkgLower.contains("tr")) "Binance TR" else "Binance"
            pkgLower.contains("btcturk") -> return if (pkgLower.contains("pro")) "BtcTurk | PRO" else "BtcTurk"
            pkgLower.contains("paribu") -> return "Paribu"
            pkgLower.contains("trendyol") -> return "Trendyol"
            pkgLower.contains("hepsiburada") -> return "Hepsiburada"
            pkgLower.contains("yemeksepeti") -> return "Yemeksepeti"
            pkgLower.contains("getir") -> return "Getir"
            pkgLower.contains("sahibinden") -> return "Sahibinden"
            pkgLower.contains("garanti") -> return "Garanti BBVA"
            pkgLower.contains("akbank") -> return "Akbank"
            pkgLower.contains("yapikredi") || pkgLower.contains("ykb") -> return "Yapı Kredi"
            pkgLower.contains("enpara") -> return "Enpara.com"
            pkgLower.contains("ziraat") -> return "Ziraat Mobil"
            pkgLower.contains("isbank") || pkgLower.contains("iscep") -> return "İşCep"
            pkgLower.contains("vakifbank") -> return "VakıfBank"
            pkgLower.contains("spotify") -> return "Spotify"
            pkgLower.contains("netflix") -> return "Netflix"
            pkgLower.contains("youtube") -> return "YouTube"
            pkgLower.contains("whatsapp") -> return "WhatsApp"
            pkgLower.contains("instagram") -> return "Instagram"
            pkgLower.contains("telegram") -> return "Telegram"
            pkgLower.contains("launcher") -> return "Sistem Başlatıcı"
        }

        // 3. Mevcut appName jenerik değilse ve geçerliyse kullan
        if (!isGeneric(appName) && appName != packageName) {
            return appName.trim()
        }

        // 4. Akıllı paket adı ayrıştırma
        return parseFromPackageName(packageName)
    }

    /**
     * PackageManager üzerinden ve akıllı eşleştirmeyle uygulama adı ve kategori çözer.
     */
    fun resolveAppInfo(packageManager: PackageManager?, pkgName: String): Pair<String, String> {
        val knownName = KNOWN_APPS[pkgName]
        if (knownName != null) {
            val category = detectCategoryForPackage(pkgName)
            return knownName to category
        }

        // Özel paket içerik kontrolü
        val pkgLower = pkgName.lowercase(Locale.ROOT)
        if (pkgLower.contains("midas")) {
            return "Midas" to "Finans & Yatırım"
        }
        if (pkgLower.contains("papara")) {
            return "Papara" to "Finans & Bankacılık"
        }
        if (pkgLower.contains("binance") || pkgLower.contains("btcturk") || pkgLower.contains("paribu")) {
            return cleanDisplayName("", pkgName) to "Kripto & Yatırım"
        }

        var resolvedLabel = ""
        var resolvedCategory = "Uygulama"

        if (packageManager != null) {
            try {
                val appInfo = packageManager.getApplicationInfo(pkgName, 0)
                val label = packageManager.getApplicationLabel(appInfo).toString().trim()
                if (!isGeneric(label) && label != pkgName) {
                    resolvedLabel = label
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    resolvedCategory = when (appInfo.category) {
                        ApplicationInfo.CATEGORY_GAME -> "Oyun"
                        ApplicationInfo.CATEGORY_AUDIO -> "Müzik & Ses"
                        ApplicationInfo.CATEGORY_VIDEO -> "Medya & Video"
                        ApplicationInfo.CATEGORY_IMAGE -> "Fotoğraf"
                        ApplicationInfo.CATEGORY_SOCIAL -> "Sosyal Medya"
                        ApplicationInfo.CATEGORY_NEWS -> "Haberler"
                        ApplicationInfo.CATEGORY_MAPS -> "Harita & Navigasyon"
                        ApplicationInfo.CATEGORY_PRODUCTIVITY -> "Üretkenlik"
                        else -> detectCategoryForPackage(pkgName)
                    }
                } else {
                    resolvedCategory = detectCategoryForPackage(pkgName)
                }
            } catch (_: Exception) {
                resolvedCategory = detectCategoryForPackage(pkgName)
            }
        }

        val finalName = if (resolvedLabel.isNotBlank()) {
            resolvedLabel
        } else {
            cleanDisplayName("", pkgName)
        }

        return finalName to resolvedCategory
    }

    fun detectCategoryForPackage(pkgName: String): String {
        val lower = pkgName.lowercase(Locale.ROOT)
        return when {
            lower.contains("whatsapp") || lower.contains("telegram") || lower.contains("instagram") ||
                    lower.contains("facebook") || lower.contains("twitter") || lower.contains("tiktok") ||
                    lower.contains("snapchat") || lower.contains("discord") || lower.contains("reddit") ||
                    lower.contains("linkedin") || lower.contains("tinder") || lower.contains("social") -> "Sosyal Medya"

            lower.contains("youtube") || lower.contains("netflix") || lower.contains("spotify") ||
                    lower.contains("twitch") || lower.contains("disney") || lower.contains("amazon.video") ||
                    lower.contains("exxen") || lower.contains("blutv") || lower.contains("gain") ||
                    lower.contains("music") || lower.contains("video") || lower.contains("audio") || lower.contains("podcasts") -> "Video & Müzik"

            lower.contains("game") || lower.contains("oyun") || lower.contains("pubg") ||
                    lower.contains("roblox") || lower.contains("brawlstars") || lower.contains("supercell") ||
                    lower.contains("king.candy") || lower.contains("subwaysurf") || lower.contains("riot") ||
                    lower.contains("minecraft") || lower.contains("clash") -> "Oyun"

            lower.contains("trendyol") || lower.contains("hepsiburada") || lower.contains("amazon") ||
                    lower.contains("getir") || lower.contains("yemeksepeti") || lower.contains("migros") ||
                    lower.contains("n11") || lower.contains("pazar") || lower.contains("sahibinden") ||
                    lower.contains("letgo") || lower.contains("aliexpress") || lower.contains("shopping") -> "Alışveriş & Yemek"

            lower.contains("midas") || lower.contains("borsa") || lower.contains("foreks") || lower.contains("matriks") ||
                    lower.contains("bank") || lower.contains("papara") || lower.contains("tosla") || lower.contains("paycell") ||
                    lower.contains("enpara") || lower.contains("binance") || lower.contains("btcturk") || lower.contains("paribu") ||
                    lower.contains("crypto") || lower.contains("iscep") || lower.contains("garanti") || lower.contains("akbank") ||
                    lower.contains("yapikredi") || lower.contains("vakif") || lower.contains("ziraat") -> "Finans & Bankacılık"

            lower.contains("gmail") || lower.contains("outlook") || lower.contains("office") ||
                    lower.contains("word") || lower.contains("excel") || lower.contains("docs") ||
                    lower.contains("drive") || lower.contains("sheets") || lower.contains("slides") ||
                    lower.contains("notion") || lower.contains("trello") || lower.contains("slack") ||
                    lower.contains("zoom") || lower.contains("teams") || lower.contains("calendar") ||
                    lower.contains("notes") || lower.contains("tasks") || lower.contains("keep") -> "Üretkenlik & Ofis"

            lower.contains("chrome") || lower.contains("browser") || lower.contains("maps") ||
                    lower.contains("harita") || lower.contains("camera") || lower.contains("gallery") ||
                    lower.contains("calculator") || lower.contains("clock") || lower.contains("alarm") ||
                    lower.contains("weather") || lower.contains("hava") || lower.contains("android") ||
                    lower.contains("system") || lower.contains("google") || lower.contains("settings") -> "Araçlar & Sistem"

            else -> "Diğer Uygulamalar"
        }
    }

    private fun parseFromPackageName(packageName: String): String {
        val parts = packageName.split(".").filter { it.isNotBlank() }
        if (parts.isEmpty()) return "Uygulama"

        // Sondan başa doğru jenerik olmayan ilk anlamlı parçayı ara
        val meaningful = parts.reversed().firstOrNull { segment ->
            val segLower = segment.lowercase(Locale.ROOT)
            segLower !in GENERIC_PACKAGE_SEGMENTS && segLower.length > 1
        } ?: parts.last()

        var cleaned = meaningful
        val lower = cleaned.lowercase(Locale.ROOT)

        // "getmidas" -> "midas"
        if (lower.startsWith("get") && lower.length > 4) {
            cleaned = cleaned.substring(3)
        }

        // "akbank_direkt" -> "Akbank Direkt"
        cleaned = cleaned.replace("_", " ").replace("-", " ")

        return cleaned.split(" ").filter { it.isNotBlank() }.joinToString(" ") { word ->
            word.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
        }.ifBlank { "Uygulama" }
    }
}
