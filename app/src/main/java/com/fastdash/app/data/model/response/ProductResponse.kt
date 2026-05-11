package com.fastdash.app.data.model.response

data class ProductResponse(
    val id: Long,
    val name: String,
    val description: String?,
    val basePrice: Double,
    val imageUrl: String?,
    val isCustomizable: Int,
    val categoryId: Long,
    val categoryName: String
)