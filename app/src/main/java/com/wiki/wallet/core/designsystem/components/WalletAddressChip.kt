package com.wiki.wallet.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.North
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
fun WalletAddressChip(
    address: String, // "0x467.f8h4"
    onCopy: () -> Unit,
    onSend: () -> Unit,
    onAdd: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(96.dp)
            .clip(WalletShapes.CardMedium)
            .background(WalletColors.InkElevated)
            .padding(14.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Address + Copy Icon Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable(onClick = onCopy)
            ) {
                Text(
                    text = address,
                    style = WalletTypography.Mono,
                    color = WalletColors.TextOnDark
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy address",
                    tint = WalletColors.TextMuted,
                    modifier = Modifier.size(14.dp)
                )
            }

            // Circular Action Buttons Row (Send ↑ and Add +)
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Send button
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(WalletColors.Ink)
                        .clickable(onClick = onSend),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.North,
                        contentDescription = "Send",
                        tint = WalletColors.TextOnDark,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Add button
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(WalletColors.Ink)
                        .clickable(onClick = onAdd),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add",
                        tint = WalletColors.TextOnDark,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun WalletAddressChipPreview() {
    WalletAddressChip(
        address = "0x467.f8h4",
        onCopy = {},
        onSend = {},
        onAdd = {}
    )
}
