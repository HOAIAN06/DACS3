package com.example.fastdash_android.data.model.response

data class LoginResponse(
    val token: String,
    val id: Long,
    val fullName: String,
    val email: String,
    val phone: String,
    val role: String
)