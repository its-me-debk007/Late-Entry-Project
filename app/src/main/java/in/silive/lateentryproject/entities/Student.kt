package `in`.silive.lateentryproject.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "student")
data class Student(
    @PrimaryKey(autoGenerate = false)
    val student_no: String,
    val batch: Int,
    val branch: String,
    val name: String,
    
)
