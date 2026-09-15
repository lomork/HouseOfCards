package com.lomork.house_of_cards.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.unit.sp
import com.lomork.house_of_cards.data.AppStore
import com.lomork.house_of_cards.data.SeedData
import com.lomork.house_of_cards.nav.Navigator
import com.lomork.house_of_cards.theme.Hoc
import com.lomork.house_of_cards.ui.HocCard
import com.lomork.house_of_cards.ui.PokerChip
import com.lomork.house_of_cards.ui.SectionHeader
import com.lomork.house_of_cards.ui.StatTile

@Composable
fun ProfileScreen(store: AppStore, nav: Navigator) {
    val p = store.profile
    var editUsername by remember { mutableStateOf(false) }
    var tempName by remember { mutableStateOf(p.username) }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            PokerChip(SeedData.chipById(p.equippedChipId), size = 64.dp)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(p.username, style = MaterialTheme.typography.headlineMedium, color = Hoc.TextPrimary)
                Text("Level ${p.level}", style = MaterialTheme.typography.labelLarge, color = Hoc.Gold)
            }
            TextButton(onClick = {
                tempName = p.username
                editUsername = true
            }) { Text("Edit", color = Hoc.Gold) }
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatTile("Wins", p.totalWins.toString(), Modifier.weight(1f))
            StatTile("Losses", p.totalLosses.toString(), Modifier.weight(1f))
            StatTile("Streak", p.streak.toString(), Modifier.weight(1f))
            StatTile("Coins", p.coins.toString(), Modifier.weight(1f))
        }

        SectionHeader("Identity")
        HocCard {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                IdentityRow("Username", p.username)
                IdentityRow("Status", p.status)
                IdentityRow("Region", p.region)
            }
        }

        SectionHeader("Achievements")
        HocCard {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SeedData.achievements.forEach { a ->
                    val unlocked = a.id in p.unlockedAchievementIds
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(a.icon, fontSize = 22.sp, modifier = Modifier.width(34.dp))
                        Column(Modifier.weight(1f)) {
                            Text(a.title, style = MaterialTheme.typography.titleMedium, color = if (unlocked) Hoc.TextPrimary else Hoc.TextMuted)
                            Text(a.description, style = MaterialTheme.typography.bodySmall, color = Hoc.TextSecondary)
                        }
                        if (!unlocked) Text("Locked", style = MaterialTheme.typography.labelSmall, color = Hoc.TextMuted)
                    }
                }
            }
        }

        SectionHeader("Your Purchases")
        HocCard {
            val ownedChips = SeedData.storeItems.filter { it.type == "chip" && it.id in p.ownedItemIds }
            if (ownedChips.isEmpty()) {
                Text("No purchases yet. Visit the Store!", color = Hoc.TextMuted, style = MaterialTheme.typography.bodyMedium)
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ownedChips.forEach { c -> PokerChip(c, size = 44.dp) }
                }
            }
        }

        Spacer(Modifier.height(24.dp))
    }

    if (editUsername) {
        AlertDialog(
            onDismissRequest = { editUsername = false },
            title = { Text("Edit username") },
            text = {
                OutlinedTextField(
                    value = tempName,
                    onValueChange = { tempName = it },
                    label = { Text("Username") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    store.setUsername(tempName)
                    editUsername = false
                }) { Text("Save", color = Hoc.Gold) }
            },
            dismissButton = {
                TextButton(onClick = { editUsername = false }) { Text("Cancel", color = Hoc.TextMuted) }
            },
        )
    }
}

@Composable
private fun IdentityRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth()) {
        Text(label, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium, color = Hoc.TextSecondary)
        Text(value, style = MaterialTheme.typography.bodyMedium, color = Hoc.TextPrimary)
    }
}