package com.fastdash.app.ui.auth

import android.app.Activity
import android.util.Patterns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
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

// Design System Colors
private val BrandRedPrimary = Color(0xFFE31837)
private val BrandRedDark = Color(0xFFB5122B)
private val BrandRedDisabled = Color(0xFFF4A3B1)
private val GrayTextPrimary = Color(0xFF1F2937)
private val GrayTextSecondary = Color(0xFF6B7280)
private val GrayBorder = Color(0xFFE5E7EB)
private val BackgroundGray = Color(0xFFF9FAFB)

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onOpenRegister: () -> Unit = {}
) {
    val context = LocalContext.current
    val owner = context as? ViewModelStoreOwner
        ?: error("LoginScreen requires a ViewModelStoreOwner")
    val tokenManager = remember { TokenManager(context) }

    val viewModel: LoginViewModel = remember(owner) {
        ViewModelProvider(
            owner,
            LoginViewModelFactory(context.applicationContext)
        )[LoginViewModel::class.java]
    }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

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
            val idToken = account.idToken
            if (!idToken.isNullOrBlank()) {
                viewModel.googleLogin(idToken)
            }
        } catch (_: ApiException) {}
    }

    LaunchedEffect(loginResult) {
        loginResult?.let { result ->
            tokenManager.saveUserId(result.id)
            tokenManager.saveToken(result.token)
            tokenManager.saveRole(result.role)
            tokenManager.saveFullName(result.fullName)
            tokenManager.saveEmail(result.email)
            tokenManager.savePhone(result.phone)
            Toast.makeText(context, "Chào mừng quay trở lại!", Toast.LENGTH_SHORT).show()
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
            .imePadding()
    ) {
        // 1. Professional Header
        AuthHeader(
            logoSize = 80,
            title = "FastDash",
            subtitle = "Pizza nóng, giao nhanh, ưu đãi mỗi ngày",
            modifier = Modifier.align(Alignment.TopCenter)
        )

        // 2. Main Form Card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.76f)
                .align(Alignment.BottomCenter),
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            color = Color.White,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(32.dp))
                
                Text(
                    text = "Chào mừng trở lại 👋",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = GrayTextPrimary
                )
                Text(
                    text = "Đăng nhập để đặt món nhanh hơn và nhận ưu đãi riêng",
                    fontSize = 14.sp,
                    color = GrayTextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
                )

                // Input Fields
                AuthTextField(
                    value = email,
                    onValueChange = { email = it },
                    placeholder = "Địa chỉ Email",
                    leadingIcon = Icons.Default.Email,
                    isError = !emailValid,
                    errorMessage = if (!emailValid) "Email không hợp lệ" else null
                )

                Spacer(modifier = Modifier.height(16.dp))

                AuthTextField(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = "Mật khẩu",
                    leadingIcon = Icons.Default.Lock,
                    isPassword = true,
                    passwordVisible = passwordVisible,
                    onPasswordToggle = { passwordVisible = !passwordVisible },
                    isError = !passwordValid,
                    errorMessage = if (!passwordValid) "Mật khẩu tối thiểu 6 ký tự" else null
                )

                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                    TextButton(onClick = { /* Forgot password */ }) {
                        Text("Quên mật khẩu?", color = BrandRedPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Primary Action
                Button(
                    onClick = { viewModel.login(email.trim(), password.trim()) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = canSubmit,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrandRedPrimary,
                        disabledContainerColor = BrandRedDisabled
                    ),
                    shape = RoundedCornerShape(16.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                ) {
                    if (loading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 3.dp)
                    } else {
                        Text("ĐĂNG NHẬP", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                    }
                }

                Spacer(Modifier.height(32.dp))

                // OR Divider
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth(0.9f)) {
                    HorizontalDivider(modifier = Modifier.weight(1f), color = GrayBorder)
                    Text(
                        text = "HOẶC",
                        modifier = Modifier.padding(horizontal = 16.dp),
                        fontSize = 12.sp,
                        color = GrayTextSecondary,
                        fontWeight = FontWeight.Bold
                    )
                    HorizontalDivider(modifier = Modifier.weight(1f), color = GrayBorder)
                }

                Spacer(Modifier.height(24.dp))

                // Google Button
                OutlinedButton(
                    onClick = {
                        if (!loading) {
                            googleSignInClient.signOut().addOnCompleteListener {
                                googleLauncher.launch(googleSignInClient.signInIntent)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, GrayBorder),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = GrayTextPrimary)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_launcher_foreground),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text("Tiếp tục với Google", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }

                Spacer(Modifier.height(32.dp))

                // Bottom Navigation
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Bạn chưa có tài khoản?", color = GrayTextSecondary, fontSize = 14.sp)
                    TextButton(onClick = onOpenRegister) {
                        Text("Đăng ký ngay", color = BrandRedPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun AuthHeader(
    logoSize: Int,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(270.dp)
            .background(
                Brush.verticalGradient(listOf(BrandRedPrimary, BrandRedDark))
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = Color.White.copy(alpha = 0.05f),
                radius = 120.dp.toPx(),
                center = androidx.compose.ui.geometry.Offset(size.width * 0.1f, size.height * 0.2f)
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.08f),
                radius = 80.dp.toPx(),
                center = androidx.compose.ui.geometry.Offset(size.width * 0.9f, size.height * 0.7f)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(bottom = 30.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                modifier = Modifier.size(logoSize.dp),
                shape = CircleShape,
                color = Color.White,
                shadowElevation = 6.dp
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo1),
                    contentDescription = null,
                    modifier = Modifier.padding(logoSize.dp / 5),
                    contentScale = ContentScale.Fit
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text = title,
                fontSize = 30.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                letterSpacing = 0.5.sp
            )
            Text(
                text = subtitle,
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.8f),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onPasswordToggle: () -> Unit = {},
    isError: Boolean = false,
    errorMessage: String? = null
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = GrayTextSecondary, fontSize = 15.sp) },
            leadingIcon = { Icon(leadingIcon, null, tint = BrandRedPrimary.copy(alpha = 0.7f), modifier = Modifier.size(20.dp)) },
            trailingIcon = if (isPassword) {
                {
                    IconButton(onClick = onPasswordToggle) {
                        Icon(
                            if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            null,
                            tint = GrayTextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            } else null,
            visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            isError = isError,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = BrandRedPrimary,
                unfocusedBorderColor = GrayBorder,
                errorBorderColor = BrandRedPrimary,
                cursorColor = BrandRedPrimary
            )
        )
        if (isError && errorMessage != null) {
            Text(
                text = errorMessage,
                color = BrandRedPrimary,
                fontSize = 11.sp,
                modifier = Modifier.padding(start = 12.dp, top = 4.dp),
                fontWeight = FontWeight.Medium
            )
        }
    }
}
