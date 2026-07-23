package com.wiki.wallet.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wiki.wallet.core.designsystem.theme.WalletColors
import com.wiki.wallet.core.designsystem.theme.WalletShapes
import com.wiki.wallet.core.designsystem.theme.WalletTypography

@Composable
fun TokenSelector(
    symbol: String,
    iconSymbol: String,
    onExpand: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = WalletColors.PaperPure,
    textColor: Color = WalletColors.TextPrimary
) {
    Row(
        modifier = modifier
            .clip(WalletShapes.Pill)
            .background(backgroundColor)
            .clickable(onClick = onExpand)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = iconSymbol,
                style = WalletTypography.LabelM,
                color = textColor
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = symbol,
                style = WalletTypography.TitleM.copy(fontSize = 16.sp),
                color = textColor
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Select Token",
                tint = textColor,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Preview
@Composable
private fun TokenSelectorPreview() {
    Row {
        TokenSelector(symbol = "ETH", iconSymbol = "⟠", onExpand = {})
        Spacer(modifier = Modifier.width(12.dp))
        TokenSelector(
            symbol = "USDT",
            iconSymbol = "₮",
            onExpand = {},
            backgroundColor = Color.White.copy(alpha = 0.2f),
            textColor = Color.White
        )
    }
}
