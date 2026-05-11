package com.fastdash.app.data.model.response

data class OrderResponse(
	val id: Long,
	val code: String,
	val status: String,
	val createdAt: String,
	val deliveryAddress: String?,
	val shippingFee: Double,
	val totalAmount: Double,
	val items: List<OrderItemResponse>? = emptyList()
)

data class OrderItemResponse(
	val id: Long,
	val productName: String,
	val quantity: Int,
	val price: Double
)


