package com.jellyflix.plugin

/**
 * Every Jellyflix plugin implements this interface. The host instantiates the
 * entry class reflectively via the no-arg constructor, so keep it public and
 * parameter-less; ask for what you need through [PluginContext].
 *
 * Implementations must be thread-safe — the host may invoke hooks from
 * different coroutine dispatchers.
 */
interface Plugin {
    /** Called once per process when the plugin is loaded. */
    fun onCreate(ctx: PluginContext) {}

    /** Called when the host tears down (e.g. user signs out). */
    fun onDestroy(ctx: PluginContext) {}
}
