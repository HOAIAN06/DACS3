package com.fastdash.app.ui.auth

import android.util.Patterns
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import com.fastdash.app.R
import com.fastdash.app.viewmodel.ForgotPasswordViewModel
import com.fastdash.app.viewmodel.ForgotPasswordViewModelFactory
import kotlinx.coroutines.delay

@Composable
fun ForgotPasswordEmailScreen(
    initialEmail: String,
    onBack: () -> Unit,
    onOtpSent: (String, Long) -> Unit
) {
    val context = LocalContext.current
    val owner = context as? ViewModelStoreOwner ?: error("ViewModelStoreOwner not found")
    val viewModel: ForgotPasswordViewModel = remember(owner) {
        ViewModelProvider(owner, ForgotPasswordViewModelFactory(context.applicationContext))[ForgotPasswordViewModel::class.java]
    }

    var email by rememberSaveable { mutableStateOf(initialEmail) }
    val loading by viewModel.loading.observeAsState(false)
    val errorMessage by viewModel.errorMessage.observeAsState()
    val requestOtpSuccess by viewModel.requestOtpSuccess.observeAsState()

    val emailValid = email.isBlank() || Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()
    val canSubmit = !loading && email.isNotBlank() && emailValid

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.consumeErrorMessage()
        }
    }

    LaunchedEffect(requestOtpSuccess) {
        if (requestOtpSuccess == true) {
            viewModel.consumeRequestOtpSuccess()
            onOtpSent(email.trim().lowercase(), System.currentTimeMillis() + 60_000L)
        }
    }

    AuthScaffold(
        heroImage = R.drawable.pizza_ui2,
        logo = R.drawable.logo2,
        title = "Lấy lại mật khẩu",
        subtitle = null,
        heroHeight = 250.dp,
        overlapHeight = 32.dp,
        titleTopSpacing = 24.dp,
        chipText = null,
        topBar = { AuthBackButton(onClick = onBack) }
    ) {
        FastDashTextField(
            value = email,
            onValueChange = { email = it },
            placeholder = "Email của bạn",
            leadingIcon = Icons.Default.Email,
            isError = !emailValid,
            errorMessage = if (!emailValid) "Email không chính xác" else null
        )

        Spacer(modifier = Modifier.height(18.dp))

        FastDashPrimaryButton(
            text = "Gửi mã OTP",
            onClick = { viewModel.requestOtp(email.trim()) },
            loading = loading,
            enabled = canSubmit
        )
    }
}

@Composable
fun VerifyOtpScreen(
    initialEmail: String,
    initialCode: String,
    resendAvailableAtMillis: Long,
    onBack: () -> Unit,
    onChangeEmail: () -> Unit,
    onVerified: (String, String) -> Unit,
    onCooldownChanged: (Long) -> Unit
) {
    val context = LocalContext.current
    val owner = context as? ViewModelStoreOwner ?: error("ViewModelStoreOwner not found")
    val viewModel: ForgotPasswordViewModel = remember(owner) {
        ViewModelProvider(owner, ForgotPasswordViewModelFactory(context.applicationContext))[ForgotPasswordViewModel::class.java]
    }

    var otpCode by rememberSaveable { mutableStateOf(initialCode) }
    var resendAvailableAt by rememberSaveable { mutableLongStateOf(resendAvailableAtMillis) }
    var remainingSeconds by remember { mutableLongStateOf(0L) }
    val focusRequester = remember { FocusRequester() }

    val loading by viewModel.loading.observeAsState(false)
    val errorMessage by viewModel.errorMessage.observeAsState()
    val verifyOtpSuccess by viewModel.verifyOtpSuccess.observeAsState()
    val requestOtpSuccess by viewModel.requestOtpSuccess.observeAsState()

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    LaunchedEffect(resendAvailableAt) {
        while (true) {
            val diff = ((resendAvailableAt - System.currentTimeMillis()) / 1000L).coerceAtLeast(0L)
            remainingSeconds = diff
            if (diff <= 0L) break
            delay(1000L)
        }
    }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.consumeErrorMessage()
        }
    }

    LaunchedEffect(verifyOtpSuccess) {
        if (verifyOtpSuccess == true) {
            viewModel.consumeVerifyOtpSuccess()
            onVerified(initialEmail, otpCode)
        }
    }

    LaunchedEffect(requestOtpSuccess) {
        if (requestOtpSuccess == true) {
            val nextCooldown = System.currentTimeMillis() + 60_000L
            resendAvailableAt = nextCooldown
            onCooldownChanged(nextCooldown)
            viewModel.consumeRequestOtpSuccess()
            Toast.makeText(context, "Mã mới đã được gửi!", Toast.LENGTH_SHORT).show()
        }
    }

    AuthScaffold(
        heroImage = R.drawable.pizza_ui1,
        logo = R.drawable.logo2,
        title = "Xác minh OTP",
        subtitle = "Nhập mã gồm 6 số.",
        heroHeight = 250.dp,
        overlapHeight = 32.dp,
        titleTopSpacing = 24.dp,
        chipText = null,
        topBar = { AuthBackButton(onClick = onBack) }
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = FastDashSurfaceAlt,
            shape = RoundedCornerShape(22.dp),
            border = BorderStroke(1.dp, FastDashLine)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(shape = CircleShape, color = FastDashWhite) {
                    Image(
                        painter = painterResource(id = R.drawable.logo2),
                        contentDescription = null,
                        modifier = Modifier
                            .size(42.dp)
                            .padding(6.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Mã được gửi đến",
                        color = FastDashMuted,
                        fontSize = 12.sp
                    )
                    Text(
                        text = initialEmail,
                        color = FastDashInk,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { focusRequester.requestFocus() }
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                repeat(6) { index ->
                    val char = otpCode.getOrNull(index)?.toString() ?: ""
                    val active = otpCode.length == index || (otpCode.length == 6 && index == 5)
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(0.9f),
                        shape = RoundedCornerShape(18.dp),
                        color = FastDashWhite,
                        border = BorderStroke(1.5.dp, if (active) FastDashRed else FastDashLine)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = char,
                                color = FastDashInk,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }

            BasicTextField(
                value = otpCode,
                onValueChange = {
                    if (it.length <= 6 && it.all(Char::isDigit)) {
                        otpCode = it
                    }
                },
                modifier = Modifier
                    .size(1.dp)
                    .focusRequester(focusRequester),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        FastDashPrimaryButton(
            text = "Xác nhận mã",
            onClick = { viewModel.verifyOtp(initialEmail, otpCode) },
            loading = loading,
            enabled = otpCode.length == 6
        )

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = if (remainingSeconds > 0) {
                "Gửi lại mã sau ${remainingSeconds}s"
            } else {
                "Gửi lại mã ngay"
            },
            color = if (remainingSeconds > 0) FastDashMuted else FastDashBlue,
            fontSize = 14.sp,
            fontWeight = if (remainingSeconds > 0) FontWeight.Medium else FontWeight.Bold,
            modifier = Modifier.clickable(enabled = remainingSeconds == 0L) {
                viewModel.requestOtp(initialEmail)
            }
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Đổi địa chỉ email",
            color = FastDashRed,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.clickable(onClick = onChangeEmail)
        )
    }
}

@Composable
fun ResetPasswordScreen(
    email: String,
    code: String,
    onBack: () -> Unit,
    onResetSuccess: () -> Unit
) {
    val context = LocalContext.current
    val owner = context as? ViewModelStoreOwner ?: error("ViewModelStoreOwner not found")
    val viewModel: ForgotPasswordViewModel = remember(owner) {
        ViewModelProvider(owner, ForgotPasswordViewModelFactory(context.applicationContext))[ForgotPasswordViewModel::class.java]
    }

    var newPassword by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var confirmPasswordVisible by rememberSaveable { mutableStateOf(false) }

    val loading by viewModel.loading.observeAsState(false)
    val errorMessage by viewModel.errorMessage.observeAsState()
    val resetPasswordSuccess by viewModel.resetPasswordSuccess.observeAsState()

    val passwordStrength = remember(newPassword) {
        when {
            newPassword.isEmpty() -> 0f
            newPassword.length < 6 -> 0.25f
            newPassword.any(Char::isDigit) && newPassword.any(Char::isLetter) -> 1f
            else -> 0.6f
        }
    }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.consumeErrorMessage()
        }
    }

    LaunchedEffect(resetPasswordSuccess) {
        if (resetPasswordSuccess == true) {
            viewModel.consumeResetPasswordSuccess()
            onResetSuccess()
        }
    }

    AuthScaffold(
        heroImage = R.drawable.pizza_ui2,
        logo = R.drawable.logo2,
        title = "Thiết lập mật khẩu mới",
        subtitle = null,
        heroHeight = 250.dp,
        overlapHeight = 32.dp,
        titleTopSpacing = 24.dp,
        chipText = null,
        topBar = { AuthBackButton(onClick = onBack) }
    ) {
        Text(
            text = buildAnnotatedString {
                append("Đặt lại cho ")
                withStyle(SpanStyle(color = FastDashRed, fontWeight = FontWeight.Bold)) {
                    append(email)
                }
            },
            color = FastDashMuted,
            fontSize = 13.sp
        )

        Spacer(modifier = Modifier.height(18.dp))

        FastDashTextField(
            value = newPassword,
            onValueChange = { newPassword = it },
            placeholder = "Mật khẩu mới",
            leadingIcon = Icons.Default.Lock,
            isPassword = true,
            passwordVisible = passwordVisible,
            onPasswordToggle = { passwordVisible = !passwordVisible }
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            repeat(4) { idx ->
                val threshold = (idx + 1) * 0.25f
                val active = passwordStrength >= threshold
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .background(
                            color = if (active) {
                                when {
                                    passwordStrength <= 0.25f -> FastDashRed
                                    passwordStrength <= 0.6f -> FastDashOrange
                                    else -> FastDashOlive
                                }
                            } else {
                                FastDashLine
                            },
                            shape = RoundedCornerShape(999.dp)
                        )
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = when {
                passwordStrength <= 0.25f -> "Mức độ bảo mật: yếu"
                passwordStrength <= 0.6f -> "Mức độ bảo mật: trung bình"
                else -> "Mức độ bảo mật: mạnh"
            },
            color = FastDashMuted,
            fontSize = 12.sp,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        FastDashTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            placeholder = "Xác nhận mật khẩu",
            leadingIcon = Icons.Default.LockReset,
            isPassword = true,
            passwordVisible = confirmPasswordVisible,
            onPasswordToggle = { confirmPasswordVisible = !confirmPasswordVisible },
            isError = confirmPassword.isNotEmpty() && confirmPassword != newPassword,
            errorMessage = if (confirmPassword.isNotEmpty() && confirmPassword != newPassword) {
                "Mật khẩu xác nhận chưa khớp"
            } else {
                null
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        FastDashPrimaryButton(
            text = "Cập nhật",
            onClick = { viewModel.resetPassword(email, code, newPassword) },
            loading = loading,
            enabled = newPassword.isNotEmpty() && newPassword == confirmPassword && newPassword.length >= 6
        )

        Spacer(modifier = Modifier.height(12.dp))

        TextButton(
            onClick = onBack,
            colors = ButtonDefaults.textButtonColors(contentColor = FastDashBlue)
        ) {
            Text(text = "Quay lại bước xác minh", fontWeight = FontWeight.Bold)
        }
    }
}
