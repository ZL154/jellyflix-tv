package com.jellyflix.plugin

import android.content.Context

/**
 * Services exposed to plugins at runtime. Stays intentionally narrow — anything
 * a plugin needs beyond this is explicit contract surface we can review.
 */
interface PluginContext {
    val appContext: Context
    fun log(tag: String, message: String)
}
