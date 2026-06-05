package com.fastdash.app.data.model.request

data class UpdateProfileRequest(
    val fullName: String,
    val email: String? = null,
    val phone: String,
    val address: String? = null
)
