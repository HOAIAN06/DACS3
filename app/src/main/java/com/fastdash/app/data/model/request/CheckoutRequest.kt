package com.fastdash.app.data.model.request

import com.google.gson.annotations.SerializedName

data class AddToCartRequest(
    @SerializedName("productId")
    val productId: Long,
    @SerializedName("productSizeId")
    val productSizeId: Long? = null,
    @SerializedName("toppingIds")
    val toppingIds: List<Long> = emptyList(),
    @SerializedName("quantity")
    val quantity: Int,
    @SerializedName("note")
    val note: String? = null
)

data class CheckoutRequest(
    @SerializedName("branchId")
    val branchId: Long,
    @SerializedName("deliveryType")
    val deliveryType: String = "DELIVERY",
    @SerializedName("receiverName")
    val receiverName: String,
    @SerializedName("receiverPhone")
    val receiverPhone: String,
    @SerializedName("deliveryAddress")
    val deliveryAddress: String,
    @SerializedName("deliveryLatitude")
    val deliveryLatitude: Double? = null,
    @SerializedName("deliveryLongitude")
    val deliveryLongitude: Double? = null,
    @SerializedName("note")
    val note: String? = null,
    @SerializedName("paymentMethod")
    val paymentMethod: String = "CASH",
    @Transient
    val branchName: String? = null,
    @Transient
    val branchAddress: String? = null
)

data class UpdateCartItemRequest(
    @SerializedName("quantity")
    val quantity: Int,
    @SerializedName("note")
    val note: String? = null
)

data class ShippingFeeQuoteRequest(
    @SerializedName("branchId")
    val branchId: Long,
    @SerializedName("latitude")
    val latitude: Double,
    @SerializedName("longitude")
    val longitude: Double
)
