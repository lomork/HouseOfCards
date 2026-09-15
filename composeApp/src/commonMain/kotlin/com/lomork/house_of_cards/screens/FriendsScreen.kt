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
import com.lomork.house_of_cards.data.AppStore
import com.lomork.house_of_cards.data.FriendRequest
import com.lomork.house_of_cards.data.nowMillis
import com.lomork.house_of_cards.games.GameMode
import com.lomork.house_of_cards.games.GameRegistry
import com.lomork.house_of_cards.nav.GameOpponent
import com.lomork.house_of_cards.nav.Navigator
import com.lomork.house_of_cards.nav.Route
import com.lomork.house_of_cards.theme.Hoc
import com.lomork.house_of_cards.ui.HocButton
import com.lomork.house_of_cards.ui.HocCard
import com.lomork.house_of_cards.ui.SectionHeader

@Composable
fun FriendsScreen(store: AppStore, nav: Navigator) {
    val s = store.saved
    var addName by remember { mutableStateOf("") }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Friends", style = MaterialTheme.typography.headlineLarge, color = Hoc.Gold)

        SectionHeader("Add friends")
        HocCard {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = addName,
                    onValueChange = { addName = it },
                    label = { Text("Username") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                HocButton("Send request") {
                    val name = addName.trim()
                    if (name.isNotEmpty()) {
                        store.update { st ->
                            st.copy(requests = st.requests + FriendRequest(
                                userId = "u_${nowMillis()}",
                                username = name,
                                level = 1,
                                incoming = false,
                                timestamp = nowMillis(),
                            ))
                        }
                        addName = ""
                    }
                }
            }
        }

        SectionHeader("Game invites")
        if (s.invites.isEmpty()) {
            Text("No invites right now.", style = MaterialTheme.typography.bodySmall, color = Hoc.TextMuted)
        } else {
            s.invites.forEach { inv ->
                HocCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("${inv.fromUsername} invited you", style = MaterialTheme.typography.titleMedium, color = Hoc.TextPrimary)
                            Text("Jack's Lines", style = MaterialTheme.typography.bodySmall, color = Hoc.TextSecondary)
                        }
                        TextButton(onClick = {
                            store.update { st -> st.copy(invites = st.invites.filterNot { it.id == inv.id }) }
                        }) { Text("Decline", color = Hoc.TextMuted) }
                        TextButton(onClick = {
                            val game = GameRegistry.games.first()
                            store.update { st -> st.copy(invites = st.invites.filterNot { it.id == inv.id }) }
                            nav.push(Route.Play(game, GameMode.FRIENDS, GameOpponent(inv.fromUsername, isFriend = true, userId = inv.fromUserId)))
                        }) { Text("Accept", color = Hoc.Gold) }
                    }
                }
            }
        }

        val incoming = s.requests.filter { it.incoming }
        val outgoing = s.requests.filter { !it.incoming }
        SectionHeader("Incoming requests")
        if (incoming.isEmpty()) {
            Text("No incoming requests.", style = MaterialTheme.typography.bodySmall, color = Hoc.TextMuted)
        } else {
            incoming.forEach { req ->
                HocCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("${req.username} wants to add you", style = MaterialTheme.typography.bodyMedium, color = Hoc.TextPrimary, modifier = Modifier.weight(1f))
                        TextButton(onClick = { store.declineRequest(req.userId) }) { Text("Decline", color = Hoc.TextMuted) }
                        TextButton(onClick = { store.acceptRequest(req.userId) }) { Text("Accept", color = Hoc.Gold) }
                    }
                }
            }
        }

        SectionHeader("Outgoing requests")
        if (outgoing.isEmpty()) {
            Text("No outgoing requests.", style = MaterialTheme.typography.bodySmall, color = Hoc.TextMuted)
        } else {
            outgoing.forEach { req ->
                HocCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Sent to ${req.username}", style = MaterialTheme.typography.bodyMedium, color = Hoc.TextPrimary, modifier = Modifier.weight(1f))
                        TextButton(onClick = { store.declineRequest(req.userId) }) { Text("Cancel", color = Hoc.Danger) }
                    }
                }
            }
        }

        SectionHeader("Friends (${s.friends.size})")
        s.friends.forEach { f ->
            HocCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("\uD83D\uDC64", fontSize = androidx.compose.ui.unit.TextUnit(20f, androidx.compose.ui.unit.TextUnitType.Sp))
                    Column(Modifier.weight(1f).padding(start = 10.dp)) {
                        Text(f.username, style = MaterialTheme.typography.titleMedium, color = Hoc.TextPrimary)
                        Text("Level ${f.level} · ${f.region}", style = MaterialTheme.typography.bodySmall, color = Hoc.TextSecondary)
                    }
                    Text(if (f.isOnline) "Online" else "Offline", style = MaterialTheme.typography.labelSmall, color = if (f.isOnline) Hoc.Success else Hoc.TextMuted)
                    TextButton(onClick = { nav.push(Route.ProfileView(f.userId)) }) { Text("View", color = Hoc.Gold) }
                }
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}