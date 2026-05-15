package com.fastdash.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.fastdash.app.data.repository.AdminCategoryRepository
import com.fastdash.app.data.repository.AdminProductRepository
import com.fastdash.app.data.repository.AdminSizeRepository
import com.fastdash.app.data.repository.AdminToppingRepository
import com.fastdash.app.data.repository.ProductRepository

class AdminProductViewModelFactory(
    private val repository: AdminProductRepository,
    private val categoryRepository: AdminCategoryRepository,
    private val toppingRepository: AdminToppingRepository,
    private val sizeRepository: AdminSizeRepository,
    private val productRepository: ProductRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AdminProductViewModel(repository, categoryRepository, toppingRepository, sizeRepository, productRepository) as T
    }
}
