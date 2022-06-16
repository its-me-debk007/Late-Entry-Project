package `in`.silive.lateentryproject.view_models

import `in`.silive.lateentryproject.models.BulkReqDataClass
import `in`.silive.lateentryproject.models.MessageDataClass
import `in`.silive.lateentryproject.repositories.FailedEntriesRepository
import `in`.silive.lateentryproject.sealed_class.Response
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class FailedEntriesViewModel : ViewModel() {

	var bulkLiveData = MutableLiveData<Response<MessageDataClass>>()

	fun bulkUpload(body: BulkReqDataClass): MutableLiveData<Response<MessageDataClass>> {
		viewModelScope.launch {
			bulkLiveData = FailedEntriesRepository().bulkUpload(body)
		}
		return bulkLiveData

	}
}