package `in`.silive.lateentryproject.models

data class MessageDataClass(
    val message: String? = null,
    val status: Boolean? = null,
    val status_code: String? = null,
    val result: ResultDataClass? = null,
    val refresh: String? = null,
    val access: String? = null,
    val type: String? = null
)