package com.fastdash.app.viewmodel

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fastdash.app.data.model.response.ApiErrorResponse
import com.fastdash.app.data.model.response.LoginResponse
import com.fastdash.app.data.repository.AuthRepository
import com.google.gson.Gson
import kotlinx.coroutines.launch
import retrofit2.Response

class LoginViewModel(
    context: Context
) : ViewModel() {

    private val repository = AuthRepository(context)
    private val gson = Gson()

    val loginResult = MutableLiveData<LoginResponse?>()
    val errorMessage = MutableLiveData<String?>()
    val loading = MutableLiveData(false)

    fun login(email: String, password: String) {
        if (loading.value == true) return

        if (email.isBlank() || password.isBlank()) {
            errorMessage.value = "Vui lòng nhập đầy đủ email và mật khẩu"
            return
        }

        viewModelScope.launch {
            loading.value = true
            try {
                val response = repository.login(email, password)
                handleLoginResponse(response, "Đăng nhập thất bại")
            } catch (e: Exception) {
                errorMessage.value = e.message ?: "Có lỗi xảy ra"
            } finally {
                loading.value = false
            }
        }
    }

    fun googleLogin(idToken: String) {
        if (loading.value == true) return
        if (idToken.isBlank()) {
            errorMessage.value = "Google idToken is required"
            return
        }

        viewModelScope.launch {
            loading.value = true
            try {
                val response = repository.googleLogin(idToken)
                handleLoginResponse(response, "Đăng nhập Google thất bại")
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

    private fun handleLoginResponse(response: Response<LoginResponse>, fallbackPrefix: String) {
        if (response.isSuccessful && response.body() != null) {
            loginResult.value = response.body()
            return
        }

        val body = runCatching { response.errorBody()?.string().orEmpty() }.getOrDefault("")
        val apiError = runCatching { gson.fromJson(body, ApiErrorResponse::class.java) }.getOrNull()
        errorMessage.value = apiError?.message?.takeIf { it.isNotBlank() }
            ?: "$fallbackPrefix: ${response.code()}"
    }
}
