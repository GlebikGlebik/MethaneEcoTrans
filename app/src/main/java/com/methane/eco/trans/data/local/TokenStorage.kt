package com.methane.eco.trans.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

// Расширение для создания DataStore
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_prefs")

class TokenStorage(context: Context) {
    private val dataStore = context.dataStore

    companion object {
        val AUTH_TOKEN = stringPreferencesKey("auth_token")
        val USER_ID = stringPreferencesKey("user_id")
        val USER_EMAIL = stringPreferencesKey("user_email")
        val USER_FIRST_NAME = stringPreferencesKey("user_first_name")
        val USER_LAST_NAME = stringPreferencesKey("user_last_name")
    }

    // Сохранение токена и данных пользователя
    suspend fun saveAuthToken(
        token: String,
        userId: String,
        email: String,
        firstName: String,
        lastName: String
    ) {
        dataStore.edit { preferences ->
            preferences[AUTH_TOKEN] = token
            preferences[USER_ID] = userId
            preferences[USER_EMAIL] = email
            preferences[USER_FIRST_NAME] = firstName
            preferences[USER_LAST_NAME] = lastName
        }
    }

    // Получение токена
    val authToken: Flow<String?> = dataStore.data.map { preferences ->
        preferences[AUTH_TOKEN]
    }

    // Получение данных пользователя
    val userId: Flow<String?> = dataStore.data.map { preferences ->
        preferences[USER_ID]
    }

    val userEmail: Flow<String?> = dataStore.data.map { preferences ->
        preferences[USER_EMAIL]
    }

    // Очистка токена (при выходе)
    suspend fun clearAuthToken() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    // Проверка, авторизован ли пользователь
    suspend fun isAuthorized(): Boolean {
        return dataStore.data.map { preferences ->
            preferences[AUTH_TOKEN] != null
        }.first()
    }
}