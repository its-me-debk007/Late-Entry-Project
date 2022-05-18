package `in`.silive.lateentryproject.view_models

import `in`.silive.lateentryproject.models.ResponseBody
import `in`.silive.lateentryproject.repositories.LoginRepository
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginViewModel : ViewModel() {

	var loginLiveData = MutableLiveData<String>()

	fun login(email: String, password: String) = viewModelScope.launch {
		loginLiveData = LoginRepository().login(email, password)
	}

}