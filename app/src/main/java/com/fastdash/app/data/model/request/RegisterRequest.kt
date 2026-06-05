package com.fastdash.app.data.model.request

import com.google.gson.annotations.SerializedName

data class RegisterRequest(
	@SerializedName("fullName")
	val fullName: String,
	val email: String,
	val phone: String,
	val password: String
)
