package com.lomork.house_of_cards.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lomork.house_of_cards.data.AppStore
import com.lomork.house_of_cards.games.GameMode
import com.lomork.house_of_cards.games.GameRegistry
import com.lomork.house_of_cards.nav.GameOpponent
import com.lomork.house_of_cards.nav.Navigator
import com.lomork.house_of_cards.nav.Route
import com.lomork.house_of_cards.theme.Hoc
import com.lomork.house_of_cards.ui.BackHeader
import com.lomork.house_of_cards.ui.HocButton
import com.lomork.house_of_cards.ui.HocCard
import com.lomork.house_of_cards.ui.SectionHeader
import kotlinx.coroutines.delay

@Composable
fun GameModeSelectScreen(store: AppStore, nav: Navigator, gameId: String) {
    val game = GameRegistry.byId(gameId)
    var friendPick by remember { mutableStateOf(false) }

    Column(
        Modifier
            .fillMaxSize()
            .background(Hoc.Background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        BackHeader(game?.title ?: "Select Mode", onBack = { nav.pop() })

        JackLinesBanner(game?.title ?: "Jack's Lines")

        SectionHeader("Choose a mode")

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ModeBox("Online", "Play a stranger", "\uD83C\uDF10", index = 0, primary = true, modifier = Modifier.weight(1f)) {
                nav.push(Route.Play(game!!, GameMode.ONLINE, null))
            }
            ModeBox("Friends", "Challenge a friend", "\uD83D\uDC65", index = 1, primary = false, modifier = Modifier.weight(1f)) {
                friendPick = !friendPick
            }
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            ModeBox("Offline", "Play the local AI", "\uD83E\uDD16", index = 2, primary = false, modifier = Modifier.fillMaxWidth(0.47f)) {
                nav.push(Route.Play(game!!, GameMode.OFFLINE, GameOpponent(name = "Local Bot")))
            }
        }

        if (friendPick) {
            SectionHeader("Choose a friend")
            val friends = store.saved.friends
            if (friends.isEmpty()) {
                Text(
                    "No friends yet — add some in the Friends tab.",
                    color = Hoc.TextMuted,
                    style = MaterialTheme.typography.bodySmall,
                )
            } else {
                friends.forEach { f ->
                    HocCard {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(f.username, style = MaterialTheme.typography.titleMedium, color = Hoc.TextPrimary)
                                Text("Level ${f.level} · ${f.region}", style = MaterialTheme.typography.bodySmall, color = Hoc.TextSecondary)
                            }
                            HocButton("Play", Modifier.fillMaxWidth(0.4f)) {
                                nav.push(Route.Play(game!!, GameMode.FRIENDS, GameOpponent(f.username, f.chipId, isFriend = true, userId = f.userId)))
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun ModeBox(
    title: String,
    subtitle: String,
    glyph: String,
    index: Int,
    primary: Boolean,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(90L * index)
        visible = true
    }

    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.93f else 1f, tween(120))

    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = fadeIn(tween(450)) +
            slideInVertically(tween(450)) { it / 3 } +
            scaleIn(tween(450), initialScale = 0.82f),
    ) {
        Column(
            Modifier
                .aspectRatio(1f)
                .graphicsLayer { scaleX = scale; scaleY = scale }
                .clip(RoundedCornerShape(22.dp))
                .background(if (primary) Hoc.SurfaceHigh else Hoc.Surface)
                .border(1.5.dp, if (primary) Hoc.Gold else Hoc.Outline, RoundedCornerShape(22.dp))
                .clickable(interactionSource = interaction, indication = null, onClick = onClick)
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(glyph, fontSize = 42.sp)
            Spacer(Modifier.height(6.dp))
            Text(title, style = MaterialTheme.typography.titleLarge, color = Hoc.TextPrimary)
            Spacer(Modifier.height(2.dp))
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = Hoc.TextMuted,
                textAlign = TextAlign.Center,
            )
        }
    }
}