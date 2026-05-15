package com.fastdash.app.data.model.request

data class CreateOrderRequest(
	val branchId: Long,
	val deliveryType: String,
	val receiverName: String,
	val receiverPhone: String,
	val deliveryAddress: String,
	val note: String? = null,
	val paymentMethod: String,
	val items: List<OrderItemRequest>
)

data class OrderItemRequest(
	val productId: Long,
	val productSizeId: Long? = null,
	val quantity: Int,
	val note: String? = null,
	val toppingIds: List<Long> = emptyList()
)

data class CreateOrderFromCartRequest(
	val branchId: Long,
	val deliveryType: String,
	val receiverName: String,
	val receiverPhone: String,
	val deliveryAddress: String,
	val note: String? = null,
	val paymentMethod: String
)
