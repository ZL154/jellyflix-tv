package com.jellyflix.plugin.hooks

import com.jellyflix.plugin.PluginContext
import java.util.UUID

/**
 * Implemented by plugins that want to observe playback lifecycle.
 * Examples: scrobblers, watch-state sync, analytics, "Skip intro" detectors.
 */
interface PlaybackHook {
    sealed interface Event {
        data class Start(val itemId: UUID, val streamUrl: String, val directPlay: Boolean) : Event
        data class Progress(val itemId: UUID, val positionMs: Long, val durationMs: Long) : Event
        data class Pause(val itemId: UUID, val positionMs: Long) : Event
        data class Stop(val itemId: UUID, val positionMs: Long, val completed: Boolean) : Event
    }

    fun onEvent(ctx: PluginContext, event: Event)
}
