package com.fastdash.app.data.model.response

import com.google.gson.annotations.SerializedName

data class AiChatResponse(
    val reply: String,
    val intent: String,
    @SerializedName(value = "suggestedProducts", alternate = ["suggested_products"])
    val suggestedProducts: List<AiSuggestedProduct> = emptyList()
)

data class AiSuggestedProduct(
    val id: Long,
    val name: String,
    val description: String? = null,
    @SerializedName(value = "basePrice", alternate = ["base_price"])
    val basePrice: Double = 0.0,
    @SerializedName(value = "imageUrl", alternate = ["image_url", "url", "secure_url"])
    val imageUrl: String? = null,
    @SerializedName(value = "categoryName", alternate = ["category_name"])
    val categoryName: String? = null
)
