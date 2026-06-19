package com.fastdash.app.data.model.response

import com.google.gson.annotations.SerializedName

data class VnpayPaymentResponse(
    @SerializedName("orderId")
    val orderId: Long,
    @SerializedName("paymentUrl")
    val paymentUrl: String? = null
)
