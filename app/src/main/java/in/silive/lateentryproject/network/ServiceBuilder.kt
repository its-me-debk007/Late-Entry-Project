package `in`.silive.lateentryproject.network

import `in`.silive.lateentryproject.ui.fragments.SplashScreenFragment
import android.util.Log
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.HttpURLConnection
import java.util.concurrent.TimeUnit

object ServiceBuilder {
    private const val baseURL = "https://late-entry.azurewebsites.net"

	fun buildService(): ApiInterface {
//		Log.e("dddd", "access token is: ${SplashScreenFragment.ACCESS_TOKEN}")

		val retrofit: Retrofit
		if (SplashScreenFragment.ACCESS_TOKEN == null || SplashScreenFragment.ACCESS_TOKEN == "_") {
			Log.e("dddd", "access is null")
			retrofit = Retrofit.Builder()
				.baseUrl(baseURL)
				.addConverterFactory(GsonConverterFactory.create())
				.build()
		} else {
			Log.e("dddd", "access is not null")
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

			retrofit = Retrofit.Builder()
				.baseUrl(baseURL)
				.addConverterFactory(GsonConverterFactory.create())
				.client(tokenClient)
				.build()
		}

		return retrofit.create(ApiInterface::class.java)
    }
}