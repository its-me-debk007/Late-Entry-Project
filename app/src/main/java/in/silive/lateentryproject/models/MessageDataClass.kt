package `in`.silive.lateentryproject.models

data class MessageDataClass(
	val message: String? = null,
	val status:Boolean?=null,
	val status_code:String?=null,
	val result: ResultDataClass,
	val refresh: String,
	val access: String
)