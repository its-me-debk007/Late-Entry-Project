package `in`.silive.lateentryproject.view_models

import `in`.silive.lateentryproject.models.MessageDataClass
import `in`.silive.lateentryproject.repositories.LateEntryRepository
import `in`.silive.lateentryproject.room_database.StudentDatabase
import `in`.silive.lateentryproject.sealed_class.Response
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class LateEntryViewModel() : ViewModel() {
	val addToDbLiveData = MutableLiveData<Pair<String, Int>>()
	private var lateEntryResult: MutableLiveData<Response<MessageDataClass>> = MutableLiveData()
	val _lateEntryResult: LiveData<Response<MessageDataClass>>
		get() = lateEntryResult

	fun submitResult(studentNo: String, venue: Int) = viewModelScope.launch {
		lateEntryResult = LateEntryRepository().lateEntry(studentNo, venue)
	}
}
