package com.wiki.wallet.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle

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
    content: @Composable () -> Unit
) {
    val isDark by ThemeManager.isDarkTheme.collectAsStateWithLifecycle()
    val colorScheme = if (isDark) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
