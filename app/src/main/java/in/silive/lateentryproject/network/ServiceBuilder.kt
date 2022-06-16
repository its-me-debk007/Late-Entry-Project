package `in`.silive.lateentryproject.network

import `in`.silive.lateentryproject.ui.fragments.SplashScreenFragment
import android.util.Log
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ServiceBuilder {
    private const val baseURL = "https://lateentry.azurewebsites.net"

    private val tokenClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val request = chain.request()
                .newBuilder()
                .addHeader(
                    "Authorization",
                    "Bearer ${SplashScreenFragment.ACCESS_TOKEN}"
                )
                .build()

            chain.proceed(request)
        }
        .build()

    private val retrofit = if (SplashScreenFragment.ACCESS_TOKEN == null || SplashScreenFragment
			.ACCESS_TOKEN == "_") Retrofit.Builder()
        	.baseUrl(baseURL)
        	.addConverterFactory(GsonConverterFactory.create())
        	.client(tokenClient)
        	.build()
		else Retrofit.Builder()
			.baseUrl(baseURL)
			.addConverterFactory(GsonConverterFactory.create())
			.build()

    fun buildService(): ApiInterface {
		Log.e("dddd", "access token is: ${SplashScreenFragment.ACCESS_TOKEN}")
        return retrofit.create(ApiInterface::class.java)
    }
}