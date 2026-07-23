package com.wiki.wallet.core.designsystem.components

import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.wiki.wallet.core.designsystem.theme.WalletColors
import com.wiki.wallet.core.designsystem.theme.WalletTypography

@Composable
fun SuperscriptAmount(
    amountText: String, // e.g. "$64,891.04"
    style: TextStyle = WalletTypography.DisplayXL,
    color: Color = WalletColors.TextOnDark,
    modifier: Modifier = Modifier
) {
    val parts = amountText.split(".")
    val mainPart = parts[0]
    val centsPart = if (parts.size > 1) ".${parts[1]}" else ""

    val annotatedString = buildAnnotatedString {
        withStyle(style.toSpanStyle().copy(color = color)) {
            append(mainPart)
        }
        if (centsPart.isNotEmpty()) {
            withStyle(
                style.toSpanStyle().copy(
                    fontSize = (style.fontSize.value * 0.55f).sp,
                    baselineShift = BaselineShift.Superscript,
                    color = color
                )
            ) {
                append(centsPart)
            }
        }
    }

    Text(
        text = annotatedString,
        modifier = modifier
    )
}

@Preview
@Composable
private fun SuperscriptAmountPreview() {
    SuperscriptAmount(
        amountText = "$64,891.04",
        color = WalletColors.TextOnDark
    )
}
