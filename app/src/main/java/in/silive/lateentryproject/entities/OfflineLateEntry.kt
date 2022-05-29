package `in`.silive.lateentryproject.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class OfflineLateEntry(
	@PrimaryKey (autoGenerate = true) val id: Int,
	val student_no: String,
	val timestamp: String,
	val venue: Int
)
