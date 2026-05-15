package com.fastdash.app.data.model.response

data class AdminPageResponse<T>(
    val content: List<T> = emptyList(),
    val totalElements: Long = 0,
    val totalPages: Int = 0,
    val size: Int = 0,
    val number: Int = 0
)

data class AdminOrderSummaryResponse(
    val id: Long,
    val orderCode: String,
    val customerName: String? = null,
    val customerEmail: String? = null,
    val status: String,
    val deliveryType: String? = null,
    val totalAmount: Double,
    val createdAt: String
)

data class AdminDashboardSummaryResponse(
    val totalUsers: Int = 0,
    val totalProducts: Int = 0,
    val totalOrders: Int = 0,
    val pendingOrders: Int = 0,
    val completedOrders: Int = 0,
    val cancelledOrders: Int = 0,
    val totalRevenue: Double = 0.0
)
