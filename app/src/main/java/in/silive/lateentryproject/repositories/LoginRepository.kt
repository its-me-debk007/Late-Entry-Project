package `in`.silive.lateentryproject.repositories

import `in`.silive.lateentryproject.models.MessageDataClass
import `in`.silive.lateentryproject.network.ServiceBuilder
import `in`.silive.lateentryproject.sealed_class.ErrorPojoClass
import `in`.silive.lateentryproject.sealed_class.Response
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import retrofit2.Call
import retrofit2.Callback

class LoginRepository {
    private val liveData = MutableLiveData<Response<MessageDataClass>>()

    fun login(email: String, password: String): MutableLiveData<Response<MessageDataClass>> {
        val call = ServiceBuilder.buildService(isTokenRequired = false).login(email, password)

        call.enqueue(object : Callback<MessageDataClass> {
            override fun onResponse(
                call: Call<MessageDataClass>, response: retrofit2.Response<MessageDataClass>
            ) {
                when {
                    response.isSuccessful -> liveData.postValue(Response.Success(response.body()!!))

                    response.code() == 403 -> {
                        val gson: Gson = GsonBuilder().create()
                        val mError: ErrorPojoClass = gson.fromJson(
                            response.errorBody()?.string(), ErrorPojoClass::class.java
                        )
                        liveData.postValue(mError.message?.let { Response.Error(it) })
                    }

                    else -> liveData.postValue(Response.Error(response.message()))
                }
            }

            override fun onFailure(call: Call<MessageDataClass>, t: Throwable) {

                val message = t.message?.let {
                    if (it.length == 7 || it.substring(0, 17)
                            .equals("failed to connect", ignoreCase = true)
                        || it.substring(0, 22) == "Unable to resolve host"
                    ) "No or poor Internet connection!" // it.length == 7 refers to it = "timeout"

                    else "$it Please try again"
                } ?: "Please try again"

                liveData.postValue(Response.Error(message))
            }
        })

        return liveData
    }
}