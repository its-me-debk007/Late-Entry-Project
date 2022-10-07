package `in`.silive.lateentryproject.repositories

import `in`.silive.lateentryproject.models.BulkDataClass
import `in`.silive.lateentryproject.network.ServiceBuilder
import `in`.silive.lateentryproject.room_database.StudentDatabase
import `in`.silive.lateentryproject.sealed_class.Response
import `in`.silive.lateentryproject.utils.generateNewToken
import android.content.Context
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback

class BulkDataRepo(private val studentDatabase: StudentDatabase) {
	private val bulkDataLiveData = MutableLiveData<Response<BulkDataClass>>()

	fun cacheData(context: Context): MutableLiveData<Response<BulkDataClass>> {
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

				} else if (response.code() == 401) {
					generateNewToken(context)
					cacheData(context)
				} else {
					bulkDataLiveData.postValue(Response.Error(response.message()))
				}
			}

			override fun onFailure(call: Call<BulkDataClass?>, t: Throwable) {
//				val message =
//					if (t.message == "Failed to connect to /13.232.227.118:80")
//						"No Internet connection!" else t.message + " Please try again"

				val message = t.message?.let {
					if (it.length == 7 || it.substring(0, 17)
							.equals("failed to connect", ignoreCase = true)
					) "No or poor Internet connection!" // it.length == 7 refers to it = "timeout"

					else "${t.message} Please try again"
				} ?: "Please try again"

				bulkDataLiveData.postValue(Response.Error(message))
			}
		})
		return bulkDataLiveData
	}
}