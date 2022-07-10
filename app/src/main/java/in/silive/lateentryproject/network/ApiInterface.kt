package `in`.silive.lateentryproject.network

import `in`.silive.lateentryproject.models.*
import retrofit2.Call
import retrofit2.http.*

interface ApiInterface {

	@POST("/auth/login/")
	@FormUrlEncoded
	fun login(@Field("email") email: String, @Field("password") password: String): Call<MessageDataClass>

	@POST("/entry/scan/")
	fun lateEntry(@Body body: LateEntryDataClass): Call<MessageDataClass>

	@POST("/entry/bulk/")
	fun bulkUpload(@Body body: BulkReqDataClass): Call<MessageDataClass>

	@GET("/entry/syncall/")
	fun cacheData():Call<BulkDataClass>

	@POST("/auth/token/refresh/")
	fun generateToken(@Body data:TokenDataClass): Call<TokenDataClass>

}