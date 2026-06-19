package com.fastdash.app.ui.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

object AuthDestinations {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val FORGOT_PASSWORD = "forgot_password"
    const val OTP = "otp"
    const val RESET_PASSWORD = "reset_password"
}

@Composable
fun AuthNavGraph(
    navController: NavHostController = rememberNavController(),
    onLoginSuccess: () -> Unit
) {
    var forgotPasswordEmail by remember { mutableStateOf("") }
    var forgotPasswordCode by remember { mutableStateOf("") }
    var resendAvailableAt by remember { mutableLongStateOf(0L) }

    NavHost(
        navController = navController,
        startDestination = AuthDestinations.LOGIN
    ) {
        composable(AuthDestinations.LOGIN) {
            LoginScreen(
                onLoginSuccess = onLoginSuccess,
                onOpenRegister = { navController.navigate(AuthDestinations.REGISTER) },
                onOpenForgotPassword = { navController.navigate(AuthDestinations.FORGOT_PASSWORD) }
            )
        }

        composable(AuthDestinations.REGISTER) {
            RegisterScreen(
                onBackToLogin = { navController.popBackStack() },
                onRegisterSuccess = { 
                    navController.navigate(AuthDestinations.LOGIN) {
                        popUpTo(AuthDestinations.REGISTER) { inclusive = true }
                    }
                }
            )
        }

        composable(AuthDestinations.FORGOT_PASSWORD) {
            ForgotPasswordEmailScreen(
                initialEmail = forgotPasswordEmail,
                onBack = { navController.popBackStack() },
                onOtpSent = { email: String, cooldown: Long ->
                    forgotPasswordEmail = email
                    forgotPasswordCode = "" // Reset code when requesting new OTP
                    resendAvailableAt = cooldown
                    navController.navigate(AuthDestinations.OTP)
                }
            )
        }

        composable(AuthDestinations.OTP) {
            VerifyOtpScreen(
                initialEmail = forgotPasswordEmail,
                initialCode = forgotPasswordCode,
                resendAvailableAtMillis = resendAvailableAt,
                onBack = { navController.popBackStack() },
                onChangeEmail = { navController.popBackStack() },
                onVerified = { email: String, code: String ->
                    forgotPasswordEmail = email
                    forgotPasswordCode = code
                    navController.navigate(AuthDestinations.RESET_PASSWORD)
                },
                onCooldownChanged = { newCooldown: Long -> resendAvailableAt = newCooldown }
            )
        }

        composable(AuthDestinations.RESET_PASSWORD) {
            ResetPasswordScreen(
                email = forgotPasswordEmail,
                code = forgotPasswordCode,
                onBack = { navController.popBackStack() },
                onResetSuccess = {
                    navController.navigate(AuthDestinations.LOGIN) {
                        popUpTo(AuthDestinations.LOGIN) { inclusive = true }
                    }
                }
            )
        }
    }
}
