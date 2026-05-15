package com.fastdash.app.data.model.request

data class AddToCartRequest(
	val productId: Long,
	val productSizeId: Long? = null,
	val toppingIds: List<Long> = emptyList(),
	val quantity: Int,
	val note: String? = null
)

data class CheckoutRequest(
	val branchId: Long,
	val deliveryType: String = "DELIVERY",
	val receiverName: String,
	val receiverPhone: String,
	val deliveryAddress: String,
	val note: String? = null,
	val paymentMethod: String = "COD"
)

data class UpdateCartItemRequest(
	val quantity: Int,
	val note: String? = null
)

