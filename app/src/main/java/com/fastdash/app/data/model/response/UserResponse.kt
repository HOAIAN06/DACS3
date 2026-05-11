package com.fastdash.app.data.model.response

data class UserResponse(
    val id: Long,
    val fullName: String,
    val email: String,
    val phone: String,
    val role: String,
    val address: String? = null
)
