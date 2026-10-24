package com.example.system

import com.example.BuildConfig
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class GeminiPart(
    @Json(name = "text") val text: String? = null
)

@JsonClass(generateAdapter = true)
data class GeminiContent(
    @Json(name = "parts") val parts: List<GeminiPart>
)

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    @Json(name = "contents") val contents: List<GeminiContent>
)

@JsonClass(generateAdapter = true)
data class GeminiCandidate(
    @Json(name = "content") val content: GeminiContent?
)

@JsonClass(generateAdapter = true)
data class GeminiResponse(
    @Json(name = "candidates") val candidates: List<GeminiCandidate>?
)

interface GeminiRestApi {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

data class AiHabitReportResult(
    val score: Int,
    val personaTitle: String,
    val summary: String,
    val strengths: List<String>,
    val risks: List<String>,
    val suggestions: List<String>,
    val isLiveAiGenerated: Boolean
)

object GeminiAiCoachService {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val api: GeminiRestApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiRestApi::class.java)
    }

    suspend fun analyzeDailyHabits(
        screenTimeMinutes: Int,
        unlockCount: Int,
        nightDrainDrop: Int,
        topApps: List<Pair<String, Int>>,
        batteryPercentage: Int,
        completedFocusMinutes: Int,
        appLimitsCount: Int
    ): AiHabitReportResult = withContext(Dispatchers.IO) {
        val apiKey = try { BuildConfig.GEMINI_API_KEY } catch (_: Exception) { "" }

        if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                val prompt = """
                    Sen uzman bir Dijital Sağlık ve Alışkanlık Koçusun (VoltGuard AI).
                    Aşağıdaki gerçek akıllı telefon kullanım metriklerine göre kullanıcıya Türkçe bir dijital sağlık raporu hazırla.
                    
                    Metrikler:
                    - Bugün Toplam Ekran Süresi: $screenTimeMinutes dakika (${screenTimeMinutes / 60} saat ${screenTimeMinutes % 60} dk)
                    - Kilit Açma Sayısı: $unlockCount kez
                    - Gece Uyku Pil Kaybı: %$nightDrainDrop
                    - En Çok Kullanılan Uygulamalar: ${topApps.joinToString(", ") { "${it.first} (${it.second} dk)" }}
                    - Mevcut Pil Seviyesi: %$batteryPercentage
                    - Bugün Tamamlanan Odak Seansı: $completedFocusMinutes dakika
                    - Belirlenen Süre Limitleri Sayısı: $appLimitsCount
                    
                    Lütfen yanıtını SADECE aşağıdaki JSON formatında döndür, başka hiçbir metin ekleme:
                    {
                      "score": 85,
                      "personaTitle": "Dengeli Dijital Kaşif",
                      "summary": "Gününüzün çoğunu üretken geçirdiniz, ancak gece ekran kullanımını sınırlamak uyku kalitenizi artıracaktır.",
                      "strengths": ["Sosyal medyayı 45 dk altında tuttunuz", "Odak seansı ile dikkat dağınıklığını önlediniz"],
                      "risks": ["Sık aralıklarla kilit açma refleksi gözlemlendi", "Gece 23:00 sonrası telefon kontrolü"],
                      "suggestions": ["Akşamları mavi ışık filtresini devreye alın", "Instagram için 40 dk günlük limit belirleyin", "Yatmadan 30 dk önce ekranı bırakın"]
                    }
                """.trimIndent()

                val request = GeminiRequest(
                    contents = listOf(
                        GeminiContent(parts = listOf(GeminiPart(text = prompt)))
                    )
                )

                val response = api.generateContent(apiKey = apiKey, request = request)
                val rawText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text

                if (!rawText.isNullOrBlank()) {
                    val cleanedJson = rawText
                        .replace("```json", "")
                        .replace("```", "")
                        .trim()

                    val parsed = parseJsonResult(cleanedJson)
                    if (parsed != null) {
                        return@withContext parsed.copy(isLiveAiGenerated = true)
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("VoltGuardGemini", "API call failed, falling back to local heuristic engine", e)
            }
        }

        // Heuristic fallback
        return@withContext generateLocalHeuristicReport(
            screenTimeMinutes = screenTimeMinutes,
            unlockCount = unlockCount,
            nightDrainDrop = nightDrainDrop,
            topApps = topApps,
            completedFocusMinutes = completedFocusMinutes
        )
    }

    private fun parseJsonResult(jsonString: String): AiHabitReportResult? {
        return try {
            val json = org.json.JSONObject(jsonString)
            val score = json.optInt("score", 80).coerceIn(10, 100)
            val persona = json.optString("personaTitle", "Dijital Kaşif")
            val summary = json.optString("summary", "Cihaz kullanımınız genel olarak dengeli.")
            
            val strengthsArr = json.optJSONArray("strengths")
            val strengths = mutableListOf<String>()
            if (strengthsArr != null) {
                for (i in 0 until strengthsArr.length()) {
                    strengths.add(strengthsArr.getString(i))
                }
            }

            val risksArr = json.optJSONArray("risks")
            val risks = mutableListOf<String>()
            if (risksArr != null) {
                for (i in 0 until risksArr.length()) {
                    risks.add(risksArr.getString(i))
                }
            }

            val suggestionsArr = json.optJSONArray("suggestions")
            val suggestions = mutableListOf<String>()
            if (suggestionsArr != null) {
                for (i in 0 until suggestionsArr.length()) {
                    suggestions.add(suggestionsArr.getString(i))
                }
            }

            AiHabitReportResult(
                score = score,
                personaTitle = persona,
                summary = summary,
                strengths = if (strengths.isNotEmpty()) strengths else listOf("Telefon kullanımınız dengeli sınırda"),
                risks = if (risks.isNotEmpty()) risks else listOf("Düzenli mola vermeyi unutmayın"),
                suggestions = if (suggestions.isNotEmpty()) suggestions else listOf("25 dakikalık odak seansı başlatın"),
                isLiveAiGenerated = true
            )
        } catch (_: Exception) {
            null
        }
    }

    private fun generateLocalHeuristicReport(
        screenTimeMinutes: Int,
        unlockCount: Int,
        nightDrainDrop: Int,
        topApps: List<Pair<String, Int>>,
        completedFocusMinutes: Int
    ): AiHabitReportResult {
        var baseScore = 95

        // Penalties / Bonuses
        if (screenTimeMinutes > 240) baseScore -= 15
        else if (screenTimeMinutes > 150) baseScore -= 8

        if (unlockCount > 60) baseScore -= 10
        else if (unlockCount > 35) baseScore -= 5

        if (nightDrainDrop > 8) baseScore -= 8
        if (completedFocusMinutes >= 25) baseScore += 8

        val score = baseScore.coerceIn(40, 99)

        val persona = when {
            completedFocusMinutes >= 45 -> "Süper Odaklı Üretken 🎯"
            screenTimeMinutes > 300 -> "Yoğun Dijital Gezgin 📱"
            unlockCount > 50 -> "Hızlı Kontrolcü ⚡"
            else -> "Dengeli Dijital Zen 🧘"
        }

        val topAppNames = topApps.take(2).map { it.first }
        val topAppText = if (topAppNames.isNotEmpty()) topAppNames.joinToString(" ve ") else "uygulamalar"

        val strengths = mutableListOf<String>()
        if (completedFocusMinutes > 0) strengths.add("Bugün $completedFocusMinutes dakika odaklanma seansı gerçekleştirdiniz.")
        if (nightDrainDrop <= 5) strengths.add("Gece uyku sırasındaki pil tüketimi oldukça düşük ve sağlıklı.")
        if (screenTimeMinutes <= 180) strengths.add("Günlük ekran süreniz sağlıklı 3 saat eşiğinin altında.")
        if (strengths.isEmpty()) strengths.add("Kritik sistem servislerini ve pil kullanımını kontrol altında tutuyorsunuz.")

        val risks = mutableListOf<String>()
        if (unlockCount > 40) risks.add("Günde $unlockCount kez kilit açmak dikkat dağınıklığını tetikleyebilir.")
        if (screenTimeMinutes > 240) risks.add("$topAppText üzerinde harcanan süre günlük limitleri zorluyor.")
        if (risks.isEmpty()) risks.add("Uzun süreli ekran kullanımında göz dinlendirme kuralına (20-20-20) dikkat edin.")

        val suggestions = mutableListOf(
            "En çok vakit alan uygulamalar için 45 dakikalık günlük süre limiti belirleyin.",
            "Çalışırken dikkat dağıtıcı bildirimleri kısmak için 25 dakikalık Pomodoro modu başlatın.",
            "Uyku öncesi mavi ışık maruziyetini azaltmak için saat 23:00'te ekranı bırakmayı hedefleyin."
        )

        val summary = "Bugünkü $screenTimeMinutes dakikalık kullanımınız ve $unlockCount kilit açma sıklığınız doğrultusunda genel dijital denge puanınız %$score olarak hesaplandı."

        return AiHabitReportResult(
            score = score,
            personaTitle = persona,
            summary = summary,
            strengths = strengths,
            risks = risks,
            suggestions = suggestions,
            isLiveAiGenerated = false
        )
    }

    suspend fun generateEnergyReport(
        summary: ThreeDayUsageSummary
    ): AiEnergyReportResult = withContext(Dispatchers.IO) {
        val apiKey = try { BuildConfig.GEMINI_API_KEY } catch (_: Exception) { "" }

        if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                val appDrainLines = summary.topDrainingApps.take(6).mapIndexed { idx, app ->
                    val hrs = app.totalForegroundMinutes / 60
                    val mins = app.totalForegroundMinutes % 60
                    val timeStr = if (hrs > 0) "${hrs}s ${mins}dk" else "${mins}dk"
                    val netMb = (app.wifiBytes + app.mobileBytes) / (1024 * 1024)
                    "${idx + 1}. ${app.appName} (${app.category}): 3 günde $timeStr, Veri: ${netMb}MB, Tahmini Pil Payı: %${app.estimatedDrainPercentage}"
                }.joinToString("\n")

                val prompt = """
                    Sen uzman bir Android Pil Optimizasyonu ve Güç Yönetimi Mühendisisin (VoltGuard Gemini AI).
                    Kullanıcının son 3 günlük gerçek akıllı telefon kullanım verilerini analiz ederek Türkçe bir 'AI Enerji Raporu' hazırla.

                    Kullanıcının Son 3 Günlük Kullanım Özeti (${summary.dateRangeLabel}):
                    - Toplam Ekran Süresi: ${summary.totalScreenTimeMinutes} dakika (Günlük ortalama: ${summary.averageDailyScreenMinutes} dakika)
                    - Toplam Ekran Açma/Kilit Sayısı: ${summary.totalUnlockCount} kez
                    - Gece/Bekleme (Standby) Pil Kaybı: %${summary.nightStandbyDrainTotal}
                    - Mevcut Pil Seviyesi: %${summary.currentBatteryLevel} | Sıcaklık: ${summary.batteryTemp}°C | Sağlık: ${summary.batteryHealth}
                    - Toplam Ağ Trafiği: ${summary.totalWifiMb} MB Wi-Fi, ${summary.totalMobileMb} MB Mobil Veri
                    - Son 3 Günün En Çok Enerji Harcayan Uygulamaları:
                    $appDrainLines

                    Lütfen yanıtını SADECE aşağıdaki JSON formatında döndür, başka hiçbir metin veya markdown formatı (JSON bloğu hariç) ekleme:
                    {
                      "title": "3 Günlük Akıllı Pil & Enerji Raporu",
                      "energyEfficiencyScore": 76,
                      "estimatedExtraBatteryHours": "+3 saat 15 dakika",
                      "potentialSavingsPercentage": 24,
                      "executiveSummary": "Son 3 gündeki kullanım profilinizde özellikle video ve sosyal medya uygulamalarının ekran ve hücresel veri tüketimi pil ömrünüzü kısaltıyor. Uygun optimizasyonlarla günlük 3 saatin üzerinde ek süre kazanabilirsiniz.",
                      "topEnergyConsumingApps": [
                        {
                          "appName": "Instagram",
                          "packageName": "com.instagram.android",
                          "usageTimeFormatted": "5 saat 40 dk (3 gün)",
                          "drainLevel": "YÜKSEK",
                          "drainPercentEst": 22,
                          "whyDraining": "Sürekli video akışı, GPU kullanımı ve arka plan soket bağlantıları pilin hızla tükenmesine neden oluyor."
                        }
                      ],
                      "personalizedSavingsTips": [
                        {
                          "title": "Arka Plan Senkronizasyonunu Sınırlandırın",
                          "explanation": "En çok tüketen uygulamalar ekran kapalıyken bile arka planda bildirim ve veri yoklaması yapıyor. Pil ayarlarından kısıtlı moda alarak güç tasarrufu sağlayın.",
                          "estimatedGain": "+45 dk pil süresi",
                          "priority": "YÜKSEK",
                          "iconType": "APP_RESTRICT"
                        },
                        {
                          "title": "Otomatik Ekran Parlaklığı & 60Hz Ayarı",
                          "explanation": "Ekran en büyük güç tüketiciniz. Dinamik parlaklık ve gerektiğinde 60Hz moduna geçmek batarya yükünü %20 azaltır.",
                          "estimatedGain": "+50 dk pil süresi",
                          "priority": "YÜKSEK",
                          "iconType": "BRIGHTNESS"
                        },
                        {
                          "title": "Gece Bekleme (Standby) Derin Uyku Modu",
                          "explanation": "Gece saatlerinde uyanık kalan arka plan servisleri %${summary.nightStandbyDrainTotal} seviyesinde gizli pil kaybına yol açıyor. VoltGuard derin uyku modunu aktif tutun.",
                          "estimatedGain": "+35 dk pil süresi",
                          "priority": "ORTA",
                          "iconType": "STANDBY"
                        },
                        {
                          "title": "Zayıf Hücresel Ağ Yerine Wi-Fi Kullanımı",
                          "explanation": "Mobil şebeke sinyali zayıf olduğunda telefon modemi yüksek güçte çalışır. Ev ve iş yerinde Wi-Fi bağlantısını tercih edin.",
                          "estimatedGain": "+25 dk pil süresi",
                          "priority": "ÖNERİ",
                          "iconType": "NETWORK"
                        }
                      ],
                      "standbyDrainInsight": "Gece bekleme modunda son 3 günde toplam %${summary.nightStandbyDrainTotal} pil harcandı. Arka plan servislerinin derin uykuya alınmasıyla bu kayıp %2'nin altına çekilebilir.",
                      "quickActionRecommendations": [
                        {
                          "label": "Tüm Yüksek Tüketenleri Derin Uykuya Al",
                          "description": "Arka planda uyanık kalarak pili sömüren uygulamaları dondurun.",
                          "actionType": "DEEP_SLEEP"
                        },
                        {
                          "label": "Uygulamalar İçin Süre Limiti Koy",
                          "description": "En çok tüketen uygulamalar için günlük süre sınırı tanımlayın.",
                          "actionType": "APP_LIMITS"
                        },
                        {
                          "label": "VoltGuard Dengeli Güç Modunu Aç",
                          "description": "CPU saat hızını ve arka plan senkronizasyonunu optimize edin.",
                          "actionType": "POWER_MODE"
                        }
                      ]
                    }
                """.trimIndent()

                val request = GeminiRequest(
                    contents = listOf(
                        GeminiContent(parts = listOf(GeminiPart(text = prompt)))
                    )
                )

                val response = api.generateContent(apiKey = apiKey, request = request)
                val rawText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text

                if (!rawText.isNullOrBlank()) {
                    val cleanedJson = rawText
                        .replace("```json", "")
                        .replace("```", "")
                        .trim()

                    val parsed = parseEnergyReportJson(cleanedJson)
                    if (parsed != null) {
                        return@withContext parsed.copy(isLiveAiGenerated = true)
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("VoltGuardGemini", "Energy report API call failed, falling back to local heuristic engine", e)
            }
        }

        return@withContext generateLocalHeuristicEnergyReport(summary)
    }

    private fun parseEnergyReportJson(jsonString: String): AiEnergyReportResult? {
        return try {
            val json = org.json.JSONObject(jsonString)
            val title = json.optString("title", "3 Günlük Akıllı Pil & Enerji Raporu")
            val score = json.optInt("energyEfficiencyScore", 75).coerceIn(10, 100)
            val extraHours = json.optString("estimatedExtraBatteryHours", "+3 saat 20 dakika")
            val potentialSavings = json.optInt("potentialSavingsPercentage", 22).coerceIn(5, 50)
            val executiveSummary = json.optString("executiveSummary", "Son 3 günlük kullanım verileriniz analiz edildi.")
            val standbyInsight = json.optString("standbyDrainInsight", "Gece bekleme modunda düzenli güç yönetimi pil ömrünüzü uzatır.")

            val topAppsList = mutableListOf<AppEnergyConsumptionDetail>()
            val topAppsArr = json.optJSONArray("topEnergyConsumingApps")
            if (topAppsArr != null) {
                for (i in 0 until topAppsArr.length()) {
                    val obj = topAppsArr.getJSONObject(i)
                    topAppsList.add(
                        AppEnergyConsumptionDetail(
                            appName = obj.optString("appName", "Uygulama"),
                            packageName = obj.optString("packageName", ""),
                            usageTimeFormatted = obj.optString("usageTimeFormatted", ""),
                            drainLevel = obj.optString("drainLevel", "YÜKSEK"),
                            drainPercentEst = obj.optInt("drainPercentEst", 15),
                            whyDraining = obj.optString("whyDraining", "Yüksek ekran ve ağ tüketimi.")
                        )
                    )
                }
            }

            val tipsList = mutableListOf<EnergySavingTip>()
            val tipsArr = json.optJSONArray("personalizedSavingsTips")
            if (tipsArr != null) {
                for (i in 0 until tipsArr.length()) {
                    val obj = tipsArr.getJSONObject(i)
                    tipsList.add(
                        EnergySavingTip(
                            title = obj.optString("title", "Enerji Tasarrufu Önerisi"),
                            explanation = obj.optString("explanation", ""),
                            estimatedGain = obj.optString("estimatedGain", "+30 dk pil süresi"),
                            priority = obj.optString("priority", "ORTA"),
                            iconType = obj.optString("iconType", "APP_RESTRICT")
                        )
                    )
                }
            }

            val actionsList = mutableListOf<QuickEnergyAction>()
            val actionsArr = json.optJSONArray("quickActionRecommendations")
            if (actionsArr != null) {
                for (i in 0 until actionsArr.length()) {
                    val obj = actionsArr.getJSONObject(i)
                    actionsList.add(
                        QuickEnergyAction(
                            label = obj.optString("label", "Hızlı Optimizasyon"),
                            description = obj.optString("description", ""),
                            actionType = obj.optString("actionType", "DEEP_SLEEP")
                        )
                    )
                }
            }

            AiEnergyReportResult(
                title = title,
                energyEfficiencyScore = score,
                estimatedExtraBatteryHours = extraHours,
                potentialSavingsPercentage = potentialSavings,
                executiveSummary = executiveSummary,
                topEnergyConsumingApps = topAppsList,
                personalizedSavingsTips = tipsList,
                standbyDrainInsight = standbyInsight,
                quickActionRecommendations = actionsList,
                isLiveAiGenerated = true
            )
        } catch (_: Exception) {
            null
        }
    }

    private fun generateLocalHeuristicEnergyReport(summary: ThreeDayUsageSummary): AiEnergyReportResult {
        var baseScore = 88
        if (summary.totalScreenTimeMinutes > 720) baseScore -= 14
        else if (summary.totalScreenTimeMinutes > 450) baseScore -= 8

        if (summary.nightStandbyDrainTotal > 12f) baseScore -= 10
        else if (summary.nightStandbyDrainTotal > 6f) baseScore -= 5

        if (summary.totalUnlockCount > 120) baseScore -= 8

        val score = baseScore.coerceIn(45, 96)
        val savingsPercent = when {
            score < 65 -> 28
            score < 80 -> 22
            else -> 15
        }

        val extraMins = when {
            score < 65 -> 240 // 4 hours
            score < 80 -> 195 // 3 hours 15 mins
            else -> 135 // 2 hours 15 mins
        }
        val extraHrsText = "+${extraMins / 60} saat ${extraMins % 60} dakika"

        val topConsumingList = summary.topDrainingApps.take(4).map { app ->
            val hrs = app.totalForegroundMinutes / 60
            val mins = app.totalForegroundMinutes % 60
            val timeText = if (hrs > 0) "${hrs} sa ${mins} dk (3 gün)" else "${mins} dk (3 gün)"
            val why = when (app.category) {
                "Sosyal Medya" -> "Yoğun video akışı, GPU arayüz çizimi ve arka plan bildirim kontrolü."
                "Video & Müzik" -> "Yüksek çözünürlüklü video çözme, ses donanımı ve sürekli Wi-Fi/hücresel veri akışı."
                "Oyun" -> "İşlemci ve GPU çekirdeklerinin maksimum frekansta çalışması ve termal ısınma."
                "İletişim" -> "Sürekli uyanık kalan arka plan soket bağlantıları ve periyodik senkronizasyon."
                else -> "Aktif ekran süresi ve arka plan veri trafiği."
            }
            AppEnergyConsumptionDetail(
                appName = app.appName,
                packageName = app.packageName,
                usageTimeFormatted = timeText,
                drainLevel = if (app.estimatedDrainPercentage >= 20f) "KRİTİK" else if (app.estimatedDrainPercentage >= 12f) "YÜKSEK" else "ORTA",
                drainPercentEst = app.estimatedDrainPercentage.toInt().coerceAtLeast(8),
                whyDraining = why
            )
        }.ifEmpty {
            listOf(
                AppEnergyConsumptionDetail(
                    appName = "Instagram",
                    packageName = "com.instagram.android",
                    usageTimeFormatted = "4 saat 10 dk (3 gün)",
                    drainLevel = "YÜKSEK",
                    drainPercentEst = 24,
                    whyDraining = "Sürekli hikaye/video yüklemesi ve arka plan soket yoklamaları."
                ),
                AppEnergyConsumptionDetail(
                    appName = "YouTube",
                    packageName = "com.google.android.youtube",
                    usageTimeFormatted = "3 saat 25 dk (3 gün)",
                    drainLevel = "YÜKSEK",
                    drainPercentEst = 21,
                    whyDraining = "Donanımsal video kod çözücü ve yüksek ekran parlaklığı."
                ),
                AppEnergyConsumptionDetail(
                    appName = "WhatsApp",
                    packageName = "com.whatsapp",
                    usageTimeFormatted = "2 saat 15 dk (3 gün)",
                    drainLevel = "ORTA",
                    drainPercentEst = 14,
                    whyDraining = "Arka plan mesajlaşma servisleri ve medya senkronizasyonu."
                )
            )
        }

        val topAppName = topConsumingList.firstOrNull()?.appName ?: "sosyal medya uygulamaları"

        val tips = mutableListOf(
            EnergySavingTip(
                title = "$topAppName Arka Planını Kısıtlayın",
                explanation = "$topAppName son 3 günde pilinizin en büyük payını tüketti. Uygulama bilgilerinden 'Pil Tasarrufu: Kısıtlı' modunu açarak ekran kapalıyken güç çekmesini engelleyin.",
                estimatedGain = "+45 dk pil süresi",
                priority = "YÜKSEK",
                iconType = "APP_RESTRICT"
            ),
            EnergySavingTip(
                title = "Otomatik Parlaklık & Karanlık Tema",
                explanation = "Ekran süreniz 3 günde toplam ${summary.totalScreenTimeMinutes / 60} saatin üzerinde. Karanlık tema (Dark Mode) OLED/AMOLED piksellerini kapatarak %18 enerji tasarrufu sağlar.",
                estimatedGain = "+40 dk pil süresi",
                priority = "YÜKSEK",
                iconType = "BRIGHTNESS"
            ),
            EnergySavingTip(
                title = "Gece Bekleme (Standby) Derin Uyku Koruması",
                explanation = "Son 3 gecede cihazınız uykudayken toplam %${summary.nightStandbyDrainTotal.toInt()} pil kaybetti. VoltGuard otomatik derin uyku motoru ile bekleme tüketimini %1.5 seviyesine indirebilirsiniz.",
                estimatedGain = "+35 dk pil süresi",
                priority = "ORTA",
                iconType = "STANDBY"
            ),
            EnergySavingTip(
                title = "Zayıf Mobil Sinyal Yerine Wi-Fi Kullanımı",
                explanation = "Toplam ${summary.totalMobileMb} MB mobil veri kullanıldı. Zayıf çeken baz istasyonlarına bağlanırken modem iki kat güç harcar; mümkün olduğunda Wi-Fi tercih edin.",
                estimatedGain = "+20 dk pil süresi",
                priority = "ÖNERİ",
                iconType = "NETWORK"
            )
        )

        val actions = listOf(
            QuickEnergyAction(
                label = "Tüm Yüksek Tüketenleri Derin Uykuya Al",
                description = "Arka planda uyanık kalarak pil harcayan uygulamaları anında durdurun.",
                actionType = "DEEP_SLEEP"
            ),
            QuickEnergyAction(
                label = "Uygulamalar İçin Süre Limiti Koy",
                description = "Günlük kullanım kotası belirleyerek aşırı tüketimi dizginleyin.",
                actionType = "APP_LIMITS"
            ),
            QuickEnergyAction(
                label = "VoltGuard Dengeli Güç Modunu Aç",
                description = "Pil performansını optimize edin ve sıcaklığı düşürün.",
                actionType = "POWER_MODE"
            )
        )

        val summaryText = "Son 3 gün içinde toplam ${summary.totalScreenTimeMinutes / 60} saat ${summary.totalScreenTimeMinutes % 60} dakika ekran süresi kaydedildi. En yüksek enerji kaybı $topAppName ve ekran panelinden kaynaklanıyor. Önerilen optimizasyonlarla günlük yaklaşık $extraHrsText ekstra batarya süresi elde edebilirsiniz."

        return AiEnergyReportResult(
            title = "3 Günlük Akıllı Pil & Enerji Raporu",
            energyEfficiencyScore = score,
            estimatedExtraBatteryHours = extraHrsText,
            potentialSavingsPercentage = savingsPercent,
            executiveSummary = summaryText,
            topEnergyConsumingApps = topConsumingList,
            personalizedSavingsTips = tips,
            standbyDrainInsight = "Son 3 gecede bekleme modundaki toplam kayıp %${summary.nightStandbyDrainTotal.toInt()} olarak ölçüldü. Bu değer ideal %3 eşiğinin üzerinde olup standby optimizasyonu ile korunabilir.",
            quickActionRecommendations = actions,
            isLiveAiGenerated = false
        )
    }
}

