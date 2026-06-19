package com.fastdash.app.data.model.request

data class VerifyResetCodeRequest(
    val email: String,
    val code: String
)
