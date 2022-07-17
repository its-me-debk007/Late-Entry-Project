package `in`.silive.lateentryproject.repositories

import `in`.silive.lateentryproject.models.BulkReqDataClass
import `in`.silive.lateentryproject.models.MessageDataClass
import `in`.silive.lateentryproject.network.ServiceBuilder
import `in`.silive.lateentryproject.sealed_class.ErrorPojoClass
import `in`.silive.lateentryproject.sealed_class.Response
import `in`.silive.lateentryproject.utils.Utils
import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import retrofit2.Call
import retrofit2.Callback

class FailedEntriesRepository {

	private val liveData = MutableLiveData<Response<MessageDataClass>>()
	fun bulkUpload(body: BulkReqDataClass,
				   context: Context): MutableLiveData<Response<MessageDataClass>> {
		val call = ServiceBuilder.buildService().bulkUpload(body)

		call.enqueue(object : Callback<MessageDataClass> {
			override fun onResponse(
				call: Call<MessageDataClass>,
				response: retrofit2.Response<MessageDataClass>
			) {
				when {
					response.isSuccessful -> {
						liveData.postValue(Response.Success(response.body()!!))
					}
					response.code() == 401 -> {
						Utils().generateNewToken(context)
						bulkUpload(body, context)
					}
					response.code() == 403 -> {
						val gson: Gson = GsonBuilder().create()
						val mError: ErrorPojoClass =
							gson.fromJson(
								response.errorBody()?.string(),
								ErrorPojoClass::class.java
							)
						liveData.postValue(mError.message?.let { Response.Error(it) })
					}

					else -> liveData.postValue(Response.Error(response.message()))
				}

			}

			override fun onFailure(call: Call<MessageDataClass>, t: Throwable) {
//				val message = if (t.message?.substring(0, 22) == "Unable to resolve host")
//					"No Internet connection"
//					else t.message + " Please try again"
				val message =
					if (t.message == "Unable to resolve host \"lateentry.azurewebsites.net\": No address associated with hostname")
						"No Internet connection! Please connect to the Internet first!" else t.message + " Please try again"

				liveData.postValue(Response.Error(message))
			}
		})

		return liveData
	}
}