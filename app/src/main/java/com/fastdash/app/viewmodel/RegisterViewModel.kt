package com.fastdash.app.viewmodel

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fastdash.app.data.repository.AuthRepository
import kotlinx.coroutines.launch

class RegisterViewModel(
    context: Context
) : ViewModel() {

    private val repository = AuthRepository(context)

    val loading = MutableLiveData(false)
    val registerSuccess = MutableLiveData<Boolean?>(null)
    val errorMessage = MutableLiveData<String?>(null)

    fun register(
        fullName: String,
        email: String,
        phone: String,
        password: String
    ) {
        if (loading.value == true) return

        if (fullName.isBlank() || email.isBlank() || password.isBlank()) {
            errorMessage.value = "Vui lòng nhập đầy đủ họ tên, email và mật khẩu."
            return
        }

        viewModelScope.launch {
            loading.value = true
            try {
                val response = repository.register(
                    fullName = fullName.trim(),
                    email = email.trim(),
                    phone = phone.trim(),
                    password = password
                )
                if (response.isSuccessful) {
                    registerSuccess.value = true
                } else {
                    errorMessage.value = response.body()?.message
                        ?: "Đăng ký thất bại: ${response.code()}"
                }
            } catch (e: Exception) {
                errorMessage.value = e.message ?: "Không thể kết nối máy chủ."
            } finally {
                loading.value = false
            }
        }
    }

    fun consumeSuccess() {
        registerSuccess.value = null
    }

    fun consumeError() {
        errorMessage.value = null
    }
}
