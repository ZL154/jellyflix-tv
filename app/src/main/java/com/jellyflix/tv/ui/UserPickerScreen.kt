package com.jellyflix.tv.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.OutlinedButton
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import coil.compose.AsyncImage

private val DefaultAccent = Color(0xFF7E5BEF)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun UserPickerScreen(
    onPickUser: (username: String, requiresPassword: Boolean) -> Unit,
    onAddUser: () -> Unit,
    onChangeServer: () -> Unit,
    vm: UserPickerViewModel = hiltViewModel(),
) {
    val state by vm.state.collectAsState()
    val accent = state.accentColor?.let { Color(it) } ?: DefaultAccent

    Surface(Modifier.fillMaxSize()) {
        Box(Modifier.fillMaxSize()) {
            // Backdrop: server splashscreen blurred, or a subtle gradient.
            if (state.splashscreenUrl != null) {
                AsyncImage(
                    model = state.splashscreenUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().blur(42.dp),
                )
                Box(
                    Modifier.fillMaxSize().background(
                        Brush.verticalGradient(
                            listOf(Color(0xCC0B0D12), Color(0xE60B0D12), Color(0xFF0B0D12)),
                        )
                    )
                )
            } else {
                Box(
                    Modifier.fillMaxSize().background(
                        Brush.radialGradient(
                            colors = listOf(accent.copy(alpha = 0.18f), Color(0xFF0B0D12)),
                            radius = 1400f,
                        )
                    )
                )
            }

            // Content
            Column(
                modifier = Modifier.fillMaxSize().padding(horizontal = 48.dp, vertical = 56.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(Modifier.weight(0.5f))

                Text(
                    "Who's watching?",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 56.sp,
                    ),
                    textAlign = TextAlign.Center,
                )
                state.serverName?.takeIf { it.isNotBlank() }?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                        color = Color(0xFFA8ADBD),
                        modifier = Modifier.padding(top = 12.dp),
                    )
                }

                Spacer(Modifier.height(56.dp))

                when {
                    state.loading -> Text(
                        "Loading users…",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    state.users.isEmpty() && state.error == null -> Text(
                        "No public users on this server. Tap Add user to sign in manually.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )

                    else -> FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(40.dp, Alignment.CenterHorizontally),
                        verticalArrangement = Arrangement.spacedBy(32.dp),
                        maxItemsInEachRow = 6,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        state.users.forEach { u ->
                            UserTile(user = u, accent = accent, onClick = {
                                onPickUser(u.name, u.hasPassword)
                            })
                        }
                        AddUserTile(accent = accent, onClick = onAddUser)
                    }
                }

                state.error?.let {
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Couldn't fetch users: $it",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                    )
                }

                Spacer(Modifier.weight(1f))

                OutlinedButton(
                    onClick = onChangeServer,
                    modifier = Modifier.width(220.dp).height(48.dp),
                ) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Change server", style = MaterialTheme.typography.titleLarge)
                    }
                }
            }
        }
    }
}

@Composable
private fun UserTile(
    user: UserPickerViewModel.PublicUser,
    accent: Color,
    onClick: () -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()
    val scale by animateFloatAsState(
        targetValue = if (focused) 1.08f else 1f,
        animationSpec = tween(durationMillis = 140),
        label = "user-scale",
    )
    val ringWidth by animateFloatAsState(
        targetValue = if (focused) 4f else 0f,
        animationSpec = tween(durationMillis = 140),
        label = "user-ring",
    )

    Column(
        modifier = Modifier.width(180.dp).scale(scale),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(160.dp)
                .clip(CircleShape)
                .background(Color(0xFF1C2130))
                .border(width = ringWidth.dp, color = accent, shape = CircleShape)
                .clickable(interactionSource = interaction, indication = null, onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            if (user.avatarUrl != null) {
                AsyncImage(
                    model = user.avatarUrl,
                    contentDescription = user.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                Text(
                    user.name.take(1).uppercase(),
                    style = MaterialTheme.typography.displayLarge.copy(fontSize = 72.sp),
                    color = Color(0xFFECEEF5),
                )
            }
        }
        Spacer(Modifier.height(16.dp))
        Text(
            user.name,
            style = MaterialTheme.typography.titleLarge,
            color = if (focused) Color.White else Color(0xFFA8ADBD),
            maxLines = 1,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun AddUserTile(accent: Color, onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()
    val scale by animateFloatAsState(
        targetValue = if (focused) 1.08f else 1f,
        animationSpec = tween(durationMillis = 140),
        label = "add-scale",
    )

    Column(
        modifier = Modifier.width(180.dp).scale(scale),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(160.dp)
                .clip(CircleShape)
                .background(Color(0x221C2130))
                .border(
                    width = if (focused) 4.dp else 2.dp,
                    color = if (focused) accent else Color(0xFF6C7389),
                    shape = CircleShape,
                )
                .clickable(interactionSource = interaction, indication = null, onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Filled.PersonAdd,
                contentDescription = "Add user",
                tint = if (focused) accent else Color(0xFFA8ADBD),
                modifier = Modifier.size(48.dp),
            )
        }
        Spacer(Modifier.height(16.dp))
        Text(
            "Add user",
            style = MaterialTheme.typography.titleLarge,
            color = if (focused) Color.White else Color(0xFFA8ADBD),
            textAlign = TextAlign.Center,
        )
    }
}
