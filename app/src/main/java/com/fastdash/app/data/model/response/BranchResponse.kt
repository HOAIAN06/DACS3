package com.fastdash.app.data.model.response

import com.google.gson.annotations.SerializedName

data class BranchResponse(
    @SerializedName("id")
    val id: Long,
    @SerializedName("name")
    val name: String,
    @SerializedName("address")
    val address: String,
    @SerializedName("phone")
    val phone: String,
    @SerializedName("latitude")
    val latitude: Double? = null,
    @SerializedName("longitude")
    val longitude: Double? = null,
    @SerializedName(value = "openTime", alternate = ["open_time", "openingTime"])
    val openTime: String? = null,
    @SerializedName(value = "closeTime", alternate = ["close_time", "closingTime"])
    val closeTime: String? = null,
    @SerializedName("status")
    val status: Int = 1,
    @SerializedName("createdAt")
    val createdAt: String? = null,
    @SerializedName("updatedAt")
    val updatedAt: String? = null
)
