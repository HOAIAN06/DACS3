package com.fastdash.app.data.model.request

import com.google.gson.annotations.SerializedName

data class VnpayPaymentRequest(
    @SerializedName("orderId")
    val orderId: Long
)
