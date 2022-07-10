package `in`.silive.lateentryproject.repositories

import `in`.silive.lateentryproject.models.BulkDataClass
import `in`.silive.lateentryproject.network.ServiceBuilder
import `in`.silive.lateentryproject.room_database.StudentDatabase
import `in`.silive.lateentryproject.sealed_class.ErrorPojoClass
import `in`.silive.lateentryproject.sealed_class.Response
import `in`.silive.lateentryproject.ui.activities.MainActivity
import `in`.silive.lateentryproject.utils.Datastore
import `in`.silive.lateentryproject.utils.Utils
import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import retrofit2.Call
import retrofit2.Callback

class BulkDataRepo(private val studentDatabase: StudentDatabase) {
	private val bulkDataLiveData = MutableLiveData<Response<BulkDataClass>>()

	fun cacheData(context:Context): MutableLiveData<Response<BulkDataClass>> {
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

				}else if(response.code()==401){
					runBlocking{
						Datastore(context).getAccessToken()?.let {
							Datastore(context).getRefreshToken()?.let { it1 ->
								Utils().generateToken(
									it, it1,context
								)
							}
						}
						cacheData(context)
					}
				}
				else {
					bulkDataLiveData.postValue(Response.Error(response.message()))
				}
			}

			override fun onFailure(call: Call<BulkDataClass?>, t: Throwable) {
				val message = if (t.message == "Unable to resolve host \"lateentry.azurewebsites.net\": No address associated with hostname")
					"No Internet connection! Please connect to the Internet first!" else t.message+ " Please try again"

				bulkDataLiveData.postValue(Response.Error(message))
			}
		})
		return bulkDataLiveData
	}
}