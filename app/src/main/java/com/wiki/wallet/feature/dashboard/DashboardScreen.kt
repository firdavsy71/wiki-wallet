package com.wiki.wallet.feature.dashboard

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wiki.wallet.core.database.entity.TransactionType
import com.wiki.wallet.core.designsystem.components.DeltaChip
import com.wiki.wallet.core.designsystem.components.IncomeBarChart
import com.wiki.wallet.core.designsystem.components.SuperscriptAmount
import com.wiki.wallet.core.designsystem.theme.WalletColors
import com.wiki.wallet.core.designsystem.theme.WalletShapes
import com.wiki.wallet.core.designsystem.theme.WalletTypography
import com.wiki.wallet.domain.model.TimePeriod
import com.wiki.wallet.domain.model.Transaction
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardRoute(
    onNavigateToAddTransaction: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToCategories: () -> Unit,
    viewModel: DashboardViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    DashboardScreen(
        uiState = uiState,
        onEvent = { event ->
            when (event) {
                DashboardUiEvent.OnNavigateToAddTransaction -> onNavigateToAddTransaction()
                DashboardUiEvent.OnNavigateToHistory -> onNavigateToHistory()
                DashboardUiEvent.OnNavigateToCategories -> onNavigateToCategories()
                else -> viewModel.onEvent(event)
            }
        },
        modifier = modifier
    )
}

@Composable
fun DashboardScreen(
    uiState: DashboardUiState,
    onEvent: (DashboardUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(WalletColors.PaperPure)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Hello, ${uiState.userName}! 👋",
                        style = WalletTypography.DisplayL,
                        color = WalletColors.TextPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Overview & Net Cash Flow",
                        style = WalletTypography.BodyM,
                        color = WalletColors.TextMuted
                    )
                }

                // Top Actions (Categories & History navigation)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(WalletColors.Paper)
                            .clickable { onEvent(DashboardUiEvent.OnNavigateToCategories) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Category,
                            contentDescription = "Categories & Budgets",
                            tint = WalletColors.Ink,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(WalletColors.Paper)
                            .clickable { onEvent(DashboardUiEvent.OnNavigateToHistory) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "Transaction History",
                            tint = WalletColors.Ink,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Hero Card — Net Balance (Dark Ink Card)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(WalletShapes.CardLarge)
                    .background(WalletColors.Ink)
                    .padding(20.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Net Balance",
                            style = WalletTypography.BodyM,
                            color = WalletColors.TextOnDark
                        )

                        // Weekly / Monthly Toggle Chips
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier
                                .clip(WalletShapes.Pill)
                                .background(WalletColors.InkElevated)
                                .padding(4.dp)
                        ) {
                            TimePeriodChip(
                                text = "Weekly",
                                isSelected = uiState.selectedPeriod == TimePeriod.WEEKLY,
                                onClick = { onEvent(DashboardUiEvent.OnPeriodSelected(TimePeriod.WEEKLY)) }
                            )
                            TimePeriodChip(
                                text = "Monthly",
                                isSelected = uiState.selectedPeriod == TimePeriod.MONTHLY,
                                onClick = { onEvent(DashboardUiEvent.OnPeriodSelected(TimePeriod.MONTHLY)) }
                            )
                        }
                    }

                    SuperscriptAmount(
                        amountText = uiState.netBalanceText,
                        style = WalletTypography.DisplayXL,
                        color = WalletColors.TextOnDark
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DeltaChip(
                            text = "Savings Rate ${uiState.savingsRateText}",
                            isPositive = !uiState.savingsRateText.startsWith("-"),
                            backgroundColor = WalletColors.InkElevated,
                            textColor = WalletColors.TextOnDark
                        )
                    }
                }
            }

            // Daily Net Cash Flow Bar Chart
            IncomeBarChart(
                items = uiState.chartItems,
                cardHeight = 190.dp
            )

            // Income / Expense / Savings Stat Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Income Card
                StatCard(
                    title = "Income",
                    amount = uiState.periodIncomeText,
                    accentColor = WalletColors.MintChip,
                    modifier = Modifier.weight(1f)
                )

                // Expense Card
                StatCard(
                    title = "Expenses",
                    amount = uiState.periodExpenseText,
                    accentColor = WalletColors.Coral,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Recent Transactions Section Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Transactions",
                    style = WalletTypography.TitleM,
                    color = WalletColors.TextPrimary
                )
                Text(
                    text = "See All",
                    style = WalletTypography.LabelM,
                    color = WalletColors.Coral,
                    modifier = Modifier.clickable { onEvent(DashboardUiEvent.OnNavigateToHistory) }
                )
            }

            // Recent Transactions List
            if (uiState.recentTransactions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No transactions logged yet",
                        style = WalletTypography.BodyM,
                        color = WalletColors.TextMuted
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    uiState.recentTransactions.forEach { tx ->
                        TransactionRowItem(transaction = tx)
                    }
                }
            }

            Spacer(modifier = Modifier.height(60.dp)) // Clearance for FAB
        }

        // Floating Action Button to Add Transaction
        FloatingActionButton(
            onClick = { onEvent(DashboardUiEvent.OnNavigateToAddTransaction) },
            containerColor = WalletColors.Coral,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .size(56.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Transaction",
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
private fun TimePeriodChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(WalletShapes.Pill)
            .background(if (isSelected) WalletColors.Coral else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            style = WalletTypography.LabelS,
            color = if (isSelected) Color.White else WalletColors.TextMuted
        )
    }
}

@Composable
private fun StatCard(
    title: String,
    amount: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(WalletShapes.CardMedium)
            .background(WalletColors.Paper)
            .border(1.dp, WalletColors.CardBorder, WalletShapes.CardMedium)
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(accentColor)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = title,
                    style = WalletTypography.LabelM,
                    color = WalletColors.TextMuted
                )
            }
            Text(
                text = amount,
                style = WalletTypography.TitleM,
                color = WalletColors.TextPrimary
            )
        }
    }
}

@Composable
fun TransactionRowItem(
    transaction: Transaction,
    modifier: Modifier = Modifier
) {
    val isIncome = transaction.type == TransactionType.INCOME
    val amountColor = if (isIncome) WalletColors.MintChip else WalletColors.Coral
    val amountPrefix = if (isIncome) "+$" else "-$"

    val dateFormat = SimpleDateFormat("MMM d, HH:mm", Locale.getDefault())
    val dateText = dateFormat.format(Date(transaction.date))

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(WalletShapes.CardMedium)
            .background(WalletColors.PaperPure)
            .border(1.dp, WalletColors.CardBorder, WalletShapes.CardMedium)
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
                        .background(
                            if (isIncome) WalletColors.MintChip.copy(alpha = 0.15f)
                            else WalletColors.Coral.copy(alpha = 0.15f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = transaction.categoryIcon,
                        style = WalletTypography.TitleM
                    )
                }

                Column {
                    Text(
                        text = transaction.categoryName,
                        style = WalletTypography.TitleM,
                        color = WalletColors.TextPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = transaction.note ?: dateText,
                        style = WalletTypography.BodyM,
                        color = WalletColors.TextMuted
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "$amountPrefix${String.format(Locale.US, "%.2f", transaction.amount)}",
                    style = WalletTypography.TitleM,
                    color = amountColor
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = dateText,
                    style = WalletTypography.LabelS,
                    color = WalletColors.TextMuted
                )
            }
        }
    }
}

@Preview
@Composable
private fun DashboardScreenPreview() {
    DashboardScreen(
        uiState = DashboardUiState(),
        onEvent = {}
    )
}
