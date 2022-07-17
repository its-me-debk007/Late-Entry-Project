package `in`.silive.lateentryproject.view_models

import `in`.silive.lateentryproject.models.BulkReqDataClass
import `in`.silive.lateentryproject.models.MessageDataClass
import `in`.silive.lateentryproject.repositories.FailedEntriesRepository
import `in`.silive.lateentryproject.sealed_class.Response
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class FailedEntriesViewModel : ViewModel() {

	private var bulkLiveData = MutableLiveData<Response<MessageDataClass>>()
	val _bulkLiveData: LiveData<Response<MessageDataClass>>
		get() = bulkLiveData

	fun bulkUpload(body: BulkReqDataClass, context: Context) {
		viewModelScope.launch {
			bulkLiveData = FailedEntriesRepository().bulkUpload(body, context)
		}

	}
}