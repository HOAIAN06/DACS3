package com.fastdash.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fastdash.app.data.model.request.AdminProductRequest
import com.fastdash.app.data.remote.api.AdminCategoryResponse
import com.fastdash.app.data.remote.api.AdminToppingResponse
import com.fastdash.app.data.remote.api.AdminProductResponse
import com.fastdash.app.data.remote.api.UpdateProductRequest
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
    val hasSizes: Boolean = false,
    val sizes: List<ProductSizeInput> = emptyList(),
    val selectedToppingIds: Set<Long> = emptySet(),
    val categories: List<AdminCategoryResponse> = emptyList(),
    val allToppingsList: List<AdminToppingResponse> = emptyList(),
    val loading: Boolean = false,
    val message: String? = null,
    val isError: Boolean = false,
    // New fields for multi-step form
    val showAddForm: Boolean = false,
    val editingProductId: Long? = null,
    val currentStep: Int = 1
) {
    val canSubmit: Boolean
        get() = !loading &&
                (imageUrl.isNotBlank() || editingProductId != null) &&
                name.isNotBlank() &&
                description.isNotBlank() &&
                basePriceInput.toDoubleOrNull() != null &&
                basePriceInput.toDoubleOrNull()!! >= 0.0 &&
                categoryIdInput.toLongOrNull() != null &&
                (!hasSizes || (
                    sizes.isNotEmpty() &&
                        sizes.all { size ->
                            size.name.isNotBlank() &&
                                size.price.toDoubleOrNull() != null &&
                                size.price.toDoubleOrNull()!! >= 0.0
                        }
                ))

    val canGoNext: Boolean
        get() = when (currentStep) {
            1 -> imageUrl.isNotBlank() && name.isNotBlank() && description.isNotBlank() && basePriceInput.toDoubleOrNull() != null
            2 -> categoryIdInput.toLongOrNull() != null
            3 -> true // Step 3 is final configuration, always can try to submit
            else -> false
        }
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
                val catResponse = categoryRepository.getCategories()
                if (!catResponse.isSuccessful) {
                    val serverError = catResponse.errorBody()?.string().orEmpty()
                    throw IllegalStateException("Tải danh mục thất bại (${catResponse.code()})" + if (serverError.isNotBlank()) ": $serverError" else "")
                }
                val cats = catResponse.body()
                    ?: throw IllegalStateException("Tải danh mục thất bại: BE trả body rỗng")

                val toppingResponse = toppingRepository.getToppings()
                if (!toppingResponse.isSuccessful) {
                    val serverError = toppingResponse.errorBody()?.string().orEmpty()
                    throw IllegalStateException("Tải topping thất bại (${toppingResponse.code()})" + if (serverError.isNotBlank()) ": $serverError" else "")
                }
                val toppings = toppingResponse.body()
                    ?: throw IllegalStateException("Tải topping thất bại: BE trả body rỗng")

                val safeCategories = cats.filter { it.id > 0L }.distinctBy { it.id }
                val safeToppings = toppings.filter { it.id > 0L }.distinctBy { it.id }

                _uiState.update { it.copy(categories = safeCategories, allToppingsList = safeToppings) }
            } catch (e: Exception) {
                _uiState.update { it.copy(message = "Lỗi tải dữ liệu form: ${e.message}", isError = true) }
            }
        }
    }

    fun setShowAddForm(show: Boolean) {
        if (show) {
            // If we are opening the form, and we weren't already showing it as "Add" (not editing),
            // we should probably reset to ensure it's clean for a new product.
            // BUT wait, if we call showEditForm, it sets showAddForm = true.
            // Let's make FAB specifically call a "startAdd" method.
            loadFormData()
        }
        _uiState.update { it.copy(showAddForm = show, message = null, isError = false) }
    }

    fun startAddForm() {
        resetFormData()
        _uiState.update { it.copy(showAddForm = true) }
        loadFormData()
    }

    fun showEditForm(product: AdminProductResponse) {
        _uiState.update {
            it.copy(
                showAddForm = true,
                editingProductId = product.id,
                categoryIdInput = product.categoryId.toString(),
                name = product.name.orEmpty(),
                description = product.description.orEmpty(),
                basePriceInput = product.basePrice.toString(),
                imageUrl = product.imageUrl.orEmpty(),
                currentStep = 1,
                hasSizes = product.isCustomizable == 1,
                message = null,
                isError = false
            )
        }
        loadFormData()
        
        // Load sizes and toppings for the product being edited
        viewModelScope.launch {
            try {
                val sizesResp = sizeRepository.getSizesByProduct(product.id)
                if (sizesResp.isSuccessful) {
                    val productSizes = sizesResp.body()?.map { ProductSizeInput(it.sizeName, it.price.toString()) }.orEmpty()
                    _uiState.update { it.copy(sizes = productSizes, hasSizes = productSizes.isNotEmpty()) }
                }
                
                // Fetch product detail or directly toppings to see which are selected
                val detailResp = repository.getProductDetail(product.id)
                if (detailResp.isSuccessful) {
                    // Potentially map selected toppings if available in detail
                    // For now, at least we loaded sizes
                }
            } catch (e: Exception) {
                // Failed to load extra configuration, continuing with basic info
            }
        }
    }

    fun resetFormData() {
        _uiState.update {
            it.copy(
                showAddForm = false,
                editingProductId = null,
                categoryIdInput = "",
                name = "",
                description = "",
                basePriceInput = "",
                imageUrl = "",
                hasSizes = false,
                sizes = emptyList(),
                selectedToppingIds = emptySet(),
                currentStep = 1,
                message = null,
                isError = false
            )
        }
    }

    fun nextStep() {
        _uiState.update { 
            if (it.currentStep < 3) it.copy(currentStep = it.currentStep + 1) else it
        }
    }

    fun previousStep() {
        _uiState.update { 
            if (it.currentStep > 1) it.copy(currentStep = it.currentStep - 1) else it
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
            if (index !in state.sizes.indices) return@update state
            val newSizes = state.sizes.toMutableList()
            newSizes[index] = ProductSizeInput(name, price)
            state.copy(sizes = newSizes)
        }
    }

    fun addSizeInput() {
        _uiState.update { state ->
            val seed = if (state.sizes.isEmpty()) ProductSizeInput("M", "0") else ProductSizeInput()
            state.copy(hasSizes = true, sizes = state.sizes + seed)
        }
    }

    fun removeSizeInput(index: Int) {
        _uiState.update { state ->
            if (index !in state.sizes.indices) return@update state
            val updated = state.sizes.filterIndexed { i, _ -> i != index }
            state.copy(sizes = updated)
        }
    }

    fun setHasSizes(enabled: Boolean) {
        _uiState.update { state ->
            if (!enabled) {
                state.copy(hasSizes = false, sizes = emptyList(), message = null, isError = false)
            } else {
                val seeded = if (state.sizes.isEmpty()) listOf(ProductSizeInput("M", "0")) else state.sizes
                state.copy(hasSizes = true, sizes = seeded, message = null, isError = false)
            }
        }
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

    fun toggleProductStatus(productId: Long, currentStatus: Int, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(loading = true, message = null, isError = false) }
                val newStatus = if (currentStatus == 1) 0 else 1
                val response = repository.updateProductStatus(productId, newStatus)
                if (response.isSuccessful) {
                    _uiState.update { it.copy(loading = false, message = "Đã cập nhật trạng thái món ăn") }
                    onSuccess()
                } else {
                    _uiState.update { it.copy(loading = false, message = "Lỗi cập nhật trạng thái: ${response.code()}", isError = true) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(loading = false, message = "Lỗi kết nối: ${e.message}", isError = true) }
            }
        }
    }

    fun setMessage(message: String, isError: Boolean = false) {
        _uiState.update { it.copy(message = message, isError = isError) }
    }

    fun uploadImage(file: MultipartBody.Part) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(loading = true, message = null, isError = false) }
                val response = repository.uploadImage(file)
                if (response.isSuccessful) {
                    val url = response.body()?.resolvedImageUrl.orEmpty()
                    if (url.isBlank()) {
                        _uiState.update {
                            it.copy(
                                loading = false,
                                message = "Upload thành công nhưng BE chưa trả về 'imageUrl'. Hãy sửa response upload để trả đúng key imageUrl.",
                                isError = true
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                imageUrl = url,
                                loading = false,
                                message = "Upload ảnh thành công",
                                isError = false
                            )
                        }
                    }
                } else {
                    val serverError = response.errorBody()?.string().orEmpty()
                    _uiState.update {
                        it.copy(
                            loading = false,
                            message = "Upload lỗi (${response.code()}): ${if (serverError.isNotBlank()) serverError else "Không rõ"}",
                            isError = true
                        )
                    }
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

    suspend fun createProduct(): Boolean {
        val state = _uiState.value
        val categoryId = state.categoryIdInput.toLongOrNull()
        val basePrice = state.basePriceInput.toDoubleOrNull()

        if (categoryId == null) {
            _uiState.update { it.copy(message = "Vui lòng chọn danh mục", isError = true) }
            return false
        }

        if (basePrice == null || basePrice < 0.0) {
            _uiState.update { it.copy(message = "Giá cơ bản phải lớn hơn 0", isError = true) }
            return false
        }

        if (basePrice <= 0.0) {
            _uiState.update { it.copy(message = "Giá cơ bản phải lớn hơn 0", isError = true) }
            return false
        }

        if (state.hasSizes && state.sizes.isEmpty()) {
            _uiState.update { it.copy(message = "Vui lòng thêm ít nhất một size", isError = true) }
            return false
        }

        val invalidSize = state.sizes.firstOrNull { size ->
            size.name.isBlank() || size.price.toDoubleOrNull() == null || size.price.toDoubleOrNull()!! < 0.0
        }
        if (state.hasSizes && invalidSize != null) {
            _uiState.update {
                it.copy(
                    message = "Size '${invalidSize.name.ifBlank { "(trống)" }}' có giá không hợp lệ",
                    isError = true
                )
            }
            return false
        }

            var createdProductId: Long? = null

        try {
            _uiState.update { it.copy(loading = true, message = null, isError = false) }

            val request = AdminProductRequest(
                categoryId = categoryId,
                name = state.name.trim(),
                description = state.description.trim(),
                basePrice = basePrice,
                imageUrl = state.imageUrl,
                isCustomizable = if (state.hasSizes || state.selectedToppingIds.isNotEmpty()) 1 else 0,
                status = 1
            )

            val productResponse = repository.createProduct(request)
            if (!productResponse.isSuccessful) {
                val serverError = productResponse.errorBody()?.string().orEmpty()
                throw IllegalStateException("Tạo sản phẩm thất bại (${productResponse.code()}): ${if (serverError.isNotBlank()) serverError else "Không rõ"}")
            }
            val createdProduct = productResponse.body() ?: throw IllegalStateException("Tạo sản phẩm trả về body rỗng")
            createdProductId = createdProduct.id

            if (state.hasSizes) {
                val existingSizesResponse = sizeRepository.getSizesByProduct(createdProductId)
                val existingByName = if (existingSizesResponse.isSuccessful) {
                    existingSizesResponse.body().orEmpty()
                        .associateBy { it.sizeName.trim().uppercase() }
                } else {
                    emptyMap()
                }

                state.sizes.forEach { sizeInput ->
                    val cleanName = sizeInput.name.trim()
                    val sizePrice = sizeInput.price.toDoubleOrNull()
                        ?: throw IllegalArgumentException("Giá size '$cleanName' không hợp lệ")
                    val request = CreateSizeRequest(sizeName = cleanName, price = sizePrice)

                    val existing = existingByName[cleanName.uppercase()]
                    val sizeResponse = if (existing != null) {
                        sizeRepository.updateSize(existing.id, request)
                    } else {
                        sizeRepository.createSize(createdProductId, request)
                    }

                    if (!sizeResponse.isSuccessful) {
                        val serverError = sizeResponse.errorBody()?.string().orEmpty()
                        throw IllegalStateException(
                            "Không thể lưu size '$cleanName' (${sizeResponse.code()})" +
                                if (serverError.isNotBlank()) ": $serverError" else ""
                        )
                    }
                }
            }

            state.selectedToppingIds.forEach { toppingId ->
                val toppingResponse = repository.addToppingToProduct(createdProductId, toppingId)
                if (!toppingResponse.isSuccessful) {
                    throw IllegalStateException("Không thể gán topping ID=$toppingId")
                }
            }

            _uiState.update {
                it.copy(
                    categoryIdInput = "",
                    name = "",
                    description = "",
                    basePriceInput = "",
                    imageUrl = "",
                    hasSizes = false,
                    sizes = emptyList(),
                    selectedToppingIds = emptySet(),
                    loading = false,
                    message = "Thêm món và cấu hình thành công",
                    isError = false,
                    showAddForm = false,
                    currentStep = 1
                )
            }
            return true
        } catch (e: Exception) {
            if (createdProductId != null) {
                runCatching { repository.deleteProduct(createdProductId) }
            }
            _uiState.update {
                it.copy(
                    loading = false,
                    message = "Lỗi hệ thống: ${e.message ?: "Không rõ nguyên nhân"}",
                    isError = true
                )
            }
            return false
        }
    }

    suspend fun updateProduct(): Boolean {
        val state = _uiState.value
        val productId = state.editingProductId ?: return false
        val categoryId = state.categoryIdInput.toLongOrNull()
        val basePrice = state.basePriceInput.toDoubleOrNull()

        if (categoryId == null) {
            _uiState.update { it.copy(message = "Vui lòng chọn danh mục", isError = true) }
            return false
        }

        if (basePrice == null || basePrice <= 0.0) {
            _uiState.update { it.copy(message = "Giá cơ bản phải lớn hơn 0", isError = true) }
            return false
        }

        if (state.hasSizes && state.sizes.isEmpty()) {
            _uiState.update { it.copy(message = "Vui lòng thêm ít nhất một size", isError = true) }
            return false
        }

        val invalidSize = state.sizes.firstOrNull { size ->
            size.name.isBlank() || size.price.toDoubleOrNull() == null || size.price.toDoubleOrNull()!! < 0.0
        }
        if (state.hasSizes && invalidSize != null) {
            _uiState.update {
                it.copy(
                    message = "Size '${invalidSize.name.ifBlank { "(trống)" }}' có giá không hợp lệ",
                    isError = true
                )
            }
            return false
        }

        try {
            _uiState.update { it.copy(loading = true, message = null, isError = false) }

            val request = UpdateProductRequest(
                categoryId = categoryId,
                name = state.name.trim(),
                description = state.description.trim(),
                basePrice = basePrice,
                imageUrl = state.imageUrl.ifBlank { "default-image-url" },
                isCustomizable = if (state.hasSizes || state.selectedToppingIds.isNotEmpty()) 1 else 0
            )

            val productResponse = repository.updateProduct(productId, request)
            if (!productResponse.isSuccessful) {
                val serverError = productResponse.errorBody()?.string().orEmpty()
                throw IllegalStateException("Cập nhật sản phẩm thất bại (${productResponse.code()}): ${if (serverError.isNotBlank()) serverError else "Không rõ"}")
            }

            _uiState.update {
                it.copy(
                    loading = false,
                    message = "Cập nhật sản phẩm thành công",
                    isError = false,
                    showAddForm = false,
                    editingProductId = null,
                    currentStep = 1
                )
            }
            resetFormData()
            return true
        } catch (e: Exception) {
            _uiState.update {
                it.copy(
                    loading = false,
                    message = "Lỗi cập nhật sản phẩm: ${e.message ?: "Không rõ nguyên nhân"}",
                    isError = true
                )
            }
            return false
        }
    }
}
