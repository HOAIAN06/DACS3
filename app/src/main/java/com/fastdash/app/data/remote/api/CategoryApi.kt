package com.fastdash.app.data.remote.api

import com.fastdash.app.data.model.response.CategoryResponse
import retrofit2.Response
import retrofit2.http.GET

interface CategoryApi {

    @GET("api/v1/categories")
    suspend fun getCategories(): Response<List<CategoryResponse>>
}