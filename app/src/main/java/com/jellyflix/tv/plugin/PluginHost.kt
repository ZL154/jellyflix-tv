package com.jellyflix.tv.plugin

import android.content.Context
import com.jellyflix.plugin.PluginContext
import com.jellyflix.plugin.hooks.HomeRowProvider
import com.jellyflix.plugin.hooks.PlaybackHook
import com.jellyflix.tv.playback.StreamUrlResolver
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import timber.log.Timber

/**
 * Dispatches lifecycle events from the app into loaded plugins. Plugins opt in
 * by implementing one of the hook interfaces from the plugin-api module.
 */
@Singleton
class PluginHost @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val manager: PluginManager,
) {
    private val ctx = object : PluginContext {
        override val appContext: Context = this@PluginHost.appContext
        override fun log(tag: String, message: String) { Timber.tag(tag).i(message) }
    }

    fun initialize() {
        manager.discover().forEach {
            runCatching { it.plugin.onCreate(ctx) }
                .onFailure { t -> Timber.w(t, "Plugin %s onCreate failed", it.manifest.id) }
        }
    }

    fun homeRows(): List<HomeRowProvider.Row> = manager.loaded()
        .mapNotNull { it.plugin as? HomeRowProvider }
        .flatMap { runCatching { it.provideRows(ctx) }.getOrDefault(emptyList()) }

    fun onPlaybackStart(itemId: UUID, stream: StreamUrlResolver.Stream) {
        val event = PlaybackHook.Event.Start(itemId, stream.url, stream.isDirectPlay)
        manager.loaded()
            .mapNotNull { it.plugin as? PlaybackHook }
            .forEach { runCatching { it.onEvent(ctx, event) } }
    }
}
