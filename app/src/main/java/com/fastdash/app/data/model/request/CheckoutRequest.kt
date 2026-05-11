package com.fastdash.app.data.model.request

data class AddToCartRequest(
	val productId: Long,
	val quantity: Int,
	val sizeName: String? = null,
	val toppingIds: List<Long> = emptyList(),
	val note: String? = null
)

data class CheckoutRequest(
	val receiverName: String,
	val receiverPhone: String,
	val deliveryAddress: String,
	val deliveryType: String = "DELIVERY",
	val note: String? = null,
	val paymentMethod: String = "COD"
)

