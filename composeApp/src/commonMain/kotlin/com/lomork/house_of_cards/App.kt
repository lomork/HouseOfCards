package com.lomork.house_of_cards

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import com.lomork.house_of_cards.data.AppStore
import com.lomork.house_of_cards.nav.AppShell
import com.lomork.house_of_cards.screens.LoginScreen
import com.lomork.house_of_cards.theme.HouseOfCardsTheme

@Composable
fun App() {
    val store = remember { AppStore() }
    val darkTheme = store.saved.settings.darkMode
    LaunchedEffect(Unit) { store.refreshFromCloud() }
    PlatformSystemBars(dark = darkTheme)
    HouseOfCardsTheme(darkTheme = darkTheme) {
        if (store.saved.isLoggedIn) {
            AppShell(store)
        } else {
            LoginScreen(store)
        }
    }
}