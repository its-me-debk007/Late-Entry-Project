package `in`.silive.lateentryproject.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "failedData")
data class FailedStudentEntryEntity(
	@PrimaryKey val student_no: String,
	val time: String,
	val venue: Int
)