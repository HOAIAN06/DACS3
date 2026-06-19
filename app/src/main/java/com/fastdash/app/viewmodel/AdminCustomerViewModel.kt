package com.fastdash.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fastdash.app.data.model.request.UpdateProfileRequest
import com.fastdash.app.data.model.response.AdminCustomerOrderResponse
import com.fastdash.app.data.model.response.AdminCustomerResponse
import com.fastdash.app.data.repository.AdminCustomerRepository
import com.fastdash.app.ui.admin.friendlyErrorMessage
import com.fastdash.app.ui.admin.mapAdminOrdersError
import com.fastdash.app.ui.admin.parseApiErrorMessage
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class CustomerSegmentFilter {
    ALL,
    ACTIVE_BUYER,
    NEW,
    BLOCKED
}

data class AdminCustomersUiState(
    val customers: List<AdminCustomerResponse> = emptyList(),
    val loading: Boolean = false,
    val errorMessage: String? = null,
    val errorCode: Int? = null,
    val keyword: String = "",
    val selectedStatus: Int? = null,
    val selectedSegment: CustomerSegmentFilter = CustomerSegmentFilter.ALL,
    val page: Int = 0,
    val size: Int = 10,
    val sortBy: String = "createdAt",
    val sortDir: String = "desc",
    val totalElements: Long = 0,
    val totalPages: Int = 0,
    val detailOpen: Boolean = false,
    val selectedCustomerId: Long? = null,
    val selectedCustomer: AdminCustomerResponse? = null,
    val detailLoading: Boolean = false,
    val detailErrorMessage: String? = null,
    val ordersOpen: Boolean = false,
    val ordersCustomer: AdminCustomerResponse? = null,
    val customerOrders: List<AdminCustomerOrderResponse> = emptyList(),
    val ordersLoading: Boolean = false,
    val ordersErrorMessage: String? = null,
    val ordersPage: Int = 0,
    val ordersSize: Int = 10,
    val ordersTotalPages: Int = 0,
    val ordersTotalElements: Long = 0,
    val confirmOpen: Boolean = false,
    val statusTargetCustomer: AdminCustomerResponse? = null,
    val nextStatus: Int? = null,
    val statusUpdating: Boolean = false,
    val editOpen: Boolean = false,
    val editLoading: Boolean = false,
    val editTargetCustomer: AdminCustomerResponse? = null,
    val editFullName: String = "",
    val editEmail: String = "",
    val editPhone: String = "",
    val editAddress: String = "",
    val editErrorMessage: String? = null,
    val successMessage: String? = null
)

class AdminCustomerViewModel(
    private val repository: AdminCustomerRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(AdminCustomersUiState())
    val uiState: StateFlow<AdminCustomersUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    init {
        loadCustomers()
    }

    fun loadCustomers(refreshing: Boolean = false) {
        viewModelScope.launch {
            val current = _uiState.value
            _uiState.value = current.copy(
                loading = true,
                errorMessage = null,
                errorCode = null,
                successMessage = if (refreshing) current.successMessage else null
            )
            try {
                val response = repository.getCustomers(
                    keyword = current.keyword.trim().ifBlank { null },
                    status = current.selectedStatus,
                    page = current.page,
                    size = current.size,
                    sortBy = current.sortBy,
                    sortDir = current.sortDir
                )
                if (!response.isSuccessful) {
                    _uiState.value = _uiState.value.copy(
                        loading = false,
                        errorCode = response.code(),
                        errorMessage = friendlyErrorMessage(response, "Khong the tai danh sach khach hang")
                    )
                    return@launch
                }
                val body = response.body()
                _uiState.value = _uiState.value.copy(
                    loading = false,
                    customers = body?.content.orEmpty(),
                    totalElements = body?.totalElements ?: 0,
                    totalPages = body?.totalPages ?: 0,
                    page = body?.number ?: current.page
                )
            } catch (_: Exception) {
                _uiState.value = _uiState.value.copy(
                    loading = false,
                    errorMessage = "Khong the tai danh sach khach hang"
                )
            }
        }
    }

    fun refresh() = loadCustomers(refreshing = true)

    fun onKeywordChange(value: String) {
        _uiState.value = _uiState.value.copy(keyword = value, page = 0)
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(500)
            loadCustomers()
        }
    }

    fun clearSearch() {
        _uiState.value = _uiState.value.copy(keyword = "", page = 0)
        searchJob?.cancel()
        loadCustomers()
    }

    fun onStatusChange(status: Int?) {
        _uiState.value = _uiState.value.copy(selectedStatus = status, page = 0)
        loadCustomers()
    }

    fun onSegmentChange(segment: CustomerSegmentFilter) {
        _uiState.value = _uiState.value.copy(selectedSegment = segment)
    }

    fun onEditFullNameChange(value: String) {
        _uiState.value = _uiState.value.copy(editFullName = value, editErrorMessage = null)
    }

    fun onEditEmailChange(value: String) {
        _uiState.value = _uiState.value.copy(editEmail = value, editErrorMessage = null)
    }

    fun onEditPhoneChange(value: String) {
        _uiState.value = _uiState.value.copy(editPhone = value, editErrorMessage = null)
    }

    fun onEditAddressChange(value: String) {
        _uiState.value = _uiState.value.copy(editAddress = value, editErrorMessage = null)
    }

    fun onPageChange(page: Int) {
        if (page < 0) return
        _uiState.value = _uiState.value.copy(page = page)
        loadCustomers()
    }

    fun onSizeChange(size: Int) {
        _uiState.value = _uiState.value.copy(size = size, page = 0)
        loadCustomers()
    }

    fun clearFilters() {
        _uiState.value = _uiState.value.copy(
            keyword = "",
            selectedStatus = null,
            selectedSegment = CustomerSegmentFilter.ALL,
            page = 0,
            size = 10,
            sortBy = "createdAt",
            sortDir = "desc"
        )
        loadCustomers()
    }

    fun toggleCreatedAtSort() {
        val nextDir = if (_uiState.value.sortDir == "desc") "asc" else "desc"
        _uiState.value = _uiState.value.copy(sortBy = "createdAt", sortDir = nextDir, page = 0)
        loadCustomers()
    }

    fun openDetail(customerId: Long) {
        _uiState.value = _uiState.value.copy(
            detailOpen = true,
            selectedCustomerId = customerId,
            detailLoading = true,
            detailErrorMessage = null
        )
        viewModelScope.launch {
            try {
                val response = repository.getCustomerDetail(customerId)
                if (!response.isSuccessful) {
                    _uiState.value = _uiState.value.copy(
                        detailLoading = false,
                        detailErrorMessage = when (response.code()) {
                            404 -> "Khong tim thay khach hang"
                            else -> friendlyErrorMessage(response, "Khong the tai chi tiet khach hang")
                        }
                    )
                    return@launch
                }
                _uiState.value = _uiState.value.copy(
                    detailLoading = false,
                    selectedCustomer = response.body(),
                    detailErrorMessage = null
                )
            } catch (_: Exception) {
                _uiState.value = _uiState.value.copy(
                    detailLoading = false,
                    detailErrorMessage = "Khong the tai chi tiet khach hang"
                )
            }
        }
    }

    fun closeDetail() {
        _uiState.value = _uiState.value.copy(
            detailOpen = false,
            selectedCustomerId = null,
            selectedCustomer = null,
            detailLoading = false,
            detailErrorMessage = null
        )
    }

    fun openEditCustomer(customer: AdminCustomerResponse) {
        _uiState.value = _uiState.value.copy(
            editOpen = true,
            editTargetCustomer = customer,
            editFullName = customer.fullName.orEmpty(),
            editEmail = customer.email.orEmpty(),
            editPhone = customer.phone.orEmpty(),
            editAddress = customer.address.orEmpty(),
            editErrorMessage = null,
            editLoading = false
        )
    }

    fun closeEditCustomer() {
        if (_uiState.value.editLoading) return
        _uiState.value = _uiState.value.copy(
            editOpen = false,
            editLoading = false,
            editTargetCustomer = null,
            editFullName = "",
            editEmail = "",
            editPhone = "",
            editAddress = "",
            editErrorMessage = null
        )
    }

    fun openOrders(customer: AdminCustomerResponse) {
        _uiState.value = _uiState.value.copy(
            ordersOpen = true,
            ordersCustomer = customer,
            ordersPage = 0,
            customerOrders = emptyList(),
            ordersTotalPages = 0,
            ordersTotalElements = 0,
            ordersErrorMessage = null
        )
        loadOrders(customer.id, 0, _uiState.value.ordersSize)
    }

    fun closeOrders() {
        _uiState.value = _uiState.value.copy(
            ordersOpen = false,
            ordersCustomer = null,
            customerOrders = emptyList(),
            ordersLoading = false,
            ordersErrorMessage = null,
            ordersPage = 0,
            ordersTotalPages = 0,
            ordersTotalElements = 0
        )
    }

    fun onOrdersPageChange(page: Int) {
        val customerId = _uiState.value.ordersCustomer?.id ?: return
        if (page < 0) return
        loadOrders(customerId, page, _uiState.value.ordersSize)
    }

    private fun loadOrders(customerId: Long, page: Int, size: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                ordersLoading = true,
                ordersErrorMessage = null
            )
            try {
                val response = repository.getCustomerOrders(customerId, page, size)
                if (!response.isSuccessful) {
                    _uiState.value = _uiState.value.copy(
                        ordersLoading = false,
                        ordersErrorMessage = friendlyErrorMessage(response, "Khong the tai lich su don hang")
                    )
                    return@launch
                }
                val body = response.body()
                _uiState.value = _uiState.value.copy(
                    ordersLoading = false,
                    customerOrders = body?.content.orEmpty(),
                    ordersPage = body?.number ?: page,
                    ordersSize = body?.size ?: size,
                    ordersTotalPages = body?.totalPages ?: 0,
                    ordersTotalElements = body?.totalElements ?: 0
                )
            } catch (_: Exception) {
                _uiState.value = _uiState.value.copy(
                    ordersLoading = false,
                    ordersErrorMessage = "Khong the tai lich su don hang"
                )
            }
        }
    }

    fun openStatusConfirm(customer: AdminCustomerResponse, nextStatus: Int) {
        _uiState.value = _uiState.value.copy(
            confirmOpen = true,
            statusTargetCustomer = customer,
            nextStatus = nextStatus
        )
    }

    fun closeStatusConfirm() {
        if (_uiState.value.statusUpdating) return
        _uiState.value = _uiState.value.copy(
            confirmOpen = false,
            statusTargetCustomer = null,
            nextStatus = null
        )
    }

    fun confirmStatusUpdate(onUnauthorized: (() -> Unit)? = null) {
        val customer = _uiState.value.statusTargetCustomer ?: return
        val nextStatus = _uiState.value.nextStatus ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                statusUpdating = true,
                errorMessage = null,
                errorCode = null,
                successMessage = null
            )
            try {
                val response = repository.updateCustomerStatus(customer.id, nextStatus)
                if (!response.isSuccessful) {
                    if (response.code() == 401) onUnauthorized?.invoke()
                    _uiState.value = _uiState.value.copy(
                        statusUpdating = false,
                        errorCode = response.code(),
                        errorMessage = friendlyErrorMessage(response, "Khong the cap nhat trang thai khach hang")
                    )
                    return@launch
                }
                val updated = response.body() ?: customer.copy(status = nextStatus)
                _uiState.value = _uiState.value.copy(
                    statusUpdating = false,
                    confirmOpen = false,
                    statusTargetCustomer = null,
                    nextStatus = null,
                    selectedCustomer = if (_uiState.value.selectedCustomer?.id == updated.id) updated else _uiState.value.selectedCustomer,
                    ordersCustomer = if (_uiState.value.ordersCustomer?.id == updated.id) updated else _uiState.value.ordersCustomer,
                    customers = _uiState.value.customers.map { item -> if (item.id == updated.id) updated else item },
                    successMessage = if (nextStatus == 1) "Mo khoa khach hang thanh cong" else "Khoa khach hang thanh cong"
                )
            } catch (_: Exception) {
                _uiState.value = _uiState.value.copy(
                    statusUpdating = false,
                    errorMessage = mapAdminOrdersError(null, "Khong the cap nhat trang thai khach hang")
                )
            }
        }
    }

    fun saveCustomerEdits(onUnauthorized: (() -> Unit)? = null) {
        val state = _uiState.value
        val customer = state.editTargetCustomer ?: return
        val fullName = state.editFullName.trim()
        val email = state.editEmail.trim()
        val phone = state.editPhone.trim()
        val address = state.editAddress.trim().ifBlank { null }

        if (fullName.isBlank()) {
            _uiState.value = state.copy(editErrorMessage = "Ten khach hang khong duoc de trong")
            return
        }
        if (email.isBlank()) {
            _uiState.value = state.copy(editErrorMessage = "Email khong duoc de trong")
            return
        }
        if (phone.isBlank()) {
            _uiState.value = state.copy(editErrorMessage = "So dien thoai khong duoc de trong")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                editLoading = true,
                editErrorMessage = null,
                errorMessage = null,
                errorCode = null,
                successMessage = null
            )
            try {
                val response = repository.updateCustomer(
                    customer.id,
                    UpdateProfileRequest(
                        fullName = fullName,
                        email = email,
                        phone = phone,
                        address = address
                    )
                )
                if (!response.isSuccessful) {
                    if (response.code() == 401) onUnauthorized?.invoke()
                    val apiMessage = parseApiErrorMessage(runCatching { response.errorBody()?.string() }.getOrNull())
                    _uiState.value = _uiState.value.copy(
                        editLoading = false,
                        errorCode = response.code(),
                        editErrorMessage = mapUpdateCustomerError(response.code(), apiMessage)
                    )
                    return@launch
                }
                val updated = response.body() ?: customer.copy(
                    fullName = fullName,
                    email = email,
                    phone = phone,
                    address = address
                )
                _uiState.value = _uiState.value.copy(
                    editOpen = false,
                    editLoading = false,
                    editTargetCustomer = null,
                    editFullName = "",
                    editEmail = "",
                    editPhone = "",
                    editAddress = "",
                    editErrorMessage = null,
                    selectedCustomer = if (_uiState.value.selectedCustomer?.id == updated.id) updated else _uiState.value.selectedCustomer,
                    ordersCustomer = if (_uiState.value.ordersCustomer?.id == updated.id) updated else _uiState.value.ordersCustomer,
                    statusTargetCustomer = if (_uiState.value.statusTargetCustomer?.id == updated.id) updated else _uiState.value.statusTargetCustomer,
                    customers = _uiState.value.customers.map { item -> if (item.id == updated.id) updated else item },
                    successMessage = "Cap nhat thong tin khach hang thanh cong"
                )
            } catch (_: Exception) {
                _uiState.value = _uiState.value.copy(
                    editLoading = false,
                    editErrorMessage = "Khong the cap nhat thong tin khach hang"
                )
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(errorMessage = null, errorCode = null, successMessage = null)
    }

    private fun mapUpdateCustomerError(code: Int, apiMessage: String?): String {
        return when (code) {
            400 -> when (apiMessage) {
                "Customer fullName is required" -> "Ten khach hang khong duoc de trong"
                "Customer email is required" -> "Email khong duoc de trong"
                "Customer email is invalid" -> "Email khong hop le"
                "Customer phone is required" -> "So dien thoai khong duoc de trong"
                "Customer phone must not exceed 20 characters" -> "So dien thoai khong duoc vuot qua 20 ky tu"
                else -> apiMessage ?: "Du lieu khach hang khong hop le"
            }
            404 -> "Khong tim thay khach hang"
            409 -> when (apiMessage) {
                "Customer email already exists" -> "Email da ton tai"
                "Customer phone already exists" -> "So dien thoai da ton tai"
                else -> apiMessage ?: "Thong tin khach hang bi trung"
            }
            401 -> "Phien dang nhap da het han"
            403 -> "Ban khong co quyen cap nhat khach hang"
            else -> apiMessage ?: "Khong the cap nhat thong tin khach hang"
        }
    }
}
