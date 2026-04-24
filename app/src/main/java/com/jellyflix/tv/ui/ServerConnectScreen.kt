package com.jellyflix.tv.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dns
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Button
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import com.jellyflix.tv.ui.components.JellyflixTextField

@Composable
fun ServerConnectScreen(onConnected: (String) -> Unit) {
    var url by remember { mutableStateOf("http://") }
    val fieldFocus = remember { FocusRequester() }

    LaunchedEffect(Unit) { fieldFocus.requestFocus() }

    val submit: () -> Unit = {
        val trimmed = url.trim()
        if (trimmed.isNotEmpty() && trimmed != "http://") onConnected(trimmed)
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 96.dp, vertical = 64.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start,
        ) {
            Icon(Icons.Filled.Dns, contentDescription = null, modifier = Modifier.padding(bottom = 16.dp))
            Text("Connect to Jellyfin", style = MaterialTheme.typography.displayMedium)
            Text(
                "Enter your server address",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 8.dp, bottom = 32.dp),
            )

            JellyflixTextField(
                value = url,
                onValueChange = { url = it },
                label = "Server URL",
                placeholder = "http://192.168.1.2:8096",
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Uri,
                    imeAction = ImeAction.Go,
                ),
                keyboardActions = KeyboardActions(onGo = { submit() }),
                modifier = Modifier.width(640.dp).focusRequester(fieldFocus),
            )

            Spacer(Modifier.height(24.dp))

            Button(onClick = submit, modifier = Modifier.width(220.dp).height(56.dp)) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Continue", style = MaterialTheme.typography.titleLarge)
                }
            }
        }
    }
}
