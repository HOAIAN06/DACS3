package com.fastdash.app.ui.admin

import androidx.compose.ui.graphics.Color
import com.fastdash.app.data.model.response.ApiErrorResponse
import com.google.gson.Gson
import retrofit2.Response
import java.text.NumberFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale
import kotlin.math.roundToLong

enum class OrderGroup {
    ALL,
    NEED_ACTION,
    IN_PROGRESS,
    COMPLETED,
    OTHER
}

enum class OrderSortOption(val apiValue: String, val label: String) {
    NEWEST("createdAt,desc", "Mới nhất"),
    OLDEST("createdAt,asc", "Cũ nhất"),
    HIGHEST_VALUE("totalAmount,desc", "Giá trị cao"),
    LOWEST_VALUE("totalAmount,asc", "Giá trị thấp");

    companion object {
        fun fromApiValue(value: String?): OrderSortOption {
            return entries.firstOrNull { it.apiValue == value } ?: NEWEST
        }
    }
}

data class AdminOrderStatusFilter(
    val label: String,
    val value: String?
)

data class OrderAction(
    val targetStatus: String,
    val label: String,
    val isDestructive: Boolean = false
)

data class QuickSummaryFilter(
    val label: String,
    val status: String,
    val count: Long?
)

private val gson = Gson()
private val displayDateTimeFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("dd/MM HH:mm", Locale("vi", "VN"))
private val isoDateFormatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE
private val fullDateFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale("vi", "VN"))

val adminOrderStatusFilters = listOf(
    AdminOrderStatusFilter("Tất cả", null),
    AdminOrderStatusFilter("Chờ thanh toán", "PENDING_PAYMENT"),
    AdminOrderStatusFilter("Thanh toán lỗi", "PAYMENT_FAILED"),
    AdminOrderStatusFilter("Chờ xác nhận", "PENDING_CONFIRMATION"),
    AdminOrderStatusFilter("Đã xác nhận", "CONFIRMED"),
    AdminOrderStatusFilter("Đang chuẩn bị", "PREPARING"),
    AdminOrderStatusFilter("Đang giao", "DELIVERING"),
    AdminOrderStatusFilter("Hoàn thành", "COMPLETED"),
    AdminOrderStatusFilter("Đã hủy", "CANCELLED")
)

fun orderGroupLabel(group: OrderGroup): String = when (group) {
    OrderGroup.ALL -> "Tất cả"
    OrderGroup.NEED_ACTION -> "Cần xử lý"
    OrderGroup.IN_PROGRESS -> "Đang làm"
    OrderGroup.COMPLETED -> "Hoàn thành"
    OrderGroup.OTHER -> "Khác"
}

fun statusesForGroup(group: OrderGroup): List<String> = when (group) {
    OrderGroup.ALL -> emptyList()
    OrderGroup.NEED_ACTION -> listOf("PENDING_CONFIRMATION", "PENDING_PAYMENT", "PAYMENT_FAILED")
    OrderGroup.IN_PROGRESS -> listOf("CONFIRMED", "PREPARING", "DELIVERING")
    OrderGroup.COMPLETED -> listOf("COMPLETED")
    OrderGroup.OTHER -> listOf("CANCELLED")
}

fun groupForStatus(status: String?): OrderGroup = when (status?.trim()?.uppercase()) {
    "PENDING_PAYMENT", "PAYMENT_FAILED", "PENDING_CONFIRMATION" -> OrderGroup.NEED_ACTION
    "CONFIRMED", "PREPARING", "DELIVERING" -> OrderGroup.IN_PROGRESS
    "COMPLETED" -> OrderGroup.COMPLETED
    "CANCELLED" -> OrderGroup.OTHER
    else -> OrderGroup.ALL
}

fun orderStatusLabel(status: String?): String = when (status?.trim()?.uppercase()) {
    "PENDING_PAYMENT" -> "Chờ thanh toán"
    "PAYMENT_FAILED" -> "Thanh toán lỗi"
    "PENDING_CONFIRMATION" -> "Chờ xác nhận"
    "CONFIRMED" -> "Đã xác nhận"
    "PREPARING" -> "Đang chuẩn bị"
    "DELIVERING" -> "Đang giao"
    "COMPLETED" -> "Hoàn thành"
    "CANCELLED" -> "Đã hủy"
    else -> status?.takeIf { it.isNotBlank() } ?: "Không rõ"
}

fun paymentStatusLabel(status: String?): String = when (status?.trim()?.uppercase()) {
    "UNPAID" -> "Chưa thanh toán"
    "PENDING" -> "Chờ thanh toán"
    "PAID" -> "Đã thanh toán"
    "FAILED" -> "Thanh toán lỗi"
    "REFUNDED" -> "Đã hoàn tiền"
    else -> status?.takeIf { it.isNotBlank() } ?: "Không rõ"
}

fun paymentMethodLabel(method: String?): String = when (method?.trim()?.uppercase()) {
    "CASH", "COD" -> "COD"
    "VNPAY" -> "VNPAY"
    else -> method?.takeIf { it.isNotBlank() } ?: "Không rõ"
}

fun deliveryTypeLabel(type: String?): String = when (type?.trim()?.uppercase()) {
    "DELIVERY" -> "Giao tận nơi"
    "PICKUP" -> "Nhận tại cửa hàng"
    else -> type?.takeIf { it.isNotBlank() } ?: "Không rõ"
}

fun formatVnd(amount: Long): String = formatVnd(amount.toDouble())

fun formatVnd(amount: Double): String {
    val formatter = NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN"))
    formatter.maximumFractionDigits = 0
    formatter.minimumFractionDigits = 0
    return "${formatter.format(amount.roundToLong())}đ"
}

fun formatDateTime(value: String?): String {
    if (value.isNullOrBlank()) return "--"
    return try {
        LocalDateTime.parse(value).format(displayDateTimeFormatter)
    } catch (_: DateTimeParseException) {
        value
    }
}

fun formatApiDate(value: LocalDate?): String? = value?.format(isoDateFormatter)

fun formatFilterDate(value: String?): String {
    if (value.isNullOrBlank()) return ""
    return runCatching { LocalDate.parse(value).format(fullDateFormatter) }.getOrElse { value }
}

fun allowedNextActions(status: String): List<OrderAction> = when (status.trim().uppercase()) {
    "PENDING_CONFIRMATION" -> listOf(
        OrderAction(targetStatus = "CONFIRMED", label = "Xác nhận đơn"),
        OrderAction(targetStatus = "CANCELLED", label = "Hủy đơn", isDestructive = true)
    )
    "CONFIRMED" -> listOf(
        OrderAction(targetStatus = "PREPARING", label = "Bắt đầu chuẩn bị"),
        OrderAction(targetStatus = "CANCELLED", label = "Hủy đơn", isDestructive = true)
    )
    "PREPARING" -> listOf(OrderAction(targetStatus = "DELIVERING", label = "Bắt đầu giao"))
    "DELIVERING" -> listOf(OrderAction(targetStatus = "COMPLETED", label = "Hoàn thành"))
    "PENDING_PAYMENT" -> listOf(OrderAction(targetStatus = "CANCELLED", label = "Hủy đơn", isDestructive = true))
    "PAYMENT_FAILED" -> listOf(OrderAction(targetStatus = "CANCELLED", label = "Hủy đơn", isDestructive = true))
    else -> emptyList()
}

fun orderActionHint(status: String): String? = when (status.trim().uppercase()) {
    "PENDING_PAYMENT" -> "Đơn đang chờ khách thanh toán."
    "PAYMENT_FAILED" -> "Thanh toán thất bại. Có thể hủy đơn nếu cần."
    "COMPLETED" -> "Đơn đã hoàn thành."
    "CANCELLED" -> "Đơn đã hủy."
    else -> null
}

fun quickActionForList(status: String?): OrderAction? = when (status?.trim()?.uppercase()) {
    "PENDING_CONFIRMATION" -> OrderAction(targetStatus = "CONFIRMED", label = "Xác nhận")
    else -> null
}

fun quickActionHint(status: String?): String? = when (status?.trim()?.uppercase()) {
    "PENDING_PAYMENT" -> "Chờ khách thanh toán"
    "PAYMENT_FAILED" -> "Thanh toán thất bại"
    else -> null
}

fun statusContainerColor(status: String?): Color = when (status?.trim()?.uppercase()) {
    "PENDING_PAYMENT" -> Color(0xFFFFF4D6)
    "PAYMENT_FAILED" -> Color(0xFFFDE8E8)
    "PENDING_CONFIRMATION" -> Color(0xFFFFF1D6)
    "CONFIRMED" -> Color(0xFFE7F0FF)
    "PREPARING" -> Color(0xFFF3E8FF)
    "DELIVERING" -> Color(0xFFE0F7FA)
    "COMPLETED" -> Color(0xFFE8F7EC)
    "CANCELLED" -> Color(0xFFF2F4F7)
    else -> Color(0xFFF3F4F6)
}

fun statusContentColor(status: String?): Color = when (status?.trim()?.uppercase()) {
    "PENDING_PAYMENT" -> Color(0xFF9A6700)
    "PAYMENT_FAILED" -> Color(0xFFB42318)
    "PENDING_CONFIRMATION" -> Color(0xFFB54708)
    "CONFIRMED" -> Color(0xFF175CD3)
    "PREPARING" -> Color(0xFF7A1FA2)
    "DELIVERING" -> Color(0xFF0E7490)
    "COMPLETED" -> Color(0xFF15803D)
    "CANCELLED" -> Color(0xFF667085)
    else -> Color(0xFF475467)
}

fun orderSummarySortComparator(sort: String): Comparator<com.fastdash.app.data.model.response.AdminOrderSummaryResponse> {
    val descending = !sort.endsWith(",asc")
    return Comparator { a, b ->
        val compareValue = when {
            sort.startsWith("totalAmount") -> a.totalAmount.compareTo(b.totalAmount)
            else -> (a.createdAt ?: "").compareTo(b.createdAt ?: "")
        }
        if (descending) -compareValue else compareValue
    }
}

fun mapAdminOrdersError(code: Int?, fallback: String? = null): String = when (code) {
    400 -> "Không thể cập nhật trạng thái này"
    401 -> "Phiên đăng nhập đã hết hạn"
    403 -> "Bạn không có quyền truy cập"
    500 -> "Có lỗi xảy ra, vui lòng thử lại"
    else -> fallback?.takeIf { it.isNotBlank() } ?: "Có lỗi xảy ra, vui lòng thử lại"
}

fun parseApiErrorMessage(raw: String?): String? {
    if (raw.isNullOrBlank()) return null
    return runCatching { gson.fromJson(raw, ApiErrorResponse::class.java) }
        .getOrNull()
        ?.message
        ?.takeIf { it.isNotBlank() }
}

fun <T> friendlyErrorMessage(response: Response<T>, defaultMessage: String): String {
    val parsed = parseApiErrorMessage(runCatching { response.errorBody()?.string() }.getOrNull())
    return mapAdminOrdersError(response.code(), parsed ?: defaultMessage)
}
