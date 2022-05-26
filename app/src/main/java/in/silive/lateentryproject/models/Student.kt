package `in`.silive.lateentryproject.models

import android.graphics.Bitmap
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "student")
data class Student(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val batch: Int,
    val branch: String,
    val name: String,
    val student_no: String,
    val late_entry_count: Int,
    val photo: Bitmap
)
