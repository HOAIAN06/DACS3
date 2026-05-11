package com.fastdash.app.data.repository

import android.content.Context
import com.fastdash.app.data.model.response.CategoryResponse
import com.fastdash.app.data.remote.retrofit.RetrofitClient
import retrofit2.Response

class CategoryRepository(private val context: Context) {

    suspend fun getCategories(): Response<List<CategoryResponse>> {
        return RetrofitClient.categoryApi(context).getCategories()
    }
}