package `in`.silive.lateentryproject.view_models

import `in`.silive.lateentryproject.models.BulkReqDataClass
import `in`.silive.lateentryproject.repositories.BulkRepository
import `in`.silive.lateentryproject.sealed_class.Response
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class SettingsViewModel : ViewModel() {

	var bulkLiveData = MutableLiveData<Response<BulkReqDataClass>>()

	fun bulkUpload(body: BulkReqDataClass) = viewModelScope.launch {
		bulkLiveData = BulkRepository().bulkUpload(body)
	}
}