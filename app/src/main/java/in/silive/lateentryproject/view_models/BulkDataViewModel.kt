package `in`.silive.lateentryproject.view_models

import `in`.silive.lateentryproject.models.BulkDataClass
import `in`.silive.lateentryproject.repositories.BulkDataRepo
import `in`.silive.lateentryproject.sealed_class.Response
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch


class BulkDataViewModel : ViewModel() {

    private var bulkDataResult: MutableLiveData<Response<BulkDataClass>> = MutableLiveData()
    val _bulkDataResult: LiveData<Response<BulkDataClass>>
        get() = bulkDataResult

    fun sendResult() = viewModelScope.launch {
        bulkDataResult = BulkDataRepo().cacheData()
    }
}
