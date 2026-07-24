package com.wiki.wallet.feature.history

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.wiki.wallet.feature.dashboard.TransactionRowItem

@Composable
fun HistoryRoute(
    onNavigateBack: () -> Unit,
    onNavigateToEditTransaction: (String) -> Unit,
    viewModel: HistoryViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    HistoryScreen(
        uiState = uiState,
        onEvent = { event ->
            if (event is HistoryUiEvent.OnBackClicked) {
                onNavigateBack()
            } else {
                viewModel.onEvent(event)
            }
        },
        onTransactionClick = onNavigateToEditTransaction,
        modifier = modifier
    )
}

@Composable
fun HistoryScreen(
    uiState: HistoryUiState,
    onEvent: (HistoryUiEvent) -> Unit,
    onTransactionClick: (String) -> Unit,
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
                        .clickable { onEvent(HistoryUiEvent.OnBackClicked) },
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
                    text = "Transaction History",
                    style = WalletTypography.TitleM,
                    color = textColor
                )

                Spacer(modifier = Modifier.width(44.dp))
            }

            // Filter Chips (All, Income, Expense)
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChipItem(
                        text = "All",
                        isSelected = uiState.selectedFilter == TransactionFilter.ALL,
                        onClick = { onEvent(HistoryUiEvent.OnFilterSelected(TransactionFilter.ALL)) },
                        cardBg = cardBg,
                        textColor = textColor
                    )
                }
                item {
                    FilterChipItem(
                        text = "Income Only",
                        isSelected = uiState.selectedFilter == TransactionFilter.INCOME,
                        onClick = { onEvent(HistoryUiEvent.OnFilterSelected(TransactionFilter.INCOME)) },
                        cardBg = cardBg,
                        textColor = textColor
                    )
                }
                item {
                    FilterChipItem(
                        text = "Expenses Only",
                        isSelected = uiState.selectedFilter == TransactionFilter.EXPENSE,
                        onClick = { onEvent(HistoryUiEvent.OnFilterSelected(TransactionFilter.EXPENSE)) },
                        cardBg = cardBg,
                        textColor = textColor
                    )
                }
            }

            if (uiState.groupedTransactions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No matching transactions found",
                        style = WalletTypography.BodyM,
                        color = WalletColors.TextMuted
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    uiState.groupedTransactions.forEach { group ->
                        item {
                            // Date Header & Net Subtotal
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = group.dateLabel,
                                    style = WalletTypography.LabelM,
                                    color = WalletColors.TextMuted
                                )
                                val netColor = if (group.dayTotalNet >= 0) WalletColors.MintChip else WalletColors.Coral
                                val sign = if (group.dayTotalNet >= 0) "+" else "−"
                                Text(
                                    text = "$sign${CurrencyManager.format(group.dayTotalNet)}",
                                    style = WalletTypography.LabelM,
                                    color = netColor
                                )
                            }
                        }

                        items(group.transactions) { tx ->
                            TransactionRowItem(
                                transaction = tx,
                                onClick = { onTransactionClick(tx.id) },
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
}

@Composable
private fun FilterChipItem(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    cardBg: Color,
    textColor: Color
) {
    Box(
        modifier = Modifier
            .clip(WalletShapes.Pill)
            .background(if (isSelected) WalletColors.Coral else cardBg)
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            style = WalletTypography.LabelS,
            color = if (isSelected) Color.White else textColor
        )
    }
}
