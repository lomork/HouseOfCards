package com.lomork.house_of_cards.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lomork.house_of_cards.data.AppStore
import com.lomork.house_of_cards.data.AuthService
import com.lomork.house_of_cards.nav.Navigator
import com.lomork.house_of_cards.theme.Hoc
import com.lomork.house_of_cards.ui.HocButton
import com.lomork.house_of_cards.ui.HocCard
import com.lomork.house_of_cards.ui.SectionHeader

@Composable
fun SettingsScreen(store: AppStore, nav: Navigator) {
    val s = store.saved
    var confirmSignOut by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text("Settings", style = MaterialTheme.typography.headlineLarge, color = Hoc.Gold)

        SectionHeader("Preferences")
        HocCard {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                ToggleRow("Dark mode", s.settings.darkMode) { store.setSetting(darkMode = it) }
                ToggleRow("Audio", s.settings.audioEnabled) { store.setSetting(audio = it) }
                ToggleRow("Vibrations", s.settings.vibrationsEnabled) { store.setSetting(vibration = it) }
                ToggleRow("Chat", s.settings.chatEnabled) { store.setSetting(chat = it) }
            }
        }

        SectionHeader("Identity")
        HocCard {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("Status", Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium, color = Hoc.TextSecondary)
                    Text(s.player.status, style = MaterialTheme.typography.bodyMedium, color = Hoc.Gold)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatusChip("Online") { store.setStatus("Online") }
                    StatusChip("Away") { store.setStatus("Away") }
                }
                Spacer(Modifier.height(4.dp))
                Row(Modifier.fillMaxWidth()) {
                    Text("Region", Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium, color = Hoc.TextSecondary)
                    Text(s.player.region, style = MaterialTheme.typography.bodyMedium, color = Hoc.Gold)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatusChip("Global") { store.setRegion("Global") }
                    StatusChip("Europe") { store.setRegion("Europe") }
                    StatusChip("Asia") { store.setRegion("Asia") }
                    StatusChip("N. America") { store.setRegion("North America") }
                }
            }
        }

        SectionHeader("Account")
        HocCard {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                HocButton("Sign out", secondary = true) { confirmSignOut = true }
                HocButton("Delete account") { confirmDelete = true }
            }
        }

        Spacer(Modifier.height(24.dp))
    }

    if (confirmSignOut) {
        AlertDialog(
            onDismissRequest = { confirmSignOut = false },
            title = { Text("Sign out?") },
            text = { Text("You'll leave this device. Your progress is saved locally.") },
            confirmButton = {
                TextButton(onClick = {
                    store.signOut()
                    AuthService.signOut()
                    confirmSignOut = false
                }) { Text("Sign out", color = Hoc.Gold) }
            },
            dismissButton = {
                TextButton(onClick = { confirmSignOut = false }) { Text("Cancel", color = Hoc.TextMuted) }
            },
        )
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Delete account?") },
            text = { Text("This permanently erases all local data — stats, friends, purchases and history. This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    store.resetAccount()
                    AuthService.signOut()
                    confirmDelete = false
                }) { Text("Delete", color = Hoc.Danger) }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) { Text("Cancel", color = Hoc.TextMuted) }
            },
        )
    }
}

@Composable
private fun ToggleRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium, color = Hoc.TextPrimary)
        Switch(checked = checked, onCheckedChange = onChange)
    }
}

@Composable
private fun StatusChip(label: String, onClick: () -> Unit) {
    TextButton(onClick = onClick) { Text(label, color = Hoc.Gold) }
}