package com.app.vitaltrack.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

data class UserPreferences(
    val clienteAtivoId: Long?,
    val clienteAtivoNome: String?,
    val metaCalorias: Double
)

class UserPreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val CLIENTE_ATIVO_ID = longPreferencesKey("CLIENTE_ATIVO_ID")
        val CLIENTE_ATIVO_NOME = stringPreferencesKey("CLIENTE_ATIVO_NOME")
        val META_CALORIAS_DIARIA = doublePreferencesKey("META_CALORIAS_DIARIA")
    }

    val userPreferencesFlow: Flow<UserPreferences> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val clienteId = preferences[PreferencesKeys.CLIENTE_ATIVO_ID]
            val clienteNome = preferences[PreferencesKeys.CLIENTE_ATIVO_NOME]
            val metaCalorias = preferences[PreferencesKeys.META_CALORIAS_DIARIA] ?: 2000.0
            UserPreferences(clienteId, clienteNome, metaCalorias)
        }

    suspend fun updateClienteAtivo(id: Long, nome: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.CLIENTE_ATIVO_ID] = id
            preferences[PreferencesKeys.CLIENTE_ATIVO_NOME] = nome
        }
    }

    suspend fun updateMetaCalorica(meta: Double) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.META_CALORIAS_DIARIA] = meta
        }
    }
}
