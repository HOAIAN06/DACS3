package com.fastdash.app.ui.checkout

data class DeliveryLocation(
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val detailAddress: String
)
