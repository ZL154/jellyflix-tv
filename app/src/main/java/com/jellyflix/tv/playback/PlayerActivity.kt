package com.jellyflix.tv.playback

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.PlayerView
import com.jellyflix.tv.plugin.PluginHost
import com.jellyflix.tv.ui.theme.JellyflixTheme
import dagger.hilt.android.AndroidEntryPoint
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.launch
import timber.log.Timber

@OptIn(UnstableApi::class)
@AndroidEntryPoint
class PlayerActivity : ComponentActivity() {

    @Inject lateinit var resolver: StreamUrlResolver
    @Inject lateinit var pluginHost: PluginHost

    private var player: ExoPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val itemId = intent.getStringExtra(EXTRA_ITEM_ID)?.let(UUID::fromString)
            ?: run { finish(); return }
        val title = intent.getStringExtra(EXTRA_TITLE).orEmpty()

        setContent { JellyflixTheme { PlayerSurface(itemId, title) } }
    }

    @Composable
    private fun PlayerSurface(itemId: UUID, title: String) {
        val ctx = this@PlayerActivity
        var format by remember { mutableStateOf<androidx.media3.common.Format?>(null) }

        DisposableEffect(itemId) {
            val exo = buildPlayer(ctx)
            player = exo
            val listener = object : Player.Listener {
                override fun onVideoSizeChanged(size: androidx.media3.common.VideoSize) {
                    PlaybackOptimizer.matchRefreshRate(window, ctx, exo.videoFormat)
                    format = exo.videoFormat
                }
            }
            exo.addListener(listener)

            lifecycleScope.launch {
                runCatching {
                    val stream = resolver.resolve(itemId)
                    pluginHost.onPlaybackStart(itemId, stream)
                    exo.setMediaItem(
                        MediaItem.Builder()
                            .setUri(stream.url)
                            .setMediaMetadata(MediaMetadata.Builder().setTitle(title).build())
                            .build()
                    )
                    exo.prepare()
                    exo.playWhenReady = true
                }.onFailure { Timber.e(it, "Playback start failed") }
            }

            onDispose {
                exo.removeListener(listener)
                exo.release()
                player = null
            }
        }

        AndroidView(
            modifier = Modifier.fillMaxSize().background(Color.Black),
            factory = { c ->
                PlayerView(c).apply {
                    setShowBuffering(PlayerView.SHOW_BUFFERING_WHEN_PLAYING)
                    useController = true
                    controllerShowTimeoutMs = 3000
                }
            },
            update = { it.player = player },
        )
    }

    private fun buildPlayer(ctx: Context): ExoPlayer {
        val renderers = PlaybackOptimizer.renderers(ctx)
        val sourceFactory = DefaultMediaSourceFactory(ctx).setDataSourceFactory(DefaultDataSource.Factory(ctx))
        return ExoPlayer.Builder(ctx, renderers)
            .setMediaSourceFactory(sourceFactory)
            .setSeekBackIncrementMs(10_000)
            .setSeekForwardIncrementMs(30_000)
            .build()
            .apply {
                trackSelectionParameters = PlaybackOptimizer.trackParameters(ctx)
                playWhenReady = true
            }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        val p = player
        if (p != null) when (keyCode) {
            KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE, KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> {
                if (p.isPlaying) p.pause() else p.play()
                return true
            }
            KeyEvent.KEYCODE_MEDIA_REWIND, KeyEvent.KEYCODE_DPAD_LEFT -> { p.seekBack(); return true }
            KeyEvent.KEYCODE_MEDIA_FAST_FORWARD, KeyEvent.KEYCODE_DPAD_RIGHT -> { p.seekForward(); return true }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onStop() { super.onStop(); player?.pause() }

    companion object {
        private const val EXTRA_ITEM_ID = "itemId"
        private const val EXTRA_TITLE = "title"
        fun intent(ctx: Context, itemId: UUID, title: String) =
            Intent(ctx, PlayerActivity::class.java).apply {
                putExtra(EXTRA_ITEM_ID, itemId.toString())
                putExtra(EXTRA_TITLE, title)
            }
    }
}
