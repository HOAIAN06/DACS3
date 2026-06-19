package com.fastdash.app.data.model.response

import com.google.gson.annotations.SerializedName

data class AdminCustomersPageResponse(
    val content: List<AdminCustomerResponse> = emptyList(),
    val totalElements: Long = 0,
    val totalPages: Int = 0,
    val size: Int = 10,
    val number: Int = 0
)

data class AdminCustomerOrdersPageResponse(
    val content: List<AdminCustomerOrderResponse> = emptyList(),
    val totalElements: Long = 0,
    val totalPages: Int = 0,
    val size: Int = 10,
    val number: Int = 0
)

data class AdminCustomerResponse(
    val id: Long = 0L,
    val fullName: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val address: String? = null,
    val status: Int = 0,
    @SerializedName(value = "totalOrders", alternate = ["total_orders"])
    val totalOrders: Int = 0,
    @SerializedName(value = "completedOrders", alternate = ["completed_orders"])
    val completedOrders: Int = 0,
    @SerializedName(value = "cancelledOrders", alternate = ["cancelled_orders"])
    val cancelledOrders: Int = 0,
    @SerializedName(value = "totalSpent", alternate = ["total_spent"])
    val totalSpent: Double = 0.0,
    @SerializedName(value = "lastOrderAt", alternate = ["last_order_at"])
    val lastOrderAt: String? = null,
    @SerializedName("segment")
    val segment: String? = null,
    @SerializedName(value = "roleName", alternate = ["role_name"])
    val roleName: String? = null,
    @SerializedName(value = "createdAt", alternate = ["created_at"])
    val createdAt: String? = null,
    @SerializedName(value = "updatedAt", alternate = ["updated_at"])
    val updatedAt: String? = null
)

data class AdminCustomerOrderResponse(
    val id: Long = 0L,
    @SerializedName(value = "orderCode", alternate = ["order_code", "code"])
    val orderCode: String? = null,
    @SerializedName(value = "totalAmount", alternate = ["total_amount"])
    val totalAmount: Double = 0.0,
    @SerializedName(value = "paymentMethod", alternate = ["payment_method"])
    val paymentMethod: String? = null,
    @SerializedName(value = "paymentStatus", alternate = ["payment_status"])
    val paymentStatus: String? = null,
    @SerializedName(value = "orderStatus", alternate = ["order_status", "status"])
    val orderStatus: String? = null,
    @SerializedName(value = "deliveryAddress", alternate = ["delivery_address"])
    val deliveryAddress: String? = null,
    @SerializedName(value = "createdAt", alternate = ["created_at"])
    val createdAt: String? = null,
    @SerializedName(value = "updatedAt", alternate = ["updated_at"])
    val updatedAt: String? = null
)
