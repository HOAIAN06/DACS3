package com.fastdash.app.data.model.response

import com.google.gson.annotations.SerializedName

data class CartToppingResponse(
    val id: Long = 0L,
    val name: String? = null,
    val price: Double = 0.0,
    @SerializedName(value = "imageUrl", alternate = ["image_url", "url", "secure_url"])
    val imageUrl: String? = null
)

data class CartItemResponse(
    val id: Long = 0L,
    @SerializedName(value = "productId", alternate = ["product_id"])
    val productId: Long? = null,
    @SerializedName(value = "productName", alternate = ["product_name"])
    val productName: String? = null,
    @SerializedName(value = "productImageUrl", alternate = ["product_image_url", "imageUrl", "image_url"])
    val productImageUrl: String? = null,
    @SerializedName(value = "productSizeId", alternate = ["product_size_id"])
    val productSizeId: Long? = null,
    @SerializedName(
        value = "productSizeName",
        alternate = ["product_size_name", "sizeName", "size_name"]
    )
    val productSizeName: String? = null,
    val quantity: Int = 0,
    @SerializedName(value = "unitPrice", alternate = ["unit_price"])
    val unitPrice: Double = 0.0,
    val note: String? = null,
    val toppings: List<CartToppingResponse> = emptyList(),
    @SerializedName(value = "totalPrice", alternate = ["total_price"])
    private val rawTotalPrice: Double? = null
) {
    val resolvedProductName: String
        get() = productName.orEmpty()

    val resolvedSizeName: String?
        get() = productSizeName

    val totalPrice: Double
        get() = rawTotalPrice ?: (unitPrice * quantity + toppings.sumOf { it.price } * quantity)
}

data class CartResponse(
    val id: Long = 0L,
    val items: List<CartItemResponse> = emptyList(),
    @SerializedName(value = "totalPrice", alternate = ["total_price"])
    private val rawTotalPrice: Double? = null,
    @SerializedName(value = "shippingFee", alternate = ["shipping_fee"])
    val shippingFee: Double? = null
) {
    val subtotal: Double
        get() = rawTotalPrice ?: items.sumOf { it.totalPrice }

    val resolvedShippingFee: Double
        get() = shippingFee ?: 0.0

    val total: Double
        get() = subtotal + resolvedShippingFee
}
