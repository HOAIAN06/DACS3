package com.fastdash.app.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fastdash.app.data.model.response.LoginResponse
import com.fastdash.app.data.repository.AuthRepository
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {

    private val repository = AuthRepository()

    val loginResult = MutableLiveData<LoginResponse?>()
    val errorMessage = MutableLiveData<String>()
    val loading = MutableLiveData<Boolean>()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            loading.value = true
            try {
                val response = repository.login(email, password)

                if (response.isSuccessful && response.body() != null) {
                    loginResult.value = response.body()
                } else {
                    errorMessage.value = "Đăng nhập thất bại"
                }
            } catch (e: Exception) {
                errorMessage.value = e.message ?: "Có lỗi xảy ra"
            } finally {
                loading.value = false
            }
        }
    }
}