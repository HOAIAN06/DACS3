package com.fastdash.app.data.remote.interceptor

import android.content.Context
import com.fastdash.app.utils.TokenManager
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(context: Context) : Interceptor {

    private val tokenManager = TokenManager(context)
    private val authPathsWithoutBearer = setOf(
        "/api/v1/auth/login",
        "/api/v1/auth/register",
        "/api/v1/auth/google",
        "/api/v1/auth/forgot-password",
        "/api/v1/auth/verify-reset-code",
        "/api/v1/auth/reset-password"
    )

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = tokenManager.getToken()
        val request = chain.request()
        val encodedPath = request.url.encodedPath
        val hasAuthorizationHeader = !request.header("Authorization").isNullOrBlank()

        val requestBuilder = request.newBuilder()

        if (!hasAuthorizationHeader && !token.isNullOrEmpty() && encodedPath !in authPathsWithoutBearer) {
            requestBuilder.addHeader("Authorization", "Bearer $token")
        }

        val response = chain.proceed(requestBuilder.build())

        if (response.code == 401) {
            tokenManager.clear()
        }

        return response
    }
}
