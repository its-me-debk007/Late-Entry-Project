package `in`.silive.lateentryproject.repositories

import `in`.silive.lateentryproject.models.MessageDataClass
import `in`.silive.lateentryproject.network.ServiceBuilder
import `in`.silive.lateentryproject.sealed_class.ErrorPojoClass
import `in`.silive.lateentryproject.sealed_class.Response
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import retrofit2.Call
import retrofit2.Callback

class LoginRepository {
	private val loginLiveData = MutableLiveData<Response<MessageDataClass>>()

	fun login(email: String, password: String): MutableLiveData<Response<MessageDataClass>> {
		val call = ServiceBuilder.buildService().login(email, password)

		call.enqueue(object : Callback<MessageDataClass> {
			override fun onResponse(
				call: Call<MessageDataClass>,
				response: retrofit2.Response<MessageDataClass>
			) {
				when {
					response.isSuccessful -> {
						val responseBody = response.body()!!
						loginLiveData.postValue(Response.Success(responseBody))
					}
					response.code() == 403 -> {
						val gson: Gson = GsonBuilder().create()
						val mError: ErrorPojoClass =
							gson.fromJson(response.errorBody()?.string(),
										  ErrorPojoClass::class.java)
						loginLiveData.postValue(mError.message?.let { Response.Error(it) })
					}
					else -> loginLiveData.postValue(Response.Error(response.message()))
				}
			}

			override fun onFailure(call: Call<MessageDataClass>, t: Throwable) {
				val message = if (t.message == "Unable to resolve host \"lateentry.herokuapp" +
					".com\": No address associated with hostname") "No Internet connection. " +
						"Please connect to the Internet first!" else t.message + "\nPlease try again"

				loginLiveData.postValue(Response.Error(message))
			}
		})
		return loginLiveData
	}
}