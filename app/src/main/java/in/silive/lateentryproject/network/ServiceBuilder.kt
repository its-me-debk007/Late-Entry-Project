package `in`.silive.lateentryproject.network

import `in`.silive.lateentryproject.ui.fragments.SplashScreenFragment
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

object ServiceBuilder {
	private const val baseURL = "http://13.232.227.118/"

	fun buildService(isTokenRequired: Boolean = true): ApiInterface {
		if (!isTokenRequired) return Retrofit.Builder()
			.baseUrl(baseURL)
			.addConverterFactory(MoshiConverterFactory.create())
			.build()
			.create(ApiInterface::class.java)

		val tokenInterceptor = Interceptor { chain ->
			var request = chain.request()
			request = request.newBuilder()
				.addHeader("Authorization", "Bearer ${SplashScreenFragment.ACCESS_TOKEN}")
				.build()
			chain.proceed(request)
		}

		val tokenClient = OkHttpClient.Builder()
			.addInterceptor(tokenInterceptor)
			.connectTimeout(30, TimeUnit.SECONDS)
			.writeTimeout(30, TimeUnit.SECONDS)
			.readTimeout(30, TimeUnit.SECONDS)
			.build()

		return Retrofit.Builder()
			.baseUrl(baseURL)
			.addConverterFactory(MoshiConverterFactory.create())
			.client(tokenClient)
			.build()
			.create(ApiInterface::class.java)
	}
}