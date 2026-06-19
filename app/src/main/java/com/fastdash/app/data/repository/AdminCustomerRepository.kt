package com.fastdash.app.data.repository

import android.content.Context
import com.fastdash.app.data.model.request.UpdateProfileRequest
import com.fastdash.app.data.remote.retrofit.RetrofitClient

class AdminCustomerRepository(context: Context) {
    private val api = RetrofitClient.adminCustomerApi(context.applicationContext)

    suspend fun getCustomers(
        keyword: String? = null,
        status: Int? = null,
        page: Int = 0,
        size: Int = 10,
        sortBy: String = "createdAt",
        sortDir: String = "desc"
    ) = api.getCustomers(keyword, status, page, size, sortBy, sortDir)

    suspend fun getCustomerDetail(id: Long) = api.getCustomerDetail(id)

    suspend fun updateCustomer(id: Long, request: UpdateProfileRequest) =
        api.updateCustomer(id, request)

    suspend fun updateCustomerStatus(id: Long, status: Int) = api.updateCustomerStatus(id, status)

    suspend fun getCustomerOrders(id: Long, page: Int = 0, size: Int = 10) =
        api.getCustomerOrders(id, page, size)
}
