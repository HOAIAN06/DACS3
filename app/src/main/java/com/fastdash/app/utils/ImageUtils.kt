package com.fastdash.app.utils

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
}
