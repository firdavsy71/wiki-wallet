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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Reorder
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wiki.wallet.core.database.entity.TransactionType
import com.wiki.wallet.core.designsystem.components.DeltaChip
import com.wiki.wallet.core.designsystem.components.IncomeBarChart
import com.wiki.wallet.core.designsystem.components.SuperscriptAmount
import com.wiki.wallet.core.designsystem.theme.ThemeManager
import com.wiki.wallet.core.designsystem.theme.WalletColors
import com.wiki.wallet.core.designsystem.theme.WalletShapes
import com.wiki.wallet.core.designsystem.theme.WalletTypography
import com.wiki.wallet.core.util.CurrencyManager
import com.wiki.wallet.domain.model.Account
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
    onNavigateToSettings: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToCalendar: () -> Unit,
    onNavigateToAccountDetail: (String) -> Unit,
    onNavigateToEditTransaction: (String) -> Unit,
    onNavigateToAddAccount: () -> Unit,
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
                DashboardUiEvent.OnNavigateToSettings -> onNavigateToSettings()
                DashboardUiEvent.OnNavigateToCalendar -> onNavigateToCalendar()
                is DashboardUiEvent.OnAccountClick -> onNavigateToAccountDetail(event.accountId)
                is DashboardUiEvent.OnTransactionClick -> onNavigateToEditTransaction(event.transactionId)
                else -> viewModel.onEvent(event)
            }
        },
        onNavigateToProfile = onNavigateToProfile,
        onNavigateToAddAccount = onNavigateToAddAccount,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    uiState: DashboardUiState,
    onEvent: (DashboardUiEvent) -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToAddAccount: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark by ThemeManager.isDarkTheme.collectAsStateWithLifecycle()
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Row with Profile Icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    HeaderIconButton(
                        icon = Icons.Default.Person,
                        contentDescription = "Profile",
                        onClick = onNavigateToProfile,
                        containerColor = cardBg,
                        tintColor = textColor
                    )

                    Column {
                        Text(
                            text = "${uiState.userName} 👋",
                            style = WalletTypography.TitleM,
                            color = textColor
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "ApexBudget Overview",
                            style = WalletTypography.LabelS,
                            color = WalletColors.TextMuted
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    HeaderIconButton(
                        icon = Icons.Default.CalendarMonth,
                        contentDescription = "Bill Calendar",
                        onClick = { onEvent(DashboardUiEvent.OnNavigateToCalendar) },
                        containerColor = cardBg,
                        tintColor = textColor
                    )
                    HeaderIconButton(
                        icon = Icons.Default.Category,
                        contentDescription = "Categories & Budgets",
                        onClick = { onEvent(DashboardUiEvent.OnNavigateToCategories) },
                        containerColor = cardBg,
                        tintColor = textColor
                    )
                    HeaderIconButton(
                        icon = Icons.Default.History,
                        contentDescription = "Transaction History",
                        onClick = { onEvent(DashboardUiEvent.OnNavigateToHistory) },
                        containerColor = cardBg,
                        tintColor = textColor
                    )
                    HeaderIconButton(
                        icon = Icons.Default.Settings,
                        contentDescription = "Settings",
                        onClick = { onEvent(DashboardUiEvent.OnNavigateToSettings) },
                        containerColor = cardBg,
                        tintColor = textColor
                    )
                }
            }

            // Hero Card — Net Balance (Dark Ink Card)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(WalletShapes.CardLarge)
                    .background(if (isDark) WalletColors.Ink else WalletColors.Ink)
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

            // Accounts Carousel Section
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "My Accounts",
                        style = WalletTypography.TitleM,
                        color = textColor
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                            modifier = Modifier
                                .clickable { onEvent(DashboardUiEvent.OnReorderModalToggle(true)) }
                                .padding(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Reorder,
                                contentDescription = "Reorder",
                                tint = WalletColors.Coral,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Reorder",
                                style = WalletTypography.LabelM,
                                color = WalletColors.Coral
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                            modifier = Modifier
                                .clickable { onNavigateToAddAccount() }
                                .padding(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Manage Accounts",
                                tint = WalletColors.Coral,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Manage",
                                style = WalletTypography.LabelM,
                                color = WalletColors.Coral
                            )
                        }
                    }
                }

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.accounts) { account ->
                        AccountBalanceCard(
                            account = account,
                            onClick = { onEvent(DashboardUiEvent.OnAccountClick(account.id)) },
                            cardBg = cardBg,
                            textColor = textColor,
                            borderColor = borderColor
                        )
                    }
                }
            }

            // Daily Net Cash Flow Bar Chart
            IncomeBarChart(
                items = uiState.chartItems,
                cardHeight = 220.dp
            )

            // Income / Expense Stat Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    title = "Income",
                    amount = uiState.periodIncomeText,
                    accentColor = WalletColors.MintChip,
                    cardBg = cardBg,
                    textColor = textColor,
                    borderColor = borderColor,
                    modifier = Modifier.weight(1f)
                )

                StatCard(
                    title = "Expenses",
                    amount = uiState.periodExpenseText,
                    accentColor = WalletColors.Coral,
                    cardBg = cardBg,
                    textColor = textColor,
                    borderColor = borderColor,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Recent Transactions Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Transactions",
                    style = WalletTypography.TitleM,
                    color = textColor
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
                        text = "No transactions logged yet. Tap + to add one!",
                        style = WalletTypography.BodyM,
                        color = WalletColors.TextMuted
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    uiState.recentTransactions.forEach { tx ->
                        TransactionRowItem(
                            transaction = tx,
                            onClick = { onEvent(DashboardUiEvent.OnTransactionClick(tx.id)) },
                            cardBg = cardBg,
                            textColor = textColor,
                            borderColor = borderColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(60.dp))
        }

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

    // Modal Bottom Sheet for Vertical Account Reordering
    if (uiState.isReorderModalOpen) {
        ModalBottomSheet(
            onDismissRequest = { onEvent(DashboardUiEvent.OnReorderModalToggle(false)) },
            sheetState = rememberModalBottomSheetState(),
            containerColor = cardBg
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Reorder Accounts Order",
                    style = WalletTypography.TitleM,
                    color = textColor
                )

                LazyColumn(
                    modifier = Modifier.height(300.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    itemsIndexed(uiState.accounts) { index, account ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(WalletShapes.CardMedium)
                                .background(bgColor)
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
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text(text = account.iconKey, style = WalletTypography.TitleM)
                                    Column {
                                        Text(text = account.name, style = WalletTypography.TitleM, color = textColor)
                                        Text(text = CurrencyManager.format(account.currentBalance), style = WalletTypography.LabelS, color = WalletColors.TextMuted)
                                    }
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    if (index > 0) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(cardBg)
                                                .clickable { onEvent(DashboardUiEvent.OnMoveAccountUp(account.id)) },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(imageVector = Icons.Default.ArrowUpward, contentDescription = "Up", tint = textColor, modifier = Modifier.size(18.dp))
                                        }
                                    }
                                    if (index < uiState.accounts.lastIndex) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(cardBg)
                                                .clickable { onEvent(DashboardUiEvent.OnMoveAccountDown(account.id)) },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(imageVector = Icons.Default.ArrowDownward, contentDescription = "Down", tint = textColor, modifier = Modifier.size(18.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HeaderIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    containerColor: Color,
    tintColor: Color
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(containerColor)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tintColor,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun AccountBalanceCard(
    account: Account,
    onClick: () -> Unit,
    cardBg: Color,
    textColor: Color,
    borderColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .width(160.dp)
            .clip(WalletShapes.CardMedium)
            .background(cardBg)
            .border(1.dp, borderColor, WalletShapes.CardMedium)
            .clickable { onClick() }
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = account.iconKey, style = WalletTypography.TitleM)
                Box(
                    modifier = Modifier
                        .clip(WalletShapes.Pill)
                        .background(WalletColors.InkElevated)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = account.currency,
                        style = WalletTypography.LabelS,
                        color = WalletColors.TextOnDark
                    )
                }
            }

            Text(
                text = account.name,
                style = WalletTypography.LabelM,
                color = WalletColors.TextMuted,
                maxLines = 1
            )

            Text(
                text = CurrencyManager.format(account.currentBalance),
                style = WalletTypography.TitleM,
                color = textColor
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
    cardBg: Color,
    textColor: Color,
    borderColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(WalletShapes.CardMedium)
            .background(cardBg)
            .border(1.dp, borderColor, WalletShapes.CardMedium)
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
                color = textColor
            )
        }
    }
}

@Composable
fun TransactionRowItem(
    transaction: Transaction,
    onClick: (() -> Unit)? = null,
    cardBg: Color = WalletColors.PaperPure,
    textColor: Color = WalletColors.TextPrimary,
    borderColor: Color = WalletColors.CardBorder,
    modifier: Modifier = Modifier
) {
    val isIncome = transaction.type == TransactionType.INCOME
    val amountColor = if (isIncome) WalletColors.MintChip else WalletColors.Coral
    val amountSign = if (isIncome) "+" else "−"

    val dateFormat = SimpleDateFormat("MMM d, HH:mm", Locale.getDefault())
    val dateText = dateFormat.format(Date(transaction.date))

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(WalletShapes.CardMedium)
            .background(cardBg)
            .border(1.dp, borderColor, WalletShapes.CardMedium)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
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
                        color = textColor
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
                    text = "$amountSign${CurrencyManager.format(transaction.amount)}",
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
