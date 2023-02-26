package `in`.silive.lateentryproject.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class OfflineLateEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,
    val student_no: String? = null,
    val timestamp: String? = null,
    val venue: Int? = null
)
