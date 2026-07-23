package com.wiki.wallet.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
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
fun DeltaChip(
    text: String,
    isPositive: Boolean,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.White,
    textColor: Color = WalletColors.TextPrimary,
    showPercentPrefix: String? = null
) {
    Row(
        modifier = modifier
            .clip(WalletShapes.Pill)
            .background(backgroundColor)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val dotColor = if (isPositive) WalletColors.MintChip else WalletColors.Coral
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(dotColor)
        )
        if (showPercentPrefix != null) {
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = showPercentPrefix,
                style = WalletTypography.LabelS,
                color = textColor
            )
        }
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            style = WalletTypography.LabelS,
            color = textColor
        )
    }
}

@Preview
@Composable
private fun DeltaChipPreview() {
    Row {
        DeltaChip(text = "+27,4USD", isPositive = true)
        Spacer(modifier = Modifier.width(8.dp))
        DeltaChip(text = "−35,4USD", isPositive = false)
        Spacer(modifier = Modifier.width(8.dp))
        DeltaChip(text = "(+$27,4)", isPositive = true, showPercentPrefix = "2,5%", backgroundColor = WalletColors.InkElevated, textColor = WalletColors.TextOnDark)
    }
}
