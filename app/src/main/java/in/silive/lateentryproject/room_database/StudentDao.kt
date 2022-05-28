package `in`.silive.lateentryproject.room_database

import `in`.silive.lateentryproject.models.Student
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface StudentDao {

    @Insert
    suspend fun addStudent(studentData:List<Student>)

    @Query("SELECT * FROM student")
    suspend fun getStudentDetails():List<Student>

}