package com.jellyflix.tv.plugin

import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import com.jellyflix.plugin.Plugin
import com.jellyflix.plugin.PluginManifest
import dagger.hilt.android.qualifiers.ApplicationContext
import dalvik.system.DexClassLoader
import javax.inject.Inject
import javax.inject.Singleton
import timber.log.Timber

/**
 * Discovers installed plugin APKs and loads their entry class via DexClassLoader.
 * A plugin APK declares a service with action "com.jellyflix.plugin.ENTRY" and
 * meta-data `plugin.entry` pointing at a class implementing [Plugin].
 *
 * This is a local, in-process plugin model. Plugins run in Jellyflix's process
 * sandboxed by the PluginPermissions they declare in their manifest. Trust is
 * established by the user via a confirmation dialog before first enable; the
 * dialog shows the plugin's requested permissions.
 */
@Singleton
class PluginManager @Inject constructor(
    @ApplicationContext private val appContext: Context,
) {
    private val loaded = mutableMapOf<String, LoadedPlugin>()

    data class LoadedPlugin(
        val manifest: PluginManifest,
        val plugin: Plugin,
    )

    @Suppress("UNCHECKED_CAST")
    fun discover(): List<LoadedPlugin> {
        val pm = appContext.packageManager
        val intent = android.content.Intent("com.jellyflix.plugin.ENTRY")
        val services = pm.queryIntentServices(intent, PackageManager.GET_META_DATA)

        return services.mapNotNull { resolved ->
            val info: ServiceInfo = resolved.serviceInfo ?: return@mapNotNull null
            val packageName = info.packageName
            if (loaded.containsKey(packageName)) return@mapNotNull loaded[packageName]

            runCatching {
                val entryClass = info.metaData?.getString("plugin.entry")
                    ?: error("Plugin $packageName missing plugin.entry meta-data")
                val name = info.metaData.getString("plugin.name") ?: packageName
                val version = info.metaData.getString("plugin.version").orEmpty()
                val permissions = info.metaData.getString("plugin.permissions")
                    ?.split(',')?.map(String::trim)?.filter(String::isNotEmpty).orEmpty()

                val appInfo = pm.getApplicationInfo(packageName, 0)
                val optimized = appContext.codeCacheDir.resolve("plugins/$packageName").apply { mkdirs() }
                val loader = DexClassLoader(
                    appInfo.sourceDir,
                    optimized.absolutePath,
                    appInfo.nativeLibraryDir,
                    appContext.classLoader,
                )
                val klass = loader.loadClass(entryClass)
                val instance = klass.getDeclaredConstructor().newInstance() as Plugin
                val manifest = PluginManifest(
                    id = packageName,
                    name = name,
                    version = version,
                    permissions = permissions,
                )
                LoadedPlugin(manifest, instance).also { loaded[packageName] = it }
            }.onFailure { Timber.w(it, "Failed to load plugin") }.getOrNull()
        }
    }

    fun loaded(): Collection<LoadedPlugin> = loaded.values
}
