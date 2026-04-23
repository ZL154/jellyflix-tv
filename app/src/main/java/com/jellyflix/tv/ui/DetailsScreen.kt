package com.jellyflix.tv.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.tv.material3.Button
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import com.jellyflix.tv.data.ImageUrls
import com.jellyflix.tv.ui.components.MediaCard
import java.util.Locale

@Composable
fun DetailsScreen(
    itemId: String,
    onPlay: (itemId: String, title: String) -> Unit,
    onBack: () -> Unit,
    vm: DetailsViewModel = hiltViewModel(),
) {
    val state by vm.state.collectAsState()
    LaunchedEffect(itemId) { vm.load(itemId) }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            val item = state.item ?: return@Column

            // Hero: backdrop with gradient scrim.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(440.dp),
            ) {
                ImageUrls.backdrop(item)?.let {
                    AsyncImage(model = it, contentDescription = null, modifier = Modifier.fillMaxSize())
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 240.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color.Transparent, MaterialTheme.colorScheme.background)
                                )
                            ),
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 48.dp, vertical = 40.dp),
                    verticalArrangement = Arrangement.Bottom,
                ) {
                    Text(
                        item.name.orEmpty(),
                        style = MaterialTheme.typography.displayMedium,
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 8.dp),
                    ) {
                        item.productionYear?.let {
                            Chip(it.toString())
                        }
                        item.officialRating?.let { Chip(it) }
                        item.runTimeTicks?.let { ticks ->
                            val minutes = (ticks / 10_000_000L / 60L).toInt()
                            if (minutes > 0) Chip(String.format(Locale.getDefault(), "%dh %dm", minutes / 60, minutes % 60))
                        }
                        item.communityRating?.let { Chip(String.format(Locale.getDefault(), "★ %.1f", it)) }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 48.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                Button(
                    onClick = { onPlay(item.id.toString(), item.name.orEmpty()) },
                    modifier = Modifier.height(56.dp).width(220.dp),
                ) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Play", style = MaterialTheme.typography.titleLarge)
                }

                item.overview?.takeIf { it.isNotBlank() }?.let { overview ->
                    Text(
                        overview,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 6,
                    )
                }

                if (state.episodes.isNotEmpty()) {
                    Text("Episodes", style = MaterialTheme.typography.headlineMedium)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        items(state.episodes, key = { it.id.toString() }) { ep ->
                            MediaCard(item = ep, onClick = { onPlay(ep.id.toString(), ep.name.orEmpty()) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Chip(text: String) {
    Surface(shape = MaterialTheme.shapes.small) {
        Text(
            text,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
        )
    }
}

