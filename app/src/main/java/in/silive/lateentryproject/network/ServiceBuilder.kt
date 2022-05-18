package `in`.silive.lateentryproject.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ServiceBuilder {
    private const val baseURL = "https://lateentry.herokuapp.com"

    private val retrofit = Retrofit.Builder()
        .baseUrl(baseURL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    fun buildService(): ApiInterface {
        return retrofit.create(ApiInterface::class.java)
    }
}