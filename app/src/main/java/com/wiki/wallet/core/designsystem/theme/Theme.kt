package com.wiki.wallet.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = WalletColors.Coral,
    onPrimary = WalletColors.TextOnDark,
    background = WalletColors.PaperPure,
    onBackground = WalletColors.TextPrimary,
    surface = WalletColors.Paper,
    onSurface = WalletColors.TextPrimary,
    surfaceVariant = WalletColors.Canvas,
    onSurfaceVariant = WalletColors.TextMuted
)

private val DarkColorScheme = darkColorScheme(
    primary = WalletColors.Coral,
    onPrimary = WalletColors.TextOnDark,
    background = WalletColors.Ink,
    onBackground = WalletColors.TextOnDark,
    surface = WalletColors.Ink,
    onSurface = WalletColors.TextOnDark,
    surfaceVariant = WalletColors.InkElevated,
    onSurfaceVariant = WalletColors.TextMutedDark
)

@Composable
fun WikiWalletTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
