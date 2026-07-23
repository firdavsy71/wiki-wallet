package com.wiki.wallet.core.designsystem.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.wiki.wallet.core.designsystem.theme.WalletColors
import com.wiki.wallet.core.designsystem.theme.WalletShapes
import com.wiki.wallet.core.designsystem.theme.WalletTypography
import com.wiki.wallet.core.util.CurrencyManager
import kotlinx.coroutines.delay

data class BarChartItem(
    val dayLabel: String,
    val valueRatio: Float,
    val incomeAmount: Double = 0.0,
    val expenseAmount: Double = 0.0,
    val isActive: Boolean = false,
    val isPositive: Boolean = true,
    val deltaChipText: String? = null,
    val isDeltaPositive: Boolean = true
)

@Composable
fun HatchedBar(
    heightRatio: Float,
    isActive: Boolean,
    isPositive: Boolean = true,
    modifier: Modifier = Modifier,
    animationDelayMs: Long = 0
) {
    val animatedRatio = remember { Animatable(0f) }

    LaunchedEffect(heightRatio) {
        delay(animationDelayMs)
        animatedRatio.animateTo(
            targetValue = heightRatio.coerceIn(0.1f, 1f),
            animationSpec = spring(
                dampingRatio = 0.75f,
                stiffness = Spring.StiffnessLow
            )
        )
    }

    val currentRatio = animatedRatio.value
    val barColor = if (isPositive) WalletColors.MintChip else WalletColors.Coral

    Box(
        modifier = modifier
            .fillMaxHeight(currentRatio)
            .clip(WalletShapes.BarRounded)
            .then(
                if (isActive) {
                    Modifier.background(barColor)
                } else {
                    Modifier
                        .background(barColor.copy(alpha = 0.25f))
                        .border(1.dp, barColor.copy(alpha = 0.5f), WalletShapes.BarRounded)
                }
            )
    ) {
        if (!isActive) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height
                val strokeWidth = 1.dp.toPx()
                val step = 8.dp.toPx()
                val strokeColor = barColor.copy(alpha = 0.4f)

                val cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                val path = Path().apply {
                    addRoundRect(
                        RoundRect(
                            rect = Rect(0f, 0f, width, height),
                            topLeft = cornerRadius,
                            topRight = cornerRadius,
                            bottomRight = CornerRadius.Zero,
                            bottomLeft = CornerRadius.Zero
                        )
                    )
                }

                clipPath(path) {
                    var x = -height
                    while (x < width + height) {
                        drawLine(
                            color = strokeColor,
                            start = Offset(x, height),
                            end = Offset(x + height, 0f),
                            strokeWidth = strokeWidth
                        )
                        x += step
                    }
                }
            }
        }
    }
}

@Composable
fun IncomeBarChart(
    items: List<BarChartItem>,
    modifier: Modifier = Modifier,
    cardHeight: Dp = 230.dp
) {
    var selectedIndex by remember { mutableStateOf<Int?>(null) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(cardHeight)
            .clip(WalletShapes.CardLarge)
            .background(WalletColors.Paper)
            .border(1.dp, WalletColors.CardBorder, WalletShapes.CardLarge)
            .padding(14.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header Legend & Selected Day Detail
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val selectedItem = selectedIndex?.let { items.getOrNull(it) }
                if (selectedItem != null) {
                    Text(
                        text = "${selectedItem.dayLabel}: +${CurrencyManager.format(selectedItem.incomeAmount)} / -${CurrencyManager.format(selectedItem.expenseAmount)}",
                        style = WalletTypography.LabelM,
                        color = WalletColors.TextPrimary,
                        maxLines = 1
                    )
                } else {
                    Text(
                        text = "Daily Net Flow",
                        style = WalletTypography.LabelM,
                        color = WalletColors.TextMuted
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LegendItem(label = "In", color = WalletColors.MintChip)
                    LegendItem(label = "Out", color = WalletColors.Coral)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Bars Area
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalAlignment = Alignment.Bottom
            ) {
                items.forEachIndexed { index, item ->
                    val isSelected = selectedIndex == index

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable {
                                selectedIndex = if (selectedIndex == index) null else index
                            },
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            if (item.deltaChipText != null && !isSelected) {
                                DeltaChip(
                                    text = item.deltaChipText,
                                    isPositive = item.isDeltaPositive,
                                    modifier = Modifier.offset(y = (-2).dp)
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .height(115.dp)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            HatchedBar(
                                heightRatio = item.valueRatio,
                                isActive = item.isActive || isSelected,
                                isPositive = item.isPositive,
                                modifier = Modifier
                                    .fillMaxWidth(0.70f)
                                    .fillMaxHeight(),
                                animationDelayMs = (index * 30).toLong()
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = item.dayLabel,
                            style = WalletTypography.LabelS,
                            color = if (isSelected) WalletColors.TextPrimary else WalletColors.TextMuted,
                            maxLines = 1
                        )
                    }

                    if (index < items.size - 1) {
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun LegendItem(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            style = WalletTypography.LabelS,
            color = WalletColors.TextMuted
        )
    }
}
