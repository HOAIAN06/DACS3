package com.fastdash.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fastdash.app.data.remote.api.AdminToppingResponse
import com.fastdash.app.data.remote.api.CreateToppingRequest
import com.fastdash.app.data.repository.AdminToppingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AdminToppingUiState(
    val toppings: List<AdminToppingResponse> = emptyList(),
    val searchQuery: String = "",
    val loading: Boolean = false,
    val message: String? = null,
    val isError: Boolean = false,
    val showAddForm: Boolean = false,
    val editingId: Long? = null,
    // Form fields
    val nameInput: String = "",
    val priceInput: String = "",
    val imageUrlInput: String = "",
    val formLoading: Boolean = false,
    val formMessage: String? = null,
    val formError: Boolean = false
)

class AdminToppingViewModel(
    private val repository: AdminToppingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminToppingUiState())
    val uiState: StateFlow<AdminToppingUiState> = _uiState.asStateFlow()

    init {
        loadToppings()
    }

    fun loadToppings() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(loading = true, message = null, isError = false) }
                val response = repository.getToppings()
                _uiState.update {
                    it.copy(
                        toppings = response.body().orEmpty(),
                        loading = false,
                        message = null,
                        isError = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        loading = false,
                        message = "Lỗi tải topping: ${e.message ?: "Không rõ nguyên nhân"}",
                        isError = true
                    )
                }
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun onNameChanged(value: String) {
        _uiState.update { it.copy(nameInput = value, formMessage = null, formError = false) }
    }

    fun onPriceChanged(value: String) {
        _uiState.update { it.copy(priceInput = value, formMessage = null, formError = false) }
    }

    fun onImageUrlChanged(value: String) {
        _uiState.update { it.copy(imageUrlInput = value, formMessage = null, formError = false) }
    }

    fun showAddForm() {
        _uiState.update {
            it.copy(
                showAddForm = true,
                editingId = null,
                nameInput = "",
                priceInput = "",
                imageUrlInput = "",
                formMessage = null,
                formError = false
            )
        }
    }

    fun showEditForm(topping: AdminToppingResponse) {
        _uiState.update {
            it.copy(
                showAddForm = true,
                editingId = topping.id,
                nameInput = topping.name,
                priceInput = topping.price.toString(),
                imageUrlInput = topping.imageUrl.orEmpty(),
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
                nameInput = "",
                priceInput = "",
                imageUrlInput = "",
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

    fun createTopping() {
        val state = _uiState.value
        if (state.nameInput.isBlank()) {
            _uiState.update { it.copy(formMessage = "Tên topping không được để trống", formError = true) }
            return
        }

        val price = state.priceInput.toDoubleOrNull()
        if (price == null || price < 0) {
            _uiState.update { it.copy(formMessage = "Giá topping không hợp lệ", formError = true) }
            return
        }

        viewModelScope.launch {
            try {
                _uiState.update { it.copy(formLoading = true, formMessage = null, formError = false) }
                val request = CreateToppingRequest(
                    name = state.nameInput.trim(),
                    price = price,
                    imageUrl = state.imageUrlInput.trim().ifBlank { null }
                )
                repository.createTopping(request)
                _uiState.update {
                    it.copy(
                        formLoading = false,
                        formMessage = "Thêm topping thành công",
                        formError = false,
                        showAddForm = false,
                        nameInput = "",
                        priceInput = "",
                        imageUrlInput = ""
                    )
                }
                loadToppings()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        formLoading = false,
                        formMessage = "Thêm topping lỗi: ${e.message ?: "Không rõ nguyên nhân"}",
                        formError = true
                    )
                }
            }
        }
    }

    fun updateTopping() {
        val state = _uiState.value
        val toppingId = state.editingId ?: return

        if (state.nameInput.isBlank()) {
            _uiState.update { it.copy(formMessage = "Tên topping không được để trống", formError = true) }
            return
        }

        val price = state.priceInput.toDoubleOrNull()
        if (price == null || price < 0) {
            _uiState.update { it.copy(formMessage = "Giá topping không hợp lệ", formError = true) }
            return
        }

        viewModelScope.launch {
            try {
                _uiState.update { it.copy(formLoading = true, formMessage = null, formError = false) }
                val request = CreateToppingRequest(
                    name = state.nameInput.trim(),
                    price = price,
                    imageUrl = state.imageUrlInput.trim().ifBlank { null }
                )
                repository.updateTopping(toppingId, request)
                _uiState.update {
                    it.copy(
                        formLoading = false,
                        formMessage = "Cập nhật topping thành công",
                        formError = false,
                        showAddForm = false,
                        editingId = null,
                        nameInput = "",
                        priceInput = "",
                        imageUrlInput = ""
                    )
                }
                loadToppings()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        formLoading = false,
                        formMessage = "Cập nhật topping lỗi: ${e.message ?: "Không rõ nguyên nhân"}",
                        formError = true
                    )
                }
            }
        }
    }

    fun deleteTopping(toppingId: Long) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(loading = true, message = null, isError = false) }
                repository.deleteTopping(toppingId)
                _uiState.update {
                    it.copy(
                        loading = false,
                        message = "Xóa topping thành công",
                        isError = false
                    )
                }
                loadToppings()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        loading = false,
                        message = "Xóa topping lỗi: ${e.message ?: "Không rõ nguyên nhân"}",
                        isError = true
                    )
                }
            }
        }
    }

    fun toggleToppingStatus(toppingId: Long, currentStatus: Int) {
        val newStatus = if (currentStatus == 1) 0 else 1
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(loading = true, message = null, isError = false) }
                repository.updateToppingStatus(toppingId, newStatus)
                _uiState.update {
                    it.copy(
                        loading = false,
                        message = "Cập nhật trạng thái topping thành công",
                        isError = false
                    )
                }
                loadToppings()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        loading = false,
                        message = "Cập nhật trạng thái topping lỗi: ${e.message ?: "Không rõ nguyên nhân"}",
                        isError = true
                    )
                }
            }
        }
    }
}
