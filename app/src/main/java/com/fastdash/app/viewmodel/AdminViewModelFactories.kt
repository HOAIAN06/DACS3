package com.fastdash.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.fastdash.app.data.repository.AdminCategoryRepository
import com.fastdash.app.data.repository.AdminSizeRepository
import com.fastdash.app.data.repository.AdminToppingRepository

class AdminCategoryViewModelFactory(
    private val repository: AdminCategoryRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AdminCategoryViewModel(repository) as T
    }
}

class AdminSizeViewModelFactory(
    private val repository: AdminSizeRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AdminSizeViewModel(repository) as T
    }
}

class AdminToppingViewModelFactory(
    private val repository: AdminToppingRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AdminToppingViewModel(repository) as T
    }
}
