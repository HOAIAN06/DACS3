package com.fastdash.app.data.repository

import android.content.Context
import com.fastdash.app.data.remote.retrofit.RetrofitClient

class BranchRepository(context: Context) {
    private val api = RetrofitClient.branchApi(context)

    suspend fun getBranches() = api.getBranches()

    suspend fun getBranchDetail(id: Long) = api.getBranchDetail(id)
}
