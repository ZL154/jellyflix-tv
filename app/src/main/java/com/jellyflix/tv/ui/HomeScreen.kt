package com.jellyflix.tv.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.tv.material3.Button
import androidx.tv.material3.Icon
import androidx.tv.material3.IconButton
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import com.jellyflix.tv.data.ImageUrls
import com.jellyflix.tv.ui.components.MediaCard
import kotlinx.coroutines.delay
import org.jellyfin.sdk.model.api.BaseItemDto

// TV-safe horizontal padding that keeps content inside the action-safe area on
// panels with overscan, and gives enough breathing room on the ones that don't.
private val EdgePad = 56.dp

@Composable
fun HomeScreen(
    onOpenLibrary: (String) -> Unit,
    onOpenDetails: (String) -> Unit,
    onOpenSettings: () -> Unit,
    vm: HomeViewModel = hiltViewModel(),
) {
    val state by vm.state.collectAsState()

    val heroItems = remember(state.continueWatching, state.nextUp) {
        (state.continueWatching + state.nextUp).take(6)
    }
    var heroIndex by remember { mutableIntStateOf(0) }
    LaunchedEffect(heroItems.size) {
        if (heroItems.size > 1) {
            while (true) {
                delay(9_000)
                heroIndex = (heroIndex + 1) % heroItems.size
            }
        }
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Box(Modifier.fillMaxSize()) {

            // === Background backdrop (cross-fades between hero items) ===
            val currentHero = heroItems.getOrNull(heroIndex)
            AnimatedContent(
                targetState = currentHero?.id?.toString(),
                transitionSpec = {
                    (fadeIn(tween(700)) togetherWith fadeOut(tween(700)))
                },
                label = "hero-backdrop",
            ) { _ ->
                val item = currentHero
                if (item != null) {
                    val url = ImageUrls.backdrop(item)
                    if (url != null) {
                        AsyncImage(
                            model = url,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                        )
                    } else {
                        Box(Modifier.fillMaxSize().background(Color(0xFF0B0D12)))
                    }
                } else {
                    Box(Modifier.fillMaxSize().background(Color(0xFF0B0D12)))
                }
            }

            // Left-to-right dim + bottom fade so foreground text / cards stay legible.
            Box(
                Modifier.fillMaxSize().background(
                    Brush.horizontalGradient(
                        0f to Color(0xF20B0D12),
                        0.45f to Color(0x990B0D12),
                        1f to Color(0x330B0D12),
                    )
                )
            )
            Box(
                Modifier.fillMaxSize().background(
                    Brush.verticalGradient(
                        0f to Color.Transparent,
                        0.55f to Color(0x550B0D12),
                        1f to Color(0xFF0B0D12),
                    )
                )
            )

            // === Foreground: top bar, hero text, rows ===
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 48.dp),
                verticalArrangement = Arrangement.spacedBy(36.dp),
            ) {
                // Top bar
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = EdgePad, vertical = 32.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            "Jellyflix",
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 40.sp,
                            ),
                            color = Color.White,
                            modifier = Modifier.weight(1f),
                        )
                        IconButton(onClick = onOpenSettings) {
                            Icon(Icons.Filled.Settings, contentDescription = "Settings", tint = Color.White)
                        }
                    }
                }

                // Hero text block
                currentHero?.let { hero ->
                    item { HeroText(item = hero, onPlay = onOpenDetails) }
                }

                // Rows
                if (state.continueWatching.isNotEmpty()) {
                    item { RowSection("Continue watching", state.continueWatching, onClick = onOpenDetails) }
                }
                if (state.nextUp.isNotEmpty()) {
                    item { RowSection("Next up", state.nextUp, onClick = onOpenDetails) }
                }
                if (state.libraries.isNotEmpty()) {
                    item { LibraryRow(state.libraries, onOpenLibrary) }
                }

                if (state.loading && state.continueWatching.isEmpty() && state.libraries.isEmpty()) {
                    item {
                        Text(
                            "Loading your library…",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color(0xFFA8ADBD),
                            modifier = Modifier.padding(horizontal = EdgePad),
                        )
                    }
                }
                state.error?.let { err ->
                    item {
                        Text(
                            "Couldn't load home: $err",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(horizontal = EdgePad),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HeroText(item: BaseItemDto, onPlay: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = EdgePad)
            .padding(top = 16.dp, bottom = 12.dp)
            .width(780.dp),
    ) {
        Text(
            item.seriesName ?: item.name.orEmpty(),
            style = MaterialTheme.typography.displayLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 54.sp,
            ),
            color = Color.White,
            maxLines = 2,
        )
        item.name?.takeIf { it != item.seriesName }?.let {
            Text(
                it,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Medium),
                color = Color(0xFFD8DBE5),
                modifier = Modifier.padding(top = 6.dp),
                maxLines = 1,
            )
        }
        item.overview?.takeIf { it.isNotBlank() }?.let {
            Text(
                it,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFA8ADBD),
                maxLines = 3,
                modifier = Modifier.padding(top = 12.dp).width(680.dp),
            )
        }
        Spacer(Modifier.height(20.dp))
        Button(
            onClick = { onPlay(item.id.toString()) },
            modifier = Modifier.height(52.dp).width(220.dp),
        ) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Play",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                    )
                }
            }
        }
    }
}

@Composable
private fun RowSection(title: String, items: List<BaseItemDto>, onClick: (String) -> Unit) {
    Column(modifier = Modifier.padding(start = EdgePad, end = EdgePad)) {
        Text(
            title,
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.SemiBold),
            color = Color.White,
            modifier = Modifier.padding(bottom = 14.dp),
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            items(items, key = { it.id.toString() }) { item ->
                MediaCard(item = item, onClick = { onClick(item.id.toString()) })
            }
        }
    }
}

@Composable
private fun LibraryRow(libs: List<BaseItemDto>, onOpen: (String) -> Unit) {
    Column(modifier = Modifier.padding(start = EdgePad, end = EdgePad)) {
        Text(
            "Your libraries",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.SemiBold),
            color = Color.White,
            modifier = Modifier.padding(bottom = 14.dp),
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            items(libs, key = { it.id.toString() }) { lib ->
                MediaCard(item = lib, onClick = { onOpen(lib.id.toString()) }, widthDp = 240, heightDp = 140)
            }
        }
    }
}
