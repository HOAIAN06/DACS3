package com.fastdash.app.data.model.response

import com.google.gson.annotations.SerializedName

data class OrderResponse(
	val id: Long,
	@SerializedName(value = "orderCode", alternate = ["code"]) val orderCode: String,
	val status: String,
	val createdAt: String,
	val deliveryType: String? = null,
	val receiverName: String? = null,
	val receiverPhone: String? = null,
	val deliveryAddress: String?,
	val subtotal: Double = 0.0,
	val shippingFee: Double = 0.0,
	val totalAmount: Double,
	val paymentMethod: String? = null,
	val paymentStatus: String? = null,
	val items: List<OrderItemResponse>? = emptyList()
) {
	val code: String
		get() = orderCode
}

data class OrderItemResponse(
	val id: Long,
	val productName: String,
	val sizeName: String? = null,
	val quantity: Int,
	@SerializedName(value = "unitPrice", alternate = ["price"]) val unitPrice: Double,
	val totalPrice: Double = 0.0,
	val note: String? = null,
	val toppings: List<String> = emptyList()
) {
	val price: Double
		get() = unitPrice
}


