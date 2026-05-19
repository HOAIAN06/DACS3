package com.fastdash.app.ui.auth

import android.app.Activity
import android.util.Patterns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
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

private val PizzaHutRed = Color(0xFFC8102E)

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
            if (idToken.isNullOrBlank()) {
                Toast.makeText(context, "Không lấy được Google idToken", Toast.LENGTH_SHORT).show()
            } else {
                viewModel.googleLogin(idToken)
            }
        } catch (e: ApiException) {
            Toast.makeText(context, "Google Sign-In thất bại: ${e.statusCode}", Toast.LENGTH_SHORT).show()
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo1),
            contentDescription = "FastDash Logo",
            modifier = Modifier.size(150.dp),
            contentScale = ContentScale.Fit
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Chào mừng bạn!",
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.Black
        )
        Text(
            text = "Đăng nhập để đặt món nóng hổi ngay",
            fontSize = 14.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(40.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            isError = !emailValid,
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PizzaHutRed,
                focusedLabelColor = PizzaHutRed
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Mật khẩu") },
            visualTransformation = PasswordVisualTransformation(),
            isError = !passwordValid,
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PizzaHutRed,
                focusedLabelColor = PizzaHutRed
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { viewModel.login(email.trim(), password.trim()) },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            enabled = canSubmit,
            colors = ButtonDefaults.buttonColors(containerColor = PizzaHutRed),
            shape = RoundedCornerShape(12.dp)
        ) {
            if (loading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text("ĐĂNG NHẬP", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }

        Spacer(Modifier.height(20.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            HorizontalDivider(modifier = Modifier.weight(1f), color = Color.LightGray)
            Text(
                text = "HOẶC",
                modifier = Modifier.padding(horizontal = 12.dp),
                style = MaterialTheme.typography.labelMedium,
                color = Color.Gray
            )
            HorizontalDivider(modifier = Modifier.weight(1f), color = Color.LightGray)
        }

        Spacer(Modifier.height(20.dp))

        Button(
            onClick = {
                if (loading) return@Button
                if (Constants.GOOGLE_CLIENT_ID.isBlank()) {
                    Toast.makeText(context, "Thiếu fastdash.googleClientId", Toast.LENGTH_SHORT).show()
                } else {
                    googleSignInClient.signOut().addOnCompleteListener {
                        googleLauncher.launch(googleSignInClient.signInIntent)
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp)
        ) {
            Surface(
                color = Color(0xFFF1F3F4),
                shape = RoundedCornerShape(999.dp),
                modifier = Modifier.size(28.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("G", color = Color(0xFF4285F4), fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.size(12.dp))
            Text("Đăng nhập bằng Google", color = Color.Black, fontWeight = FontWeight.SemiBold)
        }

        Spacer(Modifier.height(16.dp))

        TextButton(onClick = onOpenRegister) {
            Text("Chưa có tài khoản? Đăng ký ngay", color = PizzaHutRed, fontWeight = FontWeight.SemiBold)
        }
    }
}
