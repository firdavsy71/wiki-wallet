package com.wiki.wallet.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.wiki.wallet.core.designsystem.theme.WalletColors
import com.wiki.wallet.core.designsystem.theme.WalletShapes
import com.wiki.wallet.core.designsystem.theme.WalletTypography

enum class SwapCardType {
    PayWith,
    Receive
}

@Composable
fun SwapCard(
    type: SwapCardType,
    tokenSymbol: String,       // "ETH", "USDT"
    tokenIconSymbol: String,   // "⟠", "₮"
    amountText: String,        // "2.04", "8.134"
    usdEquivalentText: String, // "~$8.127", "~$8.125"
    footerLeftText: String,    // "Balance 5.07ETH", "Fee $3.45"
    actionButtonText: String,  // "MAX", "FAST"
    onAmountChange: (String) -> Unit,
    onTokenSelectClick: () -> Unit,
    onActionButtonClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isPayWith = type == SwapCardType.PayWith
    val cardBg = if (isPayWith) WalletColors.Paper else WalletColors.Coral
    val headerTextColor = if (isPayWith) WalletColors.TextMutedDark else Color.White.copy(alpha = 0.7f)
    val amountColor = if (isPayWith) WalletColors.TextPrimary else Color.White
    val tokenBg = if (isPayWith) WalletColors.PaperPure else Color.White.copy(alpha = 0.2f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(WalletShapes.CardLarge)
            .background(cardBg)
            .then(
                if (isPayWith) Modifier.border(1.dp, WalletColors.CardBorder, WalletShapes.CardLarge)
                else Modifier
            )
            .padding(18.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isPayWith) "Pay with" else "Receive",
                    style = WalletTypography.LabelM,
                    color = headerTextColor
                )

                if (isPayWith) {
                    Icon(
                        imageVector = Icons.Default.OpenInFull,
                        contentDescription = "Expand",
                        tint = WalletColors.TextMutedDark,
                        modifier = Modifier.size(16.dp)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(WalletColors.Ink),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.BarChart,
                            contentDescription = "Chart",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            // Main Input / Amount Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TokenSelector(
                    symbol = tokenSymbol,
                    iconSymbol = tokenIconSymbol,
                    onExpand = onTokenSelectClick,
                    backgroundColor = tokenBg,
                    textColor = amountColor
                )

                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    if (isPayWith) {
                        BasicTextField(
                            value = amountText,
                            onValueChange = onAmountChange,
                            textStyle = WalletTypography.DisplayL.copy(
                                color = amountColor,
                                textAlign = TextAlign.End
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                    } else {
                        Text(
                            text = amountText,
                            style = WalletTypography.DisplayL,
                            color = amountColor
                        )
                    }

                    Text(
                        text = usdEquivalentText,
                        style = WalletTypography.LabelS,
                        color = if (isPayWith) WalletColors.TextMuted else Color.White.copy(alpha = 0.8f)
                    )
                }
            }

            // Footer Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = footerLeftText,
                    style = WalletTypography.LabelS,
                    color = headerTextColor
                )

                PillButton(
                    text = actionButtonText,
                    onClick = onActionButtonClick,
                    variant = if (isPayWith) PillButtonVariant.Outlined else PillButtonVariant.Soft
                )
            }
        }
    }
}

@Preview
@Composable
private fun SwapCardPreview() {
    Column(
        modifier = Modifier
            .background(WalletColors.Ink)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SwapCard(
            type = SwapCardType.PayWith,
            tokenSymbol = "ETH",
            tokenIconSymbol = "⟠",
            amountText = "2.04",
            usdEquivalentText = "~$8.127",
            footerLeftText = "Balance 5.07ETH",
            actionButtonText = "MAX",
            onAmountChange = {},
            onTokenSelectClick = {},
            onActionButtonClick = {}
        )

        SwapCard(
            type = SwapCardType.Receive,
            tokenSymbol = "USDT",
            tokenIconSymbol = "₮",
            amountText = "8.134",
            usdEquivalentText = "~$8.125",
            footerLeftText = "Fee $3.45",
            actionButtonText = "FAST",
            onAmountChange = {},
            onTokenSelectClick = {},
            onActionButtonClick = {}
        )
    }
}
