package com.fastdash.app.utils

import android.content.Context
import coil.request.ImageRequest

object ImageUtils {
    fun formatImageUrl(url: String?): String {
        if (url.isNullOrBlank()) return ""

        // Handle localhost issues in emulator
        var formattedUrl = url
        if (formattedUrl.contains("localhost")) {
            formattedUrl = formattedUrl.replace("localhost", "10.0.2.2")
        }

        if (formattedUrl.startsWith("http")) return formattedUrl

        val baseUrl = Constants.BASE_URL
        val cleanUrl = if (formattedUrl.startsWith("/")) formattedUrl.substring(1) else formattedUrl

        return if (baseUrl.endsWith("/")) {
            "$baseUrl$cleanUrl"
        } else {
            "$baseUrl/$cleanUrl"
        }
    }

    /**
     * Build a Coil ImageRequest that adds Authorization header when a token exists.
     * This ensures protected image endpoints can be fetched with the same JWT used for API calls.
     */
    fun buildImageRequest(context: Context, url: String?): ImageRequest {
        val formatted = formatImageUrl(url)
        val builder = ImageRequest.Builder(context)
            .data(formatted)

        // Add Authorization header if token present
        TokenManager(context).getToken()?.let { token ->
            if (token.isNotBlank()) builder.addHeader("Authorization", "Bearer $token")
        }

        return builder.build()
    }
}
