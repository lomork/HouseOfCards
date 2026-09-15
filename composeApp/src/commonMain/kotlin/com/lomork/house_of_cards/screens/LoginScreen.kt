package com.lomork.house_of_cards.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lomork.house_of_cards.data.AppStore
import com.lomork.house_of_cards.data.AuthOutcome
import com.lomork.house_of_cards.data.AuthService
import com.lomork.house_of_cards.theme.Hoc
import com.lomork.house_of_cards.ui.HocButton
import com.lomork.house_of_cards.ui.HocCard
import kotlinx.coroutines.launch

private enum class AuthMode { LOGIN, SIGNUP }

@Composable
fun LoginScreen(store: AppStore) {
    var mode by remember { mutableStateOf(AuthMode.LOGIN) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    fun submit() {
        if (busy) return
        if (mode == AuthMode.SIGNUP && username.trim().isEmpty()) {
            error = "Choose a username."
            return
        }
        busy = true
        error = null
        scope.launch {
            val outcome = if (mode == AuthMode.SIGNUP) {
                AuthService.signUp(email, password)
            } else {
                AuthService.signIn(email, password)
            }
            when (outcome) {
                is AuthOutcome.Success -> {
                    val uid = outcome.uid
                    if (mode == AuthMode.SIGNUP) {
                        store.setUsername(username)
                        val syncError = store.provisionCloud(uid, username)
                        if (syncError != null) {
                            error = syncError
                            busy = false
                            return@launch
                        }
                    } else {
                        val syncError = store.hydrateFromCloud(uid)
                        if (syncError != null) {
                            error = syncError
                            busy = false
                            return@launch
                        }
                    }
                    store.markLoggedIn(email, uid)
                    busy = false
                }
                is AuthOutcome.Failure -> {
                    error = outcome.message
                    busy = false
                }
            }
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Spacer(Modifier.height(24.dp))
        Text("\u2660", fontSize = 52.sp, color = Hoc.Gold)
        Text("House of Cards", style = MaterialTheme.typography.headlineLarge, color = Hoc.Gold)
        Text("Pick your table.", style = MaterialTheme.typography.bodyMedium, color = Hoc.TextSecondary)
        Spacer(Modifier.height(8.dp))

        HocCard {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (mode == AuthMode.SIGNUP) {
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Username") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth(),
                )

                if (error != null) {
                    Text(error ?: "", style = MaterialTheme.typography.bodySmall, color = Hoc.Danger)
                }

                HocButton(
                    text = if (mode == AuthMode.SIGNUP) "Create account" else "Sign in",
                    enabled = !busy,
                ) { submit() }

                if (busy) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.CenterHorizontally).height(24.dp),
                        color = Hoc.Gold,
                    )
                }

                TextButton(onClick = {
                    mode = if (mode == AuthMode.LOGIN) AuthMode.SIGNUP else AuthMode.LOGIN
                    error = null
                }) {
                    Text(
                        if (mode == AuthMode.LOGIN) "New here? Create an account" else "Have an account? Sign in",
                        color = Hoc.Gold,
                    )
                }
            }
        }

        Text(
            if (AuthService.isConfigured) "Sign-in is backed by Firebase (email)."
            else "Firebase isn't configured yet — using local demo auth.",
            style = MaterialTheme.typography.labelSmall,
            color = Hoc.TextMuted,
        )

        Spacer(Modifier.height(24.dp))
    }
}