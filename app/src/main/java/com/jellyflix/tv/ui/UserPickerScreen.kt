package com.jellyflix.tv.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
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

@Composable
fun UserPickerScreen(
    onPickUser: (username: String, requiresPassword: Boolean) -> Unit,
    onAddUser: () -> Unit,
    onChangeServer: () -> Unit,
    vm: UserPickerViewModel = hiltViewModel(),
) {
    val state by vm.state.collectAsState()

    Surface(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 48.dp, vertical = 72.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                "Who's watching?",
                style = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.SemiBold, fontSize = 56.sp),
                textAlign = TextAlign.Center,
            )
            Text(
                state.serverName ?: "",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp),
            )

            Spacer(Modifier.height(48.dp))

            when {
                state.loading -> Text("Loading users…", color = MaterialTheme.colorScheme.onSurfaceVariant)
                state.users.isEmpty() && state.error == null -> Text(
                    "No public users on this server. Tap Add user to sign in manually.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
                else -> LazyRow(
                    contentPadding = PaddingValues(horizontal = 32.dp),
                    horizontalArrangement = Arrangement.spacedBy(32.dp),
                ) {
                    items(state.users, key = { it.id }) { u ->
                        UserTile(user = u, onClick = {
                            onPickUser(u.name, u.hasPassword)
                        })
                    }
                    item {
                        AddUserTile(onClick = onAddUser)
                    }
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

            Spacer(Modifier.height(48.dp))

            OutlinedButton(
                onClick = onChangeServer,
                modifier = Modifier.width(220.dp).height(52.dp),
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Change server", style = MaterialTheme.typography.titleLarge)
                }
            }
        }
    }
}

@Composable
private fun UserTile(user: UserPickerViewModel.PublicUser, onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()
    val scale by animateFloatAsState(
        targetValue = if (focused) 1.12f else 1f,
        animationSpec = tween(durationMillis = 140),
        label = "user-scale",
    )

    Column(
        modifier = Modifier.width(180.dp).scale(scale),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Surface(
            onClick = onClick,
            interactionSource = interaction,
            shape = CircleShape,
            modifier = Modifier
                .size(160.dp)
                .clip(CircleShape)
                .border(
                    width = if (focused) 4.dp else 0.dp,
                    color = if (focused) Color(0xFF7E5BEF) else Color.Transparent,
                    shape = CircleShape,
                ),
        ) {
            Box(
                Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant),
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
                    )
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        Text(
            user.name,
            style = MaterialTheme.typography.titleLarge,
            color = if (focused) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun AddUserTile(onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()
    val scale by animateFloatAsState(
        targetValue = if (focused) 1.12f else 1f,
        animationSpec = tween(durationMillis = 140),
        label = "add-scale",
    )

    Column(
        modifier = Modifier.width(180.dp).scale(scale),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Surface(
            onClick = onClick,
            interactionSource = interaction,
            shape = CircleShape,
            modifier = Modifier
                .size(160.dp)
                .clip(CircleShape)
                .border(
                    width = if (focused) 4.dp else 2.dp,
                    color = if (focused) Color(0xFF7E5BEF) else Color(0xFF6C7389),
                    shape = CircleShape,
                ),
        ) {
            Box(
                Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Filled.PersonAdd,
                    contentDescription = "Add user",
                    modifier = Modifier.size(48.dp),
                )
            }
        }
        Spacer(Modifier.height(16.dp))
        Text(
            "Add user",
            style = MaterialTheme.typography.titleLarge,
            color = if (focused) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}
