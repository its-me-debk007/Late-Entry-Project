package `in`.silive.lateentryproject.view_models

import `in`.silive.lateentryproject.models.MessageDataClass
import `in`.silive.lateentryproject.models.TokenDataClass
import `in`.silive.lateentryproject.repositories.LoginRepository
import `in`.silive.lateentryproject.sealed_class.Response
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {

	var loginLiveData = MutableLiveData<Response<MessageDataClass>>()

	fun login(email: String, password: String) = viewModelScope.launch {
		loginLiveData = LoginRepository().login(email, password)
	}
}