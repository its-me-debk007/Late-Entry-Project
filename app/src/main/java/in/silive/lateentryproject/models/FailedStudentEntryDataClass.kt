package `in`.silive.lateentryproject.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "failedData")
data class FailedStudentEntryDataClass(
    @PrimaryKey(autoGenerate = true)
    val id:Int,
    val student_no: String,
    val time: String,
    val venue: Int
)