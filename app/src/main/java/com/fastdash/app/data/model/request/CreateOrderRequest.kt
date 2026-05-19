package com.fastdash.app.data.model.request

import com.google.gson.annotations.SerializedName

data class CreateOrderRequest(
    @SerializedName("branchId")
    val branchId: Long,
    @SerializedName("deliveryType")
    val deliveryType: String,
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
    val paymentMethod: String,
    @SerializedName("items")
    val items: List<OrderItemRequest>
)

data class OrderItemRequest(
    @SerializedName("productId")
    val productId: Long,
    @SerializedName("productSizeId")
    val productSizeId: Long? = null,
    @SerializedName("quantity")
    val quantity: Int,
    @SerializedName("note")
    val note: String? = null,
    @SerializedName("toppingIds")
    val toppingIds: List<Long> = emptyList()
)

data class CreateOrderFromCartRequest(
    @SerializedName("branchId")
    val branchId: Long,
    @SerializedName("deliveryType")
    val deliveryType: String,
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
    val paymentMethod: String
)
