package com.example.fastdash_android.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fastdash_android.data.model.response.LoginResponse
import com.example.fastdash_android.data.repository.AuthRepository
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