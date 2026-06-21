package com.app.vitaltrack.data.gamification

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.gamificationDataStore: DataStore<Preferences> by preferencesDataStore(name = "gamification_prefs")

class GamificationDataStore(private val context: Context) {
    private val json = Json { ignoreUnknownKeys = true }

    private fun getStateKey(clientId: Long) = stringPreferencesKey("gamification_state_client_$clientId")
    private fun getDailyStateKey(clientId: Long, date: String) = stringPreferencesKey("gamification_daily_state_client_${clientId}_$date")

    fun getGamificationStateFlow(clientId: Long): Flow<GamificationState> {
        val key = getStateKey(clientId)
        return context.gamificationDataStore.data.map { prefs ->
            val jsonString = prefs[key]
            if (jsonString != null) {
                json.decodeFromString(jsonString)
            } else {
                GamificationState(clientId = clientId)
            }
        }
    }

    suspend fun getGamificationState(clientId: Long): GamificationState {
        return getGamificationStateFlow(clientId).first()
    }

    suspend fun saveGamificationState(state: GamificationState) {
        val key = getStateKey(state.clientId)
        context.gamificationDataStore.edit { prefs ->
            prefs[key] = json.encodeToString(state)
        }
    }

    suspend fun getDailyState(clientId: Long, date: String): DailyGamificationState {
        val key = getDailyStateKey(clientId, date)
        val jsonString = context.gamificationDataStore.data.map { it[key] }.first()
        return if (jsonString != null) {
            json.decodeFromString(jsonString)
        } else {
            DailyGamificationState(date = date)
        }
    }

    suspend fun saveDailyState(clientId: Long, state: DailyGamificationState) {
        val key = getDailyStateKey(clientId, state.date)
        context.gamificationDataStore.edit { prefs ->
            prefs[key] = json.encodeToString(state)
        }
    }
}
