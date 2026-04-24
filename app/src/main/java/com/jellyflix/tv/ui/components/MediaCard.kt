package com.jellyflix.tv.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Card
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import com.jellyflix.tv.data.ImageUrls
import org.jellyfin.sdk.model.api.BaseItemDto

private val FocusRing = Color(0xFF7E5BEF)

@Composable
fun MediaCard(
    item: BaseItemDto,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    widthDp: Int = 180,
    heightDp: Int = 270,
) {
    val poster = remember(item.id) { ImageUrls.primary(item, maxWidth = widthDp * 2) }
    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()
    val scale by animateFloatAsState(
        targetValue = if (focused) 1.06f else 1f,
        animationSpec = tween(durationMillis = 120),
        label = "card-scale",
    )

    Column(modifier = modifier.width(widthDp.dp)) {
        Card(
            onClick = onClick,
            interactionSource = interaction,
            modifier = Modifier
                .width(widthDp.dp)
                .height(heightDp.dp)
                .scale(scale)
                .border(
                    width = if (focused) 2.dp else 0.dp,
                    color = if (focused) FocusRing else Color.Transparent,
                    shape = RoundedCornerShape(12.dp),
                )
                .clip(RoundedCornerShape(12.dp)),
        ) {
            Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant)) {
                if (poster != null) {
                    AsyncImage(
                        model = poster,
                        contentDescription = item.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    Text(
                        item.name.orEmpty().take(1).uppercase(),
                        style = MaterialTheme.typography.displayLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(24.dp).align(Alignment.Center),
                    )
                }
            }
        }
        Text(
            text = item.name.orEmpty(),
            style = MaterialTheme.typography.labelLarge,
            maxLines = 1,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}
