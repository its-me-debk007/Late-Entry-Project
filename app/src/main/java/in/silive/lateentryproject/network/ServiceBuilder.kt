package `in`.silive.lateentryproject.network

import `in`.silive.lateentryproject.ui.fragments.SplashScreenFragment
import `in`.silive.lateentryproject.utils.BASE_URL
import `in`.silive.lateentryproject.utils.generateNewToken
import okhttp3.Authenticator
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

object ServiceBuilder {

    fun buildService(isTokenRequired: Boolean = true): ApiInterface {
        if (!isTokenRequired) return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(ApiInterface::class.java)

        val tokenInterceptor = Interceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("Authorization", "Bearer ${SplashScreenFragment.ACCESS_TOKEN}")
                .build()

            chain.proceed(request)
        }

        val tokenAuthenticator = Authenticator { _, response ->
            generateNewToken()

            response.request().newBuilder()
                .header("Authorization", "Bearer ${SplashScreenFragment.ACCESS_TOKEN}")
                .build()
        }

        val tokenClient = OkHttpClient.Builder()
            .addInterceptor(tokenInterceptor)
            .authenticator(tokenAuthenticator)
            .connectTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create())
            .client(tokenClient)
            .build()
            .create(ApiInterface::class.java)
    }
}