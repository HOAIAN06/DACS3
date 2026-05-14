package com.fastdash.app.data.model.response

import com.google.gson.annotations.SerializedName

data class UploadImageResponse(
    @SerializedName(value = "imageUrl", alternate = ["image_url", "url", "secure_url"])
    val imageUrl: String?
) {
    val resolvedImageUrl: String?
        get() = imageUrl
}
