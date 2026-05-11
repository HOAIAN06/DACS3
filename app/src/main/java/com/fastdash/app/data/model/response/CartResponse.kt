package com.fastdash.app.data.model.response

data class CartItemResponse(
	val id: Long,
	val productId: Long,
	val productName: String,
	val quantity: Int,
	val unitPrice: Double,
	val sizeName: String? = null,
	val toppings: List<ToppingResponse> = emptyList()
) {
	val totalPrice: Double
		get() = unitPrice * quantity + toppings.sumOf { it.price } * quantity
}

data class CartResponse(
	val id: Long,
	val userId: Long,
	val items: List<CartItemResponse> = emptyList()
) {
	val subtotal: Double
		get() = items.sumOf { it.totalPrice }

	val shippingFee: Double = 15000.0

	val total: Double
		get() = subtotal + shippingFee
}

