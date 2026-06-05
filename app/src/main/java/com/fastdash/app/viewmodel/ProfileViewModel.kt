package com.fastdash.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fastdash.app.data.model.request.UpdateProfileRequest
import com.fastdash.app.data.model.response.ApiErrorResponse
import com.fastdash.app.data.model.response.UserResponse
import com.fastdash.app.data.repository.UserRepository
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(private val repository: UserRepository) : ViewModel() {
    private val gson = Gson()

    private val _user = MutableStateFlow<UserResponse?>(null)
    val user: StateFlow<UserResponse?> = _user.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    fun loadProfile(force: Boolean = false) {
        if (_isLoading.value || (!force && _user.value != null)) return

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = repository.getMe()
                if (response.isSuccessful) {
                    _user.value = response.body()
                } else {
                    _errorMessage.value = "Không thể tải thông tin cá nhân"
                }
            } catch (_: Exception) {
                _errorMessage.value = "Không thể tải thông tin cá nhân"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateProfile(fullName: String, email: String, phone: String) {
        if (_isSaving.value) return

        viewModelScope.launch {
            _isSaving.value = true
            try {
                val response = repository.updateMe(
                    UpdateProfileRequest(
                        fullName = fullName.trim(),
                        email = email.trim().ifBlank { null },
                        phone = phone.trim()
                    )
                )

                if (response.isSuccessful) {
                    _user.value = response.body() ?: _user.value?.copy(
                        fullName = fullName.trim(),
                        email = email.trim(),
                        phone = phone.trim()
                    )
                    _successMessage.value = "Cập nhật thông tin thành công"
                } else {
                    _errorMessage.value = parseApiError(response.errorBody()?.string()) ?: "Không thể cập nhật thông tin. Vui lòng thử lại"
                }
            } catch (_: Exception) {
                _errorMessage.value = "Không thể cập nhật thông tin. Vui lòng thử lại"
            } finally {
                _isSaving.value = false
            }
        }
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    fun clearSuccessMessage() {
        _successMessage.value = null
    }

    private fun parseApiError(raw: String?): String? {
        val body = raw?.takeIf { it.isNotBlank() } ?: return null
        return runCatching { gson.fromJson(body, ApiErrorResponse::class.java) }
            .getOrNull()
            ?.message
            ?.takeIf { it.isNotBlank() }
    }
}
