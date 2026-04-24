package com.jellyflix.tv.ui

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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
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
import org.jellyfin.sdk.model.api.BaseItemDto

@Composable
fun HomeScreen(
    onOpenLibrary: (String) -> Unit,
    onOpenDetails: (String) -> Unit,
    onOpenSettings: () -> Unit,
    vm: HomeViewModel = hiltViewModel(),
) {
    val state by vm.state.collectAsState()

    Surface(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 48.dp),
            verticalArrangement = Arrangement.spacedBy(28.dp),
        ) {
            // Top bar (over-hero)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 48.dp, vertical = 28.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "Jellyflix",
                        style = MaterialTheme.typography.displayLarge,
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Filled.Settings, contentDescription = "Settings")
                    }
                }
            }

            // Hero banner from the first Continue-Watching item (falls back to first Next-Up)
            state.continueWatching.firstOrNull()?.let { hero ->
                item { Hero(item = hero, onPlay = onOpenDetails) }
            } ?: state.nextUp.firstOrNull()?.let { hero ->
                item { Hero(item = hero, onPlay = onOpenDetails) }
            }

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
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 48.dp),
                    )
                }
            }
            state.error?.let { err ->
                item {
                    Text(
                        "Couldn't load home: $err",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(horizontal = 48.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun Hero(item: BaseItemDto, onPlay: (String) -> Unit) {
    val backdrop = ImageUrls.backdrop(item)
    Box(modifier = Modifier.fillMaxWidth().height(420.dp)) {
        if (backdrop != null) {
            AsyncImage(
                model = backdrop,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }
        // Gradient scrim so the text stays legible over any backdrop.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0x66000000), Color(0xCC0B0D12), Color(0xFF0B0D12)),
                    )
                )
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 48.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.Bottom,
        ) {
            Text(
                item.seriesName ?: item.name.orEmpty(),
                style = MaterialTheme.typography.displayMedium,
            )
            item.name?.takeIf { it != item.seriesName }?.let {
                Text(
                    it,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
            item.overview?.takeIf { it.isNotBlank() }?.let {
                Text(
                    it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    modifier = Modifier.padding(top = 8.dp).width(720.dp),
                )
            }
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = { onPlay(item.id.toString()) },
                modifier = Modifier.height(52.dp).width(200.dp),
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.PlayArrow, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Resume", style = MaterialTheme.typography.titleLarge)
                    }
                }
            }
        }
    }
}

@Composable
private fun RowSection(title: String, items: List<BaseItemDto>, onClick: (String) -> Unit) {
    Column(modifier = Modifier.padding(start = 48.dp, end = 48.dp)) {
        Text(title, style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(bottom = 12.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            items(items, key = { it.id.toString() }) { item ->
                MediaCard(item = item, onClick = { onClick(item.id.toString()) })
            }
        }
    }
}

@Composable
private fun LibraryRow(libs: List<BaseItemDto>, onOpen: (String) -> Unit) {
    Column(modifier = Modifier.padding(start = 48.dp, end = 48.dp)) {
        Text("Your libraries", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(bottom = 12.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            items(libs, key = { it.id.toString() }) { lib ->
                MediaCard(item = lib, onClick = { onOpen(lib.id.toString()) }, widthDp = 220, heightDp = 140)
            }
        }
    }
}
