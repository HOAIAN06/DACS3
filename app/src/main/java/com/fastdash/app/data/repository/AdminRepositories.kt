package com.fastdash.app.data.repository

import android.content.Context
import com.fastdash.app.data.remote.retrofit.RetrofitClient

class AdminUserRepository(context: Context) {
    private val api = RetrofitClient.adminUserApi(context)

    suspend fun getUsers() = api.getUsers()
    suspend fun getUserDetail(id: Long) = api.getUserDetail(id)
    suspend fun updateUserStatus(id: Long, status: Int) =
        api.updateUserStatus(id, com.fastdash.app.data.remote.api.UpdateUserStatusRequest(status))

    suspend fun updateUserRole(id: Long, role: String) =
        api.updateUserRole(id, com.fastdash.app.data.remote.api.UpdateUserRoleRequest(role))
}

class AdminBranchRepository(context: Context) {
    private val api = RetrofitClient.adminBranchApi(context)

    suspend fun getBranches() = api.getBranches()
    suspend fun createBranch(request: com.fastdash.app.data.remote.api.CreateBranchRequest) =
        api.createBranch(request)

    suspend fun updateBranch(id: Long, request: com.fastdash.app.data.remote.api.CreateBranchRequest) =
        api.updateBranch(id, request)

    suspend fun deleteBranch(id: Long) = api.deleteBranch(id)
    suspend fun updateBranchStatus(id: Long, status: Int) = api.updateBranchStatus(id, status)
}

class AdminPaymentRepository(context: Context) {
    private val api = RetrofitClient.adminPaymentApi(context)

    suspend fun getPayments() = api.getPayments()
    suspend fun getPaymentDetail(id: Long) = api.getPaymentDetail(id)
    suspend fun updatePaymentStatus(id: Long, status: String) =
        api.updatePaymentStatus(id, status)
}

class AdminOrderStatusRepository(context: Context) {
    private val api = RetrofitClient.adminOrderStatusApi(context.applicationContext)

    suspend fun updateOrderStatus(id: Long, status: String) =
        api.updateOrderStatus(id, com.fastdash.app.data.remote.api.UpdateOrderStatusRequest(status))
}

class AdminProductRepositoryExtended(context: Context) {
    private val api = RetrofitClient.adminProductApiExtended(context.applicationContext)

    suspend fun getProducts() = api.getProducts()
    suspend fun getProductDetail(id: Long) = api.getProductDetail(id)
    suspend fun updateProduct(id: Long, request: com.fastdash.app.data.remote.api.UpdateProductRequest) =
        api.updateProduct(id, request)

    suspend fun deleteProduct(id: Long) = api.deleteProduct(id)
    suspend fun updateProductStatus(id: Long, status: Int) = api.updateProductStatus(id, status)

    suspend fun addToppingToProduct(productId: Long, toppingId: Long) =
        api.addToppingToProduct(productId, toppingId)

    suspend fun removeToppingFromProduct(productId: Long, toppingId: Long) =
        api.removeToppingFromProduct(productId, toppingId)
}
