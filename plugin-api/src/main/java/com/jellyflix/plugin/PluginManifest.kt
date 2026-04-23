package com.jellyflix.plugin

/**
 * Parsed from a plugin APK's `<meta-data>` entries. The `permissions` list is
 * surfaced to the user when they first enable the plugin.
 *
 * Known permission strings:
 *  - `network`       — plugin may perform outbound HTTP requests
 *  - `playback`      — plugin may read playback events
 *  - `home-rows`     — plugin may add rows to the home screen
 *  - `subtitles`     — plugin may register a subtitle provider
 *  - `persistence`   — plugin may persist files under its own scoped directory
 */
data class PluginManifest(
    val id: String,
    val name: String,
    val version: String,
    val permissions: List<String>,
)
