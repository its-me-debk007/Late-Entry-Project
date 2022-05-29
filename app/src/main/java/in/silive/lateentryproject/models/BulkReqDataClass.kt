package `in`.silive.lateentryproject.models

import `in`.silive.lateentryproject.entities.OfflineLateEntry

data class BulkReqDataClass(
    val entry: List<OfflineLateEntry>
)