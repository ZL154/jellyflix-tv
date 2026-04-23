# Architecture

## Layered module graph

```
┌──────────────────┐
│       app        │  Compose UI, ViewModels, Activities, Hilt graph
│                  │
│   playback/ ─────┼──► StreamUrlResolver → Jellyfin /PlaybackInfo
│   plugin/ ───────┼──► PluginManager (DexClassLoader)
│   data/ ─────────┼──► JellyfinClient (SDK) + SessionStore (DataStore)
└────────┬─────────┘
         │ depends on
         ▼
┌──────────────────┐
│   plugin-api     │  Plugin, PluginContext, hooks.*
└──────────────────┘
         ▲
         │ compileOnly
┌────────┴─────────┐
│  sample-plugin   │  Separate APK, user-installable
└──────────────────┘
```

`plugin-api` is deliberately its own library module so third-party plugin
authors can depend on it as a versioned artifact (eventually published to
Maven Central) without pulling in the whole client.

## Data flow: playback

```
User clicks "Play" on a Movie/Episode detail
 ├─► PlayerActivity starts with itemId
 ├─► StreamUrlResolver.resolve(itemId)
 │     ├─ buildDeviceProfile() — from MediaCodecList
 │     ├─ POST /Items/{id}/PlaybackInfo
 │     └─ pick the first MediaSource
 ├─► ExoPlayer.setMediaItem(stream.url)
 ├─► onVideoSizeChanged → PlaybackOptimizer.matchRefreshRate(window, format)
 └─► PluginHost.onPlaybackStart(itemId, stream)
       └─ broadcasts to every PlaybackHook-implementing plugin
```

Direct-play vs transcode is decided server-side. Our job is to honestly
advertise the codecs the decoder actually supports so the server doesn't
defensively transcode (which wastes CPU and loses HDR metadata).

## Auth & session

`SessionStore` keeps the server URL, user id, access token, and device id in
a single `DataStore<Preferences>` file (`auth`). The file is explicitly
excluded from cloud backup via `backup_rules.xml` so a Google Backup restore
on a new TV does not leak the token.

`SessionViewModel` is the top-level state holder and drives the NavHost in
`MainActivity`. Its stages — `NeedsServer / NeedsLogin / Authenticated` —
map 1:1 to the `connect` / `login` / `home` routes.

## Plugin loading

1. `PluginManager.discover()` runs `queryIntentServices(ACTION="com.jellyflix.plugin.ENTRY", GET_META_DATA)`.
2. For each match, it reads `plugin.entry`, `plugin.name`, `plugin.version`,
   and `plugin.permissions` from the service `<meta-data>`.
3. It creates a `DexClassLoader` scoped to the plugin APK's `sourceDir`,
   pointed at `app.codeCacheDir/plugins/<pkg>` for optimised DEX.
4. It reflectively instantiates the entry class via its no-arg constructor.
5. The instance is kept in a map; `PluginHost` dispatches lifecycle events
   to every instance that implements the relevant hook interface.

This in-process model means plugins run at full Kotlin speed, can call any
public API they have a reference to, and share a heap with the host. The
tradeoff is trust: the host MUST show the user the declared permissions
before enabling a plugin, and plugins are expected to limit themselves to
those permissions. A future milestone is to enforce permissions — e.g.,
route all plugin network traffic through a host-provided OkHttp instance
that rejects calls from plugins without `network`.

## Optimisation levers

| Lever                          | Where                                           |
| ------------------------------ | ----------------------------------------------- |
| Tunneled decoding              | `PlaybackOptimizer.renderers`                   |
| Refresh-rate matching          | `PlaybackOptimizer.matchRefreshRate`            |
| Device profile from MediaCodec | `StreamUrlResolver.buildDeviceProfile`          |
| Audio offload + float          | `DefaultRenderersFactory` flags in `renderers`  |
| Track selection defaults       | `PlaybackOptimizer.trackParameters`             |
| MediaSession / Now Playing     | `PlaybackService`                               |
| Seek increments (10s / 30s)    | `ExoPlayer.Builder` in `PlayerActivity`         |

## Things explicitly deferred

- Settings UI for toggling playback prefs (tunneling off, force transcode).
- Details screen (episode list, cast, similar).
- Quick Connect.
- 4K HDR track preferences + Dolby Vision fallback policy.
- Trickplay thumbnail scrubbing (Jellyfin 10.9+).
- Chapter-based skip intro / skip credits UI.
- In-app plugin browser ("store").
