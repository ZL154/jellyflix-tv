package com.jellyflix.tv.data

import android.content.Context
import android.os.Build
import com.jellyflix.tv.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first
import org.jellyfin.sdk.Jellyfin
import org.jellyfin.sdk.android
import org.jellyfin.sdk.api.client.ApiClient
import org.jellyfin.sdk.createJellyfin
import org.jellyfin.sdk.model.ClientInfo
import org.jellyfin.sdk.model.DeviceInfo

/**
 * Thin wrapper around the Jellyfin Kotlin SDK that binds session state
 * (server URL, access token) to a single reusable ApiClient.
 */
@Singleton
class JellyfinClient @Inject constructor(
    @ApplicationContext private val context: Context,
    private val session: SessionStore,
) {
    private val jellyfin: Jellyfin = createJellyfin {
        clientInfo = ClientInfo(name = BuildConfig.CLIENT_NAME, version = BuildConfig.CLIENT_VERSION)
        android(context)
    }

    @Volatile private var cached: ApiClient? = null

    suspend fun api(): ApiClient {
        cached?.let { return it }
        val s = session.session.first()
        val client = jellyfin.createApi(
            baseUrl = s.serverUrl,
            accessToken = s.accessToken,
            deviceInfo = DeviceInfo(id = s.deviceId ?: defaultDeviceId(), name = "Jellyflix-${Build.MODEL}"),
        )
        cached = client
        ImageUrls.baseUrl = s.serverUrl
        return client
    }

    fun invalidate() { cached = null }

    private fun defaultDeviceId(): String = "jellyflix-${Build.MODEL}-${Build.ID}".take(64)

    /** Builder used before auth — the server URL is known but no token yet. */
    suspend fun unauthenticated(baseUrl: String): ApiClient =
        jellyfin.createApi(baseUrl = baseUrl)
}
