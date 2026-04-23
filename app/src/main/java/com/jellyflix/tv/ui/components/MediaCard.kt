package com.jellyflix.tv.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Card
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import com.jellyflix.tv.data.ImageUrls
import org.jellyfin.sdk.model.api.BaseItemDto

@Composable
fun MediaCard(
    item: BaseItemDto,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val poster = remember(item.id) { ImageUrls.primary(item) }
    Column(modifier = modifier.width(180.dp)) {
        Card(onClick = onClick, modifier = Modifier.width(180.dp).height(270.dp)) {
            Box(Modifier.fillMaxSize()) {
                if (poster != null) {
                    AsyncImage(
                        model = poster,
                        contentDescription = item.name,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }
        Text(
            text = item.name.orEmpty(),
            style = MaterialTheme.typography.labelLarge,
            maxLines = 2,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}
