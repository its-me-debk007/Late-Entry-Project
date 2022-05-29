package `in`.silive.lateentryproject.room_database

import `in`.silive.lateentryproject.entities.Student
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase


@Database(entities = [Student::class], version = 1)
abstract class StudentDatabase: RoomDatabase() {

    abstract fun studentDao():StudentDao

    companion object{
        @Volatile
        private var INSTANCE : StudentDatabase?=null
        fun getDatabase(context: Context):StudentDatabase{
            if(INSTANCE == null) {
                synchronized(this) {
                    INSTANCE = Room.databaseBuilder(
                        context, StudentDatabase::class.java,
                        "studentDB"
                    )
                        .build()
                }
            }
            return INSTANCE!!
        }
    }
}