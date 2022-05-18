package `in`.silive.lateentryproject.repositories

import `in`.silive.lateentryproject.models.ResponseBody
import `in`.silive.lateentryproject.network.ServiceBuilder
import androidx.lifecycle.MutableLiveData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginRepository {
	private val loginLiveData = MutableLiveData<String>()

	fun login(email: String, password: String): MutableLiveData<String> {
		val call = ServiceBuilder.buildService().login(email, password)

		call.enqueue(object : Callback<ResponseBody> {
			override fun onResponse(call: Call<ResponseBody>,
									response: Response<ResponseBody>) {
				when {
					response.isSuccessful ->
						loginLiveData.postValue(response.body()!!.message)

					response.code() == 401 ->
						loginLiveData.postValue("Incorrect password\nPlease try again")

					response.code() == 406 ->
						loginLiveData.postValue("No matching user found\nPlease try again")
				}
			}

			override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
				loginLiveData.postValue(t.message + "\nPlease try again")
			}
		})
		return loginLiveData
	}

}