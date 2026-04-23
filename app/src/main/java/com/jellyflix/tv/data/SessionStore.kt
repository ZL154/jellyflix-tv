package com.jellyflix.tv.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.sessionDataStore: DataStore<Preferences> by preferencesDataStore(name = "auth")

/**
 * Persists the server URL, user id, and access token.
 * Token lives here rather than in SharedPreferences so it is excluded from cloud backup.
 */
@Singleton
class SessionStore @Inject constructor(@ApplicationContext private val ctx: Context) {

    private val serverKey = stringPreferencesKey("server_url")
    private val userIdKey = stringPreferencesKey("user_id")
    private val tokenKey = stringPreferencesKey("access_token")
    private val deviceIdKey = stringPreferencesKey("device_id")

    val session: Flow<Session> = ctx.sessionDataStore.data.map { prefs ->
        Session(
            serverUrl = prefs[serverKey],
            userId = prefs[userIdKey],
            accessToken = prefs[tokenKey],
            deviceId = prefs[deviceIdKey],
        )
    }

    suspend fun setServer(url: String) = ctx.sessionDataStore.edit { it[serverKey] = url }

    suspend fun setAuth(userId: String, token: String) = ctx.sessionDataStore.edit {
        it[userIdKey] = userId
        it[tokenKey] = token
    }

    suspend fun setDeviceId(id: String) = ctx.sessionDataStore.edit { it[deviceIdKey] = id }

    suspend fun clear() = ctx.sessionDataStore.edit { it.clear() }

    data class Session(
        val serverUrl: String?,
        val userId: String?,
        val accessToken: String?,
        val deviceId: String?,
    ) {
        val isAuthenticated: Boolean get() = !serverUrl.isNullOrBlank() && !accessToken.isNullOrBlank()
        val hasServer: Boolean get() = !serverUrl.isNullOrBlank()
    }
}
