package `in`.silive.lateentryproject.repositories

import `in`.silive.lateentryproject.models.BulkReqDataClass
import `in`.silive.lateentryproject.network.ServiceBuilder
import `in`.silive.lateentryproject.sealed_class.ErrorPojoClass
import `in`.silive.lateentryproject.sealed_class.Response
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import retrofit2.Call
import retrofit2.Callback

class BulkRepository {
	private val liveData = MutableLiveData<Response<BulkReqDataClass>>()

	fun bulkUpload(body: BulkReqDataClass): MutableLiveData<Response<BulkReqDataClass>> {
		val call = ServiceBuilder.buildService().bulkUpload(body)

		call.enqueue(object : Callback<BulkReqDataClass> {
			override fun onResponse(
				call: Call<BulkReqDataClass>,
				response: retrofit2.Response<BulkReqDataClass>
			) {
				when {
					response.isSuccessful ->
						liveData.postValue(Response.Success(response.body()!!))

					response.code() == 403 -> {
						val gson: Gson = GsonBuilder().create()
						val mError: ErrorPojoClass =
							gson.fromJson(response.errorBody()?.string(),
										  ErrorPojoClass::class.java)
						liveData.postValue(mError.message?.let { Response.Error(it) })
					}

					else -> liveData.postValue(Response.Error(response.message()))
				}
			}

			override fun onFailure(call: Call<BulkReqDataClass>, t: Throwable) {
				val message = if (t.message == "Unable to resolve host \"lateentry.herokuapp" +
					".com\": No address associated with hostname") "No Internet connection. " +
						"Please connect to the Internet first!" else t.message.toString() + "\nPlease try again"

				liveData.postValue(Response.Error(message))
			}
		})

		return liveData
	}
}