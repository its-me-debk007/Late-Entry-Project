package `in`.silive.lateentryproject.view_model_factories

import `in`.silive.lateentryproject.room_database.StudentDatabase
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class BulkDataViewModelFactory(private val studentDatabase: StudentDatabase) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return modelClass.getConstructor(StudentDatabase::class.java).newInstance(studentDatabase)
    }

}