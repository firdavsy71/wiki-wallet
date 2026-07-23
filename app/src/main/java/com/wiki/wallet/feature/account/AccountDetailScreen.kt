package com.wiki.wallet.feature.account

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import com.wiki.wallet.core.designsystem.components.SuperscriptAmount
import com.wiki.wallet.core.designsystem.theme.WalletColors
import com.wiki.wallet.core.designsystem.theme.WalletShapes
import com.wiki.wallet.core.designsystem.theme.WalletTypography
import com.wiki.wallet.feature.dashboard.TransactionRowItem
import java.util.Locale

@Composable
fun AccountDetailRoute(
    onNavigateBack: () -> Unit,
    viewModel: AccountDetailViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    AccountDetailScreen(
        uiState = uiState,
        onEvent = { event ->
            if (event is AccountDetailUiEvent.OnBackClicked) {
                onNavigateBack()
            } else {
                viewModel.onEvent(event)
            }
        },
        modifier = modifier
    )
}

@Composable
fun AccountDetailScreen(
    uiState: AccountDetailUiState,
    onEvent: (AccountDetailUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val account = uiState.account

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(WalletColors.PaperPure)
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
                        .background(WalletColors.Paper)
                        .clickable { onEvent(AccountDetailUiEvent.OnBackClicked) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = WalletColors.Ink,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Text(
                    text = account?.name ?: "Account Details",
                    style = WalletTypography.TitleM,
                    color = WalletColors.TextPrimary
                )

                Spacer(modifier = Modifier.width(44.dp))
            }

            // Account Hero Card (Dark Ink background)
            if (account != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(WalletShapes.CardLarge)
                        .background(WalletColors.Ink)
                        .padding(20.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(text = account.iconKey, style = WalletTypography.DisplayL)
                                Text(text = account.name, style = WalletTypography.TitleM, color = WalletColors.TextOnDark)
                            }
                            Box(
                                modifier = Modifier
                                    .clip(WalletShapes.Pill)
                                    .background(WalletColors.InkElevated)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(text = account.currency, style = WalletTypography.LabelS, color = WalletColors.TextOnDark)
                            }
                        }

                        Text(text = "Available Balance", style = WalletTypography.LabelM, color = WalletColors.TextMuted)

                        SuperscriptAmount(
                            amountText = "$${String.format(Locale.US, "%,.2f", account.currentBalance)}",
                            style = WalletTypography.DisplayXL,
                            color = WalletColors.TextOnDark
                        )

                        // Income / Expense row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Total Income: +$${String.format(Locale.US, "%,.2f", uiState.totalIncome)}",
                                style = WalletTypography.LabelS,
                                color = WalletColors.MintChip
                            )
                            Text(
                                text = "Total Spent: -$${String.format(Locale.US, "%,.2f", uiState.totalExpense)}",
                                style = WalletTypography.LabelS,
                                color = WalletColors.Coral
                            )
                        }
                    }
                }
            }

            Text(
                text = "Account Activity (${uiState.transactions.size})",
                style = WalletTypography.TitleM,
                color = WalletColors.TextPrimary
            )

            // Transactions List
            if (uiState.transactions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No transactions logged for this account",
                        style = WalletTypography.BodyM,
                        color = WalletColors.TextMuted
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(uiState.transactions) { tx ->
                        TransactionRowItem(transaction = tx)
                    }
                }
            }
        }
    }
}
