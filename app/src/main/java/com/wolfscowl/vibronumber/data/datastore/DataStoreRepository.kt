package com.wolfscowl.vibronumber.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.wolfscowl.vibronumber.presentation.model.AppPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class DataStoreRepository(private val context: Context) {

    private object Keys {
        val PTS_START_HOLD_FACTOR = floatPreferencesKey("pts_start_hold_factor")
        val PTS_END_HOLD_FACTOR = floatPreferencesKey("pts_end_hold_factor")
    }

    val preferences: Flow<AppPreferences> = context.dataStore.data.map { prefs ->
        AppPreferences(
            ptsStartHoldFactor = prefs[Keys.PTS_START_HOLD_FACTOR] ?: 0.5f,
            ptsEndHoldFactor = prefs[Keys.PTS_END_HOLD_FACTOR] ?: 0.5f
        )
    }

    suspend fun savePtsStartHoldFactor(factor: Float) {
        context.dataStore.edit { prefs ->
            prefs[Keys.PTS_START_HOLD_FACTOR] = factor
        }
    }

    suspend fun savePtsEndHoldFactor(factor: Float) {
        context.dataStore.edit { prefs ->
            prefs[Keys.PTS_END_HOLD_FACTOR] = factor
        }
    }
}
