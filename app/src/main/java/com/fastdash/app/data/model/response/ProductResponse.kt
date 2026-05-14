package com.fastdash.app.data.model.response

import com.google.gson.annotations.SerializedName

data class ProductResponse(
    val id: Long,
    val name: String,
    val description: String?,
    @SerializedName(value = "basePrice", alternate = ["base_price"]) val basePrice: Double,
    @SerializedName(value = "imageUrl", alternate = ["image_url", "url", "secure_url"]) val imageUrl: String?,
    val isCustomizable: Int,
    val categoryId: Long,
    val categoryName: String
)