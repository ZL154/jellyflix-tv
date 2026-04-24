package com.jellyflix.tv.data

import javax.inject.Inject
import javax.inject.Singleton
import org.jellyfin.sdk.api.client.extensions.pluginsApi
import org.jellyfin.sdk.api.client.extensions.systemApi
import org.jellyfin.sdk.model.api.PluginInfo
import org.jellyfin.sdk.model.api.PublicSystemInfo

@Singleton
class ServerInfoRepository @Inject constructor(
    private val client: JellyfinClient,
) {
    suspend fun systemInfo(): Result<PublicSystemInfo> = runCatching {
        client.api().systemApi.getPublicSystemInfo().content
    }

    /** Returns the list of plugins installed on the Jellyfin *server*. Requires admin on most servers. */
    suspend fun serverPlugins(): Result<List<PluginInfo>> = runCatching {
        client.api().pluginsApi.getPlugins().content
    }
}
