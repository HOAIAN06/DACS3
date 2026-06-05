package com.fastdash.app.data.model.response

import com.google.gson.annotations.SerializedName

data class AdminPageResponse<T>(
    val content: List<T> = emptyList(),
    val totalElements: Long = 0,
    val totalPages: Int = 0,
    val size: Int = 0,
    val number: Int = 0
)

data class AdminOrderSummaryResponse(
    val id: Long = 0L,
    @SerializedName(value = "orderCode", alternate = ["order_code", "code"])
    val orderCode: String = "",
    @SerializedName(value = "receiverName", alternate = ["receiver_name"])
    val receiverName: String? = null,
    @SerializedName(value = "customerName", alternate = ["fullName", "full_name"])
    val customerName: String? = null,
    @SerializedName(value = "customerEmail", alternate = ["email", "userEmail", "user_email"])
    val customerEmail: String? = null,
    @SerializedName(value = "receiverPhone", alternate = ["receiver_phone", "phone"])
    val receiverPhone: String? = null,
    val status: String = "",
    @SerializedName(value = "orderStatus", alternate = ["order_status"])
    val orderStatus: String? = null,
    @SerializedName(value = "deliveryType", alternate = ["delivery_type"])
    val deliveryType: String? = null,
    @SerializedName(value = "paymentMethod", alternate = ["payment_method"])
    val paymentMethod: String? = null,
    @SerializedName(value = "paymentStatus", alternate = ["payment_status"])
    val paymentStatus: String? = null,
    @SerializedName(value = "totalAmount", alternate = ["total_amount"])
    val totalAmount: Double = 0.0,
    @SerializedName(value = "createdAt", alternate = ["created_at"])
    val createdAt: String? = null
)

data class AdminOrdersPageResponse(
    val content: List<AdminOrderSummaryResponse> = emptyList(),
    val totalElements: Long = 0,
    val totalPages: Int = 0,
    val size: Int = 0,
    val number: Int = 0
)

data class AdminOrderDetailResponse(
    val id: Long = 0L,
    @SerializedName(value = "orderCode", alternate = ["order_code", "code"])
    val orderCode: String = "",
    val status: String = "",
    @SerializedName(value = "orderStatus", alternate = ["order_status"])
    val orderStatus: String? = null,
    @SerializedName(value = "deliveryType", alternate = ["delivery_type"])
    val deliveryType: String? = null,
    @SerializedName(value = "receiverName", alternate = ["receiver_name", "customerName", "customer_name"])
    val receiverName: String? = null,
    @SerializedName(value = "receiverPhone", alternate = ["receiver_phone"])
    val receiverPhone: String? = null,
    @SerializedName(value = "deliveryAddress", alternate = ["delivery_address"])
    val deliveryAddress: String? = null,
    @SerializedName(value = "deliveryLatitude", alternate = ["delivery_latitude", "latitude"])
    val deliveryLatitude: Double? = null,
    @SerializedName(value = "deliveryLongitude", alternate = ["delivery_longitude", "longitude"])
    val deliveryLongitude: Double? = null,
    @SerializedName(value = "subtotal", alternate = ["sub_total"])
    val subtotal: Double = 0.0,
    @SerializedName(value = "shippingFee", alternate = ["shipping_fee"])
    val shippingFee: Double = 0.0,
    @SerializedName(value = "totalAmount", alternate = ["total_amount"])
    val totalAmount: Double = 0.0,
    @SerializedName(value = "paymentMethod", alternate = ["payment_method"])
    val paymentMethod: String? = null,
    @SerializedName(value = "paymentStatus", alternate = ["payment_status"])
    val paymentStatus: String? = null,
    @SerializedName(value = "paymentUrl", alternate = ["payment_url"])
    val paymentUrl: String? = null,
    @SerializedName(value = "createdAt", alternate = ["created_at"])
    val createdAt: String? = null,
    val items: List<OrderItemResponse> = emptyList()
)

data class AdminDashboardSummaryResponse(
    @SerializedName("totalUsers")
    val totalUsers: Long = 0L,
    @SerializedName("totalProducts")
    val totalProducts: Long = 0L,
    @SerializedName("totalOrders")
    val totalOrders: Long = 0L,
    @SerializedName("pendingOrders")
    val pendingOrders: Long = 0L,
    @SerializedName("pendingPaymentOrders")
    val pendingPaymentOrders: Long = 0L,
    @SerializedName("preparingOrders")
    val preparingOrders: Long = 0L,
    @SerializedName("deliveringOrders")
    val deliveringOrders: Long = 0L,
    @SerializedName("completedOrders")
    val completedOrders: Long = 0L,
    @SerializedName("cancelledOrders")
    val cancelledOrders: Long = 0L,
    @SerializedName("totalRevenue")
    val totalRevenue: Long = 0L,
    @SerializedName("todayRevenue")
    val todayRevenue: Long? = null,
    @SerializedName("allTimeRevenue")
    val allTimeRevenue: Long? = null
)

data class RevenueReportChartPointResponse(
    @SerializedName("label")
    val label: String = "",
    @SerializedName("date")
    val date: String? = null,
    @SerializedName("hour")
    val hour: Int? = null,
    @SerializedName("revenue")
    val revenue: Long = 0L,
    @SerializedName("orderCount")
    val orderCount: Long = 0L
)

data class RevenueReportResponse(
    @SerializedName("period")
    val period: String = "WEEK",
    @SerializedName("from")
    val from: String? = null,
    @SerializedName("to")
    val to: String? = null,
    @SerializedName("totalRevenue")
    val totalRevenue: Long = 0L,
    @SerializedName("totalOrders")
    val totalOrders: Long = 0L,
    @SerializedName("completedOrders")
    val completedOrders: Long = 0L,
    @SerializedName("paidCompletedOrders")
    val paidCompletedOrders: Long = 0L,
    @SerializedName("cancelledOrders")
    val cancelledOrders: Long = 0L,
    @SerializedName("averageOrderValue")
    val averageOrderValue: Long = 0L,
    @SerializedName("completionRate")
    val completionRate: Float = 0f,
    @SerializedName("cancelRate")
    val cancelRate: Float = 0f,
    @SerializedName("growthRate")
    val growthRate: Float = 0f,
    @SerializedName("peakLabel")
    val peakLabel: String? = null,
    @SerializedName("peakRevenue")
    val peakRevenue: Long = 0L,
    @SerializedName("peakOrderCount")
    val peakOrderCount: Long = 0L,
    @SerializedName("chartData")
    val chartData: List<RevenueReportChartPointResponse> = emptyList()
)
