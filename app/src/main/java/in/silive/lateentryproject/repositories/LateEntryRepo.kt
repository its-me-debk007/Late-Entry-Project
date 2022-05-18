package `in`.silive.lateentryproject.repositories

import `in`.silive.lateentryproject.models.LateEntryDataClass
import `in`.silive.lateentryproject.models.MessageDataClass
import `in`.silive.lateentryproject.network.ServiceBuilder
import `in`.silive.lateentryproject.sealed_class.Response
import android.util.Log
import androidx.lifecycle.MutableLiveData
import retrofit2.Call
import retrofit2.Callback

class LateEntryRepo {
	private val lateEntryLiveData = MutableLiveData<Response<MessageDataClass>>()

	fun lateEntry(
		studentNo: String?,
		venue: Int?
	): MutableLiveData<Response<MessageDataClass>> {
		val call = ServiceBuilder.buildService().lateEntry(
            LateEntryDataClass(
				student_no = studentNo,
				venue = venue
			)
		)

		call.enqueue(object : Callback<MessageDataClass?> {
			override fun onResponse(
				call: Call<MessageDataClass?>,
				response: retrofit2.Response<MessageDataClass?>
			) {
				if (response.isSuccessful) {
					val responseBody = response.body()!!
					lateEntryLiveData.postValue(Response.Success(responseBody))

				} else {
					lateEntryLiveData.postValue(Response.Error(response.message()))
					Log.e("dddd", response.code().toString()+response.message())
				}
			}

			override fun onFailure(call: Call<MessageDataClass?>, t: Throwable) {
				lateEntryLiveData.postValue(Response.Error("Something went wrong ${t.message}"))
			}
		})
		return lateEntryLiveData
	}
}