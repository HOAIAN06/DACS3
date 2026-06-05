package com.fastdash.app.data.repository

import android.content.Context
import com.fastdash.app.data.remote.retrofit.RetrofitClient
import okhttp3.MultipartBody
import okhttp3.RequestBody

class AdminCategoryRepository(context: Context) {
    private val api = RetrofitClient.adminCategoryApi(context.applicationContext)

    suspend fun getCategories() = api.getCategories()

    suspend fun createCategory(
        name: RequestBody,
        description: RequestBody,
        status: RequestBody,
        image: MultipartBody.Part
    ) = api.createCategory(name, description, status, image)

    suspend fun updateCategory(
        id: Long,
        name: RequestBody,
        description: RequestBody,
        status: RequestBody,
        image: MultipartBody.Part?
    ) = api.updateCategory(id, name, description, status, image)

    suspend fun deleteCategory(id: Long) = api.deleteCategory(id)

    suspend fun updateCategoryStatus(id: Long, status: Int) = api.updateCategoryStatus(id, status)
}

