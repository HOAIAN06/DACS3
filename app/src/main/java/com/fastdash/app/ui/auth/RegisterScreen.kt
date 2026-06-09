package com.fastdash.app.ui.auth

import android.util.Patterns
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.fastdash.app.viewmodel.RegisterViewModel
import com.fastdash.app.viewmodel.RegisterViewModelFactory

// Design System Colors
private val BrandRedPrimary = Color(0xFFE31837)
private val BrandRedDark = Color(0xFFB5122B)
private val BrandRedDisabled = Color(0xFFF4A3B1)
private val GrayTextPrimary = Color(0xFF1F2937)
private val GrayTextSecondary = Color(0xFF6B7280)
private val GrayBorder = Color(0xFFE5E7EB)
private val BackgroundGray = Color(0xFFF9FAFB)

@Composable
fun RegisterScreen(
    onBackToLogin: () -> Unit,
    onRegisterSuccess: () -> Unit
) {
    val context = LocalContext.current
    val owner = context as? ViewModelStoreOwner
        ?: error("RegisterScreen requires a ViewModelStoreOwner")
    val viewModel: RegisterViewModel = remember(owner) {
        ViewModelProvider(
            owner,
            RegisterViewModelFactory(context.applicationContext)
        )[RegisterViewModel::class.java]
    }

    var fullName by remember { mutableStateOf("") }
    var emailOrPhone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmVisible by remember { mutableStateOf(false) }

    val loading by viewModel.loading.observeAsState(false)
    val registerSuccess by viewModel.registerSuccess.observeAsState()
    val errorMessage by viewModel.errorMessage.observeAsState()

    // Basic Validation
    val isEmail = Patterns.EMAIL_ADDRESS.matcher(emailOrPhone.trim()).matches()
    val isPhone = emailOrPhone.length >= 9 && emailOrPhone.all { it.isDigit() }
    val identifierValid = emailOrPhone.isBlank() || isEmail || isPhone
    val passwordValid = password.isBlank() || password.length >= 6
    val confirmValid = confirmPassword.isBlank() || confirmPassword == password
    
    val canSubmit = !loading && fullName.isNotBlank() && emailOrPhone.isNotBlank() && 
                    password.isNotBlank() && confirmPassword.isNotBlank() && 
                    identifierValid && passwordValid && confirmValid

    LaunchedEffect(registerSuccess) {
        if (registerSuccess == true) {
            Toast.makeText(context, "Chào mừng bạn mới gia nhập!", Toast.LENGTH_SHORT).show()
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
            .imePadding()
    ) {
        // 1. Compact Header
        AuthHeaderCompact(
            logoSize = 56,
            title = "Tham gia FastDash",
            modifier = Modifier.align(Alignment.TopCenter)
        )

        // 2. Registration Card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.84f)
                .align(Alignment.BottomCenter),
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            color = Color.White,
            tonalElevation = 2.dp,
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
                    text = "Tạo tài khoản mới",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = GrayTextPrimary
                )
                Text(
                    text = "Chỉ mất vài giây để bắt đầu đặt hàng",
                    fontSize = 14.sp,
                    color = GrayTextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp, bottom = 32.dp)
                )

                // Họ và tên
                AuthTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    placeholder = "Họ và tên của bạn",
                    leadingIcon = Icons.Default.Person
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Email hoặc số điện thoại
                AuthTextField(
                    value = emailOrPhone,
                    onValueChange = { emailOrPhone = it },
                    placeholder = "Email hoặc số điện thoại",
                    leadingIcon = Icons.Default.ContactMail,
                    isError = !identifierValid,
                    errorMessage = if (!identifierValid) "Thông tin không hợp lệ" else null
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Mật khẩu
                AuthTextField(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = "Mật khẩu (ít nhất 6 ký tự)",
                    leadingIcon = Icons.Default.Lock,
                    isPassword = true,
                    passwordVisible = passwordVisible,
                    onPasswordToggle = { passwordVisible = !passwordVisible },
                    isError = !passwordValid,
                    errorMessage = if (!passwordValid) "Mật khẩu tối thiểu 6 ký tự" else null
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Xác nhận mật khẩu
                AuthTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    placeholder = "Xác nhận lại mật khẩu",
                    leadingIcon = Icons.Default.LockReset,
                    isPassword = true,
                    passwordVisible = confirmVisible,
                    onPasswordToggle = { confirmVisible = !confirmVisible },
                    isError = !confirmValid,
                    errorMessage = if (!confirmValid) "Mật khẩu xác nhận không khớp" else null
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Submit Button
                Button(
                    onClick = { 
                        val emailParam = if (isEmail) emailOrPhone.trim() else ""
                        val phoneParam = if (isPhone) emailOrPhone.trim() else ""
                        viewModel.register(fullName, emailParam, phoneParam, password) 
                    },
                    enabled = canSubmit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
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
                        Text("ĐĂNG KÝ NGAY", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                    }
                }

                Spacer(Modifier.height(24.dp))

                // Back to Login
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Đã có tài khoản?", color = GrayTextSecondary, fontSize = 14.sp)
                    TextButton(onClick = onBackToLogin) {
                        Text("Đăng nhập", color = BrandRedPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun AuthHeaderCompact(
    logoSize: Int,
    title: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(230.dp)
            .background(
                Brush.verticalGradient(listOf(BrandRedPrimary, BrandRedDark))
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = Color.White.copy(alpha = 0.05f),
                radius = 100.dp.toPx(),
                center = androidx.compose.ui.geometry.Offset(size.width * 0.9f, size.height * 0.1f)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(bottom = 30.dp, start = 24.dp, end = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Surface(
                modifier = Modifier.size(logoSize.dp),
                shape = CircleShape,
                color = Color.White,
                shadowElevation = 4.dp
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo1),
                    contentDescription = null,
                    modifier = Modifier.padding(logoSize.dp / 5),
                    contentScale = ContentScale.Fit
                )
            }
            Spacer(Modifier.width(16.dp))
            Text(
                text = title,
                fontSize = 26.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
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
