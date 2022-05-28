package `in`.silive.lateentryproject.repositories

import `in`.silive.lateentryproject.models.BulkDataClass
import `in`.silive.lateentryproject.network.ServiceBuilder
import `in`.silive.lateentryproject.sealed_class.Response
import android.util.Log
import androidx.lifecycle.MutableLiveData
import retrofit2.Call
import retrofit2.Callback

class BulkDataRepo {
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
					bulkDataLiveData.postValue(Response.Success(responseBody))

				} else {
					bulkDataLiveData.postValue(Response.Error(response.message()))
				}
			}

			override fun onFailure(call: Call<BulkDataClass?>, t: Throwable) {
				bulkDataLiveData.postValue(Response.Error("Something went wrong ${t.message}"))
				Log.e("dddd", t.message.toString())
			}
		})
		return bulkDataLiveData
	}
}