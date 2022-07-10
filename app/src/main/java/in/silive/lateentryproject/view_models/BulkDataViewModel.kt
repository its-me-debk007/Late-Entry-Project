package `in`.silive.lateentryproject.view_models

import `in`.silive.lateentryproject.models.BulkDataClass
import `in`.silive.lateentryproject.repositories.BulkDataRepo
import `in`.silive.lateentryproject.room_database.StudentDatabase
import `in`.silive.lateentryproject.sealed_class.Response
import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch


class BulkDataViewModel(private val studentDatabase: StudentDatabase) : ViewModel() {

	private var bulkDataResult: MutableLiveData<Response<BulkDataClass>> = MutableLiveData()
	val _bulkDataResult: LiveData<Response<BulkDataClass>>
		get() = bulkDataResult

    fun sendResult(context: Context) = viewModelScope.launch {
        bulkDataResult = BulkDataRepo(studentDatabase).cacheData(context)

    }
}
