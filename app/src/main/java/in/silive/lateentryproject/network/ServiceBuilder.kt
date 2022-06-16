package `in`.silive.lateentryproject.network

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ServiceBuilder {
	private const val baseURL = "https://lateentry.azurewebsites.net"

	private val tokenClient = OkHttpClient.Builder()
		.addInterceptor { chain ->
			val request = chain.request()
				.newBuilder()
				.addHeader("Authorization", "Bearer ${9}")
				.build()

			chain.proceed(request)
		}
		.build()

	private val retrofit = Retrofit.Builder()
		.baseUrl(baseURL)
		.addConverterFactory(GsonConverterFactory.create())
		.client(tokenClient)
		.build()

	fun buildService(): ApiInterface {
		return retrofit.create(ApiInterface::class.java)
	}
}