package com.fastdash.app.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fastdash.app.data.remote.api.AdminCategoryResponse
import com.fastdash.app.data.repository.AdminCategoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response

data class AdminCategoryUiState(
    val categories: List<AdminCategoryResponse> = emptyList(),
    val searchQuery: String = "",
    val loading: Boolean = false,
    val message: String? = null,
    val isError: Boolean = false,
    val showAddForm: Boolean = false,
    val editingId: Long? = null,
    val nameInput: String = "",
    val descriptionInput: String = "",
    val imagePreview: String = "",
    val formLoading: Boolean = false,
    val formMessage: String? = null,
    val formError: Boolean = false
)

class AdminCategoryViewModel(
    private val repository: AdminCategoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminCategoryUiState())
    val uiState: StateFlow<AdminCategoryUiState> = _uiState.asStateFlow()
    private var selectedImagePart: MultipartBody.Part? = null
    private var selectedImageUri: Uri? = null

    init {
        loadCategories()
    }

    private suspend fun fetchCategories(): List<AdminCategoryResponse> {
        val response = repository.getCategories()
        if (!response.isSuccessful) {
            throw IllegalStateException(buildApiError("Tai danh muc", response))
        }
        return normalizeCategories(
            response.body() ?: throw IllegalStateException("Tai danh muc that bai: body rong")
        )
    }

    fun loadCategories() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(loading = true, message = null, isError = false) }
                val categories = fetchCategories()

                _uiState.update {
                    it.copy(
                        categories = categories,
                        loading = false,
                        message = null,
                        isError = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        loading = false,
                        message = "Loi tai danh muc: ${e.message ?: "Khong ro"}",
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
                imagePreview = "",
                formLoading = false,
                formMessage = null,
                formError = false
            )
        }
        selectedImagePart = null
        selectedImageUri = null
    }

    fun showEditForm(category: AdminCategoryResponse) {
        _uiState.update {
            it.copy(
                showAddForm = true,
                editingId = category.id,
                nameInput = category.name.orEmpty(),
                descriptionInput = category.description.orEmpty(),
                imagePreview = category.imageUrl.orEmpty(),
                formLoading = false,
                formMessage = null,
                formError = false
            )
        }
        selectedImagePart = null
        selectedImageUri = null
    }

    fun closeForm() {
        _uiState.update {
            it.copy(
                showAddForm = false,
                editingId = null,
                nameInput = "",
                descriptionInput = "",
                imagePreview = "",
                formLoading = false,
                formMessage = null,
                formError = false
            )
        }
        selectedImagePart = null
        selectedImageUri = null
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null, isError = false) }
    }

    fun onImageSelected(uri: Uri, imagePart: MultipartBody.Part) {
        selectedImageUri = uri
        selectedImagePart = imagePart
        _uiState.update {
            it.copy(
                imagePreview = uri.toString(),
                formMessage = null,
                formError = false
            )
        }
    }

    fun createCategory() {
        val state = _uiState.value
        val name = state.nameInput.trim()
        val description = state.descriptionInput.trim()

        when {
            name.isBlank() -> {
                _uiState.update { it.copy(formMessage = "Ten danh muc khong duoc de trong", formError = true) }
                return
            }
            hasDuplicateName(name, null) -> {
                _uiState.update { it.copy(formMessage = "Ten danh muc da ton tai", formError = true) }
                return
            }
            selectedImagePart == null -> {
                _uiState.update { it.copy(formMessage = "Vui long chon anh danh muc", formError = true) }
                return
            }
        }

        viewModelScope.launch {
            try {
                _uiState.update { it.copy(formLoading = true, formMessage = null, formError = false) }
                val response = repository.createCategory(
                    name = name.toFormPart(),
                    description = description.toFormPart(),
                    status = "1".toFormPart(),
                    image = selectedImagePart ?: throw IllegalStateException("Vui long chon anh danh muc")
                )
                if (!response.isSuccessful) {
                    throw IllegalStateException(buildApiError("Them danh muc", response))
                }
                val refreshedCategories = fetchCategories()

                _uiState.update {
                    it.copy(
                        categories = refreshedCategories,
                        showAddForm = false,
                        editingId = null,
                        nameInput = "",
                        descriptionInput = "",
                        imagePreview = "",
                        formLoading = false,
                        formMessage = null,
                        formError = false,
                        message = "Them danh muc thanh cong",
                        isError = false
                    )
                }
                selectedImagePart = null
                selectedImageUri = null
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        formLoading = false,
                        formMessage = "Them danh muc loi: ${e.message ?: "Khong ro"}",
                        formError = true
                    )
                }
            }
        }
    }

    fun updateCategory() {
        val state = _uiState.value
        val categoryId = state.editingId ?: return
        val name = state.nameInput.trim()
        val description = state.descriptionInput.trim()

        when {
            name.isBlank() -> {
                _uiState.update { it.copy(formMessage = "Ten danh muc khong duoc de trong", formError = true) }
                return
            }
            hasDuplicateName(name, categoryId) -> {
                _uiState.update { it.copy(formMessage = "Ten danh muc da ton tai", formError = true) }
                return
            }
        }

        viewModelScope.launch {
            try {
                _uiState.update { it.copy(formLoading = true, formMessage = null, formError = false) }
                val currentStatus = _uiState.value.categories.firstOrNull { it.id == categoryId }?.status ?: 1
                val response = repository.updateCategory(
                    id = categoryId,
                    name = name.toFormPart(),
                    description = description.toFormPart(),
                    status = currentStatus.toString().toFormPart(),
                    image = selectedImagePart
                )
                if (!response.isSuccessful) {
                    throw IllegalStateException(buildApiError("Cap nhat danh muc", response))
                }
                val refreshedCategories = fetchCategories()

                _uiState.update {
                    it.copy(
                        categories = refreshedCategories,
                        showAddForm = false,
                        editingId = null,
                        nameInput = "",
                        descriptionInput = "",
                        imagePreview = "",
                        formLoading = false,
                        formMessage = null,
                        formError = false,
                        message = "Cap nhat danh muc thanh cong",
                        isError = false
                    )
                }
                selectedImagePart = null
                selectedImageUri = null
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        formLoading = false,
                        formMessage = "Cap nhat danh muc loi: ${e.message ?: "Khong ro"}",
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
                val response = repository.deleteCategory(categoryId)
                if (!response.isSuccessful) {
                    throw IllegalStateException(buildApiError("Xoa danh muc", response))
                }
                val refreshedCategories = fetchCategories()

                _uiState.update {
                    it.copy(
                        categories = refreshedCategories,
                        loading = false,
                        message = "Xoa danh muc thanh cong",
                        isError = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        loading = false,
                        message = "Xoa danh muc loi: ${e.message ?: "Khong ro"}",
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
                val response = repository.updateCategoryStatus(categoryId, newStatus)
                if (!response.isSuccessful) {
                    throw IllegalStateException(buildApiError("Cap nhat trang thai danh muc", response))
                }
                val refreshedCategories = fetchCategories()
                _uiState.update { state ->
                    state.copy(
                        categories = refreshedCategories,
                        loading = false,
                        message = "Cap nhat trang thai thanh cong",
                        isError = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        loading = false,
                        message = "Cap nhat trang thai loi: ${e.message ?: "Khong ro"}",
                        isError = true
                    )
                }
            }
        }
    }

    private fun normalizeCategories(categories: List<AdminCategoryResponse>): List<AdminCategoryResponse> {
        return categories
            .filter { it.id > 0L }
            .distinctBy { it.id }
            .sortedBy { it.name.orEmpty().trim().lowercase() }
    }

    private fun hasDuplicateName(name: String, editingId: Long?): Boolean {
        val normalized = name.trim().lowercase()
        return _uiState.value.categories.any { category ->
            category.id != editingId && category.name.orEmpty().trim().lowercase() == normalized
        }
    }

    private fun buildApiError(action: String, response: Response<*>): String {
        val serverError = response.errorBody()?.string().orEmpty()
        val normalizedServerError = serverError.lowercase()
        val friendlyError = when {
            response.code() == 401 -> "Phien dang nhap het han. Vui long dang nhap lai."
            response.code() == 403 -> "Tai khoan hien tai khong co quyen thao tac admin."
            response.code() == 409 && normalizedServerError.contains("category is in use by products") ->
                "Danh muc dang duoc gan voi san pham. Khong the xoa."
            normalizedServerError.contains("violates foreign key constraint") ||
                normalizedServerError.contains("is still referenced") ->
                "Danh muc dang duoc gan voi du lieu khac. Khong the xoa cung, hay chuyen sang an/vo hieu hoa."
            normalizedServerError.contains("duplicate") ||
                normalizedServerError.contains("already exists") ||
                normalizedServerError.contains("unique") ->
                "Ten danh muc da ton tai."
            else -> null
        }

        return friendlyError
            ?: ("$action that bai (${response.code()})" + if (serverError.isNotBlank()) ": $serverError" else "")
    }

    private fun String.toFormPart() = toRequestBody("text/plain".toMediaType())
}
