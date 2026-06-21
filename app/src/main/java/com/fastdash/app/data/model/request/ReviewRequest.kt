package com.fastdash.app.data.model.request

import com.google.gson.annotations.SerializedName

data class ReviewRequest(
    @SerializedName("orderId")
    val orderId: Long,
    @SerializedName("productId")
    val productId: Long,
    @SerializedName("rating")
    val rating: Int,
    @SerializedName("comment")
    val comment: String?
)
