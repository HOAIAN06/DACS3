package com.fastdash.app.data.model.request

data class AdminProductRequest(
    val categoryId: Long,
    val name: String,
    val description: String,
    val basePrice: Double,
    val imageUrl: String,
    val isCustomizable: Int = 1,
    val status: Int = 1
)