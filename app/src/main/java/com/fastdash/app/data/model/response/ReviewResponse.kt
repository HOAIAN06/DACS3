package com.fastdash.app.data.model.response

import com.google.gson.annotations.SerializedName

data class ReviewResponse(
    val id: Long = 0L,
    @SerializedName(value = "orderId", alternate = ["order_id"])
    val orderId: Long? = null,
    @SerializedName(value = "orderCode", alternate = ["order_code"])
    val orderCode: String? = null,
    @SerializedName(value = "productId", alternate = ["product_id"])
    val productId: Long? = null,
    @SerializedName(value = "productName", alternate = ["product_name"])
    val productName: String? = null,
    @SerializedName(value = "productImageUrl", alternate = ["product_image_url", "imageUrl", "image_url"])
    val productImageUrl: String? = null,
    @SerializedName(value = "userId", alternate = ["user_id"])
    val userId: Long? = null,
    @SerializedName(value = "userName", alternate = ["user_name", "fullName", "full_name"])
    val userName: String? = null,
    @SerializedName(value = "userAvatar", alternate = ["user_avatar", "avatar"])
    val userAvatar: String? = null,
    val rating: Int = 0,
    val comment: String? = null,
    val images: List<String> = emptyList(),
    @SerializedName(value = "isVisible", alternate = ["is_visible"])
    val isVisible: Boolean = true,
    @SerializedName(value = "createdAt", alternate = ["created_at"])
    val createdAt: String? = null,
    @SerializedName(value = "updatedAt", alternate = ["updated_at"])
    val updatedAt: String? = null
)

data class ReviewableItemResponse(
    val id: Long = 0L,
    @SerializedName(value = "orderId", alternate = ["order_id"])
    val orderId: Long? = null,
    @SerializedName(value = "orderCode", alternate = ["order_code"])
    val orderCode: String? = null,
    @SerializedName(value = "productId", alternate = ["product_id"])
    val productId: Long = 0L,
    @SerializedName(value = "productName", alternate = ["product_name", "name"])
    val productName: String = "",
    @SerializedName(value = "productImageUrl", alternate = ["product_image_url", "imageUrl", "image_url"])
    val productImageUrl: String? = null,
    @SerializedName(value = "reviewed", alternate = ["isReviewed", "hasReviewed", "alreadyReviewed"])
    val reviewed: Boolean = false,
    @SerializedName(value = "review", alternate = ["existingReview", "myReview"])
    val review: ReviewResponse? = null
)
