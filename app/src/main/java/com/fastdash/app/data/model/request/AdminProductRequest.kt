package com.fastdash.app.data.model.request

import com.google.gson.annotations.SerializedName

data class AdminProductRequest(
    @SerializedName("categoryId")
    val categoryId: Long,
    val name: String,
    val description: String,
    @SerializedName("basePrice")
    val basePrice: Double,
    @SerializedName("imageUrl")
    val imageUrl: String,
    @SerializedName("isCustomizable")
    val isCustomizable: Int = 1,
    val status: Int = 1
)