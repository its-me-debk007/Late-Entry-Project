package `in`.silive.lateentryproject.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
	private const val baseURL = "https://lateentry.herokuapp.com/"

	fun getInstance(): ApiInterface {
		return Retrofit.Builder()
			.baseUrl(baseURL)
			.addConverterFactory(GsonConverterFactory.create())
			.build()
			.create(ApiInterface::class.java)
	}
}