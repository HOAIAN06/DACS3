package com.fastdash.app.ui.auth

import android.app.Activity
import android.util.Patterns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.livedata.observeAsState
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
import com.fastdash.app.utils.Constants
import com.fastdash.app.utils.TokenManager
import com.fastdash.app.viewmodel.LoginViewModel
import com.fastdash.app.viewmodel.LoginViewModelFactory
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onOpenRegister: () -> Unit = {},
    onOpenForgotPassword: () -> Unit = {}
) {
    val context = LocalContext.current
    val owner = context as? ViewModelStoreOwner ?: error("ViewModelStoreOwner not found")
    val tokenManager = remember { TokenManager(context) }
    val viewModel: LoginViewModel = remember(owner) {
        ViewModelProvider(owner, LoginViewModelFactory(context.applicationContext))[LoginViewModel::class.java]
    }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(false) }

    val loginResult by viewModel.loginResult.observeAsState()
    val errorMessage by viewModel.errorMessage.observeAsState()
    val loading by viewModel.loading.observeAsState(false)

    val emailValid = email.isBlank() || Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()
    val passwordValid = password.isBlank() || password.length >= 6
    val canSubmit = !loading && email.isNotBlank() && password.isNotBlank() && emailValid && passwordValid

    val googleSignInClient = remember {
        GoogleSignIn.getClient(
            context,
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestIdToken(Constants.GOOGLE_CLIENT_ID)
                .build()
        )
    }

    val googleLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode != Activity.RESULT_OK) return@rememberLauncherForActivityResult
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            account.idToken?.let { viewModel.googleLogin(it) }
        } catch (_: ApiException) {
        }
    }

    LaunchedEffect(loginResult) {
        loginResult?.let { result ->
            tokenManager.saveUserId(result.id)
            tokenManager.saveToken(result.token)
            tokenManager.saveRole(result.role)
            tokenManager.saveFullName(result.fullName)
            tokenManager.saveEmail(result.email)
            tokenManager.savePhone(result.phone)
            Toast.makeText(context, "Chào mừng bạn quay lại!", Toast.LENGTH_SHORT).show()
            viewModel.consumeLoginResult()
            onLoginSuccess()
        }
    }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.consumeErrorMessage()
        }
    }

    AuthScaffold(
        heroImage = R.drawable.pizza_ui1,
        logo = R.drawable.logo2,
        title = "Đăng nhập",
        subtitle = null,
        heroHeight = 280.dp,
        overlapHeight = 32.dp,
        titleTopSpacing = 24.dp,
        chipText = null
    ) {
        FastDashTextField(
            value = email,
            onValueChange = { email = it },
            placeholder = "Email hoặc số điện thoại",
            leadingIcon = Icons.Default.Email,
            isError = !emailValid,
            errorMessage = if (!emailValid) "Email không hợp lệ" else null
        )

        Spacer(modifier = Modifier.height(16.dp))

        FastDashTextField(
            value = password,
            onValueChange = { password = it },
            placeholder = "Mật khẩu",
            leadingIcon = Icons.Default.Lock,
            isPassword = true,
            passwordVisible = passwordVisible,
            onPasswordToggle = { passwordVisible = !passwordVisible },
            isError = !passwordValid,
            errorMessage = if (!passwordValid) "Mật khẩu cần ít nhất 6 ký tự" else null
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = rememberMe,
                    onCheckedChange = { rememberMe = it },
                    colors = CheckboxDefaults.colors(
                        checkedColor = FastDashRed,
                        uncheckedColor = FastDashMuted,
                        checkmarkColor = Color.White
                    )
                )
                Text(
                    text = "Ghi nhớ tài khoản",
                    color = FastDashMuted,
                    fontSize = 13.sp,
                    modifier = Modifier.clickable { rememberMe = !rememberMe }
                )
            }

            Text(
                text = "Quên mật khẩu?",
                color = FastDashBlue,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable(onClick = onOpenForgotPassword)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        FastDashPrimaryButton(
            text = "Đăng nhập",
            onClick = { viewModel.login(email.trim(), password.trim()) },
            loading = loading,
            enabled = canSubmit
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            HorizontalDivider(modifier = Modifier.weight(1f), color = FastDashLine)
            Text(
                text = " hoặc đăng nhập với ",
                color = FastDashMuted,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            HorizontalDivider(modifier = Modifier.weight(1f), color = FastDashLine)
        }

        Spacer(modifier = Modifier.height(16.dp))

        FastDashSocialButton(
            text = "Google",
            icon = R.drawable.logo_gg,
            onClick = {
                googleSignInClient.signOut().addOnCompleteListener {
                    googleLauncher.launch(googleSignInClient.signInIntent)
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(22.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Chưa có tài khoản? ",
                color = FastDashMuted,
                fontSize = 14.sp
            )
            Text(
                text = "Đăng ký ngay",
                color = FastDashRed,
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.clickable(onClick = onOpenRegister)
            )
        }
    }
}
