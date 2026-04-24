package com.jellyflix.tv.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.jellyflix.tv.data.ImageUrls
import org.jellyfin.sdk.model.api.BaseItemDto

private val CardShape = RoundedCornerShape(12.dp)
private val FocusRing = Color(0xFF7E5BEF)

@Composable
fun MediaCard(
    item: BaseItemDto,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    widthDp: Int = 180,
    heightDp: Int = 270,
) {
    // Request at ~3x logical width so the TV's pixel density doesn't over-sample.
    val posterUrl = remember(item.id, widthDp) { ImageUrls.primary(item, maxWidth = widthDp * 3) }
    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()
    val scale by animateFloatAsState(
        targetValue = if (focused) 1.05f else 1f,
        animationSpec = tween(120),
        label = "card-scale",
    )

    Column(modifier = modifier.width(widthDp.dp)) {
        Box(
            modifier = Modifier
                .width(widthDp.dp)
                .height(heightDp.dp)
                // Use graphicsLayer for scale — cheaper than Modifier.scale because
                // it hints the compositor to use a render layer.
                .graphicsLayer(scaleX = scale, scaleY = scale)
                .clip(CardShape)
                .background(Color(0xFF1C2130))
                .border(
                    width = if (focused) 2.dp else 0.dp,
                    color = if (focused) FocusRing else Color.Transparent,
                    shape = CardShape,
                )
                .clickable(interactionSource = interaction, indication = null, onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            if (posterUrl != null) {
                AsyncImage(
                    model = posterUrl,
                    contentDescription = item.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                androidx.tv.material3.Text(
                    item.name.orEmpty().take(1).uppercase(),
                    style = androidx.tv.material3.MaterialTheme.typography.displayLarge,
                    color = Color(0xFFA8ADBD),
                )
            }
        }
        androidx.tv.material3.Text(
            text = item.name.orEmpty(),
            style = androidx.tv.material3.MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium),
            color = if (focused) Color.White else Color(0xFFD8DBE5),
            maxLines = 1,
            modifier = Modifier.padding(top = 8.dp).width(widthDp.dp),
        )
    }
}

// Keep the deprecated reference for older code paths; noop suppress.
@Suppress("UNUSED")
private fun Modifier.noScale() = this.scale(1f)
