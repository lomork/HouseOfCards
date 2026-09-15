package com.lomork.house_of_cards.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Per-theme neutral palette. Accents (gold, felt, danger, success, chip colors)
 * are brand constants shared by both themes; only the neutrals flip between dark
 * and light so the whole app stays consistent in either mode.
 */
data class HocColors(
    val background: Color,
    val surface: Color,
    val surfaceHigh: Color,
    val surfaceVariant: Color,
    val outline: Color,
    val divider: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
)

private val DarkNeutrals = HocColors(
    background = Color(0xFF0C1016),
    surface = Color(0xFF141A22),
    surfaceHigh = Color(0xFF1C232D),
    surfaceVariant = Color(0xFF232B37),
    outline = Color(0xFF33404F),
    divider = Color(0xFF2A323D),
    textPrimary = Color(0xFFEFE9DC),
    textSecondary = Color(0xFFA9A296),
    textMuted = Color(0xFF6E6A62),
)

private val LightNeutrals = HocColors(
    background = Color(0xFFF2EDE0),
    surface = Color(0xFFFAF6EC),
    surfaceHigh = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFE7DFCD),
    outline = Color(0xFFC9B896),
    divider = Color(0xFFDDD3BE),
    textPrimary = Color(0xFF2B2417),
    textSecondary = Color(0xFF6C624F),
    textMuted = Color(0xFF998C73),
)

private val LocalHocColors = staticCompositionLocalOf { DarkNeutrals }

/** Central palette — modern but classy: deep slate, ivory, antique gold, felt green. */
object Hoc {
    // Brand accents (theme-independent).
    val Gold = Color(0xFFD9B56A)
    val GoldSoft = Color(0xFFF1DEAE)
    val GoldDark = Color(0xFF9C7E41)
    val OnGold = Color(0xFF241A08)

    val Felt = Color(0xFF3E7A66)      // accent / felt green
    val Danger = Color(0xFFC65A52)    // red joker / hearts / error
    val Success = Color(0xFF5BA675)

    val ChipGold = Color(0xFFD9B56A)
    val ChipBurgundy = Color(0xFF93444C)
    val LockedGlow = Color(0xFFFFE3A1)

    // Theme-aware neutrals.
    val Background: Color
        @Composable get() = LocalHocColors.current.background
    val Surface: Color
        @Composable get() = LocalHocColors.current.surface
    val SurfaceHigh: Color
        @Composable get() = LocalHocColors.current.surfaceHigh
    val SurfaceVariant: Color
        @Composable get() = LocalHocColors.current.surfaceVariant
    val Outline: Color
        @Composable get() = LocalHocColors.current.outline
    val Divider: Color
        @Composable get() = LocalHocColors.current.divider
    val TextPrimary: Color
        @Composable get() = LocalHocColors.current.textPrimary
    val TextSecondary: Color
        @Composable get() = LocalHocColors.current.textSecondary
    val TextMuted: Color
        @Composable get() = LocalHocColors.current.textMuted
}

private val DarkScheme = darkColorScheme(
    primary = Hoc.Gold,
    onPrimary = Hoc.OnGold,
    primaryContainer = Hoc.GoldDark,
    onPrimaryContainer = Hoc.GoldSoft,
    secondary = Hoc.Felt,
    onSecondary = Color.White,
    tertiary = Hoc.ChipBurgundy,
    background = DarkNeutrals.background,
    onBackground = DarkNeutrals.textPrimary,
    surface = DarkNeutrals.surface,
    onSurface = DarkNeutrals.textPrimary,
    surfaceVariant = DarkNeutrals.surfaceVariant,
    onSurfaceVariant = DarkNeutrals.textSecondary,
    error = Hoc.Danger,
    onError = Color.White,
    outline = DarkNeutrals.outline,
)

private val LightScheme = lightColorScheme(
    primary = Hoc.GoldDark,
    onPrimary = Color(0xFFF7F0DB),
    primaryContainer = Hoc.GoldSoft,
    onPrimaryContainer = Hoc.OnGold,
    secondary = Hoc.Felt,
    onSecondary = Color.White,
    tertiary = Hoc.ChipBurgundy,
    background = LightNeutrals.background,
    onBackground = LightNeutrals.textPrimary,
    surface = LightNeutrals.surface,
    onSurface = LightNeutrals.textPrimary,
    surfaceVariant = LightNeutrals.surfaceVariant,
    onSurfaceVariant = LightNeutrals.textSecondary,
    error = Hoc.Danger,
    onError = Color.White,
    outline = LightNeutrals.outline,
)

private val HocTypography = Typography(
    headlineLarge = TextStyle(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 30.sp),
    headlineMedium = TextStyle(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 24.sp),
    titleLarge = TextStyle(fontFamily = FontFamily.Serif, fontWeight = FontWeight.SemiBold, fontSize = 20.sp),
    titleMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 16.sp),
    bodyLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Normal, fontSize = 16.sp),
    bodyMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Normal, fontSize = 14.sp),
    bodySmall = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Normal, fontSize = 12.sp),
    labelLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 14.sp),
    labelMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium, fontSize = 12.sp),
    labelSmall = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium, fontSize = 10.sp),
)

@Composable
fun HouseOfCardsTheme(darkTheme: Boolean = true, content: @Composable () -> Unit) {
    val neutrals = if (darkTheme) DarkNeutrals else LightNeutrals
    val scheme = if (darkTheme) DarkScheme else LightScheme
    CompositionLocalProvider(LocalHocColors provides neutrals) {
        MaterialTheme(colorScheme = scheme, typography = HocTypography, content = content)
    }
}