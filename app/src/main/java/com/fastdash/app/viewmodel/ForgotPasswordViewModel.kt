package com.fastdash.app.viewmodel

import android.content.Context
import android.util.Patterns
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fastdash.app.data.model.response.ApiErrorResponse
import com.fastdash.app.data.model.response.BasicMessageResponse
import com.fastdash.app.data.repository.AuthRepository
import com.google.gson.Gson
import kotlinx.coroutines.launch
import retrofit2.Response

class ForgotPasswordViewModel(
    context: Context
) : ViewModel() {

    private val repository = AuthRepository(context)
    private val gson = Gson()

    val loading = MutableLiveData(false)
    val successMessage = MutableLiveData<String?>(null)
    val errorMessage = MutableLiveData<String?>(null)
    val requestOtpSuccess = MutableLiveData<Boolean?>(null)
    val verifyOtpSuccess = MutableLiveData<Boolean?>(null)
    val resetPasswordSuccess = MutableLiveData<Boolean?>(null)

    fun requestOtp(email: String) {
        if (loading.value == true) return

        val normalizedEmail = email.trim().lowercase()
        if (!Patterns.EMAIL_ADDRESS.matcher(normalizedEmail).matches()) {
            errorMessage.value = "Vui lòng nhập email hợp lệ."
            return
        }

        viewModelScope.launch {
            loading.value = true
            try {
                val response = repository.forgotPassword(normalizedEmail)
                handleMessageResponse(
                    response = response,
                    success = {
                        successMessage.value = "Nếu email tồn tại, mã OTP đặt lại mật khẩu đã được gửi."
                        requestOtpSuccess.value = true
                    },
                    fallbackError = "Không thể gửi mã OTP lúc này."
                )
            } catch (_: Exception) {
                errorMessage.value = "Không thể kết nối đến máy chủ."
            } finally {
                loading.value = false
            }
        }
    }

    fun verifyOtp(email: String, code: String) {
        if (loading.value == true) return

        val normalizedEmail = email.trim().lowercase()
        val normalizedCode = code.trim()
        if (!Patterns.EMAIL_ADDRESS.matcher(normalizedEmail).matches()) {
            errorMessage.value = "Email không hợp lệ."
            return
        }
        if (!normalizedCode.matches(Regex("^\\d{6}$"))) {
            errorMessage.value = "Mã OTP phải gồm đúng 6 chữ số."
            return
        }

        viewModelScope.launch {
            loading.value = true
            try {
                val response = repository.verifyResetCode(normalizedEmail, normalizedCode)
                handleMessageResponse(
                    response = response,
                    success = {
                        successMessage.value = "Mã OTP hợp lệ."
                        verifyOtpSuccess.value = true
                    },
                    fallbackError = "Không thể xác minh mã OTP."
                )
            } catch (_: Exception) {
                errorMessage.value = "Không thể kết nối đến máy chủ."
            } finally {
                loading.value = false
            }
        }
    }

    fun resetPassword(email: String, code: String, newPassword: String) {
        if (loading.value == true) return

        val normalizedEmail = email.trim().lowercase()
        val normalizedCode = code.trim()
        if (!Patterns.EMAIL_ADDRESS.matcher(normalizedEmail).matches()) {
            errorMessage.value = "Email không hợp lệ."
            return
        }
        if (!normalizedCode.matches(Regex("^\\d{6}$"))) {
            errorMessage.value = "Mã OTP phải gồm đúng 6 chữ số."
            return
        }
        if (newPassword.length !in 6..72) {
            errorMessage.value = "Mật khẩu mới phải từ 6 đến 72 ký tự."
            return
        }

        viewModelScope.launch {
            loading.value = true
            try {
                val response = repository.resetPassword(normalizedEmail, normalizedCode, newPassword)
                handleMessageResponse(
                    response = response,
                    success = {
                        successMessage.value = "Đặt lại mật khẩu thành công."
                        resetPasswordSuccess.value = true
                    },
                    fallbackError = "Không thể đặt lại mật khẩu."
                )
            } catch (_: Exception) {
                errorMessage.value = "Không thể kết nối đến máy chủ."
            } finally {
                loading.value = false
            }
        }
    }

    fun consumeSuccessMessage() {
        successMessage.value = null
    }

    fun consumeErrorMessage() {
        errorMessage.value = null
    }

    fun consumeRequestOtpSuccess() {
        requestOtpSuccess.value = null
    }

    fun consumeVerifyOtpSuccess() {
        verifyOtpSuccess.value = null
    }

    fun consumeResetPasswordSuccess() {
        resetPasswordSuccess.value = null
    }

    private fun handleMessageResponse(
        response: Response<BasicMessageResponse>,
        success: () -> Unit,
        fallbackError: String
    ) {
        if (response.isSuccessful) {
            success()
            return
        }

        val body = runCatching { response.errorBody()?.string().orEmpty() }.getOrDefault("")
        val apiError = runCatching { gson.fromJson(body, ApiErrorResponse::class.java) }.getOrNull()
        errorMessage.value = translateBackendMessage(apiError?.message).ifBlank {
            "$fallbackError (${response.code()})"
        }
    }

    private fun translateBackendMessage(message: String?): String {
        val normalized = message.orEmpty().trim()
        if (normalized.isBlank()) return ""

        return when {
            normalized.equals("Reset code is incorrect", ignoreCase = true) ->
                "Mã OTP không chính xác."
            normalized.equals("Reset code has expired", ignoreCase = true) ->
                "Mã OTP đã hết hạn."
            normalized.equals("Reset code is invalid or expired", ignoreCase = true) ->
                "Mã OTP không hợp lệ hoặc đã hết hạn."
            normalized.equals("Too many invalid attempts. Please request a new code", ignoreCase = true) ->
                "Bạn đã nhập sai quá số lần cho phép. Vui lòng yêu cầu mã OTP mới."
            normalized.startsWith("Please wait ", ignoreCase = true) &&
                normalized.endsWith(" seconds before requesting another code", ignoreCase = true) -> {
                val seconds = Regex("(\\d+)").find(normalized)?.groupValues?.getOrNull(1) ?: "0"
                "Vui lòng chờ $seconds giây trước khi yêu cầu gửi lại mã OTP."
            }
            normalized.equals("If the email exists, a password reset code has been sent", ignoreCase = true) ->
                "Nếu email tồn tại, mã OTP đặt lại mật khẩu đã được gửi."
            normalized.equals("Reset code is valid", ignoreCase = true) ->
                "Mã OTP hợp lệ."
            normalized.equals("Password has been reset successfully", ignoreCase = true) ->
                "Đặt lại mật khẩu thành công."
            else -> normalized
        }
    }
}
