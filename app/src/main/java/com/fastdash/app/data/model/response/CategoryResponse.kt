package com.fastdash.app.data.model.response

import com.google.gson.annotations.SerializedName

data class CategoryResponse(
    val id: Long,
    val name: String,
    val description: String?,
    @SerializedName(value = "imageUrl", alternate = ["image_url", "url", "secure_url"])
    val imageUrl: String? = null
)
