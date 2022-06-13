package `in`.silive.lateentryproject.room_database

import `in`.silive.lateentryproject.entities.OfflineLateEntry
import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface OfflineLateEntryDao {

	@Insert(onConflict = OnConflictStrategy.ABORT)
	suspend fun addLateEntry(lateEntry: OfflineLateEntry)

	@Query("SELECT * FROM OfflineLateEntry")
	suspend fun getLateEntryDetails(): List<OfflineLateEntry>

	@Query("DELETE FROM OfflineLateEntry")
	suspend fun clearLateEntryTable()
}