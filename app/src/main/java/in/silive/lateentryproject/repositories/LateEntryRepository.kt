package `in`.silive.lateentryproject.repositories

import `in`.silive.lateentryproject.models.LateEntryDataClass
import `in`.silive.lateentryproject.models.MessageDataClass
import `in`.silive.lateentryproject.network.ServiceBuilder
import `in`.silive.lateentryproject.sealed_class.ErrorPojoClass
import `in`.silive.lateentryproject.sealed_class.Response
import `in`.silive.lateentryproject.utils.Datastore
import `in`.silive.lateentryproject.utils.Utils
import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import retrofit2.Call
import retrofit2.Callback

class LateEntryRepository() {
	private val lateEntryLiveData = MutableLiveData<Response<MessageDataClass>>()

	fun lateEntry(
		studentNo: String, venue: Int,context: Context): MutableLiveData<Response<MessageDataClass>> {
		val call = ServiceBuilder.buildService()
			.lateEntry(LateEntryDataClass(studentNo, Utils().currentTimeInIsoFormat(), venue))
		Log.i("lateentry", "lateEntry: "+studentNo+venue+Utils().currentTimeInIsoFormat())
		call.enqueue(object : Callback<MessageDataClass?> {
			override fun onResponse(
				call: Call<MessageDataClass?>,
				response: retrofit2.Response<MessageDataClass?>
			) {
				if (response.isSuccessful) {
					val responseBody = response.body()!!
					lateEntryLiveData.postValue(Response.Success(responseBody))


				}else if(response.code()==401){
					GlobalScope.launch {
						Datastore(context).getAccessToken()?.let {
							Datastore(context).getRefreshToken()?.let { it1 ->
								Utils().generateToken(
									it, it1,context
								)
							}
						}
						lateEntry(studentNo, venue,context)
					}
				} else if (response.code() == 400) {
					val gson: Gson = GsonBuilder().create()
					val mError: ErrorPojoClass =
						gson.fromJson(response.errorBody()?.string(), ErrorPojoClass::class.java)
					lateEntryLiveData.postValue(mError.message?.let { Response.Error(it) })
				}
			}

			override fun onFailure(call: Call<MessageDataClass?>, t: Throwable) {
				lateEntryLiveData.postValue(Response.Error("Save to DB"))
			}
		})
		return lateEntryLiveData
	}
}