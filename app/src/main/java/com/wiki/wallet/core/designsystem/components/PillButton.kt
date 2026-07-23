package com.wiki.wallet.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.wiki.wallet.core.designsystem.theme.WalletColors
import com.wiki.wallet.core.designsystem.theme.WalletShapes
import com.wiki.wallet.core.designsystem.theme.WalletTypography

enum class PillButtonVariant {
    Outlined,
    Soft,
    Primary
}

@Composable
fun PillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: PillButtonVariant = PillButtonVariant.Outlined,
    enabled: Boolean = true,
    height: Dp = 32.dp
) {
    val alpha = if (enabled) 1f else 0.4f

    val (bgColor, textColor, borderModifier) = when (variant) {
        PillButtonVariant.Outlined -> Triple(
            Color.Transparent,
            WalletColors.TextMuted,
            Modifier.border(1.dp, WalletColors.TextMuted, WalletShapes.Pill)
        )
        PillButtonVariant.Soft -> Triple(
            Color.White.copy(alpha = 0.25f),
            Color.White,
            Modifier
        )
        PillButtonVariant.Primary -> Triple(
            WalletColors.Paper,
            WalletColors.Ink,
            Modifier
        )
    }

    val textStyle: TextStyle = if (variant == PillButtonVariant.Primary) {
        WalletTypography.TitleM
    } else {
        WalletTypography.LabelS
    }

    Box(
        modifier = modifier
            .alpha(alpha)
            .height(height)
            .clip(WalletShapes.Pill)
            .background(bgColor)
            .then(borderModifier)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = textStyle,
            color = textColor
        )
    }
}

@Preview
@Composable
private fun PillButtonPreview() {
    Box(
        modifier = Modifier
            .background(WalletColors.Ink)
            .padding(16.dp)
    ) {
        PillButton(text = "MAX", onClick = {}, variant = PillButtonVariant.Outlined)
        PillButton(text = "FAST", onClick = {}, variant = PillButtonVariant.Soft, modifier = Modifier.padding(top = 40.dp))
        PillButton(
            text = "Swap",
            onClick = {},
            variant = PillButtonVariant.Primary,
            height = 56.dp,
            modifier = Modifier
                .padding(top = 80.dp)
                .fillMaxWidth()
        )
    }
}
