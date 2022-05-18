package `in`.silive.lateentryproject.viewModels

import `in`.silive.lateentryproject.models.ResponseBody
import `in`.silive.lateentryproject.repositories.LoginRepository
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginViewModel : ViewModel() {

	val loginLiveData = MutableLiveData<String>()

	fun login(email: String, password: String) {
		val repository = LoginRepository()

		repository.login(email, password).enqueue(object : Callback<ResponseBody> {
			override fun onResponse(call: Call<ResponseBody>,
									response: Response<ResponseBody>) {
				if (response.isSuccessful) {
					loginLiveData.postValue(response.body()!!.message)
					Log.e("dddd", response.code().toString() + response.body()?.message)
				}
				else {
					loginLiveData.postValue(response.body()?.message + "\nPlease try again")
					Log.e("dddd", response.code().toString() + response.body()?.message)
				}
			}

			override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
				loginLiveData.postValue(t.message + "\nPlease try again")
			}
		})
	}
}