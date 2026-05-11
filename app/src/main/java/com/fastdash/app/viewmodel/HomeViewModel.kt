package com.fastdash.app.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fastdash.app.data.model.response.CategoryResponse
import com.fastdash.app.data.model.response.ProductResponse
import com.fastdash.app.data.repository.CategoryRepository
import com.fastdash.app.data.repository.ProductRepository
import kotlinx.coroutines.launch

class HomeViewModel(
    context: Context
) : ViewModel() {

    private val categoryRepository = CategoryRepository(context.applicationContext)
    private val productRepository = ProductRepository(context.applicationContext)

    private val _categories = MutableLiveData<List<CategoryResponse>>(emptyList())
    val categories: LiveData<List<CategoryResponse>> = _categories

    private val _products = MutableLiveData<List<ProductResponse>>(emptyList())
    val products: LiveData<List<ProductResponse>> = _products

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    fun loadHomeData() {
        loadCategories()
        loadProducts()
    }

    fun loadCategories() {
        viewModelScope.launch {
            try {
                val response = categoryRepository.getCategories()
                if (response.isSuccessful) {
                    _categories.value = response.body().orEmpty()
                } else {
                    _errorMessage.value = response.errorBody()?.string()
                        ?: "Không tải được danh mục"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage ?: "Có lỗi xảy ra khi tải danh mục"
            }
        }
    }

    fun loadProducts(categoryId: Long? = null) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val response = productRepository.getProducts(categoryId)
                if (response.isSuccessful) {
                    _products.value = response.body().orEmpty()
                } else {
                    _products.value = emptyList()
                    _errorMessage.value = response.errorBody()?.string()
                        ?: "Không tải được sản phẩm"
                }
            } catch (e: Exception) {
                _products.value = emptyList()
                _errorMessage.value = e.localizedMessage ?: "Có lỗi xảy ra khi tải sản phẩm"
            } finally {
                _loading.value = false
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
