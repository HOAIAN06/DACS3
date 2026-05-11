package com.fastdash.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fastdash.app.data.remote.api.AdminSizeResponse
import com.fastdash.app.data.remote.api.CreateSizeRequest
import com.fastdash.app.data.repository.AdminSizeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AdminSizeUiState(
    val sizes: List<AdminSizeResponse> = emptyList(),
    val loading: Boolean = false,
    val message: String? = null,
    val isError: Boolean = false,
    val showAddForm: Boolean = false,
    val editingId: Long? = null,
    val selectedProductId: Long = 0,
    val productIdInput: String = "",
    // Form fields
    val sizeNameInput: String = "",
    val priceInput: String = "",
    val formLoading: Boolean = false,
    val formMessage: String? = null,
    val formError: Boolean = false
)

class AdminSizeViewModel(
    private val repository: AdminSizeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminSizeUiState())
    val uiState: StateFlow<AdminSizeUiState> = _uiState.asStateFlow()

    fun onProductIdChanged(value: String) {
        _uiState.update { it.copy(productIdInput = value) }
    }

    fun loadSizesByProduct(productId: Long) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(loading = true, message = null, isError = false, selectedProductId = productId) }
                val response = repository.getSizesByProduct(productId)
                _uiState.update {
                    it.copy(
                        sizes = response.body().orEmpty(),
                        loading = false,
                        message = null,
                        isError = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        loading = false,
                        message = "Lỗi tải size: ${e.message ?: "Không rõ nguyên nhân"}",
                        isError = true
                    )
                }
            }
        }
    }

    fun onSizeNameChanged(value: String) {
        _uiState.update { it.copy(sizeNameInput = value, formMessage = null, formError = false) }
    }

    fun onPriceChanged(value: String) {
        _uiState.update { it.copy(priceInput = value, formMessage = null, formError = false) }
    }

    fun showAddForm() {
        _uiState.update {
            it.copy(
                showAddForm = true,
                editingId = null,
                sizeNameInput = "",
                priceInput = "",
                formMessage = null,
                formError = false
            )
        }
    }

    fun showEditForm(size: AdminSizeResponse) {
        _uiState.update {
            it.copy(
                showAddForm = true,
                editingId = size.id,
                sizeNameInput = size.sizeName,
                priceInput = size.price.toString(),
                formMessage = null,
                formError = false
            )
        }
    }

    fun closeForm() {
        _uiState.update {
            it.copy(
                showAddForm = false,
                editingId = null,
                sizeNameInput = "",
                priceInput = "",
                formMessage = null,
                formError = false
            )
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null, isError = false) }
    }

    fun clearFormMessage() {
        _uiState.update { it.copy(formMessage = null, formError = false) }
    }

    fun createSize() {
        val state = _uiState.value
        if (state.sizeNameInput.isBlank()) {
            _uiState.update { it.copy(formMessage = "Tên size không được để trống", formError = true) }
            return
        }

        val price = state.priceInput.toDoubleOrNull()
        if (price == null || price < 0) {
            _uiState.update { it.copy(formMessage = "Giá size không hợp lệ", formError = true) }
            return
        }

        if (state.selectedProductId <= 0) {
            _uiState.update { it.copy(message = "Vui lòng chọn sản phẩm trước", isError = true) }
            return
        }

        viewModelScope.launch {
            try {
                _uiState.update { it.copy(formLoading = true, formMessage = null, formError = false) }
                val request = CreateSizeRequest(
                    sizeName = state.sizeNameInput.trim(),
                    price = price
                )
                repository.createSize(state.selectedProductId, request)
                _uiState.update {
                    it.copy(
                        formLoading = false,
                        formMessage = "Thêm size thành công",
                        formError = false,
                        showAddForm = false,
                        sizeNameInput = "",
                        priceInput = ""
                    )
                }
                loadSizesByProduct(state.selectedProductId)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        formLoading = false,
                        formMessage = "Thêm size lỗi: ${e.message ?: "Không rõ nguyên nhân"}",
                        formError = true
                    )
                }
            }
        }
    }

    fun updateSize() {
        val state = _uiState.value
        val sizeId = state.editingId ?: return

        if (state.sizeNameInput.isBlank()) {
            _uiState.update { it.copy(formMessage = "Tên size không được để trống", formError = true) }
            return
        }

        val price = state.priceInput.toDoubleOrNull()
        if (price == null || price < 0) {
            _uiState.update { it.copy(formMessage = "Giá size không hợp lệ", formError = true) }
            return
        }

        viewModelScope.launch {
            try {
                _uiState.update { it.copy(formLoading = true, formMessage = null, formError = false) }
                val request = CreateSizeRequest(
                    sizeName = state.sizeNameInput.trim(),
                    price = price
                )
                repository.updateSize(sizeId, request)
                _uiState.update {
                    it.copy(
                        formLoading = false,
                        formMessage = "Cập nhật size thành công",
                        formError = false,
                        showAddForm = false,
                        editingId = null,
                        sizeNameInput = "",
                        priceInput = ""
                    )
                }
                loadSizesByProduct(state.selectedProductId)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        formLoading = false,
                        formMessage = "Cập nhật size lỗi: ${e.message ?: "Không rõ nguyên nhân"}",
                        formError = true
                    )
                }
            }
        }
    }

    fun deleteSize(sizeId: Long) {
        val state = _uiState.value
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(loading = true, message = null, isError = false) }
                repository.deleteSize(sizeId)
                _uiState.update {
                    it.copy(
                        loading = false,
                        message = "Xóa size thành công",
                        isError = false
                    )
                }
                loadSizesByProduct(state.selectedProductId)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        loading = false,
                        message = "Xóa size lỗi: ${e.message ?: "Không rõ nguyên nhân"}",
                        isError = true
                    )
                }
            }
        }
    }

    fun toggleSizeStatus(sizeId: Long, currentStatus: Int) {
        val state = _uiState.value
        val newStatus = if (currentStatus == 1) 0 else 1
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(loading = true, message = null, isError = false) }
                repository.updateSizeStatus(sizeId, newStatus)
                _uiState.update {
                    it.copy(
                        loading = false,
                        message = "Cập nhật trạng thái size thành công",
                        isError = false
                    )
                }
                loadSizesByProduct(state.selectedProductId)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        loading = false,
                        message = "Cập nhật trạng thái size lỗi: ${e.message ?: "Không rõ nguyên nhân"}",
                        isError = true
                    )
                }
            }
        }
    }
}
