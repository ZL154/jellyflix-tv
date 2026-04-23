package com.jellyflix.tv.ui

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dns
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Button
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text

@Composable
fun ServerConnectScreen(onConnected: (String) -> Unit) {
    var url by remember { mutableStateOf(TextFieldValue("")) }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
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

            Box(
                modifier = Modifier
                    .width(640.dp)
                    .padding(bottom = 24.dp),
            ) {
                BasicTextField(
                    value = url,
                    onValueChange = { url = it },
                    singleLine = true,
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Uri,
                        imeAction = ImeAction.Done,
                    ),
                    keyboardActions = KeyboardActions(onDone = {
                        url.text.trim().takeIf { it.isNotEmpty() }?.let(onConnected)
                    }),
                    modifier = Modifier
                        .fillMaxSize()
                        .focusable(),
                )
                if (url.text.isEmpty()) {
                    Text("http://jellyfin.local:8096", style = MaterialTheme.typography.bodyLarge)
                }
            }

            Button(onClick = { url.text.trim().takeIf { it.isNotEmpty() }?.let(onConnected) }) {
                Text("Continue")
            }
        }
    }
}
