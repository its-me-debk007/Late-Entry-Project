package `in`.silive.lateentryproject.repositories

import `in`.silive.lateentryproject.models.BulkDataClass
import `in`.silive.lateentryproject.network.ServiceBuilder
import `in`.silive.lateentryproject.room_database.StudentDatabase
import `in`.silive.lateentryproject.sealed_class.Response
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback

class BulkDataRepo(private val studentDatabase: StudentDatabase) {
    private val bulkDataLiveData = MutableLiveData<Response<BulkDataClass>>()

    fun cacheData(): MutableLiveData<Response<BulkDataClass>> {
        val call = ServiceBuilder.buildService().cacheData()

        call.enqueue(object : Callback<BulkDataClass?> {
            override fun onResponse(
                call: Call<BulkDataClass?>,
                response: retrofit2.Response<BulkDataClass?>
            ) {

                if (response.isSuccessful) {
                    val responseBody = response.body()!!
                    GlobalScope.launch {
                        studentDatabase.studentDao().clearStudentTable()
                        studentDatabase.studentDao().addStudent(responseBody.student_data)
                    }
                    bulkDataLiveData.postValue(Response.Success(responseBody))

                } else
                    bulkDataLiveData.postValue(Response.Error(response.message()))
            }

            override fun onFailure(call: Call<BulkDataClass?>, t: Throwable) {

                val message = t.message?.let {
                    if (it.length == 7 || it.substring(0, 17)
                            .equals("failed to connect", ignoreCase = true)
                        || it.substring(0, 22) == "Unable to resolve host"
                    ) "No or poor Internet connection!" // it.length == 7 refers to it = "timeout"

                    else "$it Please try again"
                } ?: "Please try again"

                bulkDataLiveData.postValue(Response.Error(message))
            }
        })
        return bulkDataLiveData
    }
}