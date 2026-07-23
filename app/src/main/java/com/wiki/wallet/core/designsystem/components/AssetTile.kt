package com.wiki.wallet.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowOutward
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.wiki.wallet.core.designsystem.theme.WalletColors
import com.wiki.wallet.core.designsystem.theme.WalletShapes
import com.wiki.wallet.core.designsystem.theme.WalletTypography

@Composable
fun AssetTile(
    symbol: String,
    name: String,
    amountFormatted: String,
    iconSymbol: String,
    isDarkBg: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor = if (isDarkBg) WalletColors.Ink else WalletColors.Coral
    val textColor = WalletColors.TextOnDark
    val badgeBg = if (isDarkBg) WalletColors.InkElevated else Color.White.copy(alpha = 0.2f)
    val subtitleColor = if (isDarkBg) WalletColors.TextMuted else Color.White.copy(alpha = 0.7f)

    Box(
        modifier = modifier
            .height(100.dp)
            .clip(WalletShapes.CardLarge)
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Row: Badge left + Arrow right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Token Badge
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(badgeBg),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = iconSymbol,
                        style = WalletTypography.LabelM,
                        color = textColor
                    )
                }

                // Top-right Arrow
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowOutward,
                        contentDescription = "Detail",
                        tint = textColor,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            // Bottom Column: Name(Symbol) + Amount
            Column {
                Text(
                    text = "$name($symbol)",
                    style = WalletTypography.LabelS,
                    color = subtitleColor
                )
                Text(
                    text = amountFormatted,
                    style = WalletTypography.TitleM,
                    color = textColor
                )
            }
        }
    }
}

@Preview
@Composable
private fun AssetTilePreview() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AssetTile(
            symbol = "BTC",
            name = "Bitcoin",
            amountFormatted = "+$541,2",
            iconSymbol = "₿",
            isDarkBg = true,
            onClick = {},
            modifier = Modifier.weight(1f)
        )
        AssetTile(
            symbol = "ETH",
            name = "Ethereum",
            amountFormatted = "+$357,8",
            iconSymbol = "Ξ",
            isDarkBg = false,
            onClick = {},
            modifier = Modifier.weight(1f)
        )
    }
}
