package com.fastdash.app.data.model.response

data class LoginResponse(
    val token: String,
    val id: Long,
    val fullName: String,
    val email: String,
    val phone: String,
    val role: String
)