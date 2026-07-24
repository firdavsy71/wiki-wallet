package com.wiki.wallet.feature.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wiki.wallet.core.designsystem.theme.ThemeManager
import com.wiki.wallet.core.designsystem.theme.WalletColors
import com.wiki.wallet.core.designsystem.theme.WalletShapes
import com.wiki.wallet.core.designsystem.theme.WalletTypography
import com.wiki.wallet.core.util.CurrencyManager
import com.wiki.wallet.domain.model.Transaction
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CalendarRoute(
    onNavigateBack: () -> Unit,
    viewModel: CalendarViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    CalendarScreen(
        uiState = uiState,
        onEvent = { event ->
            if (event is CalendarUiEvent.OnBackClicked) {
                onNavigateBack()
            } else {
                viewModel.onEvent(event)
            }
        },
        modifier = modifier
    )
}

@Composable
fun CalendarScreen(
    uiState: CalendarUiState,
    onEvent: (CalendarUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor = ThemeManager.backgroundColor
    val cardBg = ThemeManager.cardColor
    val textColor = ThemeManager.textColorPrimary
    val borderColor = ThemeManager.cardBorderColor

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(WalletShapes.Pill)
                        .background(cardBg)
                        .clickable { onEvent(CalendarUiEvent.OnBackClicked) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = textColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Text(
                    text = "Bill Calendar",
                    style = WalletTypography.TitleM,
                    color = textColor
                )

                Spacer(modifier = Modifier.width(44.dp))
            }

            // Month Header Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(WalletShapes.CardMedium)
                    .background(WalletColors.Ink)
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = uiState.currentMonthLabel, style = WalletTypography.TitleM, color = WalletColors.TextOnDark)
                    Text(text = "${uiState.billTransactions.size} Scheduled Bills", style = WalletTypography.LabelS, color = WalletColors.MintChip)
                }
            }

            // Days of Month Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.height(240.dp)
            ) {
                items(uiState.totalDaysInMonth) { index ->
                    val dayNum = index + 1
                    val isSelected = dayNum == uiState.selectedDayOfMonth
                    val billCount = uiState.dayBillCountMap[dayNum] ?: 0

                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) WalletColors.Coral else if (billCount > 0) cardBg else Color.Transparent)
                            .clickable { onEvent(CalendarUiEvent.OnDaySelected(dayNum)) },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$dayNum",
                                style = WalletTypography.LabelM,
                                color = if (isSelected) Color.White else textColor
                            )
                            if (billCount > 0) {
                                Box(
                                    modifier = Modifier
                                        .size(4.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) Color.White else WalletColors.Coral)
                                )
                            }
                        }
                    }
                }
            }

            // Filter Row (All, Due, Paid)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("All", "Due", "Paid").forEach { filter ->
                    val isSelected = filter == uiState.selectedFilter
                    Box(
                        modifier = Modifier
                            .clip(WalletShapes.Pill)
                            .background(if (isSelected) WalletColors.Coral else cardBg)
                            .clickable { onEvent(CalendarUiEvent.OnFilterSelected(filter)) }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = filter,
                            style = WalletTypography.LabelS,
                            color = if (isSelected) Color.White else textColor
                        )
                    }
                }
            }

            // Bills List
            val filteredBills = uiState.billTransactions.filter { bill ->
                when (uiState.selectedFilter) {
                    "Due" -> !bill.isPaid
                    "Paid" -> bill.isPaid
                    else -> true
                }
            }

            if (filteredBills.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No scheduled bills for this filter",
                        style = WalletTypography.BodyM,
                        color = WalletColors.TextMuted
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredBills) { bill ->
                        BillRowItem(
                            bill = bill,
                            onMarkAsPaid = { onEvent(CalendarUiEvent.OnMarkAsPaidClicked(bill.id)) },
                            cardBg = cardBg,
                            textColor = textColor,
                            borderColor = borderColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BillRowItem(
    bill: Transaction,
    onMarkAsPaid: () -> Unit,
    cardBg: Color,
    textColor: Color,
    borderColor: Color
) {
    val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
    val dueDateText = dateFormat.format(Date(bill.dueDate ?: bill.date))

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(WalletShapes.CardMedium)
            .background(cardBg)
            .border(1.dp, borderColor, WalletShapes.CardMedium)
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(if (bill.isPaid) WalletColors.MintChip.copy(alpha = 0.15f) else WalletColors.Coral.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = bill.categoryIcon, style = WalletTypography.TitleM)
                }

                Column {
                    Text(text = bill.categoryName, style = WalletTypography.TitleM, color = textColor)
                    Text(text = "Due: $dueDateText", style = WalletTypography.LabelS, color = WalletColors.TextMuted)
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = CurrencyManager.format(bill.amount),
                    style = WalletTypography.TitleM,
                    color = if (bill.isPaid) WalletColors.MintChip else WalletColors.Coral
                )

                if (!bill.isPaid) {
                    Box(
                        modifier = Modifier
                            .clip(WalletShapes.Pill)
                            .background(WalletColors.Coral)
                            .clickable { onMarkAsPaid() }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = "Paid", tint = Color.White, modifier = Modifier.size(14.dp))
                            Text(text = "Pay", style = WalletTypography.LabelS, color = Color.White)
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .clip(WalletShapes.Pill)
                            .background(WalletColors.MintChip.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(text = "Paid ✓", style = WalletTypography.LabelS, color = WalletColors.MintChip)
                    }
                }
            }
        }
    }
}
