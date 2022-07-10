package `in`.silive.lateentryproject.repositories

import `in`.silive.lateentryproject.models.BulkDataClass
import `in`.silive.lateentryproject.network.ServiceBuilder
import `in`.silive.lateentryproject.room_database.StudentDatabase
import `in`.silive.lateentryproject.sealed_class.ErrorPojoClass
import `in`.silive.lateentryproject.sealed_class.Response
import `in`.silive.lateentryproject.ui.activities.MainActivity
import `in`.silive.lateentryproject.utils.Utils
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.GsonBuilder
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
						studentDatabase.studentDao().addStudent(responseBody.student_data)
					}
					bulkDataLiveData.postValue(Response.Success(responseBody))

				} else {
					bulkDataLiveData.postValue(Response.Error(response.message()))
				}
			}

			override fun onFailure(call: Call<BulkDataClass?>, t: Throwable) {
				val message = if (t.message?.substring(0, 22) == "Unable to resolve host")
					"No Internet connection" else t.message + " Please try again"

				bulkDataLiveData.postValue(Response.Error(message))
			}
		})
		return bulkDataLiveData
	}
}