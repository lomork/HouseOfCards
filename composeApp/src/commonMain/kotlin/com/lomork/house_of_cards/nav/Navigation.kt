package com.lomork.house_of_cards.nav

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lomork.house_of_cards.PlatformBackHandler
import com.lomork.house_of_cards.data.AppStore
import com.lomork.house_of_cards.games.GameDef
import com.lomork.house_of_cards.games.GameMode
import com.lomork.house_of_cards.screens.FriendsScreen
import com.lomork.house_of_cards.screens.GameModeSelectScreen
import com.lomork.house_of_cards.screens.HomeScreen
import com.lomork.house_of_cards.screens.PlayScreen
import com.lomork.house_of_cards.screens.ProfileScreen
import com.lomork.house_of_cards.screens.ProfileViewScreen
import com.lomork.house_of_cards.screens.SettingsScreen
import com.lomork.house_of_cards.screens.StoreScreen
import com.lomork.house_of_cards.theme.Hoc

enum class MainTab(val label: String, val glyph: String) {
    PROFILE("Profile", "\uD83D\uDC64"),
    FRIENDS("Friends", "\uD83D\uDC65"),
    HOME("Home", "\uD83C\uDFE0"),
    STORE("Store", "\uD83D\uDECD"),
    SETTINGS("Settings", "\u2699"),
}

/** A match opponent rendered as human-facing data (never the game's internal state). */
data class GameOpponent(
    val name: String,
    val chipId: String? = null,
    val isOnline: Boolean = false,
    val isFriend: Boolean = false,
    val userId: String? = null,
)

sealed interface Route {
    data class Tab(val tab: MainTab) : Route
    data class GameModeSelect(val gameId: String) : Route
    data class Play(val game: GameDef, val mode: GameMode, val opponent: GameOpponent?) : Route
    data class ProfileView(val userId: String) : Route
}

class Navigator {
    private val stack = mutableStateListOf<Route>(Route.Tab(MainTab.HOME))
    val current: Route get() = stack.last()
    val canBack: Boolean get() = stack.size > 1

    fun push(r: Route) { stack.add(r) }
    fun pop() { if (stack.size > 1) stack.removeAt(stack.size - 1) }
    fun resetTo(r: Route) { stack.clear(); stack.add(r) }
}

@Composable
fun AppShell(store: AppStore) {
    val nav = remember { Navigator() }
    PlatformBackHandler(enabled = nav.canBack) { nav.pop() }
    when (val route = nav.current) {
        is Route.Tab -> {
            Scaffold(
                containerColor = Hoc.Background,
                bottomBar = { BottomBar(active = route.tab, onSelect = { nav.resetTo(Route.Tab(it)) }) },
            ) { inner ->
                Box(Modifier.fillMaxSize().padding(inner)) { TabContent(route.tab, store, nav) }
            }
        }
        is Route.GameModeSelect -> GameModeSelectScreen(store, nav, route.gameId)
        is Route.Play -> PlayScreen(store, nav, route.game, route.mode, route.opponent)
        is Route.ProfileView -> ProfileViewScreen(store, nav, route.userId)
    }
}

@Composable
private fun TabContent(tab: MainTab, store: AppStore, nav: Navigator) {
    when (tab) {
        MainTab.PROFILE -> ProfileScreen(store, nav)
        MainTab.FRIENDS -> FriendsScreen(store, nav)
        MainTab.HOME -> HomeScreen(store, nav)
        MainTab.STORE -> StoreScreen(store, nav)
        MainTab.SETTINGS -> SettingsScreen(store, nav)
    }
}

@Composable
private fun BottomBar(active: MainTab, onSelect: (MainTab) -> Unit) {
    val density = LocalDensity.current
    val radiusPx = with(density) { 24.dp.toPx() }
    NavigationBar(containerColor = Hoc.Background) {
        for (tab in MainTab.entries) {
            val isSelected = tab == active
            NavigationBarItem(
                selected = isSelected,
                onClick = { onSelect(tab) },
                icon = {
                    Box(contentAlignment = Alignment.Center) {
                        if (isSelected) {
                            Box(
                                Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.radialGradient(
                                            colors = listOf(Hoc.Gold.copy(alpha = 0.4f), Color.Transparent),
                                            center = Offset.Zero,
                                            radius = radiusPx
                                        )
                                    )
                            )
                        }
                        Text(tab.glyph, fontSize = 22.sp)
                    }
                },
                label = null,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Hoc.Gold,
                    indicatorColor = Color.Transparent,
                    unselectedIconColor = Hoc.TextMuted,
                ),
            )
        }
    }
}
