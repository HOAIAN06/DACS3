package com.fastdash.app.data.remote.retrofit

import android.content.Context
import android.util.Log
import com.fastdash.app.data.remote.api.AuthApi
import com.fastdash.app.data.remote.api.BranchApi
import com.fastdash.app.data.remote.api.CartApi
import com.fastdash.app.data.remote.api.CategoryApi
import com.fastdash.app.data.remote.api.OrderApi
import com.fastdash.app.data.remote.api.ProductApi
import com.fastdash.app.data.remote.api.UserApi
import com.fastdash.app.data.remote.interceptor.AuthInterceptor
import com.fastdash.app.data.remote.api.AdminProductApi
import com.fastdash.app.data.remote.api.AdminUserApi
import com.fastdash.app.data.remote.api.AdminBranchApi
import com.fastdash.app.data.remote.api.AdminToppingApi
import com.fastdash.app.data.remote.api.AdminSizeApi
import com.fastdash.app.data.remote.api.AdminPaymentApi
import com.fastdash.app.data.remote.api.AdminOrderStatusApi
import com.fastdash.app.data.remote.api.AdminCategoryApi
import com.fastdash.app.data.remote.api.AdminProductApiExtended
import com.fastdash.app.data.remote.api.AdminOrderApi
import com.fastdash.app.data.remote.api.AdminDashboardApi
import com.fastdash.app.utils.Constants
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val TAG = "RetrofitClient"

	@Volatile
	private var retrofit: Retrofit? = null

	private fun buildRetrofit(context: Context): Retrofit {
        Log.i(TAG, "Using BASE_URL=${Constants.BASE_URL}")
		val logging = HttpLoggingInterceptor().apply {
			level = HttpLoggingInterceptor.Level.BODY
		}

		val okHttpClient = OkHttpClient.Builder()
			.addInterceptor(AuthInterceptor(context))
			.addInterceptor(logging)
			.build()

		val gson = GsonBuilder()
			.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
			.create()

		return Retrofit.Builder()
			.baseUrl(Constants.BASE_URL)
			.client(okHttpClient)
			.addConverterFactory(GsonConverterFactory.create(gson))
			.build()
	}

	private fun getRetrofit(context: Context): Retrofit {
		retrofit?.let { return it }

		return synchronized(this) {
			retrofit ?: buildRetrofit(context.applicationContext).also { retrofit = it }
		}
	}

	fun authApi(context: Context): AuthApi {
		return getRetrofit(context).create(AuthApi::class.java)
	}

	fun userApi(context: Context): UserApi {
		return getRetrofit(context).create(UserApi::class.java)
	}

	fun categoryApi(context: Context): CategoryApi {
		return getRetrofit(context).create(CategoryApi::class.java)
	}

	fun branchApi(context: Context): BranchApi {
		return getRetrofit(context).create(BranchApi::class.java)
	}

	fun productApi(context: Context): ProductApi {
		return getRetrofit(context).create(ProductApi::class.java)
	}

	fun orderApi(context: Context): OrderApi {
		return getRetrofit(context).create(OrderApi::class.java)
	}

	fun cartApi(context: Context): CartApi {
		return getRetrofit(context).create(CartApi::class.java)
	}
	fun adminProductApi(context: Context): AdminProductApi {
		return getRetrofit(context).create(AdminProductApi::class.java)
	}

	fun adminUserApi(context: Context): AdminUserApi {
		return getRetrofit(context).create(AdminUserApi::class.java)
	}

	fun adminBranchApi(context: Context): AdminBranchApi {
		return getRetrofit(context).create(AdminBranchApi::class.java)
	}

	fun adminToppingApi(context: Context): AdminToppingApi {
		return getRetrofit(context).create(AdminToppingApi::class.java)
	}

	fun adminSizeApi(context: Context): AdminSizeApi {
		return getRetrofit(context).create(AdminSizeApi::class.java)
	}

	fun adminPaymentApi(context: Context): AdminPaymentApi {
		return getRetrofit(context).create(AdminPaymentApi::class.java)
	}

	fun adminOrderStatusApi(context: Context): AdminOrderStatusApi {
		return getRetrofit(context).create(AdminOrderStatusApi::class.java)
	}

	fun adminCategoryApi(context: Context): AdminCategoryApi {
		return getRetrofit(context).create(AdminCategoryApi::class.java)
	}

	fun adminProductApiExtended(context: Context): AdminProductApiExtended {
		return getRetrofit(context).create(AdminProductApiExtended::class.java)
	}

	fun adminOrderApi(context: Context): AdminOrderApi {
		return getRetrofit(context).create(AdminOrderApi::class.java)
	}

	fun adminDashboardApi(context: Context): AdminDashboardApi {
		return getRetrofit(context).create(AdminDashboardApi::class.java)
	}

}
