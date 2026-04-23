# Writing a Jellyflix plugin

A plugin is a regular Android application with one twist: it never shows its
own UI. It ships a single `<service>` declaration whose meta-data points
Jellyflix at an entry class, and Jellyflix loads that class with
`DexClassLoader` at runtime.

## 1. Add the dependency

```kotlin
// my-plugin/build.gradle.kts
dependencies {
    // compileOnly — the host provides this at runtime.
    compileOnly("com.jellyflix:plugin-api:0.1.0")
}
```

Until we publish to Maven Central, depend on the module directly:

```kotlin
compileOnly(project(":plugin-api"))
```

## 2. Implement `Plugin` + at least one hook

```kotlin
class MyPlugin : Plugin, HomeRowProvider {
    override fun onCreate(ctx: PluginContext) {
        ctx.log("MyPlugin", "hello")
    }

    override fun provideRows(ctx: PluginContext) = listOf(
        HomeRowProvider.Row(
            id = "my-plugin-featured",
            title = "Featured on MyService",
            priority = 450,
            cards = fetchFromApi(),
        )
    )
}
```

Available hooks:

| Interface          | When it fires                                            |
| ------------------ | -------------------------------------------------------- |
| `Plugin`           | `onCreate` / `onDestroy` lifecycle                       |
| `HomeRowProvider`  | Home screen composition                                  |
| `PlaybackHook`     | Playback Start / Progress / Pause / Stop                 |
| `SubtitleProvider` | When the user opens the subtitle picker during playback  |

## 3. Declare the entry in your AndroidManifest

```xml
<service
    android:name=".MyPluginEntry"
    android:exported="true"
    android:enabled="true">
    <intent-filter>
        <action android:name="com.jellyflix.plugin.ENTRY" />
    </intent-filter>
    <meta-data android:name="plugin.entry"       android:value="com.example.MyPlugin" />
    <meta-data android:name="plugin.name"        android:value="My Service Integration" />
    <meta-data android:name="plugin.version"     android:value="1.0.0" />
    <meta-data android:name="plugin.permissions" android:value="home-rows,network" />
</service>
```

The `Service` class itself can be an empty stub — Jellyflix reads its
meta-data but never binds to it.

## 4. Install

```bash
./gradlew :my-plugin:installDebug
```

Next time Jellyflix starts it will discover the plugin, show the user a
one-time permissions prompt, and (if accepted) activate its hooks.

## Permission model

| Permission    | Grants                                                        |
| ------------- | ------------------------------------------------------------- |
| `network`     | Outbound HTTP (future: via host-provided client)              |
| `playback`    | Receive `PlaybackHook` events                                 |
| `home-rows`   | Contribute to the home screen                                 |
| `subtitles`   | Appear in the subtitle picker                                 |
| `persistence` | Scoped file storage under `context.filesDir/plugins/<pkg>`    |

The host surfaces the declared list to the user on first activation. Only
ask for what you use; unknown strings are shown verbatim.

## Recommended patterns

- **Keep the entry class tiny**; delegate to a real class you own so your
  plugin is easy to unit-test off the host.
- **Never block a hook**. Playback events fire on a coroutine dispatcher
  the host controls — do network I/O on your own scope.
- **Don't cache the `PluginContext`.** It's safe to hold through the call
  but may be invalidated on sign-out.
- **Version your hooks.** If you implement `PlaybackHook` and later decide
  to implement `HomeRowProvider`, ship a new plugin version so users get
  re-prompted for the added permission.
