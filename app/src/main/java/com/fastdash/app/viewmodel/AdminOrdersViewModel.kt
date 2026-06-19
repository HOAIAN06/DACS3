package com.fastdash.app.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fastdash.app.data.model.response.AdminOrderDetailResponse
import com.fastdash.app.data.model.response.AdminOrderSummaryResponse
import com.fastdash.app.data.repository.AdminDashboardRepository
import com.fastdash.app.data.repository.AdminOrderRepository
import com.fastdash.app.ui.admin.OrderGroup
import com.fastdash.app.ui.admin.allowedNextActions
import com.fastdash.app.ui.admin.friendlyErrorMessage
import com.fastdash.app.ui.admin.groupForStatus
import com.fastdash.app.ui.admin.mapAdminOrdersError
import com.fastdash.app.ui.admin.orderSummarySortComparator
import com.fastdash.app.ui.admin.statusesForGroup
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AdminOrdersUiState(
    val orders: List<AdminOrderSummaryResponse> = emptyList(),
    val keyword: String = "",
    val selectedStatus: String? = null,
    val selectedGroup: OrderGroup = OrderGroup.ALL,
    val fromDate: String? = null,
    val toDate: String? = null,
    val sort: String = "createdAt,desc",
    val page: Int = 0,
    val totalPages: Int = 0,
    val totalElements: Long = 0,
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val errorMessage: String? = null,
    val errorCode: Int? = null,
    val isFilterSheetOpen: Boolean = false,
    val successMessage: String? = null,
    val pendingConfirmationCount: Long? = null,
    val pendingPaymentCount: Long? = null,
    val preparingCount: Long? = null,
    val deliveringCount: Long? = null,
    val hasMore: Boolean = true
)

data class AdminOrderDetailUiState(
    val order: AdminOrderDetailResponse? = null,
    val isLoading: Boolean = false,
    val isUpdatingStatus: Boolean = false,
    val errorMessage: String? = null,
    val errorCode: Int? = null,
    val successMessage: String? = null
)

class AdminOrdersViewModel(
    private val repository: AdminOrderRepository,
    private val dashboardRepository: AdminDashboardRepository
) : ViewModel() {
    private companion object {
        private const val TAG = "AdminOrders"
        private const val PAGE_SIZE = 50
        private const val DEFAULT_SORT = "createdAt,desc"
    }

    private val _uiState = MutableStateFlow(AdminOrdersUiState())
    val uiState: StateFlow<AdminOrdersUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null
    private var loadJob: Job? = null
    private var allOrdersCache: List<AdminOrderSummaryResponse> = emptyList()

    init {
        refresh()
    }

    fun applyInitialStatus(initialStatus: String?) {
        val normalized = initialStatus?.trim()?.uppercase()?.ifBlank { null }
        val current = _uiState.value
        val targetGroup = if (normalized != null) groupForStatus(normalized) else OrderGroup.ALL
        Log.d(
            TAG,
            "screen opened initialStatus=$initialStatus normalized=$normalized selectedGroup=${current.selectedGroup} selectedStatus=${current.selectedStatus} keyword=${current.keyword} fromDate=${current.fromDate} toDate=${current.toDate} sort=${current.sort} page=${current.page} size=$PAGE_SIZE"
        )
        if (
            current.selectedStatus == normalized &&
            current.selectedGroup == targetGroup &&
            current.keyword.isBlank() &&
            current.fromDate == null &&
            current.toDate == null &&
            current.orders.isNotEmpty()
        ) {
            return
        }
        _uiState.value = current.copy(
            keyword = "",
            selectedStatus = normalized,
            selectedGroup = targetGroup,
            fromDate = null,
            toDate = null,
            sort = DEFAULT_SORT,
            page = 0,
            totalPages = 0,
            totalElements = 0,
            orders = emptyList(),
            isLoading = false,
            isLoadingMore = false,
            errorMessage = null,
            errorCode = null,
            successMessage = null,
            hasMore = true
        )
        loadOrders(reset = true)
    }

    fun refresh() {
        loadSummaryCounts()
        loadOrders(reset = true)
    }

    fun loadOrders(reset: Boolean = false) {
        val current = _uiState.value
        if (!reset && (current.isLoading || current.isLoadingMore)) return
        if (reset) {
            loadJob?.cancel()
        }
        loadJob = viewModelScope.launch {
            val updatedState = _uiState.value.copy(
                isLoading = reset,
                isLoadingMore = !reset,
                errorMessage = null,
                errorCode = null,
                successMessage = _uiState.value.successMessage,
                page = if (reset) 0 else _uiState.value.page,
                orders = if (reset) emptyList() else _uiState.value.orders
            )
            _uiState.value = updatedState

            val selectedStatus = _uiState.value.selectedStatus
            val selectedGroup = _uiState.value.selectedGroup
            val keyword = _uiState.value.keyword.trim().ifBlank { null }
            val fromDate = _uiState.value.fromDate
            val toDate = _uiState.value.toDate
            val sort = _uiState.value.sort

            try {
                Log.d(
                    TAG,
                    "loadOrders request status=$selectedStatus group=$selectedGroup keyword=$keyword fromDate=$fromDate toDate=$toDate page=${if (reset) 0 else _uiState.value.page} size=$PAGE_SIZE sort=$sort reset=$reset"
                )
                loadUnfilteredOrders(reset, keyword, fromDate, toDate, sort, selectedStatus, selectedGroup)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isLoadingMore = false,
                    errorMessage = "Khong the tai don hang",
                    errorCode = null
                )
                Log.e(TAG, "loadOrders exception reset=$reset", e)
            }
        }
    }

    private suspend fun loadUnfilteredOrders(
        reset: Boolean,
        keyword: String?,
        fromDate: String?,
        toDate: String?,
        sort: String,
        selectedStatus: String?,
        selectedGroup: OrderGroup
    ) {
        val requestPage = if (reset) 0 else _uiState.value.page
        val response = repository.getOrders(
            keyword = keyword,
            fromDate = fromDate,
            toDate = toDate,
            page = requestPage,
            size = PAGE_SIZE,
            sort = sort
        )
        if (!response.isSuccessful) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                isLoadingMore = false,
                errorCode = response.code(),
                errorMessage = friendlyErrorMessage(response, "Khong the tai don hang")
            )
            Log.e(TAG, "orders response error code=${response.code()} status=$selectedStatus group=$selectedGroup")
            return
        }

        val body = response.body()
        val content = body?.content.orEmpty()
        Log.d(
            TAG,
            "orders response size=${content.size} total=${body?.totalElements} pages=${body?.totalPages} number=${body?.number}"
        )
        allOrdersCache = if (reset) content else (allOrdersCache + content).distinctBy { it.id }
        val page = body?.number ?: requestPage
        val totalPages = body?.totalPages ?: 0
        val totalElements = body?.totalElements ?: allOrdersCache.size.toLong()
        val filteredOrders = filterOrders(
            source = allOrdersCache,
            selectedStatus = selectedStatus,
            selectedGroup = selectedGroup
        )
        _uiState.value = _uiState.value.copy(
            orders = filteredOrders,
            page = page,
            totalPages = totalPages,
            totalElements = totalElements,
            hasMore = page + 1 < totalPages,
            isLoading = false,
            isLoadingMore = false,
            errorMessage = null,
            errorCode = null
        )
        Log.d(
            TAG,
            "uiState updated orders.size=${_uiState.value.orders.size} cache.size=${allOrdersCache.size} isLoading=${_uiState.value.isLoading} isLoadingMore=${_uiState.value.isLoadingMore} error=${_uiState.value.errorMessage} selectedGroup=${_uiState.value.selectedGroup} selectedStatus=${_uiState.value.selectedStatus}"
        )
    }

    fun loadMore() {
        val current = _uiState.value
        if (current.isLoading || current.isLoadingMore || !current.hasMore) return
        _uiState.value = current.copy(page = current.page + 1)
        loadOrders(reset = false)
    }

    fun onKeywordChange(value: String) {
        _uiState.value = _uiState.value.copy(keyword = value, page = 0)
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(400)
            loadOrders(reset = true)
        }
    }

    fun onGroupSelected(group: OrderGroup) {
        if (_uiState.value.selectedGroup == group && _uiState.value.selectedStatus == null) return
        _uiState.value = _uiState.value.copy(
            selectedGroup = group,
            selectedStatus = null,
            errorMessage = null,
            errorCode = null
        )
        Log.d(TAG, "onGroupSelected group=$group selectedStatus cleared")
        applyCachedFilters()
    }

    fun onStatusSelected(status: String?) {
        val normalized = status?.trim()?.uppercase()?.ifBlank { null }
        _uiState.value = _uiState.value.copy(
            selectedStatus = normalized,
            selectedGroup = if (normalized != null) groupForStatus(normalized) else OrderGroup.ALL,
            errorMessage = null,
            errorCode = null
        )
        Log.d(TAG, "onStatusSelected status=$normalized group=${_uiState.value.selectedGroup}")
        applyCachedFilters()
    }

    fun applyFilters(
        status: String?,
        fromDate: String?,
        toDate: String?,
        sort: String
    ) {
        val normalizedStatus = status?.trim()?.uppercase()?.ifBlank { null }
        _uiState.value = _uiState.value.copy(
            selectedStatus = normalizedStatus,
            selectedGroup = if (normalizedStatus != null) groupForStatus(normalizedStatus) else _uiState.value.selectedGroup,
            fromDate = fromDate,
            toDate = toDate,
            sort = sort,
            page = 0,
            totalPages = 0,
            totalElements = 0,
            orders = emptyList(),
            isLoading = false,
            isLoadingMore = false,
            hasMore = true,
            isFilterSheetOpen = false,
            errorMessage = null,
            errorCode = null
        )
        Log.d(TAG, "applyFilters status=$normalizedStatus fromDate=$fromDate toDate=$toDate sort=$sort")
        loadOrders(reset = true)
    }

    fun clearFilters(clearKeyword: Boolean = false) {
        _uiState.value = _uiState.value.copy(
            selectedStatus = null,
            selectedGroup = OrderGroup.ALL,
            fromDate = null,
            toDate = null,
            sort = DEFAULT_SORT,
            keyword = if (clearKeyword) "" else _uiState.value.keyword,
            page = 0,
            totalPages = 0,
            totalElements = 0,
            orders = emptyList(),
            isLoading = false,
            isLoadingMore = false,
            hasMore = true,
            isFilterSheetOpen = false,
            errorMessage = null,
            errorCode = null
        )
        Log.d(TAG, "clearFilters clearKeyword=$clearKeyword keyword=${_uiState.value.keyword}")
        loadOrders(reset = true)
    }

    fun clearSearch() {
        if (_uiState.value.keyword.isBlank()) return
        _uiState.value = _uiState.value.copy(
            keyword = "",
            page = 0,
            orders = emptyList(),
            isLoading = false,
            isLoadingMore = false
        )
        Log.d(TAG, "clearSearch")
        loadOrders(reset = true)
    }

    fun openFilterSheet() {
        _uiState.value = _uiState.value.copy(isFilterSheetOpen = true)
    }

    fun closeFilterSheet() {
        _uiState.value = _uiState.value.copy(isFilterSheetOpen = false)
    }

    fun updateOrderStatus(orderId: Long, nextStatus: String) {
        val order = _uiState.value.orders.firstOrNull { it.id == orderId } ?: run {
            _uiState.value = _uiState.value.copy(errorMessage = "Khong tim thay don hang")
            return
        }
        val currentStatus = (order.orderStatus ?: order.status).trim().uppercase()
        val normalizedNextStatus = nextStatus.trim().uppercase()
        val allowed = allowedNextActions(currentStatus).any { it.targetStatus == normalizedNextStatus }
        if (!allowed) {
            _uiState.value = _uiState.value.copy(errorMessage = "Khong the cap nhat trang thai nay")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(errorMessage = null, errorCode = null, successMessage = null)
            try {
                val response = repository.updateOrderStatus(orderId, normalizedNextStatus)
                if (!response.isSuccessful) {
                    _uiState.value = _uiState.value.copy(
                        errorCode = response.code(),
                        errorMessage = when (response.code()) {
                            400 -> "Khong the cap nhat trang thai nay"
                            else -> friendlyErrorMessage(response, "Khong the cap nhat trang thai")
                        }
                    )
                    return@launch
                }
                _uiState.value = _uiState.value.copy(successMessage = "Cap nhat trang thai thanh cong")
                loadSummaryCounts()
                loadOrders(reset = true)
            } catch (_: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = "Khong the cap nhat trang thai")
            }
        }
    }

    private fun loadSummaryCounts() {
        viewModelScope.launch {
            try {
                val response = dashboardRepository.getSummary()
                if (!response.isSuccessful) return@launch
                val summary = response.body() ?: return@launch
                _uiState.value = _uiState.value.copy(
                    pendingConfirmationCount = summary.pendingOrders,
                    pendingPaymentCount = summary.pendingPaymentOrders,
                    preparingCount = summary.preparingOrders,
                    deliveringCount = summary.deliveringOrders
                )
            } catch (_: Exception) {
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null, errorCode = null)
    }

    fun clearSuccess() {
        _uiState.value = _uiState.value.copy(successMessage = null)
    }

    private fun applyCachedFilters() {
        if (allOrdersCache.isEmpty()) {
            loadOrders(reset = true)
            return
        }
        _uiState.value = _uiState.value.copy(
            orders = filterOrders(
                source = allOrdersCache,
                selectedStatus = _uiState.value.selectedStatus,
                selectedGroup = _uiState.value.selectedGroup
            ),
            errorMessage = null,
            errorCode = null
        )
        Log.d(
            TAG,
            "applyCachedFilters result orders.size=${_uiState.value.orders.size} cache.size=${allOrdersCache.size} selectedGroup=${_uiState.value.selectedGroup} selectedStatus=${_uiState.value.selectedStatus}"
        )
    }

    private fun filterOrders(
        source: List<AdminOrderSummaryResponse>,
        selectedStatus: String?,
        selectedGroup: OrderGroup
    ): List<AdminOrderSummaryResponse> {
        return source.filter { order ->
            val status = (order.orderStatus ?: order.status).trim().uppercase()
            when {
                selectedStatus != null -> status == selectedStatus
                selectedGroup == OrderGroup.ALL -> true
                else -> status in statusesForGroup(selectedGroup)
            }
        }
    }
}

class AdminOrderDetailViewModel(
    private val repository: AdminOrderRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(AdminOrderDetailUiState())
    val uiState: StateFlow<AdminOrderDetailUiState> = _uiState.asStateFlow()

    fun loadOrder(id: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null, errorCode = null)
            try {
                val response = repository.getOrderDetail(id)
                if (!response.isSuccessful) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorCode = response.code(),
                        errorMessage = friendlyErrorMessage(response, "Khong the tai chi tiet don hang")
                    )
                    return@launch
                }
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    order = response.body(),
                    errorMessage = null,
                    errorCode = null
                )
            } catch (_: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Co loi xay ra, vui long thu lai"
                )
            }
        }
    }

    fun updateStatus(id: Long, status: String, onSuccess: (() -> Unit)? = null) {
        val normalizedStatus = status.trim().uppercase()
        if (
            normalizedStatus == "PENDING_CONFIRMATION" ||
            normalizedStatus == "PENDING_PAYMENT" ||
            normalizedStatus == "PAYMENT_FAILED"
        ) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Khong the cap nhat trang thai nay",
                successMessage = null
            )
            return
        }

        _uiState.value = _uiState.value.copy(
            isUpdatingStatus = true,
            errorMessage = null,
            errorCode = null,
            successMessage = null
        )
        viewModelScope.launch {
            try {
                val response = repository.updateOrderStatus(id, normalizedStatus)
                if (!response.isSuccessful) {
                    _uiState.value = _uiState.value.copy(
                        isUpdatingStatus = false,
                        errorCode = response.code(),
                        errorMessage = when (response.code()) {
                            400 -> "Khong the cap nhat trang thai nay"
                            else -> friendlyErrorMessage(response, "Khong the cap nhat trang thai")
                        }
                    )
                    return@launch
                }
                _uiState.value = _uiState.value.copy(
                    isUpdatingStatus = false,
                    successMessage = "Cap nhat trang thai thanh cong"
                )
                onSuccess?.invoke()
                loadOrder(id)
            } catch (_: Exception) {
                _uiState.value = _uiState.value.copy(
                    isUpdatingStatus = false,
                    errorMessage = mapAdminOrdersError(null, "Khong the cap nhat trang thai")
                )
            }
        }
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(errorMessage = null, errorCode = null, successMessage = null)
    }
}
