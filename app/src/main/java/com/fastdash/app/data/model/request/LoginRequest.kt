package com.fastdash.app.data.model.request

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    @SerializedName("email")
    val email: String,
    @SerializedName("password")
    val password: String
)

data class GoogleLoginRequest(
    @SerializedName("idToken")
    val idToken: String
)
