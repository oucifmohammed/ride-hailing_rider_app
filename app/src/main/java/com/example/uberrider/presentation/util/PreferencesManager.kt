package com.example.uberrider.presentation.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import com.example.uberrider.presentation.util.Constants.STORE_NAME
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferencesManager @Inject constructor(
    @ApplicationContext val context: Context
) {

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = STORE_NAME)

    private object PreferencesKeys {
        val SHOW_GET_STARTED_SCREEN = booleanPreferencesKey("show_get_started_screen")
    }

    val userRegisterStatus: Flow<Boolean> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            }else {
                throw exception
            }
        }
        .map { preferences ->
            val registerStatus = preferences[PreferencesKeys.SHOW_GET_STARTED_SCREEN] ?: false
            registerStatus
        }

    suspend fun validateUserRegisterStatus() {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SHOW_GET_STARTED_SCREEN] = true
        }
    }
}