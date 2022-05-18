package `in`.silive.lateentryproject.view_models

import `in`.silive.lateentryproject.model.Message_data_class
import `in`.silive.lateentryproject.repositories.LateEntryRepo
import `in`.silive.lateentryproject.sealed_class.Response
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch


class LateEntryViewModel() : ViewModel() {

    var studentNo = MutableLiveData<String>()
    var venue = MutableLiveData<Int>()


    private var lateEntryResult: MutableLiveData<Response<Message_data_class>> = MutableLiveData()
    val _lateEntryResult: LiveData<Response<Message_data_class>>
        get() = lateEntryResult

    fun submitResult() = viewModelScope.launch {
        lateEntryResult = LateEntryRepo().lateEntry(studentNo.value, venue.value)

    }
}
