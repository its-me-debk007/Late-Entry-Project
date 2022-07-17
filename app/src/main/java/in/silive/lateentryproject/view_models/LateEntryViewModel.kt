package `in`.silive.lateentryproject.view_models

import `in`.silive.lateentryproject.models.MessageDataClass
import `in`.silive.lateentryproject.repositories.LateEntryRepository
import `in`.silive.lateentryproject.sealed_class.Response
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class LateEntryViewModel : ViewModel() {
	private var lateEntryResult: MutableLiveData<Response<MessageDataClass>> = MutableLiveData()
	val _lateEntryResult: LiveData<Response<MessageDataClass>>
		get() = lateEntryResult

	fun submitResult(studentNo: String, venue: Int, context: Context) = viewModelScope.launch {
		lateEntryResult = LateEntryRepository().lateEntry(studentNo, venue, context)
	}
}
