package com.wiki.wallet.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.wiki.wallet.core.designsystem.theme.ThemeManager
import com.wiki.wallet.core.designsystem.theme.WalletColors
import com.wiki.wallet.core.designsystem.theme.WalletShapes
import com.wiki.wallet.core.designsystem.theme.WalletTypography
import com.wiki.wallet.core.util.CurrencyManager

data class BarChartItem(
    val dayLabel: String,
    val valueRatio: Float,         // 0f..1f
    val incomeAmount: Double = 0.0,
    val expenseAmount: Double = 0.0,
    val isActive: Boolean = false,
    val isPositive: Boolean = true,
    val deltaChipText: String? = null,
    val isDeltaPositive: Boolean = true
)

@Composable
fun IncomeBarChart(
    items: List<BarChartItem>,
    modifier: Modifier = Modifier,
    cardHeight: Dp = 230.dp
) {
    var selectedIndex by remember { mutableStateOf(items.lastIndex.coerceAtLeast(0)) }
    val activeItem = items.getOrNull(selectedIndex) ?: items.lastOrNull()

    val cardBg = ThemeManager.cardColor
    val textColor = ThemeManager.textColorPrimary
    val borderColor = ThemeManager.cardBorderColor

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(WalletShapes.CardLarge)
            .background(cardBg)
            .border(1.dp, borderColor, WalletShapes.CardLarge)
            .padding(18.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Row: Title & Active Day Cash Flow Breakdown Tooltip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Daily Cash Flow Graph",
                        style = WalletTypography.TitleM,
                        color = textColor
                    )
                    Text(
                        text = activeItem?.dayLabel?.let { "Selected: $it" } ?: "Tap bar to inspect day details",
                        style = WalletTypography.LabelS,
                        color = WalletColors.TextMuted
                    )
                }

                if (activeItem != null) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "+${CurrencyManager.format(activeItem.incomeAmount)}",
                            style = WalletTypography.LabelS,
                            color = WalletColors.MintChip
                        )
                        Text(
                            text = " / ",
                            style = WalletTypography.LabelS,
                            color = WalletColors.TextMuted
                        )
                        Text(
                            text = "-${CurrencyManager.format(activeItem.expenseAmount)}",
                            style = WalletTypography.LabelS,
                            color = WalletColors.Coral
                        )
                    }
                }
            }

            // Dual Bar Chart Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(cardHeight - 80.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    items.forEachIndexed { index, item ->
                        val isSelected = index == selectedIndex
                        val barHeightRatio = item.valueRatio.coerceIn(0.12f, 1f)

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedIndex = index }
                                .padding(horizontal = 2.dp)
                        ) {
                            // Active Tooltip Chip above selected bar
                            if (isSelected && item.deltaChipText != null) {
                                Box(
                                    modifier = Modifier
                                        .clip(WalletShapes.Pill)
                                        .background(if (item.isDeltaPositive) WalletColors.MintChip else WalletColors.Coral)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = item.deltaChipText,
                                        style = WalletTypography.LabelS,
                                        color = Color.White
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                            }

                            // Dual Pillar Bars (Income vs Expense)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth(0.85f)
                                    .fillMaxHeight(barHeightRatio)
                                    .clip(WalletShapes.CardMedium)
                                    .background(if (isSelected) WalletColors.InkElevated else cardBg)
                                    .padding(2.dp),
                                horizontalArrangement = Arrangement.spacedBy(2.dp),
                                verticalAlignment = Alignment.Bottom
                            ) {
                                // Income Bar (MintChip)
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight(if (item.incomeAmount > 0) 1f else 0.1f)
                                        .clip(WalletShapes.Pill)
                                        .background(
                                            if (item.incomeAmount > 0) WalletColors.MintChip
                                            else WalletColors.MintChip.copy(alpha = 0.2f)
                                        )
                                )
                                // Expense Bar (Coral)
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight(if (item.expenseAmount > 0) 1f else 0.1f)
                                        .clip(WalletShapes.Pill)
                                        .background(
                                            if (item.expenseAmount > 0) WalletColors.Coral
                                            else WalletColors.Coral.copy(alpha = 0.2f)
                                        )
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // Day Label
                            Text(
                                text = item.dayLabel,
                                style = WalletTypography.LabelS,
                                color = if (isSelected) textColor else WalletColors.TextMuted
                            )
                        }
                    }
                }
            }

            // Legend Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(WalletColors.MintChip))
                    Text(text = "Income", style = WalletTypography.LabelS, color = WalletColors.TextMuted)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(WalletColors.Coral))
                    Text(text = "Expenses", style = WalletTypography.LabelS, color = WalletColors.TextMuted)
                }
            }
        }
    }
}
