package com.fastdash.app.data.model.response

import com.google.gson.annotations.SerializedName

data class CartProductSummary(
	val id: Long,
	val name: String
)

data class CartProductSizeSummary(
	val id: Long,
	@SerializedName(value = "sizeName", alternate = ["size_name", "name"]) val sizeName: String? = null,
	val price: Double = 0.0
)

data class CartItemResponse(
	val id: Long,
	@SerializedName(value = "productId", alternate = ["product_id"]) private val rawProductId: Long? = null,
	@SerializedName(value = "productName", alternate = ["product_name"]) private val rawProductName: String? = null,
	@SerializedName(value = "sizeName", alternate = ["size_name"]) private val rawSizeName: String? = null,
	val product: CartProductSummary? = null,
	val productSize: CartProductSizeSummary? = null,
	val quantity: Int,
	val unitPrice: Double,
	val note: String? = null,
	val sizeName: String? = null,
	val toppings: List<ToppingResponse> = emptyList()
) {
	val productId: Long
		get() = rawProductId ?: product?.id ?: 0L

	val productName: String
		get() = rawProductName ?: product?.name.orEmpty()

	val resolvedSizeName: String?
		get() = sizeName ?: rawSizeName ?: productSize?.sizeName

	val totalPrice: Double
		get() = unitPrice * quantity + toppings.sumOf { it.price } * quantity
}

data class CartResponse(
	val id: Long,
	val userId: Long,
	val items: List<CartItemResponse> = emptyList(),
	val shippingFee: Double? = null
) {
	val subtotal: Double
		get() = items.sumOf { it.totalPrice }

	val resolvedShippingFee: Double
		get() = shippingFee ?: if (items.isEmpty()) 0.0 else 15000.0

	val total: Double
		get() = subtotal + resolvedShippingFee
}

