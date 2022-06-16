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
                .addHeader(
                    "Authorization",
                    "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ0b2tlbl90eXBlIjoiYWNjZXNzIiwiZXhwIjoxNjU1NDg1MzU3LCJpYXQiOjE2NTUzOTg5NTcsImp0aSI6ImI0Y2E2MWU4MTE1ZDQ1MTZiM2FkNWNkMThlYWFhMWFkIiwidXNlcl9pZCI6MX0.8G5YCjnBjuYilbZLrjsCOki7qAM2OGvUBC8f0T63y_Y"
                )
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