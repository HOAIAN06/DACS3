package com.fastdash.app.ui.checkout

data class PickedLocation(
    val latitude: Double,
    val longitude: Double,
    val address: String,
    val detailAddress: String = ""
)
