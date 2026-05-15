package com.fastdash.app.data.repository

import android.content.Context
import com.fastdash.app.data.model.response.AdminDashboardSummaryResponse
import com.fastdash.app.data.model.response.AdminOrderSummaryResponse
import com.fastdash.app.data.model.response.AdminPageResponse
import com.fastdash.app.data.remote.api.AdminUserApi
import com.fastdash.app.data.remote.api.AdminBranchApi
import com.fastdash.app.data.remote.api.AdminToppingApi
import com.fastdash.app.data.remote.api.AdminSizeApi
import com.fastdash.app.data.remote.api.AdminPaymentApi
import com.fastdash.app.data.remote.api.AdminOrderStatusApi
import com.fastdash.app.data.remote.api.AdminCategoryApi
import com.fastdash.app.data.remote.api.AdminProductApiExtended
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
     suspend fun updateBranchStatus(id: Long, status: Int) =
         api.updateBranchStatus(id, status)
}

class AdminToppingRepository(context: Context) {
    private val api = RetrofitClient.adminToppingApi(context)

    suspend fun getToppings() = api.getToppings()
    suspend fun createTopping(request: com.fastdash.app.data.remote.api.CreateToppingRequest) =
        api.createTopping(request)

    suspend fun updateTopping(id: Long, request: com.fastdash.app.data.remote.api.CreateToppingRequest) =
        api.updateTopping(id, request)

    suspend fun deleteTopping(id: Long) = api.deleteTopping(id)
     suspend fun updateToppingStatus(id: Long, status: Int) =
         api.updateToppingStatus(id, status)
}

class AdminSizeRepository(context: Context) {
    private val api = RetrofitClient.adminSizeApi(context)

    suspend fun getSizesByProduct(productId: Long) = api.getSizesByProduct(productId)
    suspend fun createSize(productId: Long, request: com.fastdash.app.data.remote.api.CreateSizeRequest) =
        api.createSize(productId, request)

    suspend fun updateSize(sizeId: Long, request: com.fastdash.app.data.remote.api.CreateSizeRequest) =
        api.updateSize(sizeId, request)

    suspend fun deleteSize(sizeId: Long) = api.deleteSize(sizeId)
     suspend fun updateSizeStatus(sizeId: Long, status: Int) =
         api.updateSizeStatus(sizeId, status)
}

class AdminPaymentRepository(context: Context) {
    private val api = RetrofitClient.adminPaymentApi(context)

    suspend fun getPayments() = api.getPayments()
    suspend fun getPaymentDetail(id: Long) = api.getPaymentDetail(id)
    suspend fun updatePaymentStatus(id: Long, status: String) = api.updatePaymentStatus(id, status)
}

class AdminOrderStatusRepository(context: Context) {
     private val api = RetrofitClient.adminOrderStatusApi(context.applicationContext)

     suspend fun updateOrderStatus(id: Long, status: String) =
         api.updateOrderStatus(id, com.fastdash.app.data.remote.api.UpdateOrderStatusRequest(status))
}

class AdminOrderRepository(context: Context) {
    private val api = RetrofitClient.adminOrderApi(context.applicationContext)

    suspend fun getOrders(
        page: Int? = 0,
        size: Int? = 20,
        status: String? = null,
        keyword: String? = null,
        fromDate: String? = null,
        toDate: String? = null
    ) = api.getOrders(page = page, size = size, status = status, keyword = keyword, fromDate = fromDate, toDate = toDate)

    suspend fun getOrderDetail(id: Long) = api.getOrderDetail(id)
}

class AdminDashboardRepository(context: Context) {
    private val api = RetrofitClient.adminDashboardApi(context.applicationContext)

    suspend fun getSummary() = api.getSummary()
}

class BranchRepository(context: Context) {
    private val api = RetrofitClient.branchApi(context.applicationContext)

    suspend fun getBranches() = api.getBranches()
    suspend fun getBranchDetail(id: Long) = api.getBranchDetail(id)
}

class AdminCategoryRepository(context: Context) {
     private val api = RetrofitClient.adminCategoryApi(context.applicationContext)

     suspend fun getCategories() = api.getCategories()
     suspend fun createCategory(request: com.fastdash.app.data.remote.api.CreateCategoryRequest) =
         api.createCategory(request)

     suspend fun updateCategory(id: Long, request: com.fastdash.app.data.remote.api.CreateCategoryRequest) =
         api.updateCategory(id, request)

     suspend fun deleteCategory(id: Long) = api.deleteCategory(id)
      suspend fun updateCategoryStatus(id: Long, status: Int) =
          api.updateCategoryStatus(id, status)
}

class AdminProductRepositoryExtended(context: Context) {
     private val api = RetrofitClient.adminProductApiExtended(context.applicationContext)

     suspend fun getProducts() = api.getProducts()
     suspend fun getProductDetail(id: Long) = api.getProductDetail(id)
     suspend fun updateProduct(id: Long, request: com.fastdash.app.data.remote.api.UpdateProductRequest) =
         api.updateProduct(id, request)

     suspend fun deleteProduct(id: Long) = api.deleteProduct(id)
      suspend fun updateProductStatus(id: Long, status: Int) =
          api.updateProductStatus(id, status)

     suspend fun addToppingToProduct(productId: Long, toppingId: Long) =
         api.addToppingToProduct(productId, toppingId)

     suspend fun removeToppingFromProduct(productId: Long, toppingId: Long) =
         api.removeToppingFromProduct(productId, toppingId)
}


