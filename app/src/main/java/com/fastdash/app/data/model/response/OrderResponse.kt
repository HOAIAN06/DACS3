package com.fastdash.app.data.model.response

import com.google.gson.annotations.SerializedName

data class OrderResponse(
    val id: Long = 0L,
    @SerializedName(value = "orderCode", alternate = ["code"])
    val orderCode: String? = null,
    val status: String? = null,
    val createdAt: String? = null,
    val deliveryType: String? = null,
    val receiverName: String? = null,
    val receiverPhone: String? = null,
    val deliveryAddress: String? = null,
    val subtotal: Double = 0.0,
    val shippingFee: Double = 0.0,
    val totalAmount: Double = 0.0,
    val paymentMethod: String? = null,
    val paymentStatus: String? = null,
    val items: List<OrderItemResponse>? = emptyList()
) {
    val code: String
        get() = orderCode.orEmpty()
}

data class OrderItemResponse(
    val id: Long = 0L,
    val productName: String = "",
    val sizeName: String? = null,
    val quantity: Int = 0,
    @SerializedName(value = "unitPrice", alternate = ["price"])
    val unitPrice: Double = 0.0,
    val totalPrice: Double = 0.0,
    val note: String? = null,
    val toppings: List<String> = emptyList()
) {
    val price: Double
        get() = unitPrice
}
