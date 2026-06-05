package com.fastdash.app.data.repository

import android.content.Context
import com.fastdash.app.data.remote.api.CreateSizeRequest
import com.fastdash.app.data.remote.retrofit.RetrofitClient

class AdminSizeRepository(context: Context) {
    private val api = RetrofitClient.adminSizeApi(context.applicationContext)

    suspend fun getSizesByProduct(productId: Long) = api.getSizesByProduct(productId)

    suspend fun createSize(productId: Long, request: CreateSizeRequest) =
        api.createSize(productId, request)

    suspend fun updateSize(sizeId: Long, request: CreateSizeRequest) =
        api.updateSize(sizeId, request)

    suspend fun deleteSize(sizeId: Long) = api.deleteSize(sizeId)

    suspend fun updateSizeStatus(sizeId: Long, status: Int) = api.updateSizeStatus(sizeId, status)
}

