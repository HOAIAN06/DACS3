package com.fastdash.app.data.model.response

import com.google.gson.annotations.SerializedName

data class ShippingFeeQuoteResponse(
    @SerializedName(value = "branchId", alternate = ["branch_id"])
    val branchId: Long = 0L,
    @SerializedName(value = "branchName", alternate = ["branch_name"])
    val branchName: String = "",
    @SerializedName(value = "distanceKm", alternate = ["distance_km"])
    val distanceKm: Double = 0.0,
    @SerializedName(value = "shippingFee", alternate = ["shipping_fee"])
    val shippingFee: Double = 0.0,
    @SerializedName("supported")
    val supported: Boolean = false,
    @SerializedName("message")
    val message: String = ""
)
