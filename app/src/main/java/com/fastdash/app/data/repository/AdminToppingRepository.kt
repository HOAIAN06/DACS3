package com.fastdash.app.data.repository

import android.content.Context
import com.fastdash.app.data.remote.retrofit.RetrofitClient
import okhttp3.MultipartBody
import okhttp3.RequestBody

class AdminToppingRepository(context: Context) {
    private val api = RetrofitClient.adminToppingApi(context.applicationContext)

    suspend fun getToppings() = api.getToppings()

    suspend fun createTopping(
        name: RequestBody,
        price: RequestBody,
        status: RequestBody,
        image: MultipartBody.Part
    ) = api.createTopping(name, price, status, image)

    suspend fun updateTopping(
        id: Long,
        name: RequestBody,
        price: RequestBody,
        status: RequestBody,
        image: MultipartBody.Part?
    ) = api.updateTopping(id, name, price, status, image)

    suspend fun deleteTopping(id: Long) = api.deleteTopping(id)

    suspend fun updateToppingStatus(id: Long, status: Int) = api.updateToppingStatus(id, status)
}

