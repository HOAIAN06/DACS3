package com.fastdash.app.data.model.response

data class BranchResponse(
    val id: Long,
    val name: String,
    val address: String,
    val phone: String,
    val openTime: String? = null,
    val closeTime: String? = null,
    val status: Int = 1,
    val createdAt: String? = null,
    val updatedAt: String? = null
)
