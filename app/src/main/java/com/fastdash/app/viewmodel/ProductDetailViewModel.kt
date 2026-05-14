package com.fastdash.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fastdash.app.data.model.response.ProductSizeResponse
import com.fastdash.app.data.model.response.ToppingResponse
import com.fastdash.app.data.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProductDetailViewModel(private val repository: ProductRepository) : ViewModel() {
    private val _sizes = MutableStateFlow<List<ProductSizeResponse>>(emptyList())
    val sizes: StateFlow<List<ProductSizeResponse>> = _sizes.asStateFlow()

    private val _toppings = MutableStateFlow<List<ToppingResponse>>(emptyList())
    val toppings: StateFlow<List<ToppingResponse>> = _toppings.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    fun loadDetails(productId: Long) {
        viewModelScope.launch {
            _loading.value = true
            try {
                _sizes.value = emptyList()
                _toppings.value = emptyList()

                val sizesResp = repository.getProductSizes(productId)
                val toppingsResp = repository.getProductToppings(productId)
                
                if (sizesResp.isSuccessful) {
                    _sizes.value = sizesResp.body()
                        .orEmpty()
                        .filter { it.id > 0L }
                        .distinctBy { it.id }
                }
                if (toppingsResp.isSuccessful) {
                    _toppings.value = toppingsResp.body()
                        .orEmpty()
                        .filter { it.id > 0L }
                        .distinctBy { it.id }
                }
            } catch (e: Exception) {
                // Handle error
            } finally {
                _loading.value = false
            }
        }
    }
}
