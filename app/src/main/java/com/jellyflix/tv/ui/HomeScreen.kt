package com.jellyflix.tv.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import com.jellyflix.tv.ui.components.MediaCard
import org.jellyfin.sdk.model.api.BaseItemDto

@Composable
fun HomeScreen(
    onOpenLibrary: (String) -> Unit,
    vm: HomeViewModel = hiltViewModel(),
) {
    val state by vm.state.collectAsState()

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 48.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp),
        ) {
            item {
                Text("Jellyflix", style = MaterialTheme.typography.displayLarge)
                Spacer(Modifier.height(8.dp))
            }

            if (state.continueWatching.isNotEmpty()) {
                item { Row(title = "Continue watching", items = state.continueWatching) }
            }
            if (state.nextUp.isNotEmpty()) {
                item { Row(title = "Next up", items = state.nextUp) }
            }
            if (state.libraries.isNotEmpty()) {
                item { LibraryRow(state.libraries, onOpenLibrary) }
            }
        }
    }
}

@Composable
private fun Row(title: String, items: List<BaseItemDto>) {
    Column {
        Text(title, style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(bottom = 12.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            items(items, key = { it.id.toString() }) { MediaCard(item = it, onClick = {}) }
        }
    }
}

@Composable
private fun LibraryRow(libs: List<BaseItemDto>, onOpen: (String) -> Unit) {
    Column {
        Text("Your libraries", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(bottom = 12.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            items(libs, key = { it.id.toString() }) { lib ->
                MediaCard(item = lib, onClick = { onOpen(lib.id.toString()) })
            }
        }
    }
}
