package `in`.silive.lateentryproject.network

import `in`.silive.lateentryproject.models.LateEntryDataClass
import `in`.silive.lateentryproject.models.MessageDataClass
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface ApiInterface {

	@POST("/auth/login/")
	@FormUrlEncoded
	fun login(@Field("email") email: String, @Field("password") password: String): Call<MessageDataClass>

	@POST("/entry/scan/")
	fun lateEntry(@Body data: LateEntryDataClass): Call<MessageDataClass>

}