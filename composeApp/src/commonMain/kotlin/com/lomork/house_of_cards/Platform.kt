package com.lomork.house_of_cards

import androidx.compose.runtime.Composable

/**
 * System back gesture handler. On Android this hooks the hardware/gesture back
 * so it navigates within the app instead of closing the activity; on iOS it is
 * a no-op (there is no system back button).
 */
@Composable
expect fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit)

/**
 * Keeps the status/navigation bar icons readable against the active app theme:
 * light icons on a dark background, dark icons on a light background.
 */
@Composable
expect fun PlatformSystemBars(dark: Boolean)