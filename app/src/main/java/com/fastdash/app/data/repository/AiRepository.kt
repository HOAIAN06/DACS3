package com.fastdash.app.data.repository

import android.content.Context
import com.fastdash.app.data.model.request.AiChatRequest
import com.fastdash.app.data.model.response.AiChatResponse
import com.fastdash.app.data.remote.retrofit.RetrofitClient
import com.fastdash.app.utils.TokenManager
import java.io.IOException
import java.net.SocketTimeoutException
import retrofit2.HttpException

sealed interface AiRepositoryResult {
    data class Success(val data: AiChatResponse) : AiRepositoryResult
    data class Error(val message: String) : AiRepositoryResult
}

class AiRepository(private val context: Context) {
    private val appContext = context.applicationContext
    private val tokenManager = TokenManager(appContext)

    suspend fun chat(message: String): AiRepositoryResult {
        val token = tokenManager.getToken().orEmpty().trim()
        if (token.isBlank()) {
            return AiRepositoryResult.Error("Bạn cần đăng nhập để sử dụng trợ lý AI.")
        }

        return try {
            val response = RetrofitClient.aiApi(appContext).chatWithAi(
                token = "Bearer $token",
                request = AiChatRequest(message = message)
            )
            AiRepositoryResult.Success(response)
        } catch (exception: SocketTimeoutException) {
            AiRepositoryResult.Error("Kết nối tới trợ lý AI bị quá thời gian. Vui lòng thử lại.")
        } catch (exception: IOException) {
            AiRepositoryResult.Error("Không thể kết nối mạng. Vui lòng kiểm tra Internet và thử lại.")
        } catch (exception: HttpException) {
            val messageText = when (exception.code()) {
                400 -> "Yêu cầu chưa hợp lệ. Vui lòng thử lại với nội dung khác."
                401 -> "Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại để dùng trợ lý AI."
                else -> "Trợ lý AI đang bận (${exception.code()}). Vui lòng thử lại sau."
            }
            AiRepositoryResult.Error(messageText)
        } catch (exception: Exception) {
            AiRepositoryResult.Error(exception.message ?: "Đã xảy ra lỗi khi kết nối trợ lý AI.")
        }
    }
}
