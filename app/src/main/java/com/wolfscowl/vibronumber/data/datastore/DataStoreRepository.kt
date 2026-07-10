package com.wolfscowl.vibronumber.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.wolfscowl.vibronumber.presentation.model.AppPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class DataStoreRepository(private val context: Context) {

    private object Keys {
        val POST_DIGIT_DELAY_MS = intPreferencesKey("post_digit_delay_ms")
        val DSCR_START_HOLD_FACTOR = floatPreferencesKey("dscr_start_hold_factor")
        val DSCR_END_HOLD_FACTOR = floatPreferencesKey("dscr_end_hold_factor")
        val ATM_START_HOLD_FACTOR = floatPreferencesKey("atm_start_hold_factor")
        val ATM_END_HOLD_FACTOR = floatPreferencesKey("atm_end_hold_factor")
        val PTS_START_HOLD_FACTOR = floatPreferencesKey("pts_start_hold_factor")
        val PTS_END_HOLD_FACTOR = floatPreferencesKey("pts_end_hold_factor")


    }

    val preferences: Flow<AppPreferences> = context.dataStore.data.map { prefs ->
        AppPreferences(
            postDigitDelayMs = prefs[Keys.POST_DIGIT_DELAY_MS] ?: 0,
            dscrStartHoldFactor = prefs[Keys.DSCR_START_HOLD_FACTOR] ?: 1.0f,
            dscrEndHoldFactor = prefs[Keys.DSCR_END_HOLD_FACTOR] ?: 1.0f,
            atmStartHoldFactor = prefs[Keys.ATM_START_HOLD_FACTOR] ?: 1.0f,
            atmEndHoldFactor = prefs[Keys.ATM_END_HOLD_FACTOR] ?: 1.0f,
            ptsStartHoldFactor = prefs[Keys.PTS_START_HOLD_FACTOR] ?: 0.0f,
            ptsEndHoldFactor = prefs[Keys.PTS_END_HOLD_FACTOR] ?: 0.0f,
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

    suspend fun saveAtmStartHoldFactor(factor: Float) {
        context.dataStore.edit { prefs ->
            prefs[Keys.ATM_START_HOLD_FACTOR] = factor
        }
    }

    suspend fun saveAtmEndHoldFactor(factor: Float) {
        context.dataStore.edit { prefs ->
            prefs[Keys.ATM_END_HOLD_FACTOR] = factor
        }
    }

    suspend fun saveDscrStartHoldFactor(factor: Float) {
        context.dataStore.edit { prefs ->
            prefs[Keys.DSCR_START_HOLD_FACTOR] = factor
        }
    }

    suspend fun saveDscrEndHoldFactor(factor: Float) {
        context.dataStore.edit { prefs ->
            prefs[Keys.DSCR_END_HOLD_FACTOR] = factor
        }
    }

    suspend fun savePostDigitDelayMs(delayMs: Int) {
        context.dataStore.edit { prefs ->
            prefs[Keys.POST_DIGIT_DELAY_MS] = delayMs
        }
    }
}
