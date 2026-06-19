package com.fastdash.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fastdash.app.data.model.response.ApiErrorResponse
import com.fastdash.app.data.model.response.BranchResponse
import com.fastdash.app.data.remote.api.CreateBranchRequest
import com.fastdash.app.data.repository.AdminBranchRepository
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.Response
import java.time.LocalTime
import java.time.temporal.ChronoField
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder

data class AdminBranchUiState(
    val branches: List<BranchResponse> = emptyList(),
    val loading: Boolean = false,
    val message: String? = null,
    val isError: Boolean = false,
    val showForm: Boolean = false,
    val editingBranchId: Long? = null,
    val nameInput: String = "",
    val addressInput: String = "",
    val phoneInput: String = "",
    val openTimeInput: String = "",
    val closeTimeInput: String = "",
    val latitudeInput: String = "",
    val longitudeInput: String = "",
    val isActiveInput: Boolean = true,
    val formLoading: Boolean = false,
    val formMessage: String? = null,
    val formError: Boolean = false,
    val deletingBranchId: Long? = null,
    val togglingBranchId: Long? = null
)

class AdminBranchViewModel(
    private val repository: AdminBranchRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(AdminBranchUiState())
    val uiState: StateFlow<AdminBranchUiState> = _uiState.asStateFlow()

    init {
        loadBranches()
    }

    fun loadBranches() {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, message = null, isError = false) }
            try {
                val response = repository.getBranches()
                if (!response.isSuccessful) {
                    throw IllegalStateException(buildApiError("Tai danh sach chi nhanh", response))
                }
                _uiState.update {
                    it.copy(
                        branches = response.body().orEmpty().sortedByDescending { branch -> branch.status },
                        loading = false,
                        message = null,
                        isError = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        loading = false,
                        message = e.message ?: "Khong the tai danh sach chi nhanh",
                        isError = true
                    )
                }
            }
        }
    }

    fun showAddForm() {
        _uiState.update {
            it.copy(
                showForm = true,
                editingBranchId = null,
                nameInput = "",
                addressInput = "",
                phoneInput = "",
                openTimeInput = "",
                closeTimeInput = "",
                latitudeInput = "",
                longitudeInput = "",
                isActiveInput = true,
                formLoading = false,
                formMessage = null,
                formError = false
            )
        }
    }

    fun showEditForm(branch: BranchResponse) {
        _uiState.update {
            it.copy(
                showForm = true,
                editingBranchId = branch.id,
                nameInput = branch.name,
                addressInput = branch.address,
                phoneInput = branch.phone,
                openTimeInput = formatTimeForInput(branch.openTime),
                closeTimeInput = formatTimeForInput(branch.closeTime),
                latitudeInput = branch.latitude?.toString().orEmpty(),
                longitudeInput = branch.longitude?.toString().orEmpty(),
                isActiveInput = branch.status == 1,
                formLoading = false,
                formMessage = null,
                formError = false
            )
        }
    }

    fun closeForm() {
        _uiState.update {
            it.copy(
                showForm = false,
                editingBranchId = null,
                formLoading = false,
                formMessage = null,
                formError = false
            )
        }
    }

    fun dismissMessage() {
        _uiState.update { it.copy(message = null, isError = false) }
    }

    fun dismissDeleteDialog() {
        _uiState.update { it.copy(deletingBranchId = null) }
    }

    fun requestDelete(branchId: Long) {
        _uiState.update { it.copy(deletingBranchId = branchId) }
    }

    fun onNameChanged(value: String) = updateForm { it.copy(nameInput = value, formMessage = null, formError = false) }
    fun onAddressChanged(value: String) = updateForm { it.copy(addressInput = value, formMessage = null, formError = false) }
    fun onPhoneChanged(value: String) = updateForm { it.copy(phoneInput = value, formMessage = null, formError = false) }
    fun onOpenTimeChanged(value: String) = updateForm { it.copy(openTimeInput = value, formMessage = null, formError = false) }
    fun onCloseTimeChanged(value: String) = updateForm { it.copy(closeTimeInput = value, formMessage = null, formError = false) }
    fun onLatitudeChanged(value: String) = updateForm { it.copy(latitudeInput = value, formMessage = null, formError = false) }
    fun onLongitudeChanged(value: String) = updateForm { it.copy(longitudeInput = value, formMessage = null, formError = false) }
    fun onActiveChanged(value: Boolean) = updateForm { it.copy(isActiveInput = value, formMessage = null, formError = false) }

    fun setCoordinates(latitude: Double, longitude: Double) {
        _uiState.update {
            it.copy(
                latitudeInput = latitude.toString(),
                longitudeInput = longitude.toString(),
                formMessage = null,
                formError = false
            )
        }
    }

    fun submitForm() {
        val state = _uiState.value
        val name = state.nameInput.trim()
        val address = state.addressInput.trim()
        val phone = state.phoneInput.trim()
        val openTime = normalizeTimeInput(state.openTimeInput)
        val closeTime = normalizeTimeInput(state.closeTimeInput)
        val latitudeText = state.latitudeInput.trim()
        val longitudeText = state.longitudeInput.trim()
        val latitude = latitudeText.toDoubleOrNull()
        val longitude = longitudeText.toDoubleOrNull()

        when {
            name.isBlank() -> return formError("Ten chi nhanh khong duoc de trong")
            address.isBlank() -> return formError("Dia chi khong duoc de trong")
            phone.isBlank() -> return formError("So dien thoai khong duoc de trong")
            state.openTimeInput.isNotBlank() && openTime == null -> return formError("Gio mo cua phai theo dinh dang HH:mm hoac HH:mm:ss")
            state.closeTimeInput.isNotBlank() && closeTime == null -> return formError("Gio dong cua phai theo dinh dang HH:mm hoac HH:mm:ss")
            latitudeText.isNotBlank() && latitude == null -> return formError("Vi do khong hop le")
            longitudeText.isNotBlank() && longitude == null -> return formError("Kinh do khong hop le")
            latitude != null && latitude !in -90.0..90.0 -> return formError("Vi do phai nam trong khoang -90 den 90")
            longitude != null && longitude !in -180.0..180.0 -> return formError("Kinh do phai nam trong khoang -180 den 180")
            openTime != null && closeTime != null && openTime >= closeTime -> {
                return formError("Gio mo cua phai som hon gio dong cua")
            }
        }

        val request = CreateBranchRequest(
            name = name,
            address = address,
            phone = phone,
            openTime = openTime,
            closeTime = closeTime,
            latitude = latitude,
            longitude = longitude,
            status = if (state.isActiveInput) 1 else 0
        )

        viewModelScope.launch {
            _uiState.update { it.copy(formLoading = true, formMessage = null, formError = false) }
            try {
                val response = if (state.editingBranchId == null) {
                    repository.createBranch(request)
                } else {
                    repository.updateBranch(state.editingBranchId, request)
                }
                if (!response.isSuccessful) {
                    throw IllegalStateException(
                        buildApiError(
                            if (state.editingBranchId == null) "Them chi nhanh" else "Cap nhat chi nhanh",
                            response
                        )
                    )
                }

                val refreshed = repository.getBranches()
                if (!refreshed.isSuccessful) {
                    throw IllegalStateException(buildApiError("Tai danh sach chi nhanh", refreshed))
                }
                _uiState.update {
                    it.copy(
                        branches = refreshed.body().orEmpty().sortedByDescending { branch -> branch.status },
                        loading = false,
                        message = if (state.editingBranchId == null) "Them chi nhanh thanh cong" else "Cap nhat chi nhanh thanh cong",
                        isError = false,
                        showForm = false,
                        editingBranchId = null,
                        nameInput = "",
                        addressInput = "",
                        phoneInput = "",
                        openTimeInput = "",
                        closeTimeInput = "",
                        latitudeInput = "",
                        longitudeInput = "",
                        isActiveInput = true,
                        formLoading = false,
                        formMessage = null,
                        formError = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        formLoading = false,
                        formMessage = e.message ?: "Khong the luu chi nhanh",
                        formError = true
                    )
                }
            }
        }
    }

    fun toggleBranchStatus(branch: BranchResponse) {
        viewModelScope.launch {
            _uiState.update { it.copy(togglingBranchId = branch.id, message = null, isError = false) }
            try {
                val nextStatus = if (branch.status == 1) 0 else 1
                val response = repository.updateBranchStatus(branch.id, nextStatus)
                if (!response.isSuccessful) {
                    throw IllegalStateException(buildApiError("Cap nhat trang thai chi nhanh", response))
                }
                val updatedBranch = response.body()
                _uiState.update { state ->
                    state.copy(
                        branches = state.branches.map {
                            if (it.id == branch.id) updatedBranch ?: it.copy(status = nextStatus) else it
                        }.sortedByDescending { it.status },
                        togglingBranchId = null,
                        message = if (nextStatus == 1) "Da mo lai chi nhanh" else "Da tam ngung chi nhanh",
                        isError = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        togglingBranchId = null,
                        message = e.message ?: "Khong the cap nhat trang thai chi nhanh",
                        isError = true
                    )
                }
            }
        }
    }

    fun deleteBranch() {
        val branchId = _uiState.value.deletingBranchId ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, message = null, isError = false) }
            try {
                val response = repository.deleteBranch(branchId)
                if (!response.isSuccessful) {
                    throw IllegalStateException(buildApiError("Xoa chi nhanh", response))
                }
                _uiState.update { state ->
                    state.copy(
                        branches = state.branches.filterNot { it.id == branchId },
                        loading = false,
                        deletingBranchId = null,
                        message = "Xoa chi nhanh thanh cong",
                        isError = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        loading = false,
                        deletingBranchId = null,
                        message = e.message ?: "Khong the xoa chi nhanh",
                        isError = true
                    )
                }
            }
        }
    }

    private fun updateForm(update: (AdminBranchUiState) -> AdminBranchUiState) {
        _uiState.update(update)
    }

    private fun formError(message: String) {
        _uiState.update { it.copy(formMessage = message, formError = true) }
    }

    private fun normalizeTimeInput(value: String): String? {
        val trimmed = value.trim()
        if (trimmed.isBlank()) return null
        val normalized = trimmed.replace('T', ' ')
            .substringAfterLast(' ')
            .substringBefore('.')
        val parts = normalized.split(":")
        if (parts.size !in 2..3) return null

        val candidate = when (parts.size) {
            2 -> "$normalized:00"
            else -> normalized
        }

        return runCatching { LocalTime.parse(candidate, TIME_PARSE_FORMAT) }
            .getOrNull()
            ?.format(TIME_OUTPUT_FORMAT)
    }

    private fun formatTimeForInput(value: String?): String {
        val normalized = normalizeTimeInput(value.orEmpty()) ?: return value.orEmpty()
        return runCatching { LocalTime.parse(normalized, TIME_OUTPUT_FORMAT) }
            .getOrNull()
            ?.format(TIME_INPUT_FORMAT)
            ?: normalized
    }

    private fun buildApiError(action: String, response: Response<*>): String {
        val body = runCatching { response.errorBody()?.string() }.getOrNull().orEmpty()
        val apiError = body.takeIf { it.isNotBlank() }
            ?.let { runCatching { Gson().fromJson(it, ApiErrorResponse::class.java) }.getOrNull() }

        val message = when (response.code()) {
            400 -> mapBadRequestMessage(apiError?.message)
            401 -> "Phien dang nhap da het han hoac ban chua dang nhap"
            403 -> "Tai khoan hien tai khong co quyen quan tri"
            409 -> mapConflictMessage(apiError?.message)
            else -> apiError?.message
        } ?: "Khong the thuc hien thao tac"

        return "$action: $message"
    }

    private fun mapBadRequestMessage(message: String?): String {
        return when (message) {
            "Branch name must not be blank" -> "Ten chi nhanh khong duoc de trong"
            "Branch address must not be blank" -> "Dia chi khong duoc de trong"
            "Branch phone must not be blank" -> "So dien thoai khong duoc de trong"
            "Branch status must be 0 or 1" -> "Trang thai chi nhanh chi duoc la 0 hoac 1"
            "Branch latitude is invalid" -> "Vi do khong hop le"
            "Branch longitude is invalid" -> "Kinh do khong hop le"
            "Branch open time must be before close time" -> "Gio mo cua phai som hon gio dong cua"
            "Branch open time is invalid" -> "Gio mo cua khong hop le"
            "Branch close time is invalid" -> "Gio dong cua khong hop le"
            null, "" -> "Du lieu gui len khong hop le"
            else -> message
        }
    }

    private fun mapConflictMessage(message: String?): String {
        return when (message) {
            "Branch name already exists" -> "Ten chi nhanh da ton tai"
            "Branch phone already exists" -> "So dien thoai chi nhanh da ton tai"
            "Branch data already exists or violates database constraints" -> "Du lieu chi nhanh bi trung hoac vi pham rang buoc he thong"
            null, "" -> "Du lieu chi nhanh da ton tai"
            else -> message
        }
    }

    companion object {
        private val TIME_PARSE_FORMAT: DateTimeFormatter = DateTimeFormatterBuilder()
            .appendValue(ChronoField.HOUR_OF_DAY, 1, 2, java.time.format.SignStyle.NOT_NEGATIVE)
            .appendLiteral(':')
            .appendValue(ChronoField.MINUTE_OF_HOUR, 2)
            .optionalStart()
            .appendLiteral(':')
            .appendValue(ChronoField.SECOND_OF_MINUTE, 2)
            .optionalEnd()
            .toFormatter()
        private val TIME_OUTPUT_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
        private val TIME_INPUT_FORMAT: DateTimeFormatter = DateTimeFormatterBuilder()
            .appendPattern("H:mm")
            .optionalStart()
            .appendPattern(":ss")
            .optionalEnd()
            .toFormatter()
    }
}
