package com.ben.inly.domain.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import java.net.URI

actual object HtmlMetadataFetcher {
    actual suspend fun fetchMetadata(url: String): UrlMetadata = withContext(Dispatchers.IO) {
        val validUrl = if (!url.startsWith("http://") && !url.startsWith("https://")) "https://$url" else url

        try {
            val document = Jsoup.connect(validUrl)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36")
                .timeout(5000)
                .maxBodySize(250 * 1024)
                .get()

            val title = document.title().takeIf { it.isNotBlank() }
                ?: document.select("meta[property=og:title]").attr("content").takeIf { it.isNotBlank() }

            val description = document.select("meta[name=description]").attr("content").takeIf { it.isNotBlank() }
                ?: document.select("meta[property=og:description]").attr("content").takeIf { it.isNotBlank() }

            var imageUrl: String? = document.select("meta[property=og:image]").attr("abs:content").takeIf { it.isNotBlank() }
                ?: document.select("meta[name=twitter:image]").attr("abs:content").takeIf { it.isNotBlank() }
                ?: document.select("link[rel=apple-touch-icon]").attr("abs:href").takeIf { it.isNotBlank() }
                ?: document.select("link[rel=\"shortcut icon\"]").attr("abs:href").takeIf { it.isNotBlank() }
                ?: document.select("link[rel=icon]").attr("abs:href").takeIf { it.isNotBlank() }

            if (imageUrl?.startsWith("http://") == true) {
                imageUrl = imageUrl.replace("http://", "https://")
            }

            if (imageUrl == null) {
                val finalHost = try { URI(document.location()).host } catch (_: Exception) { null }
                if (finalHost != null) imageUrl = "https://www.google.com/s2/favicons?domain=$finalHost&sz=128"
            }

            UrlMetadata(title = title, description = description, imageUrl = imageUrl)

        } catch (e: Exception) {
            val host = try { URI(validUrl).host } catch (_: Exception) { null }
            UrlMetadata(
                title = url,
                description = "Could not load preview",
                imageUrl = if (host != null) "https://www.google.com/s2/favicons?domain=$host&sz=128" else null
            )
        }
    }
}