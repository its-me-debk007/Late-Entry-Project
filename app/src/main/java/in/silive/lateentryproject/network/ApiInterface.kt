package `in`.silive.lateentryproject.network

import `in`.silive.lateentryproject.model.*
import retrofit2.Call
import retrofit2.http.*

interface ApiInterface {
    @POST("/entry/scan/")
    fun lateEntry(@Body data: Late_entry_data_class): Call<Message_data_class>

}