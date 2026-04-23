package com.jellyflix.tv.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.tv.material3.Button
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text

@Composable
fun LoginScreen(
    onAuthenticated: () -> Unit,
    vm: LoginViewModel = hiltViewModel(),
) {
    var username by remember { mutableStateOf(TextFieldValue("")) }
    var password by remember { mutableStateOf(TextFieldValue("")) }
    val state by vm.state.collectAsState()

    if (state is LoginViewModel.State.Success) onAuthenticated()

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 96.dp, vertical = 64.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            Text("Sign in", style = MaterialTheme.typography.displayMedium)
            Spacer(Modifier.height(24.dp))

            Text("Username", style = MaterialTheme.typography.labelLarge)
            BasicTextField(
                value = username,
                onValueChange = { username = it },
                singleLine = true,
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                modifier = Modifier.width(480.dp).padding(vertical = 8.dp),
            )

            Spacer(Modifier.height(16.dp))
            Text("Password", style = MaterialTheme.typography.labelLarge)
            BasicTextField(
                value = password,
                onValueChange = { password = it },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                modifier = Modifier.width(480.dp).padding(vertical = 8.dp),
            )

            Spacer(Modifier.height(24.dp))
            Button(
                onClick = { vm.signIn(username.text, password.text) },
                enabled = state !is LoginViewModel.State.Submitting,
            ) { Text("Sign in") }

            if (state is LoginViewModel.State.Error) {
                Spacer(Modifier.height(16.dp))
                Text(
                    (state as LoginViewModel.State.Error).message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}
