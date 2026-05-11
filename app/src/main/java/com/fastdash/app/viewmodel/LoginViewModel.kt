package com.fastdash.app.viewmodel

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fastdash.app.data.model.response.LoginResponse
import com.fastdash.app.data.repository.AuthRepository
import kotlinx.coroutines.launch

class LoginViewModel(
    context: Context
) : ViewModel() {

    private val repository = AuthRepository(context)

    val loginResult = MutableLiveData<LoginResponse?>()
    val errorMessage = MutableLiveData<String?>()
    val loading = MutableLiveData(false)

    fun login(email: String, password: String) {
        if (loading.value == true) return

        if (email.isBlank() || password.isBlank()) {
            errorMessage.value = "Vui long nhap day du email va mat khau"
            return
        }

        viewModelScope.launch {
            loading.value = true
            try {
                val response = repository.login(email, password)

                if (response.isSuccessful && response.body() != null) {
                    loginResult.value = response.body()
                } else {
                    errorMessage.value = "Đăng nhập thất bại: ${response.code()}"
                }
            } catch (e: Exception) {
                errorMessage.value = e.message ?: "Có lỗi xảy ra"
            } finally {
                loading.value = false
            }
        }
    }

    fun consumeLoginResult() {
        loginResult.value = null
    }

    fun consumeErrorMessage() {
        errorMessage.value = null
    }
}