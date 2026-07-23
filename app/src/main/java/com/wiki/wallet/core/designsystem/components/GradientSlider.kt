package com.wiki.wallet.core.designsystem.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.wiki.wallet.core.designsystem.theme.WalletColors
import com.wiki.wallet.core.designsystem.theme.WalletShapes
import kotlin.math.roundToInt

@Composable
fun GradientSlider(
    value: Float, // 0f to 1f
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    var isPressed by remember { mutableStateOf(false) }
    val thumbScale by animateFloatAsState(targetValue = if (isPressed) 1.15f else 1.0f, label = "thumbScale")

    val trackGradient = Brush.horizontalGradient(
        colors = listOf(WalletColors.Coral, WalletColors.CoralSoft)
    )

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(28.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        val density = LocalDensity.current
        val widthPx = with(density) { constraints.maxWidth.toFloat() }
        val thumbRadiusPx = with(density) { 14.dp.toPx() }
        val availableWidthPx = (widthPx - thumbRadiusPx * 2).coerceAtLeast(1f)

        // Background Track (14dp height)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(14.dp)
                .clip(WalletShapes.Pill)
                .background(WalletColors.InkElevated)
                .pointerInput(enabled) {
                    if (!enabled) return@pointerInput
                    detectTapGestures(
                        onPress = { offset ->
                            isPressed = true
                            val newValue = ((offset.x - thumbRadiusPx) / availableWidthPx).coerceIn(0f, 1f)
                            onValueChange(newValue)
                            tryAwaitRelease()
                            isPressed = false
                        }
                    )
                }
                .pointerInput(enabled) {
                    if (!enabled) return@pointerInput
                    detectHorizontalDragGestures(
                        onDragStart = { isPressed = true },
                        onDragEnd = { isPressed = false },
                        onDragCancel = { isPressed = false },
                        onHorizontalDrag = { change, _ ->
                            change.consume()
                            val newValue = ((change.position.x - thumbRadiusPx) / availableWidthPx).coerceIn(0f, 1f)
                            onValueChange(newValue)
                        }
                    )
                }
        ) {
            // Filled portion
            val filledFraction = value.coerceIn(0f, 1f)
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(filledFraction)
                    .clip(WalletShapes.Pill)
                    .background(trackGradient)
            )
        }

        // Thumb (28dp white circle)
        val thumbOffsetPx = (value.coerceIn(0f, 1f) * availableWidthPx).roundToInt()

        Box(
            modifier = Modifier
                .offset { IntOffset(x = thumbOffsetPx, y = 0) }
                .size(28.dp)
                .scale(thumbScale)
                .clip(CircleShape)
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            // Inner grip detail
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(WalletColors.Ink.copy(alpha = 0.15f))
            )
        }
    }
}

@Preview
@Composable
private fun GradientSliderPreview() {
    var sliderVal by remember { mutableStateOf(0.6f) }
    Box(
        modifier = Modifier
            .background(WalletColors.Ink)
            .height(60.dp)
            .fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        GradientSlider(
            value = sliderVal,
            onValueChange = { sliderVal = it }
        )
    }
}
