package com.fastdash.app.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fastdash.app.data.model.response.ApiErrorResponse
import com.fastdash.app.data.remote.api.AdminToppingResponse
import com.fastdash.app.data.repository.AdminToppingRepository
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response

data class AdminToppingUiState(
    val toppings: List<AdminToppingResponse> = emptyList(),
    val searchQuery: String = "",
    val loading: Boolean = false,
    val message: String? = null,
    val isError: Boolean = false,
    val showAddForm: Boolean = false,
    val editingId: Long? = null,
    val nameInput: String = "",
    val priceInput: String = "",
    val imageUrlInput: String = "",
    val imagePreview: String = "",
    val formLoading: Boolean = false,
    val formMessage: String? = null,
    val formError: Boolean = false,
    val isActiveInput: Boolean = true,
    val deletingToppingId: Long? = null,
    val togglingToppingId: Long? = null
)

class AdminToppingViewModel(
    private val repository: AdminToppingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminToppingUiState())
    val uiState: StateFlow<AdminToppingUiState> = _uiState.asStateFlow()
    private var selectedImagePart: MultipartBody.Part? = null
    private var selectedImageUri: Uri? = null

    init {
        loadToppings()
    }

    private suspend fun fetchToppings(): List<AdminToppingResponse> {
        val response = repository.getToppings()
        if (!response.isSuccessful) {
            throw IllegalStateException(buildApiError("Tai topping", response))
        }
        return normalizeToppings(
            response.body() ?: throw IllegalStateException("Tai topping that bai: body rong")
        )
    }

    fun loadToppings() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(loading = true, message = null, isError = false) }
                val toppings = fetchToppings()

                _uiState.update {
                    it.copy(
                        toppings = toppings,
                        loading = false,
                        message = null,
                        isError = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        loading = false,
                        message = "Loi tai topping: ${e.message ?: "Khong ro"}",
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

    fun onActiveChanged(value: Boolean) {
        _uiState.update { it.copy(isActiveInput = value, formMessage = null, formError = false) }
    }

    fun onImageSelected(uri: Uri, imagePart: MultipartBody.Part) {
        selectedImageUri = uri
        selectedImagePart = imagePart
        _uiState.update {
            it.copy(
                imageUrlInput = uri.toString(),
                imagePreview = uri.toString(),
                formMessage = null,
                formError = false
            )
        }
    }

    fun showAddForm() {
        _uiState.update {
            it.copy(
                showAddForm = true,
                editingId = null,
                nameInput = "",
                priceInput = "",
                imageUrlInput = "",
                imagePreview = "",
                isActiveInput = true,
                formLoading = false,
                formMessage = null,
                formError = false,
                deletingToppingId = null
            )
        }
        selectedImagePart = null
        selectedImageUri = null
    }

    fun showEditForm(topping: AdminToppingResponse) {
        _uiState.update {
            it.copy(
                showAddForm = true,
                editingId = topping.id,
                nameInput = topping.name.orEmpty(),
                priceInput = topping.price.toString(),
                imageUrlInput = topping.imageUrl.orEmpty(),
                imagePreview = topping.imageUrl.orEmpty(),
                isActiveInput = topping.status == 1,
                formLoading = false,
                formMessage = null,
                formError = false,
                deletingToppingId = null
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
                priceInput = "",
                imageUrlInput = "",
                imagePreview = "",
                isActiveInput = true,
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

    fun requestDelete(toppingId: Long) {
        _uiState.update { it.copy(deletingToppingId = toppingId) }
    }

    fun dismissDeleteDialog() {
        _uiState.update { it.copy(deletingToppingId = null) }
    }

    fun createTopping() {
        val state = _uiState.value
        val name = state.nameInput.trim()
        val price = state.priceInput.toDoubleOrNull()

        when {
            name.isBlank() -> {
                _uiState.update { it.copy(formMessage = "Ten topping khong duoc de trong", formError = true) }
                return
            }
            hasDuplicateName(name, null) -> {
                _uiState.update { it.copy(formMessage = "Ten topping da ton tai", formError = true) }
                return
            }
            price == null || price < 0.0 -> {
                _uiState.update { it.copy(formMessage = "Gia topping khong hop le", formError = true) }
                return
            }
            selectedImagePart == null -> {
                _uiState.update { it.copy(formMessage = "Vui long chon anh topping", formError = true) }
                return
            }
        }

        viewModelScope.launch {
            try {
                _uiState.update { it.copy(formLoading = true, formMessage = null, formError = false) }
                val response = repository.createTopping(
                    name = name.toFormPart(),
                    price = price.toString().toFormPart(),
                    status = if (state.isActiveInput) "1".toFormPart() else "0".toFormPart(),
                    image = selectedImagePart ?: throw IllegalStateException("Vui long chon anh topping")
                )
                if (!response.isSuccessful) {
                    throw IllegalStateException(buildApiError("Them topping", response))
                }
                val refreshedToppings = fetchToppings()

                _uiState.update {
                    it.copy(
                        toppings = refreshedToppings,
                        showAddForm = false,
                        editingId = null,
                        nameInput = "",
                        priceInput = "",
                        imageUrlInput = "",
                        imagePreview = "",
                        isActiveInput = true,
                        formLoading = false,
                        formMessage = null,
                        formError = false,
                        message = "Them topping thanh cong",
                        isError = false
                    )
                }
                selectedImagePart = null
                selectedImageUri = null
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        formLoading = false,
                        formMessage = "Them topping loi: ${e.message ?: "Khong ro"}",
                        formError = true
                    )
                }
            }
        }
    }

    fun updateTopping() {
        val state = _uiState.value
        val toppingId = state.editingId ?: return
        val name = state.nameInput.trim()
        val price = state.priceInput.toDoubleOrNull()

        when {
            name.isBlank() -> {
                _uiState.update { it.copy(formMessage = "Ten topping khong duoc de trong", formError = true) }
                return
            }
            hasDuplicateName(name, toppingId) -> {
                _uiState.update { it.copy(formMessage = "Ten topping da ton tai", formError = true) }
                return
            }
            price == null || price < 0.0 -> {
                _uiState.update { it.copy(formMessage = "Gia topping khong hop le", formError = true) }
                return
            }
        }

        viewModelScope.launch {
            try {
                _uiState.update { it.copy(formLoading = true, formMessage = null, formError = false) }
                val currentStatus = _uiState.value.toppings.firstOrNull { it.id == toppingId }?.status ?: 1
                val response = repository.updateTopping(
                    id = toppingId,
                    name = name.toFormPart(),
                    price = price.toString().toFormPart(),
                    status = (if (state.isActiveInput) 1 else 0).toString().toFormPart(),
                    image = selectedImagePart
                )
                if (!response.isSuccessful) {
                    throw IllegalStateException(buildApiError("Cap nhat topping", response))
                }
                val refreshedToppings = fetchToppings()

                _uiState.update {
                    it.copy(
                        toppings = refreshedToppings,
                        showAddForm = false,
                        editingId = null,
                        nameInput = "",
                        priceInput = "",
                        imageUrlInput = "",
                        imagePreview = "",
                        isActiveInput = true,
                        formLoading = false,
                        formMessage = null,
                        formError = false,
                        message = "Cap nhat topping thanh cong",
                        isError = false
                    )
                }
                selectedImagePart = null
                selectedImageUri = null
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        formLoading = false,
                        formMessage = "Cap nhat topping loi: ${e.message ?: "Khong ro"}",
                        formError = true
                    )
                }
            }
        }
    }

    fun deleteTopping() {
        val toppingId = _uiState.value.deletingToppingId ?: return
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(loading = true, message = null, isError = false) }
                val response = repository.deleteTopping(toppingId)
                if (!response.isSuccessful) {
                    throw IllegalStateException(buildApiError("Xoa topping", response))
                }
                val refreshedToppings = fetchToppings()

                _uiState.update {
                    it.copy(
                        toppings = refreshedToppings,
                        loading = false,
                        deletingToppingId = null,
                        message = "Xoa topping thanh cong",
                        isError = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        loading = false,
                        deletingToppingId = null,
                        message = "Xoa topping loi: ${e.message ?: "Khong ro"}",
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
                _uiState.update { it.copy(togglingToppingId = toppingId, message = null, isError = false) }
                val response = repository.updateToppingStatus(toppingId, newStatus)
                if (!response.isSuccessful) {
                    throw IllegalStateException(buildApiError("Cap nhat trang thai topping", response))
                }
                val refreshedToppings = fetchToppings()
                _uiState.update { state ->
                    state.copy(
                        toppings = refreshedToppings,
                        togglingToppingId = null,
                        message = "Cap nhat trang thai topping thanh cong",
                        isError = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        togglingToppingId = null,
                        message = "Cap nhat trang thai topping loi: ${e.message ?: "Khong ro"}",
                        isError = true
                    )
                }
            }
        }
    }

    private fun normalizeToppings(toppings: List<AdminToppingResponse>): List<AdminToppingResponse> {
        return toppings
            .filter { it.id > 0L }
            .distinctBy { it.id }
            .sortedBy { it.name.orEmpty().trim().lowercase() }
    }

    private fun hasDuplicateName(name: String, editingId: Long?): Boolean {
        val normalized = name.trim().lowercase()
        return _uiState.value.toppings.any { topping ->
            topping.id != editingId && topping.name.orEmpty().trim().lowercase() == normalized
        }
    }

    private fun buildApiError(action: String, response: Response<*>): String {
        val body = response.errorBody()?.string().orEmpty()
        val apiError = body.takeIf { it.isNotBlank() }
            ?.let { runCatching { Gson().fromJson(it, ApiErrorResponse::class.java) }.getOrNull() }
        val message = apiError?.message.orEmpty()
        val normalizedMessage = message.lowercase()
        val normalizedBody = body.lowercase()

        val friendlyError = when (response.code()) {
            400 -> when {
                normalizedMessage.contains("image is required") -> "Anh topping la bat buoc khi tao moi."
                normalizedMessage.contains("max size") || normalizedMessage.contains("5mb") -> "Anh topping vuot qua gioi han 5MB."
                normalizedMessage.contains("jpg") || normalizedMessage.contains("jpeg") ||
                    normalizedMessage.contains("png") || normalizedMessage.contains("webp") ||
                    normalizedMessage.contains("content-type") || normalizedMessage.contains("extension") ->
                    "Anh topping chi chap nhan jpg, jpeg, png hoac webp."
                normalizedMessage.isNotBlank() -> message
                else -> "Du lieu topping khong hop le."
            }
            401 -> "Phien dang nhap het han. Vui long dang nhap lai."
            403 -> "Tai khoan hien tai khong co quyen thao tac admin."
            404 -> "Khong tim thay topping."
            409 -> when {
                normalizedMessage.contains("topping is in use by products") ||
                    normalizedBody.contains("topping is in use by products") ->
                    "Topping dang duoc gan voi san pham. Khong the xoa."
                normalizedMessage.contains("duplicate") ||
                    normalizedMessage.contains("already exists") ||
                    normalizedMessage.contains("unique") ||
                    normalizedBody.contains("duplicate") ||
                    normalizedBody.contains("already exists") ||
                    normalizedBody.contains("unique") ->
                    "Ten topping da ton tai."
                normalizedMessage.isNotBlank() -> message
                else -> "Topping dang duoc su dung hoac bi trung du lieu."
            }
            else -> message.ifBlank { "$action that bai (${response.code()})" }
        }

        return friendlyError
    }

    private fun String.toFormPart() = toRequestBody("text/plain".toMediaType())
}
