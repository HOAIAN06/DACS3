package com.fastdash.app.data.model.response

import com.google.gson.annotations.SerializedName

data class OrderResponse(
    val id: Long = 0L,
    @SerializedName(value = "orderCode", alternate = ["code"])
    val orderCode: String? = null,
    @SerializedName("status")
    val status: String? = null,
    @SerializedName(value = "createdAt", alternate = ["created_at"])
    val createdAt: String? = null,
    @SerializedName(value = "deliveryType", alternate = ["delivery_type"])
    val deliveryType: String? = null,
    @SerializedName(value = "receiverName", alternate = ["receiver_name"])
    val receiverName: String? = null,
    @SerializedName(value = "receiverPhone", alternate = ["receiver_phone"])
    val receiverPhone: String? = null,
    @SerializedName(value = "deliveryAddress", alternate = ["delivery_address"])
    val deliveryAddress: String? = null,
    @SerializedName(value = "deliveryLatitude", alternate = ["delivery_latitude", "latitude"])
    val deliveryLatitude: Double? = null,
    @SerializedName(value = "deliveryLongitude", alternate = ["delivery_longitude", "longitude"])
    val deliveryLongitude: Double? = null,
    @SerializedName(value = "subtotal", alternate = ["sub_total"])
    val subtotal: Double = 0.0,
    @SerializedName(value = "shippingFee", alternate = ["shipping_fee"])
    val shippingFee: Double = 0.0,
    @SerializedName(value = "totalAmount", alternate = ["total_amount"])
    val totalAmount: Double = 0.0,
    @SerializedName(value = "paymentMethod", alternate = ["payment_method"])
    val paymentMethod: String? = null,
    @SerializedName(value = "paymentStatus", alternate = ["payment_status"])
    val paymentStatus: String? = null,
    @SerializedName(value = "paymentUrl", alternate = ["payment_url"])
    val paymentUrl: String? = null,
    @SerializedName(value = "orderStatus", alternate = ["order_status"])
    val orderStatus: String? = null,
    @SerializedName(value = "branchName", alternate = ["branch_name", "storeName", "store_name"])
    val branchName: String? = null,
    @SerializedName(value = "branchAddress", alternate = ["branch_address", "storeAddress", "store_address"])
    val branchAddress: String? = null,
    @SerializedName(value = "distanceKm", alternate = ["distance_km"])
    val distanceKm: Double? = null,
    @SerializedName(value = "discountAmount", alternate = ["discount_amount", "discount"])
    val discountAmount: Double = 0.0,
    @SerializedName(value = "note", alternate = ["orderNote", "order_note", "customerNote", "customer_note"])
    val note: String? = null,
    @SerializedName(value = "items", alternate = ["orderItems", "order_items"])
    val items: List<OrderItemResponse>? = emptyList()
) {
    val code: String
        get() = orderCode.orEmpty()
}

data class OrderItemResponse(
    val id: Long = 0L,
    @SerializedName(value = "productName", alternate = ["product_name", "name"])
    val productName: String = "",
    @SerializedName(value = "sizeName", alternate = ["size_name"])
    val sizeName: String? = null,
    val quantity: Int = 0,
    @SerializedName(value = "unitPrice", alternate = ["price"])
    val unitPrice: Double = 0.0,
    @SerializedName(value = "totalPrice", alternate = ["total_price"])
    val totalPrice: Double = 0.0,
    val note: String? = null,
    val toppings: List<String> = emptyList()
) {
    val price: Double
        get() = unitPrice
}
