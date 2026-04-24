package com.jellyflix.tv.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.tv.material3.Button
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.OutlinedButton
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import com.jellyflix.tv.data.BrandingRepository
import com.jellyflix.tv.session.SessionViewModel
import com.jellyflix.tv.ui.components.JellyflixPasswordField
import com.jellyflix.tv.ui.components.JellyflixTextField

@Composable
fun LoginScreen(
    onAuthenticated: () -> Unit,
    onBack: () -> Unit = {},
    initialUsername: String = "",
    vm: LoginViewModel = hiltViewModel(),
    sessionVm: SessionViewModel = hiltViewModel(),
) {
    var username by remember { mutableStateOf(initialUsername) }
    var password by remember { mutableStateOf("") }
    val state by vm.state.collectAsState()
    val branding by vm.branding.collectAsState(initial = BrandingRepository.Branding())
    val firstFocus = remember { FocusRequester() }

    LaunchedEffect(Unit) { firstFocus.requestFocus() }
    if (state is LoginViewModel.State.Success) onAuthenticated()

    val submit: () -> Unit = { vm.signIn(username, password) }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 96.dp, vertical = 64.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            Text("Sign in", style = MaterialTheme.typography.displayMedium)
            if (initialUsername.isNotBlank()) {
                Text(
                    "as $initialUsername",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
            Spacer(Modifier.height(24.dp))

            JellyflixTextField(
                value = username,
                onValueChange = { username = it },
                label = "Username",
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next,
                ),
                modifier = Modifier
                    .width(480.dp)
                    .then(if (initialUsername.isBlank()) Modifier.focusRequester(firstFocus) else Modifier),
            )

            Spacer(Modifier.height(16.dp))

            JellyflixPasswordField(
                value = password,
                onValueChange = { password = it },
                label = "Password",
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Go,
                ),
                keyboardActions = KeyboardActions(onGo = { submit() }),
                modifier = Modifier
                    .width(480.dp)
                    .then(if (initialUsername.isNotBlank()) Modifier.focusRequester(firstFocus) else Modifier),
            )

            Spacer(Modifier.height(24.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(
                    onClick = submit,
                    enabled = state !is LoginViewModel.State.Submitting,
                    modifier = Modifier.width(220.dp).height(56.dp),
                ) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Sign in", style = MaterialTheme.typography.titleLarge)
                    }
                }

                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier.width(180.dp).height(56.dp),
                ) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Back", style = MaterialTheme.typography.titleLarge)
                    }
                }
            }

            if (state is LoginViewModel.State.Error) {
                Spacer(Modifier.height(16.dp))
                Text(
                    (state as LoginViewModel.State.Error).message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                )
            }

            branding.loginDisclaimer?.let {
                Spacer(Modifier.height(32.dp))
                Text(
                    it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.width(480.dp),
                )
            }
        }
    }
}
