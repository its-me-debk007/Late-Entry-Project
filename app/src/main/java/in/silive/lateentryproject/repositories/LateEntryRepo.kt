package `in`.silive.lateentryproject.repositories

import `in`.silive.lateentryproject.model.Late_entry_data_class
import `in`.silive.lateentryproject.model.Message_data_class
import `in`.silive.lateentryproject.network.ServiceBuilder
import `in`.silive.lateentryproject.sealed_class.Response
import androidx.lifecycle.MutableLiveData
import retrofit2.Call
import retrofit2.Callback

class LateEntryRepo {
    private val lateEntryLiveData = MutableLiveData<Response<Message_data_class>>()

    fun lateEntry(
        studentNo: String?,
        venue: Int?
    ): MutableLiveData<Response<Message_data_class>> {
//        Log.i("user_idprofile", "$studentNo $venue")
        val call = ServiceBuilder.buildService().lateEntry(
            Late_entry_data_class(
                student_no = studentNo,
                venue = venue
            )
        )

        call.enqueue(object : Callback<Message_data_class?> {
            override fun onResponse(
                call: Call<Message_data_class?>,
                response: retrofit2.Response<Message_data_class?>
            ) {
                if (response.isSuccessful) {
                    val responseBody = response.body()!!
                    lateEntryLiveData.postValue(Response.Success(responseBody))

//                    Log.i("Helloprofile", "onActivityResult:" + responseBody)
                } else {
                    lateEntryLiveData.postValue(Response.Error(response.message()))
//                    Log.i("Helloprofile", "onActivityResult:" + response.message())
                }
            }

            override fun onFailure(call: Call<Message_data_class?>, t: Throwable) {
                lateEntryLiveData.postValue(Response.Error("Something went wrong ${t.message}"))
//                Log.i("Helloprofile", "onActivityResult: failed")
            }
        })
        return lateEntryLiveData
    }
}