package com.fastdash.app.utils

import java.text.NumberFormat
import java.util.Locale

object CurrencyUtils {
    fun formatVnd(amount: Double): String {
        val formatter = NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN"))
        formatter.maximumFractionDigits = 0
        formatter.minimumFractionDigits = 0
        return "${formatter.format(amount)}\u0111"
    }
}

