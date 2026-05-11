package com.fastdash.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fastdash.app.data.remote.api.AdminCategoryResponse
import com.fastdash.app.data.remote.api.CreateCategoryRequest
import com.fastdash.app.data.repository.AdminCategoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AdminCategoryUiState(
    val categories: List<AdminCategoryResponse> = emptyList(),
    val searchQuery: String = "",
    val loading: Boolean = false,
    val message: String? = null,
    val isError: Boolean = false,
    val showAddForm: Boolean = false,
    val editingId: Long? = null,
    // Form fields
    val nameInput: String = "",
    val descriptionInput: String = "",
    val formLoading: Boolean = false,
    val formMessage: String? = null,
    val formError: Boolean = false
)

class AdminCategoryViewModel(
    private val repository: AdminCategoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminCategoryUiState())
    val uiState: StateFlow<AdminCategoryUiState> = _uiState.asStateFlow()

    init {
        loadCategories()
    }

    fun loadCategories() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(loading = true, message = null, isError = false) }
                val response = repository.getCategories()
                _uiState.update {
                    it.copy(
                        categories = response.body().orEmpty(),
                        loading = false,
                        message = null,
                        isError = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        loading = false,
                        message = "Lỗi tải danh mục: ${e.message ?: "Không rõ nguyên nhân"}",
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

    fun onDescriptionChanged(value: String) {
        _uiState.update { it.copy(descriptionInput = value, formMessage = null, formError = false) }
    }

    fun showAddForm() {
        _uiState.update {
            it.copy(
                showAddForm = true,
                editingId = null,
                nameInput = "",
                descriptionInput = "",
                formMessage = null,
                formError = false
            )
        }
    }

    fun showEditForm(category: AdminCategoryResponse) {
        _uiState.update {
            it.copy(
                showAddForm = true,
                editingId = category.id,
                nameInput = category.name,
                descriptionInput = category.description.orEmpty(),
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
                descriptionInput = "",
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

    fun createCategory() {
        val state = _uiState.value
        if (state.nameInput.isBlank()) {
            _uiState.update { it.copy(formMessage = "Tên danh mục không được để trống", formError = true) }
            return
        }

        viewModelScope.launch {
            try {
                _uiState.update { it.copy(formLoading = true, formMessage = null, formError = false) }
                val request = CreateCategoryRequest(
                    name = state.nameInput.trim(),
                    description = state.descriptionInput.trim().ifBlank { null }
                )
                repository.createCategory(request)
                _uiState.update {
                    it.copy(
                        formLoading = false,
                        formMessage = "Thêm danh mục thành công",
                        formError = false,
                        showAddForm = false,
                        nameInput = "",
                        descriptionInput = ""
                    )
                }
                loadCategories()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        formLoading = false,
                        formMessage = "Thêm danh mục lỗi: ${e.message ?: "Không rõ nguyên nhân"}",
                        formError = true
                    )
                }
            }
        }
    }

    fun updateCategory() {
        val state = _uiState.value
        val categoryId = state.editingId ?: return

        if (state.nameInput.isBlank()) {
            _uiState.update { it.copy(formMessage = "Tên danh mục không được để trống", formError = true) }
            return
        }

        viewModelScope.launch {
            try {
                _uiState.update { it.copy(formLoading = true, formMessage = null, formError = false) }
                val request = CreateCategoryRequest(
                    name = state.nameInput.trim(),
                    description = state.descriptionInput.trim().ifBlank { null }
                )
                repository.updateCategory(categoryId, request)
                _uiState.update {
                    it.copy(
                        formLoading = false,
                        formMessage = "Cập nhật danh mục thành công",
                        formError = false,
                        showAddForm = false,
                        editingId = null,
                        nameInput = "",
                        descriptionInput = ""
                    )
                }
                loadCategories()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        formLoading = false,
                        formMessage = "Cập nhật danh mục lỗi: ${e.message ?: "Không rõ nguyên nhân"}",
                        formError = true
                    )
                }
            }
        }
    }

    fun deleteCategory(categoryId: Long) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(loading = true, message = null, isError = false) }
                repository.deleteCategory(categoryId)
                _uiState.update {
                    it.copy(
                        loading = false,
                        message = "Xóa danh mục thành công",
                        isError = false
                    )
                }
                loadCategories()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        loading = false,
                        message = "Xóa danh mục lỗi: ${e.message ?: "Không rõ nguyên nhân"}",
                        isError = true
                    )
                }
            }
        }
    }

    fun toggleCategoryStatus(categoryId: Long, currentStatus: Int) {
        val newStatus = if (currentStatus == 1) 0 else 1
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(loading = true, message = null, isError = false) }
                repository.updateCategoryStatus(categoryId, newStatus)
                _uiState.update {
                    it.copy(
                        loading = false,
                        message = "Cập nhật trạng thái thành công",
                        isError = false
                    )
                }
                loadCategories()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        loading = false,
                        message = "Cập nhật trạng thái lỗi: ${e.message ?: "Không rõ nguyên nhân"}",
                        isError = true
                    )
                }
            }
        }
    }
}
