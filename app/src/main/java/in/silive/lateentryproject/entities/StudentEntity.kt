package `in`.silive.lateentryproject.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "student")
data class StudentEntity(
	@PrimaryKey val student_no: String,
	val batch: String,
	val branch: String,
	val name: String,
)
