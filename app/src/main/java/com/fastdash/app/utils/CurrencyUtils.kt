package com.fastdash.app.utils

import java.text.NumberFormat
import java.util.Locale

object CurrencyUtils {
    fun formatVnd(amount: Double): String {
        val format = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
        return format.format(amount)
    }
}