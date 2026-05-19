package com.fastdash.app.data.model.response

import com.google.gson.annotations.SerializedName

data class UserRoleResponse(
    val id: Long = 0L,
    val name: String? = null
)

data class UserResponse(
    val id: Long = 0L,
    val fullName: String = "",
    val email: String = "",
    val phone: String = "",
    @SerializedName(value = "role")
    private val roleObject: UserRoleResponse? = null,
    @SerializedName(value = "roleName", alternate = ["role_name"])
    private val roleNameRaw: String? = null,
    val address: String? = null
) {
    val role: String
        get() = roleNameRaw ?: roleObject?.name.orEmpty()
}
