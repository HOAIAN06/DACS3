package com.fastdash.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fastdash.app.data.model.request.AdminProductRequest
import com.fastdash.app.data.remote.api.AdminCategoryResponse
import com.fastdash.app.data.remote.api.AdminProductResponse
import com.fastdash.app.data.remote.api.AdminToppingResponse
import com.fastdash.app.data.remote.api.CreateSizeRequest
import com.fastdash.app.data.remote.api.UpdateProductRequest
import com.fastdash.app.data.repository.AdminCategoryRepository
import com.fastdash.app.data.repository.AdminProductRepository
import com.fastdash.app.data.repository.AdminSizeRepository
import com.fastdash.app.data.repository.AdminToppingRepository
import com.fastdash.app.data.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import retrofit2.Response

data class ProductSizeInput(
    val name: String = "",
    val price: String = ""
)

data class AdminProductUiState(
    val categoryIdInput: String = "",
    val name: String = "",
    val description: String = "",
    val basePriceInput: String = "",
    val manualBasePriceInput: String = "",
    val imageUrl: String = "",
    val hasSizes: Boolean = false,
    val sizes: List<ProductSizeInput> = emptyList(),
    val selectedToppingIds: Set<Long> = emptySet(),
    val categories: List<AdminCategoryResponse> = emptyList(),
    val allToppingsList: List<AdminToppingResponse> = emptyList(),
    val loading: Boolean = false,
    val message: String? = null,
    val isError: Boolean = false,
    val showAddForm: Boolean = false,
    val editingProductId: Long? = null,
    val currentStep: Int = 1
) {
    private val manualBasePriceValue = manualBasePriceInput.toDoubleOrNull()

    val canSubmit: Boolean
        get() = !loading &&
            (imageUrl.isNotBlank() || editingProductId != null) &&
            name.isNotBlank() &&
            description.isNotBlank() &&
            categoryIdInput.toLongOrNull() != null &&
            if (hasSizes) {
                sizes.isNotEmpty() && sizes.all { size ->
                    size.name.isNotBlank() &&
                        size.price.toDoubleOrNull() != null &&
                        size.price.toDoubleOrNull()!! > 0.0
                }
            } else {
                manualBasePriceValue != null && manualBasePriceValue > 0.0
            }

    val canGoNext: Boolean
        get() = when (currentStep) {
            1 -> imageUrl.isNotBlank() && name.isNotBlank() && description.isNotBlank()
            2 -> categoryIdInput.toLongOrNull() != null
            3 -> true
            else -> false
        }
}

class AdminProductViewModel(
    private val repository: AdminProductRepository,
    private val categoryRepository: AdminCategoryRepository,
    private val toppingRepository: AdminToppingRepository,
    private val sizeRepository: AdminSizeRepository,
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminProductUiState())
    val uiState: StateFlow<AdminProductUiState> = _uiState.asStateFlow()

    init {
        loadFormData()
    }

    private fun List<ProductSizeInput>.deriveBasePriceInput(): String {
        return mapNotNull { it.price.toDoubleOrNull() }
            .filter { it > 0.0 }
            .minOrNull()
            ?.toString()
            .orEmpty()
    }

    fun loadFormData() {
        viewModelScope.launch {
            try {
                val categoriesResponse = categoryRepository.getCategories()
                if (!categoriesResponse.isSuccessful) {
                    throw IllegalStateException(buildApiError("Tai danh muc", categoriesResponse))
                }

                val toppingsResponse = toppingRepository.getToppings()
                if (!toppingsResponse.isSuccessful) {
                    throw IllegalStateException(buildApiError("Tai topping", toppingsResponse))
                }

                _uiState.update {
                    it.copy(
                        categories = categoriesResponse.body().orEmpty().filter { category -> category.id > 0L }.distinctBy { category -> category.id },
                        allToppingsList = toppingsResponse.body().orEmpty().filter { topping -> topping.id > 0L }.distinctBy { topping -> topping.id }
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(message = "Loi tai du lieu form: ${e.message}", isError = true)
                }
            }
        }
    }

    fun setShowAddForm(show: Boolean) {
        if (show) {
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
                manualBasePriceInput = product.basePrice.toString(),
                imageUrl = product.imageUrl.orEmpty(),
                hasSizes = false,
                sizes = emptyList(),
                selectedToppingIds = emptySet(),
                currentStep = 1,
                message = null,
                isError = false
            )
        }

        loadFormData()

        viewModelScope.launch {
            try {
                val detailResponse = productRepository.getProductById(product.id)
                val detail = if (detailResponse.isSuccessful) detailResponse.body() else null

                val sizesResponse = productRepository.getProductSizes(product.id)
                val existingSizes = if (sizesResponse.isSuccessful) {
                    sizesResponse.body().orEmpty()
                } else {
                    emptyList()
                }

                val toppingsResponse = productRepository.getProductToppings(product.id)
                val existingToppingIds = if (toppingsResponse.isSuccessful) {
                    toppingsResponse.body().orEmpty().map { topping -> topping.id }.toSet()
                } else {
                    emptySet()
                }

                val mappedSizes = existingSizes.map { size ->
                    ProductSizeInput(
                        name = size.sizeName.orEmpty(),
                        price = size.price.toString()
                    )
                }

                _uiState.update { state ->
                    val resolvedCategoryId = detail?.categoryId ?: product.categoryId
                    val resolvedName = detail?.name?.takeIf { it.isNotBlank() } ?: product.name.orEmpty()
                    val resolvedDescription = detail?.description?.takeIf { it.isNotBlank() } ?: product.description.orEmpty()
                    val resolvedImageUrl = detail?.imageUrl?.takeIf { it.isNotBlank() } ?: product.imageUrl.orEmpty()
                    val resolvedBasePrice = detail?.basePrice ?: product.basePrice
                    val hasSizes = mappedSizes.isNotEmpty()

                    state.copy(
                        categoryIdInput = resolvedCategoryId.toString(),
                        name = resolvedName,
                        description = resolvedDescription,
                        imageUrl = resolvedImageUrl,
                        manualBasePriceInput = resolvedBasePrice.toString(),
                        basePriceInput = if (hasSizes) mappedSizes.deriveBasePriceInput() else resolvedBasePrice.toString(),
                        hasSizes = hasSizes,
                        sizes = mappedSizes,
                        selectedToppingIds = existingToppingIds
                    )
                }
            } catch (_: Exception) {
                // Keep the basic prefilled values if loading extra config fails.
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
                manualBasePriceInput = "",
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
        _uiState.update { state ->
            if (state.currentStep < 3) state.copy(currentStep = state.currentStep + 1) else state
        }
    }

    fun previousStep() {
        _uiState.update { state ->
            if (state.currentStep > 1) state.copy(currentStep = state.currentStep - 1) else state
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
        _uiState.update {
            it.copy(
                manualBasePriceInput = value,
                basePriceInput = if (it.hasSizes) it.basePriceInput else value,
                message = null,
                isError = false
            )
        }
    }

    fun onSizeChanged(index: Int, name: String, price: String) {
        _uiState.update { state ->
            if (index !in state.sizes.indices) return@update state
            val updatedSizes = state.sizes.toMutableList()
            updatedSizes[index] = ProductSizeInput(name = name, price = price)
            state.copy(
                sizes = updatedSizes,
                basePriceInput = if (state.hasSizes) updatedSizes.deriveBasePriceInput() else state.basePriceInput,
                message = null,
                isError = false
            )
        }
    }

    fun addSizeInput() {
        _uiState.update { state ->
            val updatedSizes = state.sizes + ProductSizeInput()
            state.copy(
                hasSizes = true,
                sizes = updatedSizes,
                basePriceInput = updatedSizes.deriveBasePriceInput(),
                message = null,
                isError = false
            )
        }
    }

    fun removeSizeInput(index: Int) {
        _uiState.update { state ->
            if (index !in state.sizes.indices) return@update state
            val updatedSizes = state.sizes.filterIndexed { i, _ -> i != index }
            state.copy(
                sizes = updatedSizes,
                basePriceInput = if (updatedSizes.isNotEmpty()) updatedSizes.deriveBasePriceInput() else state.manualBasePriceInput,
                message = null,
                isError = false
            )
        }
    }

    fun setHasSizes(enabled: Boolean) {
        _uiState.update { state ->
            if (!enabled) {
                state.copy(
                    hasSizes = false,
                    sizes = emptyList(),
                    basePriceInput = state.manualBasePriceInput,
                    message = null,
                    isError = false
                )
            } else {
                val seededSizes = if (state.sizes.isEmpty()) {
                    listOf(ProductSizeInput())
                } else {
                    state.sizes
                }
                state.copy(
                    hasSizes = true,
                    sizes = seededSizes,
                    basePriceInput = seededSizes.deriveBasePriceInput(),
                    message = null,
                    isError = false
                )
            }
        }
    }

    fun toggleToppingSelection(toppingId: Long) {
        _uiState.update { state ->
            val updatedIds = state.selectedToppingIds.toMutableSet()
            if (updatedIds.contains(toppingId)) {
                updatedIds.remove(toppingId)
            } else {
                updatedIds.add(toppingId)
            }
            state.copy(selectedToppingIds = updatedIds, message = null, isError = false)
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null, isError = false) }
    }

    fun setMessage(message: String, isError: Boolean = false) {
        _uiState.update { it.copy(message = message, isError = isError) }
    }

    fun toggleProductStatus(productId: Long, currentStatus: Int, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(loading = true, message = null, isError = false) }
                val newStatus = if (currentStatus == 1) 0 else 1
                val response = repository.updateProductStatus(productId, newStatus)
                if (!response.isSuccessful) {
                    throw IllegalStateException(buildApiError("Cap nhat trang thai mon an", response))
                }
                _uiState.update { it.copy(loading = false, message = "Da cap nhat trang thai mon an", isError = false) }
                onSuccess()
            } catch (e: Exception) {
                _uiState.update { it.copy(loading = false, message = "Loi ket noi: ${e.message}", isError = true) }
            }
        }
    }

    suspend fun deleteProduct(productId: Long): Boolean {
        return try {
            _uiState.update { it.copy(loading = true, message = null, isError = false) }

            val toppingsResponse = productRepository.getProductToppings(productId)
            if (!toppingsResponse.isSuccessful) {
                throw IllegalStateException(buildApiError("Tai topping cua san pham", toppingsResponse))
            }
            toppingsResponse.body().orEmpty().forEach { topping ->
                val removeResponse = repository.removeToppingFromProduct(productId, topping.id)
                if (!removeResponse.isSuccessful) {
                    throw IllegalStateException(buildApiError("Xoa topping khoi san pham", removeResponse))
                }
            }

            val sizesResponse = sizeRepository.getSizesByProduct(productId)
            if (!sizesResponse.isSuccessful) {
                throw IllegalStateException(buildApiError("Tai size cua san pham", sizesResponse))
            }
            sizesResponse.body().orEmpty().forEach { size ->
                val deleteResponse = sizeRepository.deleteSize(size.id)
                if (!deleteResponse.isSuccessful) {
                    throw IllegalStateException(buildApiError("Xoa size '${size.sizeName}'", deleteResponse))
                }
            }

            val deleteProductResponse = repository.deleteProduct(productId)
            if (!deleteProductResponse.isSuccessful) {
                throw IllegalStateException(buildApiError("Xoa san pham", deleteProductResponse))
            }

            _uiState.update { it.copy(loading = false, message = "Xoa san pham thanh cong", isError = false) }
            true
        } catch (e: Exception) {
            _uiState.update { it.copy(loading = false, message = "Xoa san pham loi: ${e.message}", isError = true) }
            false
        }
    }

    fun uploadImage(file: MultipartBody.Part) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(loading = true, message = null, isError = false) }
                val response = repository.uploadImage(file)
                if (!response.isSuccessful) {
                    throw IllegalStateException(buildApiError("Upload anh", response))
                }

                val url = response.body()?.resolvedImageUrl.orEmpty()
                if (url.isBlank()) {
                    throw IllegalStateException("Upload thanh cong nhung backend chua tra ve imageUrl")
                }

                _uiState.update {
                    it.copy(
                        imageUrl = url,
                        loading = false,
                        message = "Upload anh thanh cong",
                        isError = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        loading = false,
                        message = "Upload loi: ${e.message}",
                        isError = true
                    )
                }
            }
        }
    }

    suspend fun createProduct(): Boolean {
        val state = _uiState.value
        val categoryId = state.categoryIdInput.toLongOrNull()
        val manualBasePrice = state.manualBasePriceInput.toDoubleOrNull()
        val resolvedBasePrice = if (state.hasSizes) {
            state.sizes.mapNotNull { it.price.toDoubleOrNull() }.filter { it > 0.0 }.minOrNull()
        } else {
            manualBasePrice
        }

        if (categoryId == null) {
            _uiState.update { it.copy(message = "Vui long chon danh muc", isError = true) }
            return false
        }

        if (!validatePricing(state, resolvedBasePrice)) {
            return false
        }

        var createdProductId: Long? = null

        return try {
            _uiState.update { it.copy(loading = true, message = null, isError = false) }

            val productRequest = AdminProductRequest(
                categoryId = categoryId,
                name = state.name.trim(),
                description = state.description.trim(),
                basePrice = resolvedBasePrice!!,
                imageUrl = state.imageUrl,
                isCustomizable = if (state.hasSizes || state.selectedToppingIds.isNotEmpty()) 1 else 0,
                status = 1
            )

            val createProductResponse = repository.createProduct(productRequest)
            if (!createProductResponse.isSuccessful) {
                throw IllegalStateException(buildApiError("Tao san pham", createProductResponse))
            }

            val createdProduct = createProductResponse.body()
                ?: throw IllegalStateException("Tao san pham that bai: body rong")
            createdProductId = createdProduct.id

            recreateSizes(createdProductId, state)
            recreateToppings(createdProductId, state.selectedToppingIds)

            _uiState.update {
                it.copy(
                    loading = false,
                    message = "Them mon va cau hinh thanh cong",
                    isError = false,
                    showAddForm = false,
                    editingProductId = null,
                    currentStep = 1,
                    categoryIdInput = "",
                    name = "",
                    description = "",
                    basePriceInput = "",
                    manualBasePriceInput = "",
                    imageUrl = "",
                    hasSizes = false,
                    sizes = emptyList(),
                    selectedToppingIds = emptySet()
                )
            }
            true
        } catch (e: Exception) {
            if (createdProductId != null) {
                runCatching { repository.deleteProduct(createdProductId) }
            }
            _uiState.update {
                it.copy(
                    loading = false,
                    message = "Loi he thong: ${e.message}",
                    isError = true
                )
            }
            false
        }
    }

    suspend fun updateProduct(): Boolean {
        return updateProductFixed()
    }

    suspend fun updateProductFixed(): Boolean {
        val state = _uiState.value
        val productId = state.editingProductId ?: return false
        val categoryId = state.categoryIdInput.toLongOrNull()
        val manualBasePrice = state.manualBasePriceInput.toDoubleOrNull()
        val resolvedBasePrice = if (state.hasSizes) {
            state.sizes.mapNotNull { it.price.toDoubleOrNull() }.filter { it > 0.0 }.minOrNull()
        } else {
            manualBasePrice
        }

        if (categoryId == null) {
            _uiState.update { it.copy(message = "Vui long chon danh muc", isError = true) }
            return false
        }

        if (!validatePricing(state, resolvedBasePrice)) {
            return false
        }

        return try {
            _uiState.update { it.copy(loading = true, message = null, isError = false) }

            val updateRequest = UpdateProductRequest(
                categoryId = categoryId,
                name = state.name.trim(),
                description = state.description.trim(),
                basePrice = resolvedBasePrice!!,
                imageUrl = state.imageUrl.ifBlank { "default-image-url" },
                isCustomizable = if (state.hasSizes || state.selectedToppingIds.isNotEmpty()) 1 else 0
            )

            val updateProductResponse = repository.updateProduct(productId, updateRequest)
            if (!updateProductResponse.isSuccessful) {
                throw IllegalStateException(buildApiError("Cap nhat san pham", updateProductResponse))
            }

            clearExistingSizes(productId)
            clearExistingToppings(productId)
            recreateSizes(productId, state)
            recreateToppings(productId, state.selectedToppingIds)

            _uiState.update {
                it.copy(
                    loading = false,
                    message = "Cap nhat san pham thanh cong",
                    isError = false,
                    showAddForm = false,
                    editingProductId = null,
                    currentStep = 1
                )
            }
            resetFormData()
            true
        } catch (e: Exception) {
            _uiState.update {
                it.copy(
                    loading = false,
                    message = "Loi cap nhat san pham: ${e.message}",
                    isError = true
                )
            }
            false
        }
    }

    private fun validatePricing(state: AdminProductUiState, resolvedBasePrice: Double?): Boolean {
        if (state.hasSizes && state.sizes.isEmpty()) {
            _uiState.update { it.copy(message = "Vui long them it nhat mot size", isError = true) }
            return false
        }

        val invalidSize = state.sizes.firstOrNull { size ->
            size.name.isBlank() ||
                size.price.toDoubleOrNull() == null ||
                size.price.toDoubleOrNull()!! <= 0.0
        }
        if (state.hasSizes && invalidSize != null) {
            _uiState.update {
                it.copy(
                    message = "Size '${invalidSize.name.ifBlank { "(trong)" }}' co gia khong hop le",
                    isError = true
                )
            }
            return false
        }

        if (!state.hasSizes && (state.manualBasePriceInput.toDoubleOrNull() == null || state.manualBasePriceInput.toDoubleOrNull()!! <= 0.0)) {
            _uiState.update { it.copy(message = "Gia ban phai lon hon 0", isError = true) }
            return false
        }

        if (resolvedBasePrice == null || resolvedBasePrice <= 0.0) {
            _uiState.update { it.copy(message = "Gia mon khong hop le", isError = true) }
            return false
        }

        return true
    }

    private suspend fun clearExistingSizes(productId: Long) {
        val existingSizesResponse = sizeRepository.getSizesByProduct(productId)
        if (!existingSizesResponse.isSuccessful) {
            throw IllegalStateException(buildApiError("Tai size hien tai cua san pham", existingSizesResponse))
        }

        existingSizesResponse.body().orEmpty().forEach { size ->
            val deleteResponse = sizeRepository.deleteSize(size.id)
            if (!deleteResponse.isSuccessful) {
                throw IllegalStateException(buildApiError("Xoa size '${size.sizeName}'", deleteResponse))
            }
        }
    }

    private suspend fun recreateSizes(productId: Long, state: AdminProductUiState) {
        if (!state.hasSizes) return

        state.sizes.forEach { input ->
            val cleanName = input.name.trim()
            val cleanPrice = input.price.toDoubleOrNull()
                ?: throw IllegalArgumentException("Gia size '$cleanName' khong hop le")

            val createResponse = sizeRepository.createSize(
                productId = productId,
                request = CreateSizeRequest(
                    sizeName = cleanName,
                    price = cleanPrice
                )
            )
            if (!createResponse.isSuccessful) {
                throw IllegalStateException(buildApiError("Tao size '$cleanName'", createResponse))
            }
        }
    }

    private suspend fun clearExistingToppings(productId: Long) {
        val existingToppingsResponse = productRepository.getProductToppings(productId)
        if (!existingToppingsResponse.isSuccessful) {
            throw IllegalStateException(buildApiError("Tai topping hien tai cua san pham", existingToppingsResponse))
        }

        existingToppingsResponse.body().orEmpty().forEach { topping ->
            val removeResponse = repository.removeToppingFromProduct(productId, topping.id)
            if (!removeResponse.isSuccessful) {
                throw IllegalStateException(buildApiError("Go topping ID=${topping.id}", removeResponse))
            }
        }
    }

    private suspend fun recreateToppings(productId: Long, toppingIds: Set<Long>) {
        toppingIds.forEach { toppingId ->
            val addResponse = repository.addToppingToProduct(productId, toppingId)
            if (!addResponse.isSuccessful) {
                throw IllegalStateException(buildApiError("Them topping ID=$toppingId", addResponse))
            }
        }
    }

    private fun buildApiError(action: String, response: Response<*>): String {
        val serverError = response.errorBody()?.string().orEmpty()
        return "$action that bai (${response.code()})" + if (serverError.isNotBlank()) ": $serverError" else ""
    }
}
