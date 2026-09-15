package com.lomork.house_of_cards.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lomork.house_of_cards.data.AppStore
import com.lomork.house_of_cards.games.GameRegistry
import com.lomork.house_of_cards.nav.Navigator
import com.lomork.house_of_cards.nav.Route
import com.lomork.house_of_cards.theme.Hoc
import com.lomork.house_of_cards.ui.HocButton
import com.lomork.house_of_cards.ui.HocCard
import com.lomork.house_of_cards.ui.SectionHeader

@Composable
fun HomeScreen(store: AppStore, nav: Navigator) {
    val p = store.profile
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Column(
            Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("House of Cards", style = MaterialTheme.typography.headlineLarge, color = Hoc.Gold)
            Text("Pick your table.", style = MaterialTheme.typography.bodyMedium, color = Hoc.TextSecondary)
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            StatPill("\uD83E\uDE99", "Coins", p.coins.toString())
            StatPill("\uD83D\uDD25", "Streak", p.streak.toString())
        }

        SectionHeader("Games")
        GameRegistry.games.forEach { game ->
            HocCard {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(game.icon, fontSize = 26.sp, color = Hoc.Gold)
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text(game.title, style = MaterialTheme.typography.titleLarge, color = Hoc.TextPrimary)
                            Text(game.subtitle, style = MaterialTheme.typography.labelMedium, color = Hoc.TextSecondary)
                        }
                    }
                    Text(game.description, style = MaterialTheme.typography.bodySmall, color = Hoc.TextMuted)
                    HocButton("Play") { nav.push(Route.GameModeSelect(game.id)) }
                }
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun StatPill(icon: String, label: String, value: String) {
    Row(
        Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(Hoc.SurfaceHigh)
            .border(1.dp, Hoc.Divider, RoundedCornerShape(999.dp))
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(icon, fontSize = 16.sp)
        Spacer(Modifier.width(8.dp))
        Column {
            Text(value, color = Hoc.Gold, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text(label.uppercase(), color = Hoc.TextSecondary, fontSize = 9.sp)
        }
    }
}