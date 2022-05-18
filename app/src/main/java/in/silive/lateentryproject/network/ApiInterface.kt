
import `in`.silive.lateentryproject.models.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface ApiInterface {

	@POST("auth/login/")
	@FormUrlEncoded
	fun login(
		@Field("email") email: String,
		@Field("password") password: String
	): Call<ResponseBody>

	@POST("/entry/scan/")
	fun lateEntry(@Body data: Late_entry_data_class): Call<Message_data_class>
}