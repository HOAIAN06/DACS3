package com.fastdash.app.data.repository

import android.content.Context
import com.fastdash.app.data.remote.retrofit.RetrofitClient

class AdminDashboardRepository(context: Context) {
    private val api = RetrofitClient.adminDashboardApi(context)

    suspend fun getSummary() = api.getAdminDashboardSummary()

    suspend fun getRevenueReport(
        period: String? = null,
        from: String? = null,
        to: String? = null
    ) = api.getRevenueReport(period = period, from = from, to = to)
}

