package com.fastdash.app.ui.order

import java.nio.charset.Charset
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

private val orderDateFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale.forLanguageTag("vi-VN"))

fun formatOrderDate(raw: String?): String {
    val value = raw.normalizeVietnameseText().trim()
    if (value.isBlank()) return "Chưa có thời gian"

    return runCatching { OffsetDateTime.parse(value).format(orderDateFormatter) }
        .recoverCatching { ZonedDateTime.parse(value).format(orderDateFormatter) }
        .recoverCatching {
            LocalDateTime.parse(value, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                .atZone(ZoneId.systemDefault())
                .format(orderDateFormatter)
        }
        .getOrElse {
            value.replace("T", " ").take(16)
        }
}

fun mapOrderStatus(status: String?): String {
    return when (status.normalizeVietnameseText().trim().uppercase()) {
        "PENDING_PAYMENT" -> "Chờ thanh toán"
        "PENDING_CONFIRMATION" -> "Chờ cửa hàng xác nhận"
        "PAYMENT_FAILED" -> "Thanh toán thất bại"
        "PENDING" -> "Chờ xác nhận"
        "CONFIRMED" -> "Đã xác nhận"
        "PREPARING" -> "Đang chuẩn bị món"
        "DELIVERING" -> "Đang giao hàng"
        "COMPLETED" -> "Hoàn thành"
        "CANCELLED" -> "Đã hủy"
        else -> "Chờ xác nhận"
    }
}

fun mapPaymentMethod(method: String?): String {
    return when (method.normalizeVietnameseText().trim().uppercase()) {
        "CASH", "COD" -> "Tiền mặt khi nhận hàng"
        "VNPAY" -> "Thanh toán VNPAY"
        "BANK_TRANSFER", "TRANSFER", "ONLINE" -> "Chuyển khoản ngân hàng"
        else -> "Chưa có thông tin"
    }
}

fun mapPaymentStatus(status: String?): String {
    return when (status.normalizeVietnameseText().trim().uppercase()) {
        "UNPAID" -> "Chưa thanh toán"
        "PENDING" -> "Chờ xác nhận thanh toán"
        "PAID" -> "Đã thanh toán"
        "FAILED" -> "Thanh toán thất bại"
        "REFUNDED" -> "Đã hoàn tiền"
        else -> "Chưa có thông tin"
    }
}

fun orderStatusColor(status: String?): androidx.compose.ui.graphics.Color {
    return when (status.normalizeVietnameseText().trim().uppercase()) {
        "PENDING_PAYMENT", "PENDING_CONFIRMATION", "PENDING", "CONFIRMED", "PREPARING" -> androidx.compose.ui.graphics.Color(0xFFF2994A)
        "PAYMENT_FAILED", "CANCELLED" -> androidx.compose.ui.graphics.Color(0xFFEB5757)
        "DELIVERING" -> androidx.compose.ui.graphics.Color(0xFF2D9CDB)
        "COMPLETED" -> androidx.compose.ui.graphics.Color(0xFF27AE60)
        else -> androidx.compose.ui.graphics.Color.Gray
    }
}

fun String?.normalizeVietnameseText(): String {
    val raw = this.orEmpty()
    if (raw.isBlank()) return raw
    val suspicious = listOf("Ãƒ", "Ã†", "Ã„", "Ã¡Âº", "Ã¡Â»")
    if (suspicious.none { raw.contains(it) }) return raw
    return runCatching {
        String(raw.toByteArray(Charset.forName("ISO-8859-1")), Charsets.UTF_8)
    }.getOrDefault(raw)
}
