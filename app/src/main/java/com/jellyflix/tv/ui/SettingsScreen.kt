package com.jellyflix.tv.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Switch
import androidx.tv.material3.Text
import com.jellyflix.tv.data.SettingsStore

@Composable
fun SettingsScreen(vm: SettingsViewModel = hiltViewModel()) {
    val prefs by vm.prefs.collectAsState(initial = SettingsStore.Prefs())
    val plugins by vm.plugins.collectAsState()

    Surface(Modifier.fillMaxSize()) {
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 72.dp, vertical = 48.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Text("Settings", style = MaterialTheme.typography.displayMedium, modifier = Modifier.padding(bottom = 24.dp))
            }

            item { SectionHeader("Playback") }
            item {
                ToggleRow(
                    label = "Prefer direct play",
                    subtitle = "Skip the server transcoder when the TV can decode the source.",
                    checked = prefs.preferDirectPlay,
                    onCheckedChange = vm::setPreferDirectPlay,
                )
            }
            item {
                ToggleRow(
                    label = "Tunneled decoding",
                    subtitle = "Lower latency. Disable if playback flickers on your TV.",
                    checked = prefs.tunneledPlayback,
                    onCheckedChange = vm::setTunneledPlayback,
                )
            }
            item {
                ToggleRow(
                    label = "Audio passthrough (Atmos / DTS)",
                    subtitle = "Bit-perfect send to AVR. Turn off if audio cuts out.",
                    checked = prefs.audioPassthrough,
                    onCheckedChange = vm::setAudioPassthrough,
                )
            }
            item {
                ToggleRow(
                    label = "Match display refresh rate",
                    subtitle = "Switches the panel to 24 / 25 / 50 Hz on matching content.",
                    checked = prefs.matchRefreshRate,
                    onCheckedChange = vm::setMatchRefreshRate,
                )
            }

            item { SectionHeader("Plugins") }
            item {
                ToggleRow(
                    label = "Enable plugins",
                    subtitle = "Load installed Jellyflix plugin APKs.",
                    checked = prefs.pluginsEnabled,
                    onCheckedChange = vm::setPluginsEnabled,
                )
            }
            items(plugins, key = { it.manifest.id }) { entry ->
                PluginRow(entry)
            }
            if (plugins.isEmpty()) {
                item {
                    Text(
                        "No plugins installed. Sideload a Jellyflix plugin APK and it'll appear here.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.headlineMedium,
        modifier = Modifier.padding(top = 16.dp, bottom = 4.dp),
    )
}

@Composable
private fun ToggleRow(
    label: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.titleLarge)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun PluginRow(entry: com.jellyflix.tv.plugin.PluginManager.LoadedPlugin) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
    ) {
        Text("${entry.manifest.name}  v${entry.manifest.version}", style = MaterialTheme.typography.titleLarge)
        Text(entry.manifest.id, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (entry.manifest.permissions.isNotEmpty()) {
            Text(
                "Permissions: " + entry.manifest.permissions.joinToString(", "),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
    }
}

