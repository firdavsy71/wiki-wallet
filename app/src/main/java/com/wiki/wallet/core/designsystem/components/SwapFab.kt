package com.wiki.wallet.core.designsystem.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.wiki.wallet.core.designsystem.theme.WalletColors

@Composable
fun SwapFab(
    isFlipped: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val rotationAngle by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 350),
        label = "fabRotation"
    )

    Box(
        modifier = modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(Color.White)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.SwapVert,
            contentDescription = "Swap Tokens",
            tint = WalletColors.Ink,
            modifier = Modifier
                .size(24.dp)
                .rotate(rotationAngle)
        )
    }
}

@Preview
@Composable
private fun SwapFabPreview() {
    Box(
        modifier = Modifier
            .size(100.dp)
            .background(WalletColors.Ink),
        contentAlignment = Alignment.Center
    ) {
        SwapFab(isFlipped = false, onClick = {})
    }
}
