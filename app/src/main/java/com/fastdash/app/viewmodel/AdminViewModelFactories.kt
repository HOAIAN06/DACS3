package com.fastdash.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.fastdash.app.data.repository.AdminCategoryRepository
import com.fastdash.app.data.repository.AdminBranchRepository
import com.fastdash.app.data.repository.AdminCustomerRepository
import com.fastdash.app.data.repository.AdminDashboardRepository
import com.fastdash.app.data.repository.AdminOrderRepository
import com.fastdash.app.data.repository.AdminSizeRepository
import com.fastdash.app.data.repository.AdminToppingRepository

class AdminCategoryViewModelFactory(
    private val repository: AdminCategoryRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AdminCategoryViewModel(repository) as T
    }
}

class AdminBranchViewModelFactory(
    private val repository: AdminBranchRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AdminBranchViewModel(repository) as T
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

class AdminOrdersViewModelFactory(
    private val repository: AdminOrderRepository,
    private val dashboardRepository: AdminDashboardRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AdminOrdersViewModel(repository, dashboardRepository) as T
    }
}

class AdminDashboardViewModelFactory(
    private val repository: AdminDashboardRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AdminDashboardViewModel(repository) as T
    }
}

class AdminCustomerViewModelFactory(
    private val repository: AdminCustomerRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AdminCustomerViewModel(repository) as T
    }
}

class AdminOrderDetailViewModelFactory(
    private val repository: AdminOrderRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AdminOrderDetailViewModel(repository) as T
    }
}
