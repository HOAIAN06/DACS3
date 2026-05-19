package com.fastdash.app.data.model.response

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @SerializedName("token")
    val token: String = "",
    @SerializedName("id")
    val id: Long = 0L,
    @SerializedName(value = "fullName", alternate = ["full_name"])
    val fullName: String = "",
    @SerializedName("email")
    val email: String = "",
    @SerializedName("phone")
    val phone: String = "",
    @SerializedName("role")
    val role: String = ""
)
