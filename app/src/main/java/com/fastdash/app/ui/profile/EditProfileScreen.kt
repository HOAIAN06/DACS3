package com.fastdash.app.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val EditBackground = Color(0xFFF7F7F7)
private val EditSurface = Color.White
private val EditTextPrimary = Color(0xFF1F1F1F)
private val EditTextSecondary = Color(0xFF777777)
private val EditBorder = Color(0xFFEAEAEA)
private val EditRed = Color(0xFFD6092F)
private val EditInput = Color(0xFFF9F9F9)
private val EditError = Color(0xFFB42318)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    initialFullName: String,
    initialEmail: String,
    initialPhone: String,
    emailEditable: Boolean = true,
    isLoading: Boolean = false,
    isSaving: Boolean = false,
    errorMessage: String? = null,
    onConsumeError: () -> Unit = {},
    onBack: () -> Unit,
    onSave: (fullName: String, email: String, phone: String) -> Unit
) {
    var fullName by rememberSaveable(initialFullName) { mutableStateOf(initialFullName) }
    var email by rememberSaveable(initialEmail) { mutableStateOf(initialEmail) }
    var phone by rememberSaveable(initialPhone) { mutableStateOf(initialPhone) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(initialFullName, initialEmail, initialPhone) {
        fullName = initialFullName
        email = initialEmail
        phone = initialPhone
    }

    LaunchedEffect(errorMessage) {
        errorMessage?.takeIf { it.isNotBlank() }?.let {
            snackbarHostState.showSnackbar(it)
            onConsumeError()
        }
    }

    val trimmedName = fullName.trim()
    val trimmedEmail = email.trim()
    val trimmedPhone = phone.trim()
    val emailValid = trimmedEmail.isBlank() || android.util.Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()
    val phoneValid = Regex("^0\\d{9}$").matches(trimmedPhone)
    val nameError = if (trimmedName.isBlank()) "Vui lòng nhập họ tên" else null
    val emailError = if (emailEditable && trimmedEmail.isNotBlank() && !emailValid) "Email không hợp lệ" else null
    val phoneError = if (trimmedPhone.isBlank() || !phoneValid) "Số điện thoại không hợp lệ" else null
    val hasChanges = trimmedName != initialFullName.trim() || trimmedEmail != initialEmail.trim() || trimmedPhone != initialPhone.trim()
    val canSave = !isLoading && !isSaving && hasChanges && nameError == null && phoneError == null && emailError == null

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chỉnh sửa thông tin", fontWeight = FontWeight.Bold, color = EditTextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại", tint = EditTextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = EditSurface)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            Surface(color = EditSurface, shadowElevation = 12.dp) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Button(
                        onClick = { onSave(trimmedName, trimmedEmail, trimmedPhone) },
                        enabled = canSave,
                        modifier = Modifier.fillMaxWidth().height(54.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EditRed,
                            contentColor = Color.White,
                            disabledContainerColor = EditBorder,
                            disabledContentColor = EditTextSecondary
                        )
                    ) {
                        if (isSaving) {
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                                Text("Đang lưu...", fontWeight = FontWeight.SemiBold)
                            }
                        } else {
                            Text("Lưu thay đổi", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        },
        containerColor = EditBackground
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(EditBackground)
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = EditSurface,
                    shape = RoundedCornerShape(20.dp),
                    shadowElevation = 4.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        ProfileInputField(
                            label = "Họ và tên",
                            value = fullName,
                            onValueChange = { fullName = it },
                            leadingIcon = Icons.Outlined.Person,
                            placeholder = "Nhập họ và tên",
                            errorMessage = nameError,
                            enabled = !isLoading && !isSaving
                        )
                        ProfileInputField(
                            label = "Email",
                            value = email,
                            onValueChange = { email = it },
                            leadingIcon = Icons.Outlined.Email,
                            placeholder = "Nhập email",
                            errorMessage = emailError,
                            enabled = emailEditable && !isLoading && !isSaving,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                        )
                        ProfileInputField(
                            label = "Số điện thoại",
                            value = phone,
                            onValueChange = { phone = it },
                            leadingIcon = Icons.Outlined.Phone,
                            placeholder = "Nhập số điện thoại",
                            errorMessage = phoneError,
                            enabled = !isLoading && !isSaving,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    leadingIcon: ImageVector,
    placeholder: String,
    errorMessage: String?,
    enabled: Boolean,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = EditTextSecondary)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder, color = EditTextSecondary) },
            leadingIcon = { Icon(leadingIcon, contentDescription = null, tint = EditTextSecondary) },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            keyboardOptions = keyboardOptions,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = EditInput,
                unfocusedContainerColor = EditInput,
                disabledContainerColor = EditInput,
                focusedBorderColor = EditRed,
                unfocusedBorderColor = EditBorder,
                disabledBorderColor = EditBorder,
                focusedTextColor = EditTextPrimary,
                unfocusedTextColor = EditTextPrimary,
                disabledTextColor = EditTextSecondary
            )
        )
        Text(
            text = errorMessage.orEmpty(),
            color = if (errorMessage != null) EditError else Color.Transparent,
            fontSize = 12.sp,
            minLines = 1
        )
    }
}
