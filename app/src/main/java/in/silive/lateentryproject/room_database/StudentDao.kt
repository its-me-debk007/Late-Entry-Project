package `in`.silive.lateentryproject.room_database

import `in`.silive.lateentryproject.entities.Student
import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface StudentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addStudent(studentData:List<Student>)

    @Query("SELECT * FROM student")
    fun getStudentDetails():LiveData<List<Student>>

}