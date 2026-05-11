package com.fastdash.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.fastdash.app.data.repository.AdminCategoryRepository
import com.fastdash.app.data.repository.AdminProductRepository
import com.fastdash.app.data.repository.AdminSizeRepository
import com.fastdash.app.data.repository.AdminToppingRepository

class AdminProductViewModelFactory(
    private val repository: AdminProductRepository,
    private val categoryRepository: AdminCategoryRepository,
    private val toppingRepository: AdminToppingRepository,
    private val sizeRepository: AdminSizeRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AdminProductViewModel(repository, categoryRepository, toppingRepository, sizeRepository) as T
    }
}
