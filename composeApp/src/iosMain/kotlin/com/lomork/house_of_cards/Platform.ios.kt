package com.lomork.house_of_cards

import androidx.compose.runtime.Composable

@Composable
actual fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit) {
    // iOS has no system back button; navigation uses the in-app back arrow.
}

@Composable
actual fun PlatformSystemBars(dark: Boolean) {
    // Status-bar styling is handled by the iOS host; nothing to do here.
}