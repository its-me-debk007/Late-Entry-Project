package `in`.silive.lateentryproject.models

data class MessageDataClass(
	val message: String? = null,
	val status:Boolean?=null,
	val status_code:Int?=null,
	val result: ResultDataClass
)