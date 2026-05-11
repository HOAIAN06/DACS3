package com.fastdash.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fastdash.app.data.model.request.AdminProductRequest
import com.fastdash.app.data.remote.api.AdminCategoryResponse
import com.fastdash.app.data.remote.api.AdminToppingResponse
import com.fastdash.app.data.remote.api.CreateSizeRequest
import com.fastdash.app.data.repository.AdminCategoryRepository
import com.fastdash.app.data.repository.AdminProductRepository
import com.fastdash.app.data.repository.AdminSizeRepository
import com.fastdash.app.data.repository.AdminToppingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.MultipartBody

data class ProductSizeInput(
    val name: String = "",
    val price: String = ""
)

data class AdminProductUiState(
    val categoryIdInput: String = "",
    val name: String = "",
    val description: String = "",
    val basePriceInput: String = "",
    val imageUrl: String = "",
    val sizes: List<ProductSizeInput> = listOf(ProductSizeInput("S", "0"), ProductSizeInput("M", "20000"), ProductSizeInput("L", "40000")),
    val selectedToppingIds: Set<Long> = emptySet(),
    val categories: List<AdminCategoryResponse> = emptyList(),
    val allToppings: List<AdminToppingResponse> = emptyList(),
    val loading: Boolean = false,
    val message: String? = null,
    val isError: Boolean = false
) {
    val canSubmit: Boolean
        get() = !loading &&
                imageUrl.isNotBlank() &&
                name.isNotBlank() &&
                description.isNotBlank() &&
                basePriceInput.toDoubleOrNull() != null &&
                categoryIdInput.toLongOrNull() != null
}

class AdminProductViewModel(
    private val repository: AdminProductRepository,
    private val categoryRepository: AdminCategoryRepository,
    private val toppingRepository: AdminToppingRepository,
    private val sizeRepository: AdminSizeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminProductUiState())
    val uiState: StateFlow<AdminProductUiState> = _uiState.asStateFlow()

    init {
        loadFormData()
    }

    fun loadFormData() {
        viewModelScope.launch {
            try {
                val cats = categoryRepository.getCategories().body().orEmpty()
                val toppings = toppingRepository.getToppings().body().orEmpty()
                _uiState.update { it.copy(categories = cats, allToppings = toppings) }
            } catch (e: Exception) {
                _uiState.update { it.copy(message = "Lỗi tải dữ liệu form: ${e.message}", isError = true) }
            }
        }
    }

    fun onCategoryIdChanged(value: String) {
        _uiState.update { it.copy(categoryIdInput = value, message = null, isError = false) }
    }

    fun onNameChanged(value: String) {
        _uiState.update { it.copy(name = value, message = null, isError = false) }
    }

    fun onDescriptionChanged(value: String) {
        _uiState.update { it.copy(description = value, message = null, isError = false) }
    }

    fun onBasePriceChanged(value: String) {
        _uiState.update { it.copy(basePriceInput = value, message = null, isError = false) }
    }

    fun onSizeChanged(index: Int, name: String, price: String) {
        _uiState.update { state ->
            val newSizes = state.sizes.toMutableList()
            newSizes[index] = ProductSizeInput(name, price)
            state.copy(sizes = newSizes)
        }
    }

    fun addSizeInput() {
        _uiState.update { it.copy(sizes = it.sizes + ProductSizeInput()) }
    }

    fun removeSizeInput(index: Int) {
        _uiState.update { it.copy(sizes = it.sizes.filterIndexed { i, _ -> i != index }) }
    }

    fun toggleToppingSelection(toppingId: Long) {
        _uiState.update { state ->
            val newSelection = state.selectedToppingIds.toMutableSet()
            if (newSelection.contains(toppingId)) newSelection.remove(toppingId)
            else newSelection.add(toppingId)
            state.copy(selectedToppingIds = newSelection)
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null, isError = false) }
    }

    fun uploadImage(file: MultipartBody.Part) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(loading = true, message = null, isError = false) }
                val response = repository.uploadImage(file)
                _uiState.update {
                    it.copy(
                        imageUrl = response.imageUrl,
                        loading = false,
                        message = "Upload ảnh thành công",
                        isError = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        loading = false,
                        message = "Upload lỗi: ${e.message}",
                        isError = true
                    )
                }
            }
        }
    }

    fun createProduct() {
        val state = _uiState.value
        val categoryId = state.categoryIdInput.toLongOrNull()
        val basePrice = state.basePriceInput.toDoubleOrNull()

        if (categoryId == null) {
            _uiState.update { it.copy(message = "Vui lòng chọn danh mục", isError = true) }
            return
        }

        viewModelScope.launch {
            try {
                _uiState.update { it.copy(loading = true, message = null, isError = false) }

                // 1. Create Product
                val request = AdminProductRequest(
                    categoryId = categoryId,
                    name = state.name.trim(),
                    description = state.description.trim(),
                    basePrice = basePrice ?: 0.0,
                    imageUrl = state.imageUrl,
                    isCustomizable = 1,
                    status = 1
                )

                val productResponse = repository.createProduct(request)
                val newProductId = productResponse.id

                // 2. Create Sizes
                state.sizes.forEach { sizeInput ->
                    if (sizeInput.name.isNotBlank()) {
                        sizeRepository.createSize(
                            newProductId,
                            CreateSizeRequest(sizeInput.name, sizeInput.price.toDoubleOrNull() ?: 0.0)
                        )
                    }
                }

                // 3. Add Toppings
                state.selectedToppingIds.forEach { toppingId ->
                    repository.addToppingToProduct(newProductId, toppingId)
                }

                _uiState.update {
                    it.copy(
                        categoryIdInput = "",
                        name = "",
                        description = "",
                        basePriceInput = "",
                        imageUrl = "",
                        selectedToppingIds = emptySet(),
                        loading = false,
                        message = "Thêm món và cấu hình thành công",
                        isError = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        loading = false,
                        message = "Lỗi hệ thống: ${e.message}",
                        isError = true
                    )
                }
            }
        }
    }
}
