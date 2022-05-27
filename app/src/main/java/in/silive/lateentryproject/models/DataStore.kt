package `in`.silive.lateentryproject.models

import android.app.Person
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Embedded
import kotlinx.coroutines.flow.first


const val DATASTORE_NAME = "bulkData"
val Context.datastore: DataStore<Preferences> by preferencesDataStore(DATASTORE_NAME)

    class Datastore(context: Context) {
        private val appContext = context.applicationContext

        companion object {
            const val LOGIN_KEY = "login_key"
            const val SYNC_KEY = "sync_key"
            const val ID_KEY = "id_key"
            const val VENUE_KEY = "venue_key"
        }

        suspend fun saveDetails(key: String, value: String?) {
            val key1 = stringPreferencesKey(key)
            appContext!!.datastore.edit { cache_data ->
                cache_data[key1] = value.toString()
            }
        }
        suspend fun saveVenueDetails(key: String, value: MutableMap<Int,String>?) {
            val key1 = stringPreferencesKey(key)
            appContext!!.datastore.edit { cache_data ->
                val data=value?.entries?.joinToString()
                cache_data[key1] = data.toString()
            }
        }

        suspend fun changeLoginState(value: Boolean) {
            val key1 = booleanPreferencesKey(LOGIN_KEY)
            appContext.datastore.edit {
                it[key1] = value
            }
        }
        suspend fun changeSyncState(value: Boolean) {
            val key1 = booleanPreferencesKey(SYNC_KEY)
            appContext.datastore.edit {
                it[key1] = value
            }
        }

        suspend fun getDetails(key: String): String? {
            val key1 = stringPreferencesKey(key)
            return appContext!!.datastore.data.first()[key1]
        }
        suspend fun getVenueDetails(key: String): String? {
            val key1 = stringPreferencesKey(key)
            return appContext!!.datastore.data.first()[key1]
        }

        suspend fun isLogin(): Boolean {
            val key1 = booleanPreferencesKey(LOGIN_KEY)
            return appContext.datastore.data.first()[key1] ?: false
        }
        suspend fun isSync(): Boolean {
            val key1 = booleanPreferencesKey(SYNC_KEY)
            return appContext.datastore.data.first()[key1] ?: true
        }
        suspend fun saveToDatastore(it: VenueData, context: Context) {
            val datastore = Datastore(context)
            datastore.changeLoginState(true)
            datastore.changeSyncState(false)
            datastore.saveDetails(ID_KEY, it.id.toString())
            datastore.saveDetails(VENUE_KEY, it.venue!!)
        }
        
    }
