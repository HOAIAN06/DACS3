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

		if (fullName.isBlank() || email.isBlank() || phone.isBlank() || password.isBlank()) {
			errorMessage.value = "Vui long nhap day du thong tin"
			return
		}

		viewModelScope.launch {
			loading.value = true
			try {
				val response = repository.register(fullName, email, phone, password)
				if (response.isSuccessful) {
					registerSuccess.value = true
				} else {
					errorMessage.value = response.body()?.message
						?: "Dang ky that bai: ${response.code()}"
				}
			} catch (e: Exception) {
				errorMessage.value = e.message ?: "Khong the ket noi may chu"
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

