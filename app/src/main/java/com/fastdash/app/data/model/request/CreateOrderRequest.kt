package com.fastdash.app.data.model.request

data class CreateOrderRequest(
	val deliveryAddress: String,
	val items: List<OrderItemRequest>
)

data class OrderItemRequest(
	val productId: Long,
	val quantity: Int,
	val customizations: String? = null
)
