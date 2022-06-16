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
				.addHeader("Authorization", "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ0b2tlbl90eXBlIjoiYWNjZXNzIiwiZXhwIjoxNjU1NDgzOTA0LCJpYXQiOjE2NTUzOTc1MDQsImp0aSI6IjI2YjE3YzE1M2Y4MjRkYjZhNmVmZGQ3ZDY5MTYyZDNkIiwidXNlcl9pZCI6MX0.4HAaQEZBKBU2ST0mfi7t8LtLxdV-dpZ-yBU_HryQM-o")
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