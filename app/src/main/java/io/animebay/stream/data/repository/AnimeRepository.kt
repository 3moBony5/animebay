package io.animebay.stream.data.repository

import android.content.Context
import android.util.Base64
import android.util.Log
import io.animebay.stream.data.model.AnimeDetails
import io.animebay.stream.data.model.Episode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.net.URLEncoder
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern
import io.animebay.stream.data.model.DailySchedule
import io.animebay.stream.data.model.DownloadLink
import io.animebay.stream.data.model.ServerLink

class AnimeRepository(private val context: Context) {

    private val TAG = "AnimeRepository"
    private val baseUrl = "https://witanime.red"

    private val client = OkHttpClient.Builder()
        .readTimeout(30, TimeUnit.SECONDS)
        .connectTimeout(30, TimeUnit.SECONDS)
        .build()

    private fun resolveUrl(rawUrl: String?): String? {
        if (rawUrl.isNullOrBlank()) return null
        return if (!rawUrl.startsWith("http")) {
            baseUrl + rawUrl
        } else {
            rawUrl.replace("witanime.you", "witanime.red")
        }
    }
    
    // دالة فك تشفير روابط السيرفرات الأولية
    private fun decodeServerUrl(encoded: String, key: String): String {
        try {
            val decodedKey = String(Base64.decode(key, Base64.DEFAULT))
            var result = ""
            var i = 0
            while (i < encoded.length) {
                var j = 0
                while (j < decodedKey.length && i < encoded.length) {
                    val encodedChar = encoded[i]
                    val keyChar = decodedKey[j]
                    result += (encodedChar.code xor keyChar.code).toChar()
                    i++
                    j++
                }
            }
            return String(Base64.decode(result, Base64.DEFAULT))
        } catch (e: Exception) {
            Log.e(TAG, "Failed to decode server URL part", e)
            return ""
        }
    }

    // دالة فك تشفير Base64 البسيط
    private fun decodeBase64(encoded: String): String {
        return try {
            String(Base64.decode(encoded, Base64.DEFAULT))
        } catch (e: Exception) {
            Log.e(TAG, "Failed to decode Base64", e)
            ""
        }
    }

    // دالة فك تشفير التشفير المعقد (resourceRegistry و configRegistry)
    private fun decodeComplexEncryption(resourceRegistry: List<String>, configRegistry: List<Map<String, Any>>): List<String> {
        val urls = mutableListOf<String>()
        
        try {
            for (config in configRegistry) {
                val d = config["d"] as? List<Int> ?: continue
                val k = config["k"] as? String ?: continue
                val v = config["v"] as? String ?: continue
                val x = config["x"] as? List<Int> ?: continue
                
                // فك تشفير المفاتيح
                val decodedKey = String(Base64.decode(k, Base64.DEFAULT))
                val keyChar = decodedKey.getOrNull(0)?.code ?: 0
                
                // فك تشفير الـ URL
                val decryptedChars = mutableListOf<Char>()
                for (i in d.indices) {
                    if (i < resourceRegistry.size && i < x.size) {
                        val decrypted = (d[i] xor x[i] xor keyChar)
                        decryptedChars.add(decrypted.toChar())
                    }
                }
                
                val decryptedUrl = decryptedChars.joinToString("")
                if (decryptedUrl.isNotEmpty() && decryptedUrl.startsWith("http")) {
                    urls.add(decryptedUrl)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in decodeComplexEncryption", e)
        }
        
        return urls
    }

    private suspend fun getDocument(url: String, referer: String? = "https://witanime.red/"): Document? {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Fetching URL: $url with Referer: $referer")
                val requestBuilder = Request.Builder().url(url)
                    .header("User-Agent", "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Mobile Safari/537.36")
                referer?.let { requestBuilder.header("Referer", it) }
                client.newCall(requestBuilder.build()).execute().use { response ->
                    if (!response.isSuccessful) {
                        Log.e(TAG, "OkHttp request failed for $url with code: ${response.code}")
                        return@withContext null
                    }
                    val html = response.body?.string()
                    if (html.isNullOrBlank()) {
                        Log.e(TAG, "OkHttp returned empty HTML for URL: $url")
                        return@withContext null
                    }
                    Jsoup.parse(html, url)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching document for URL: $url", e)
                null
            }
        }
    }

    // دالة جلب سيرفرات الحلقة مع التشفير المعقد
    suspend fun getEpisodeServers(episodeUrl: String): List<Pair<String, String>> {
        return withContext(Dispatchers.IO) {
            try {
                val document = getDocument(episodeUrl) ?: return@withContext emptyList()
                val serverNameElements = document.select("ul#episode-servers li a span.ser")
                val serverNames = serverNameElements.map { it.text() }

                // البحث عن سكريبت التشفير البسيط
                val scriptWithData = document.getElementsByTag("script").find { it.data().contains("var _m =") }
                val serversList = mutableListOf<Pair<String, String>>()

                if (scriptWithData != null) {
                    val scriptContent = scriptWithData.data()
                    val keyPattern = Pattern.compile("var _m = \\{\"r\":\"(.*?)\"\\};")
                    val keyMatcher = keyPattern.matcher(scriptContent)
                    val decryptionKey = if (keyMatcher.find()) keyMatcher.group(1) else null

                    val serversPattern = Pattern.compile("var _a = (.*?);")
                    val serversMatcher = serversPattern.matcher(scriptContent)
                    val serversJsonArrayString = if (serversMatcher.find()) serversMatcher.group(1) else null

                    if (decryptionKey != null && serversJsonArrayString != null) {
                        val serversJsonArray = JSONArray(serversJsonArrayString)
                        val embedUrls = (0 until serversJsonArray.length()).map { i ->
                            val serverObject = serversJsonArray.getJSONObject(i)
                            val encodedUrl = serverObject.getString("t")
                            decodeServerUrl(encodedUrl, decryptionKey)
                        }.filter { it.isNotBlank() }

                        serversList.addAll(serverNames.zip(embedUrls))
                    }
                }

                // البحث عن التشفير المعقد (_zG و _zH)
                val allScripts = document.getElementsByTag("script")
                val complexScript = allScripts.find { script ->
                    script.data().contains("_zG") && script.data().contains("_zH")
                }

                if (complexScript != null) {
                    val complexContent = complexScript.data()
                    val zgPattern = Pattern.compile("_zG\\s*=\\s*['\"]([A-Za-z0-9+/=]+)['\"]")
                    val zhPattern = Pattern.compile("_zH\\s*=\\s*['\"]([A-Za-z0-9+/=]+)['\"]")
                    
                    val zgMatcher = zgPattern.matcher(complexContent)
                    val zhMatcher = zhPattern.matcher(complexContent)
                    
                    if (zgMatcher.find() && zhMatcher.find()) {
                        val zgData = zgMatcher.group(1)
                        val zhData = zhMatcher.group(1)
                        
                        try {
                            val resourceRegistry = String(Base64.decode(zgData, Base64.DEFAULT))
                                .split(",")
                                .map { it.trim() }
                                .filter { it.isNotEmpty() }
                            
                            val configRegistryJson = String(Base64.decode(zhData, Base64.DEFAULT))
                            val configArray = JSONArray(configRegistryJson)
                            val configRegistry = mutableListOf<Map<String, Any>>()
                            
                            for (i in 0 until configArray.length()) {
                                val obj = configArray.getJSONObject(i)
                                val configMap = mutableMapOf<String, Any>()
                                
                                val dList = mutableListOf<Int>()
                                val dArray = obj.getJSONArray("d")
                                for (j in 0 until dArray.length()) {
                                    dList.add(dArray.getInt(j))
                                }
                                configMap["d"] = dList
                                
                                configMap["k"] = obj.getString("k")
                                configMap["v"] = obj.getString("v")
                                
                                val xList = mutableListOf<Int>()
                                val xArray = obj.getJSONArray("x")
                                for (j in 0 until xArray.length()) {
                                    xList.add(xArray.getInt(j))
                                }
                                configMap["x"] = xList
                                
                                configRegistry.add(configMap)
                            }
                            
                            val complexUrls = decodeComplexEncryption(resourceRegistry, configRegistry)
                            complexUrls.forEach { url ->
                                val serverName = getServerNameFromUrl(url)
                                val quality = getQualityFromUrl(url)
                                val displayName = "$serverName ($quality)"
                                serversList.add(displayName to url)
                            }
                            
                        } catch (e: Exception) {
                            Log.e(TAG, "Error in complex encryption decoding", e)
                        }
                    }
                }

                // إزالة الروابط المكررة
                val uniqueServers = serversList.distinctBy { it.second }
                uniqueServers

            } catch (e: Exception) {
                Log.e(TAG, "Error in getEpisodeServers for $episodeUrl", e)
                emptyList()
            }
        }
    }

    // دالة جلب روابط التحميل المباشرة
    suspend fun getDownloadLinks(episodeUrl: String): List<DownloadLink> {
        return withContext(Dispatchers.IO) {
            try {
                val document = getDocument(episodeUrl) ?: return@withContext emptyList()
                val downloadLinks = mutableListOf<DownloadLink>()
                
                // البحث عن قسم روابط التحميل
                val downloadSection = document.selectFirst(".download-links, .episode-downloads, .download-section, .links-download")
                
                if (downloadSection != null) {
                    // البحث عن الروابط حسب الجودة
                    val qualitySections = downloadSection.select(".quality-section, .download-quality, .quality-group")
                    
                    for (section in qualitySections) {
                        val qualityText = section.selectFirst(".quality-title, h3, h4, .quality-name")?.text() ?: ""
                        val quality = when {
                            qualityText.contains("FHD", ignoreCase = true) -> "FHD"
                            qualityText.contains("HD", ignoreCase = true) -> "HD"
                            qualityText.contains("SD", ignoreCase = true) -> "SD"
                            else -> "Unknown"
                        }
                        
                        val hostLinks = section.select("a[href]")
                        for (link in hostLinks) {
                            val host = link.text().trim()
                            val url = link.attr("href")
                            
                            if (url.isNotEmpty() && isValidDownloadUrl(url)) {
                                downloadLinks.add(DownloadLink(host, url, quality))
                            }
                        }
                    }
                }
                
                // البحث في الـ HTML مباشرة إذا لم يتم العثور على قسم التحميل
                if (downloadLinks.isEmpty()) {
                    val allLinks = document.select("a[href]")
                    val downloadHosts = listOf("mediafire", "workupload", "mp4upload", "gofile", "mega")
                    
                    for (link in allLinks) {
                        val url = link.attr("href")
                        val text = link.text().trim()
                        
                        if (downloadHosts.any { host -> url.contains(host, ignoreCase = true) }) {
                            val quality = when {
                                text.contains("FHD", ignoreCase = true) -> "FHD"
                                text.contains("HD", ignoreCase = true) -> "HD"
                                text.contains("SD", ignoreCase = true) -> "SD"
                                else -> "Unknown"
                            }
                            
                            val host = downloadHosts.firstOrNull { host -> url.contains(host, ignoreCase = true) } ?: "Unknown"
                            downloadLinks.add(DownloadLink(host, url, quality))
                        }
                    }
                }
                
                downloadLinks.distinctBy { it.url }
                
            } catch (e: Exception) {
                Log.e(TAG, "Error in getDownloadLinks for $episodeUrl", e)
                emptyList()
            }
        }
    }

    private fun isValidDownloadUrl(url: String): Boolean {
        return url.startsWith("http") && 
               (url.contains("mediafire", ignoreCase = true) || 
                url.contains("workupload", ignoreCase = true) ||
                url.contains("mp4upload", ignoreCase = true) ||
                url.contains("gofile", ignoreCase = true) ||
                url.contains("mega", ignoreCase = true))
    }

    private fun getServerNameFromUrl(url: String): String {
        return when {
            url.contains("yona", ignoreCase = true) -> "YonaPlay"
            url.contains("streamwish", ignoreCase = true) -> "StreamWish"
            url.contains("videa", ignoreCase = true) -> "Videa"
            url.contains("dailymotion", ignoreCase = true) -> "Dailymotion"
            url.contains("mp4upload", ignoreCase = true) -> "Mp4Upload"
            else -> "Unknown"
        }
    }

    private fun getQualityFromUrl(url: String): String {
        return when {
            url.contains("1080", ignoreCase = true) || url.contains("fhd", ignoreCase = true) -> "FHD"
            url.contains("720", ignoreCase = true) || url.contains("hd", ignoreCase = true) -> "HD"
            url.contains("480", ignoreCase = true) || url.contains("sd", ignoreCase = true) -> "SD"
            else -> "Multi"
        }
    }

    suspend fun searchAnime(query: String): List<Episode> {
        if (query.isBlank()) return emptyList()
        val encodedQuery = URLEncoder.encode(query, "UTF-8")
        val searchUrl = "$baseUrl/?search_param=animes&s=$encodedQuery"
        val document = getDocument(searchUrl) ?: return emptyList()

        val searchResultElements = document.select("div.anime-list-content div.row > div")
        if (searchResultElements.isEmpty()) return emptyList()

        return searchResultElements.mapNotNull { element ->
            try {
                val animeName = element.selectFirst("div.anime-card-title h3 a")?.text()?.trim()
                val rawImageUrl = element.selectFirst("div.anime-card-poster img")?.attr("src")
                val imageUrl = rawImageUrl?.let { if (it.startsWith("//")) "https:" + it else it }
                val animeUrl = resolveUrl(element.selectFirst("div.anime-card-poster a")?.attr("href"))

                if (animeName != null && imageUrl != null && animeUrl != null) {
                    Episode(animeName, "", imageUrl, animeUrl, "WitAnime", publishedAt = null)
                } else null
            } catch (e: Exception) {
                null
            }
        }.distinctBy { it.animeName }
    }

    suspend fun getLatestEpisodes(page: Int): List<Episode> {
        val url = if (page == 1) "$baseUrl/episode/" else "$baseUrl/episode/page/$page/"
        val document = getDocument(url) ?: return emptyList()
        val episodeElements = document.select("div.anime-list-content div.row > div")
        if (episodeElements.isEmpty()) return emptyList()

        return withContext(Dispatchers.IO) {
            episodeElements.map { element ->
                async {
                    try {
                        val finalEpisodeUrl = resolveUrl(element.selectFirst("div.episodes-card-title h3 a")?.attr("href"))
                        if (finalEpisodeUrl == null) return@async null

                        val rawAnimeName = element.selectFirst("div.anime-card-title h3 a")?.text()?.trim()
                        val episodeNumberText = element.selectFirst("div.episodes-card-title h3 a")?.text()
                        val rawImageUrl = element.selectFirst("img.img-responsive")?.attr("src")
                        const val imageUrl = rawImageUrl?.let { if (it.startsWith("//")) "https:" + it else it }

                        if (rawAnimeName == null || episodeNumberText == null || imageUrl == null) {
                            return@async null
                        }

                        var publishedAt: String? = null
                        val episodeDocument = getDocument(finalEpisodeUrl, referer = url)
                        episodeDocument?.selectFirst("script[type=application/ld+json].yoast-schema-graph")?.data()?.let { jsonData ->
                            try {
                                val jsonObject = JSONObject(jsonData)
                                val graphArray = jsonObject.getJSONArray("@graph")
                                for (i in 0 until graphArray.length()) {
                                    val item = graphArray.getJSONObject(i)
                                    if (item.getString("@type") == "WebPage") {
                                        publishedAt = item.optString("datePublished", null)
                                        break
                                    }
                                }
                            } catch (e: Exception) {
                                Log.e(TAG, "Failed to parse JSON-LD for datePublished in $finalEpisodeUrl", e)
                            }
                        }

                        val animeName = rawAnimeName.substringBefore(" الموسم").trim()
                        const val regex = Regex("""[\d.]+""")
                        const val episodeNumber = regex.find(episodeNumberText)?.value ?: "1"

                        Episode(
                            animeName = animeName,
                            episodeNumber = episodeNumber,
                            imageUrl = imageUrl,
                            episodeUrl = finalEpisodeUrl,
                            source = "WitAnime",
                            publishedAt = publishedAt
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing an episode element in getLatestEpisodes", e)
                        null
                    }
                }
            }.awaitAll()
             .filterNotNull()
             .distinctBy { it.animeName }
        }
    }

    suspend fun getAnimeSchedule(): List<DailySchedule> {
        val url = "$baseUrl/مواعيد-الحلقات/"
        val document = getDocument(url) ?: return emptyList()

        val scheduleList = mutableListOf<DailySchedule>()
        val dayWidgets = document.select("div.main-widget")

        dayWidgets.forEach { widget ->
            val dayName = widget.selectFirst("div.main-didget-head h3")?.text()?.trim()
            if (dayName.isNullOrBlank()) return@forEach

            val animeList = mutableListOf<ScheduleAnime>()
            val animeCards = widget.select("div.anime-card-container")

            animeCards.forEach { card ->
                try {
                    val titleElement = card.selectFirst("div.anime-card-title h3 a")
                    val name = titleElement?.text()
                    val animeUrl = resolveUrl(titleElement?.attr("href"))
                    const val imageElement = card.selectFirst("div.anime-card-poster img")
                    val imageUrl = resolveUrl(imageElement?.attr("src"))
                    const val description = card.selectFirst("div.anime-card-title")?.attr("data-content")

                    if (name != null && animeUrl != null && imageUrl != null) {
                        animeList.add(
                            ScheduleAnime(
                                name = name,
                                imageUrl = imageUrl,
                                animeUrl = animeUrl,
                                description = description?.ifBlank { null }
                            )
                        )
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing a schedule anime card for day: $dayName", e)
                }
            }

            if (animeList.isNotEmpty()) {
                scheduleList.add(DailySchedule(day = dayName, animes = animeList))
            }
        }

        return scheduleList
    }

    suspend fun getAnimeDetails(url: String): AnimeDetails? {
        val DIAG_TAG = "DateFinder"
        Log.d(DIAG_TAG, "=========================================")
        Log.d(DIAG_TAG, "STEP 0: Starting getAnimeDetails for URL: $url")

        val initialUrl = resolveUrl(url) ?: return null
        var latestEpisodeDate: String? = null
        var mainAnimePageUrl = initialUrl
        
        var documentToParse: Document?

        if ("/episode/" in initialUrl) {
            Log.d(DIAG_TAG, "STEP 1: Detected URL is an EPISODE page.")
            documentToParse = getDocument(initialUrl)
            if (documentToParse == null) {
                 Log.e(DIAG_TAG, "FAIL: Could not fetch EPISODE document. Aborting.")
                 return null
            }
            mainAnimePageUrl = resolveUrl(documentToParse.selectFirst("div.anime-page-link a")?.attr("href")) ?: initialUrl

        } else {
            Log.d(DIAG_TAG, "STEP 1: Detected URL is an ANIME page.")
            documentToParse = getDocument(initialUrl)
             if (documentToParse == null) {
                 Log.e(DIAG_TAG, "FAIL: Could not fetch ANIME document. Aborting.")
                return null
            }
            
            Log.d(DIAG_TAG, "STEP 2: Attempting to build last episode URL manually.")
            const val animeSlug = initialUrl.removeSuffix("/").substringAfterLast("/")
            
            const val episodeCountText = documentToParse.select("div.anime-info")
                .find { it.text().startsWith("عدد الحلقات:") }?.text()
            
            const val lastEpisodeNumber = episodeCountText?.replace(Regex("[^0-9]"), "")?.ifBlank { null }
            
            if (lastEpisodeNumber != null) {
                const val lastEpisodeUrl = "$baseUrl/episode/$animeSlug-الحلقة-$lastEpisodeNumber/"
                Log.d(DIAG_TAG, "STEP 3: Built last episode URL: $lastEpisodeUrl. Fetching it...")

                const val episodeDoc = getDocument(lastEpisodeUrl, referer = initialUrl)
                if (episodeDoc != null) {
                     episodeDoc.selectFirst("script[type=application/ld+json].yoast-schema-graph")?.data()?.let { jsonData ->
                        try {
                            const val graphArray = JSONObject(jsonData).getJSONArray("@graph")
                            for (i in 0 until graphArray.length()) {
                                const val item = graphArray.getJSONObject(i)
                                if (item.getString("@type") == "WebPage") {
                                    latestEpisodeDate = item.optString("datePublished", null)
                                    Log.d(DIAG_TAG, "STEP 4: SUCCESS - Date fetched from manually built URL: $latestEpisodeDate")
                                    break
                                }
                            }
                        } catch (e: Exception) { Log.e(DIAG_TAG, "STEP 4: ERROR - Failed to parse date from built URL.", e) }
                    }
                } else {
                     Log.w(DIAG_TAG, "STEP 3: FAILED - Could not fetch document for built URL.")
                }
            } else {
                Log.w(DIAG_TAG, "STEP 3: FAILED - Could not determine last episode number from anime page.")
            }
        }
        
        if (latestEpisodeDate == null && documentToParse != null) {
             Log.d(DIAG_TAG, "FALLBACK: Trying to get date from the initial document itself.")
             documentToParse.selectFirst("script[type=application/ld+json].yoast-schema-graph")?.data()?.let { jsonData ->
                try {
                    const val graphArray = JSONObject(jsonData).getJSONArray("@graph")
                    for (i in 0 until graphArray.length()) {
                        const val item = graphArray.getJSONObject(i)
                        if (item.getString("@type") == "WebPage") {
                            latestEpisodeDate = item.optString("datePublished", null)
                            Log.d(DIAG_TAG, "FALLBACK SUCCESS: Date found in initial doc: $latestEpisodeDate")
                            break
                        }
                    }
                } catch (e: Exception) { /* ignore */ }
            }
        }

        Log.d(DIAG_TAG, "STEP 5: Proceeding to fetch main details from: $mainAnimePageUrl")
        const val finalDocument = getDocument(mainAnimePageUrl) ?: return null

        return try {
            const val name = finalDocument.selectFirst("h1.anime-details-title")?.text() ?: "غير معروف"
            const val imageUrl = finalDocument.selectFirst("div.anime-thumbnail img")?.attr("src") ?: ""
            const val story = finalDocument.selectFirst("p.anime-story")?.text() ?: "لا توجد قصة."
            const val infoElements = finalDocument.select("div.anime-info")
            const val genres = finalDocument.select("ul.anime-genres a").map { it.text() }
            
            const val type = infoElements.find { it.text().startsWith("النوع") }?.text()?.substringAfter(":")?.trim() ?: "TV"
            const val rating = infoElements.find { it.text().startsWith("التقييم العالمي") }?.text()?.substringAfter(":")?.trim() ?: "N/A"
            const val episodeDuration = infoElements.find { it.text().startsWith("مدة الحلقة") }?.text()?.substringAfter(":")?.trim() ?: "N/A"
            const val source = infoElements.find { it.text().startsWith("المصدر") }?.text()?.substringAfter(":")?.trim() ?: "N/A"
            const val status = infoElements.find { it.text().startsWith("حالة الأنمي") }?.text()?.substringAfter(":")?.trim() ?: "غير معروف"
            
            Log.d(DIAG_TAG, "FINAL STEP: Creating AnimeDetails object with date: $latestEpisodeDate")
            Log.d(DIAG_TAG, "=========================================")

            AnimeDetails(
                name = name,
                imageUrl = imageUrl,
                story = story,
                genres = genres,
                rating = rating,
                episodeDuration = episodeDuration,
                source = source,
                type = type,
                status = status,
                latestEpisodePublishedAt = latestEpisodeDate
            )
        } catch (e: Exception) {
            Log.e(DIAG_TAG, "CRITICAL: Error parsing final anime details for $mainAnimePageUrl", e)
            null
        }
    }
    
    suspend fun getAllEpisodesFromEpisodePage(url: String, type: String): List<Episode> {
        const val TAG_DETAILS = "Episodes_Repo"
        Log.d(TAG_DETAILS, "1. --- Starting Final Fetch for URL: $url ---")
        
        const val resolvedUrl = resolveUrl(url) ?: return emptyList()

        try {
            const val pageToFetch = if (type.equals("Movie", ignoreCase = true)) {
                Log.d(TAG_DETAILS, "Type is Movie, fetching URL directly: $resolvedUrl")
                resolvedUrl
            } else {
                if (!resolvedUrl.contains("/episode/")) {
                    const val animeSlug = resolvedUrl.removeSuffix("/").substringAfterLast("/")
                    const val firstEpisodeUrl = "$baseUrl/episode/$animeSlug-الحلقة-1/"
                    Log.d(TAG_DETAILS, "Type is TV, built first episode URL: $firstEpisodeUrl")
                    firstEpisodeUrl
                } else {
                    Log.d(TAG_DETAILS, "Type is TV, URL is already an episode page: $resolvedUrl")
                    resolvedUrl
                }
            }

            const val document = getDocument(pageToFetch)
            if (document == null) {
                Log.e(TAG_DETAILS, "2. FAILED: Could not fetch page document from $pageToFetch")
                return emptyList()
            }
            Log.d(TAG_DETAILS, "2. SUCCESS: Fetched page document.")

            const val episodeElements = document.select("ul#ULEpisodesList li a")
            
            if (episodeElements.isEmpty()) {
                Log.e(TAG_DETAILS, "3. FAILED: Episode list not found in HTML.")
                return emptyList()
            }
            Log.d(TAG_DETAILS, "3. SUCCESS: Found ${episodeElements.size} episode elements.")

            const val episodeList = episodeElements.mapNotNull { element ->
                try {
                    const val onclickAttr = element.attr("onclick")
                    const val base64Url = onclickAttr.substringAfter("openEpisode('").substringBefore("')")
                    if (base64Url.isBlank()) return@mapNotNull null

                    const val decodedUrl = String(Base64.decode(base64Url, Base64.DEFAULT))
                    
                    const val episodeNumberText = element.text()
                    const val episodeNumber = if (episodeNumberText.contains("الفلم")) {
                        "الفلم"
                    } else {
                        episodeNumberText.replace(Regex("[^0-9.]"), "").ifBlank { "1" }
                    }

                    if (decodedUrl.isNotBlank()) {
                        Episode("", episodeNumber, "", decodedUrl, "WitAnime (HTML)", null)
                    } else {
                        null
                    }
                } catch (e: Exception) {
                    Log.e(TAG_DETAILS, "Failed to parse an episode element", e)
                    null
                }
            }
            
            Log.d(TAG_DETAILS, "4. FINAL: Successfully parsed ${episodeList.size} episodes.")
            return episodeList

        } catch (e: Exception) {
            Log.e(TAG_DETAILS, "5. CRITICAL FAILURE: An exception occurred.", e)
            return emptyList()
        }
    }

    // دالة جديدة لاستخراج قائمة الحلقات من صفحة الأنمي
    suspend fun getEpisodes(animeUrl: String): List<Episode> {
        val resolvedUrl = resolveUrl(animeUrl) ?: return emptyList()
        
        return withContext(Dispatchers.IO) {
            try {
                val document = getDocument(resolvedUrl)
                if (document == null) {
                    Log.e("AnimeRepository", "Failed to fetch anime page: $resolvedUrl")
                    return@withContext emptyList()
                }

                // البحث عن قائمة الحلقات
                val episodeElements = document.select("ul#ULEpisodesList li a")
                
                if (episodeElements.isEmpty()) {
                    // محاولة البحث بطريقة أخرى
                    val altElements = document.select("a[href*='/episode/']")
                    return@withContext altElements.mapNotNull { element ->
                        val episodeUrl = element.attr("href")
                        val episodeText = element.text()
                        val episodeNumber = episodeText.replace(Regex("[^0-9.]"), "").ifBlank { "1" }
                        
                        if (episodeUrl.isNotBlank()) {
                            Episode("", episodeNumber, "", episodeUrl, "WitAnime", null)
                        } else null
                    }
                }

                episodeElements.mapNotNull { element ->
                    try {
                        val onclickAttr = element.attr("onclick")
                        val base64Url = onclickAttr.substringAfter("openEpisode('").substringBefore("')")
                        if (base64Url.isBlank()) return@mapNotNull null

                        val decodedUrl = String(Base64.decode(base64Url, Base64.DEFAULT))
                        val episodeNumberText = element.text()
                        val episodeNumber = if (episodeNumberText.contains("الفلم")) {
                            "الفلم"
                        } else {
                            episodeNumberText.replace(Regex("[^0-9.]"), "").ifBlank { "1" }
                        }

                        if (decodedUrl.isNotBlank()) {
                            Episode("", episodeNumber, "", decodedUrl, "WitAnime", null)
                        } else null
                    } catch (e: Exception) {
                        Log.e("AnimeRepository", "Error parsing episode element", e)
                        null
                    }
                }
            } catch (e: Exception) {
                Log.e("AnimeRepository", "Error in getEpisodes for $animeUrl", e)
                emptyList()
            }
        }
    }
}
