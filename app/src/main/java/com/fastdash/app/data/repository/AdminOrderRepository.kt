package com.fastdash.app.data.repository

import android.util.Log
import android.content.Context
import com.fastdash.app.data.remote.retrofit.RetrofitClient

class AdminOrderRepository(context: Context) {
    companion object {
        private const val TAG = "AdminOrders"
    }
    private val api = RetrofitClient.adminOrderApi(context)

    suspend fun getOrders(
        status: String? = null,
        keyword: String? = null,
        fromDate: String? = null,
        toDate: String? = null,
        page: Int = 0,
        size: Int = 10,
        sort: String = "createdAt,desc"
    ) = try {
        val response = api.getAdminOrders(
            page = page,
            size = size,
            status = status,
            keyword = keyword,
            fromDate = fromDate,
            toDate = toDate,
            sort = sort
        )
        Log.d(
            TAG,
            "repository.getOrders status=$status keyword=$keyword fromDate=$fromDate toDate=$toDate page=$page size=$size sort=$sort code=${response.code()}"
        )
        response
    } catch (e: Exception) {
        Log.e(
            TAG,
            "repository.getOrders exception status=$status keyword=$keyword fromDate=$fromDate toDate=$toDate page=$page size=$size sort=$sort",
            e
        )
        throw e
    }

    suspend fun getOrderDetail(id: Long) = api.getAdminOrderDetail(id)

    suspend fun updateOrderStatus(id: Long, status: String) =
        api.updateAdminOrderStatus(id, com.fastdash.app.data.remote.api.UpdateOrderStatusRequest(status))
}

