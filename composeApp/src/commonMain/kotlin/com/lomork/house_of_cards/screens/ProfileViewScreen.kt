package com.lomork.house_of_cards.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lomork.house_of_cards.data.AppStore
import com.lomork.house_of_cards.data.SeedData
import com.lomork.house_of_cards.games.GameMode
import com.lomork.house_of_cards.games.GameRegistry
import com.lomork.house_of_cards.nav.GameOpponent
import com.lomork.house_of_cards.nav.Navigator
import com.lomork.house_of_cards.nav.Route
import com.lomork.house_of_cards.theme.Hoc
import com.lomork.house_of_cards.ui.BackHeader
import com.lomork.house_of_cards.ui.HocButton
import com.lomork.house_of_cards.ui.HocCard
import com.lomork.house_of_cards.ui.PokerChip
import com.lomork.house_of_cards.ui.SectionHeader

@Composable
fun ProfileViewScreen(store: AppStore, nav: Navigator, userId: String) {
    val friend = store.friendById(userId)
    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        BackHeader(friend?.username ?: "Profile", onBack = { nav.pop() })

        if (friend == null) {
            Text("This user is no longer available.", color = Hoc.TextMuted)
            return@Column
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            PokerChip(SeedData.chipById(friend.chipId), size = 64.dp)
            Spacer(Modifier.width(14.dp))
            Column {
                Text(friend.username, style = MaterialTheme.typography.headlineMedium, color = Hoc.TextPrimary)
                Text("Level ${friend.level}", style = MaterialTheme.typography.labelLarge, color = Hoc.Gold)
            }
        }

        SectionHeader("Identity")
        HocCard {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(Modifier.fillMaxWidth()) {
                    Text("Username", Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium, color = Hoc.TextSecondary)
                    Text(friend.username, style = MaterialTheme.typography.bodyMedium, color = Hoc.TextPrimary)
                }
                Row(Modifier.fillMaxWidth()) {
                    Text("Status", Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium, color = Hoc.TextSecondary)
                    Text(if (friend.isOnline) "Online" else "Offline", style = MaterialTheme.typography.bodyMedium, color = if (friend.isOnline) Hoc.Success else Hoc.TextMuted)
                }
                Row(Modifier.fillMaxWidth()) {
                    Text("Region", Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium, color = Hoc.TextSecondary)
                    Text(friend.region, style = MaterialTheme.typography.bodyMedium, color = Hoc.TextPrimary)
                }
                Row(Modifier.fillMaxWidth()) {
                    Text("Streak", Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium, color = Hoc.TextSecondary)
                    Text("${friend.streak}", style = MaterialTheme.typography.bodyMedium, color = Hoc.TextPrimary)
                }
            }
        }

        HocButton("Play ${friend.username}") {
            val game = GameRegistry.games.first()
            nav.push(Route.Play(game, GameMode.FRIENDS, GameOpponent(friend.username, friend.chipId, isFriend = true, userId = friend.userId)))
        }
        HocButton("Remove friend", secondary = true) {
            store.removeFriend(friend.userId)
            nav.pop()
        }
        Spacer(Modifier.height(16.dp))
    }
}