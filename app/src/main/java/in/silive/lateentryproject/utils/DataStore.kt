package `in`.silive.lateentryproject.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

const val DATASTORE_NAME = "bulkData"
val Context.datastore: DataStore<Preferences> by preferencesDataStore(DATASTORE_NAME)

class Datastore(val context: Context) {

	companion object {
		val LOGIN_KEY = stringPreferencesKey("login_key")
		val SYNC_KEY = booleanPreferencesKey("sync_key")
		val SYNCED_TIME_KEY = stringPreferencesKey("sync_time")
		val VENUE_KEY = stringPreferencesKey("venue_key")
		val DEFAULT_VENUE_KEY = stringPreferencesKey("default_venue_key")
		val ACCESS_TOKEN_KEY = stringPreferencesKey("access_token_key")
		val REFRESH_TOKEN_KEY = stringPreferencesKey("refresh_token_key")
	}

	suspend fun saveId(key: String, value: Int?) {
		val key1 = intPreferencesKey(key)
		context.datastore.edit { cache_data ->
			cache_data[key1] = value!!
		}
	}

	suspend fun saveDefaultVenue(value: String?) {
		context.datastore.edit { cache_data ->
			cache_data[DEFAULT_VENUE_KEY] = value.toString()
		}
	}

	suspend fun getDefaultVenue() = context.datastore.data.first()[DEFAULT_VENUE_KEY]

	suspend fun saveVenueDetails(value: MutableMap<Int, String>?) {
		context.datastore.edit { cache_data ->
			val data = value?.entries?.joinToString()
			cache_data[VENUE_KEY] = data.toString()
		}
	}

	suspend fun changeLoginState(value: String) {
		context.datastore.edit {
			it[LOGIN_KEY] = value
		}
	}

	suspend fun changeSyncState(value: Boolean) {
		context.datastore.edit {
			it[SYNC_KEY] = value
		}
	}

	suspend fun getId(key: String): Int? {
		val key1 = intPreferencesKey(key)
		return context.datastore.data.first()[key1]
	}

	suspend fun getVenueDetails() = context.datastore.data.first()[VENUE_KEY]

	suspend fun loginStatus() = context.datastore.data.first()[LOGIN_KEY] ?: "no"

	suspend fun isSync() = context.datastore.data.first()[SYNC_KEY] ?: true

	suspend fun saveSyncTime(time: String) {
		context.datastore.edit {
			it[SYNCED_TIME_KEY] = time
		}
	}

	suspend fun getSyncTime() = context.datastore.data.first()[SYNCED_TIME_KEY] ?: "Never"

	suspend fun saveAccessToken(value: String) {
		context.datastore.edit {
			it[ACCESS_TOKEN_KEY] = value
		}
	}

	suspend fun saveRefreshToken(value: String) {
		context.datastore.edit {
			it[REFRESH_TOKEN_KEY] = value
		}
	}

	suspend fun getAccessToken() = context.datastore.data.first()[ACCESS_TOKEN_KEY]
	suspend fun getRefreshToken() = context.datastore.data.first()[REFRESH_TOKEN_KEY]
}