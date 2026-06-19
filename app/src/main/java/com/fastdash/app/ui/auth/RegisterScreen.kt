package com.fastdash.app.ui.auth

import android.util.Patterns
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockClock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import com.fastdash.app.R
import com.fastdash.app.viewmodel.RegisterViewModel
import com.fastdash.app.viewmodel.RegisterViewModelFactory

@Composable
fun RegisterScreen(
    onBackToLogin: () -> Unit,
    onRegisterSuccess: () -> Unit
) {
    val context = LocalContext.current
    val owner = context as? ViewModelStoreOwner ?: error("ViewModelStoreOwner not found")
    val viewModel: RegisterViewModel = remember(owner) {
        ViewModelProvider(owner, RegisterViewModelFactory(context.applicationContext))[RegisterViewModel::class.java]
    }

    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var agreeToTerms by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmVisible by remember { mutableStateOf(false) }

    val loading by viewModel.loading.observeAsState(false)
    val registerSuccess by viewModel.registerSuccess.observeAsState()
    val errorMessage by viewModel.errorMessage.observeAsState()

    val emailValid = email.isBlank() || Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()
    val phoneValid = phone.isBlank() || phone.length >= 9
    val passwordValid = password.isBlank() || password.length >= 6
    val confirmValid = confirmPassword.isBlank() || confirmPassword == password
    val canSubmit = !loading &&
        fullName.isNotBlank() &&
        email.isNotBlank() &&
        phone.isNotBlank() &&
        password.isNotBlank() &&
        confirmPassword.isNotBlank() &&
        emailValid &&
        phoneValid &&
        passwordValid &&
        confirmValid &&
        agreeToTerms

    LaunchedEffect(registerSuccess) {
        if (registerSuccess == true) {
            Toast.makeText(context, "Tạo tài khoản thành công!", Toast.LENGTH_SHORT).show()
            viewModel.consumeSuccess()
            onRegisterSuccess()
        }
    }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.consumeError()
        }
    }

    AuthScaffold(
        heroImage = R.drawable.pizza_ui2,
        logo = R.drawable.logo2,
        title = "Tạo tài khoản",
        subtitle = null,
        heroHeight = 280.dp,
        overlapHeight = 32.dp,
        titleTopSpacing = 34.dp,
        chipText = null
    ) {
        FastDashTextField(
            value = fullName,
            onValueChange = { fullName = it },
            placeholder = "Họ và tên",
            leadingIcon = Icons.Default.Person
        )

        Spacer(modifier = Modifier.height(14.dp))

        FastDashTextField(
            value = email,
            onValueChange = { email = it },
            placeholder = "Email",
            leadingIcon = Icons.Default.Email,
            isError = !emailValid,
            errorMessage = if (!emailValid) "Email không hợp lệ" else null
        )

        Spacer(modifier = Modifier.height(14.dp))

        FastDashTextField(
            value = phone,
            onValueChange = { phone = it },
            placeholder = "Số điện thoại",
            leadingIcon = Icons.Default.Phone,
            isError = !phoneValid,
            errorMessage = if (!phoneValid) "Số điện thoại không hợp lệ" else null
        )

        Spacer(modifier = Modifier.height(14.dp))

        FastDashTextField(
            value = password,
            onValueChange = { password = it },
            placeholder = "Mật khẩu",
            leadingIcon = Icons.Default.Lock,
            isPassword = true,
            passwordVisible = passwordVisible,
            onPasswordToggle = { passwordVisible = !passwordVisible },
            isError = !passwordValid,
            errorMessage = if (!passwordValid) "Tối thiểu 6 ký tự" else null
        )

        Spacer(modifier = Modifier.height(14.dp))

        FastDashTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            placeholder = "Xác nhận mật khẩu",
            leadingIcon = Icons.Default.LockClock,
            isPassword = true,
            passwordVisible = confirmVisible,
            onPasswordToggle = { confirmVisible = !confirmVisible },
            isError = !confirmValid,
            errorMessage = if (!confirmValid) "Mật khẩu chưa khớp" else null
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = agreeToTerms,
                onCheckedChange = { agreeToTerms = it },
                colors = CheckboxDefaults.colors(
                    checkedColor = FastDashRed,
                    uncheckedColor = FastDashMuted,
                    checkmarkColor = Color.White
                )
            )
            Text(
                text = "Tôi đồng ý với ",
                color = FastDashMuted,
                fontSize = 12.sp
            )
            Text(
                text = "điều khoản và chính sách",
                color = FastDashBlue,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        FastDashPrimaryButton(
            text = "Đăng ký",
            onClick = { viewModel.register(fullName, email, phone, password) },
            loading = loading,
            enabled = canSubmit
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Đã có tài khoản? ",
                color = FastDashMuted,
                fontSize = 14.sp
            )
            Text(
                text = "Đăng nhập",
                color = FastDashRed,
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.clickable(onClick = onBackToLogin)
            )
        }
    }
}
