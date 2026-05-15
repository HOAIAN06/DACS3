package com.fastdash.app.data.model.request

data class UpdateProfileRequest(
    val fullName: String,
    val phone: String,
    val address: String? = null
)
