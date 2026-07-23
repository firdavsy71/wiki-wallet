package com.wiki.wallet.feature.categories

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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.wiki.wallet.core.designsystem.theme.WalletColors
import com.wiki.wallet.core.designsystem.theme.WalletShapes
import com.wiki.wallet.core.designsystem.theme.WalletTypography
import com.wiki.wallet.domain.model.Category
import java.util.Locale

@Composable
fun CategoriesRoute(
    onNavigateBack: () -> Unit,
    viewModel: CategoriesViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    CategoriesScreen(
        uiState = uiState,
        onEvent = { event ->
            if (event is CategoriesUiEvent.OnBackClicked) {
                onNavigateBack()
            } else {
                viewModel.onEvent(event)
            }
        },
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(
    uiState: CategoriesUiState,
    onEvent: (CategoriesUiEvent) -> Unit,
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
                .padding(horizontal = 20.dp, vertical = 24.dp),
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
                        .clickable { onEvent(CategoriesUiEvent.OnBackClicked) },
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
                    text = "Categories & Budgets",
                    style = WalletTypography.TitleM,
                    color = WalletColors.TextPrimary
                )

                Spacer(modifier = Modifier.width(44.dp))
            }

            // Categories Grid (2 Columns)
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.categories) { category ->
                    CategoryCardItem(
                        category = category,
                        onClick = { onEvent(CategoriesUiEvent.OnCategoryClick(category)) }
                    )
                }
            }
        }

        // Category Detail Bottom Sheet
        if (uiState.isDetailBottomSheetOpen && uiState.selectedCategory != null) {
            val category = uiState.selectedCategory
            ModalBottomSheet(
                onDismissRequest = { onEvent(CategoriesUiEvent.OnDismissDetail) },
                sheetState = rememberModalBottomSheetState(),
                containerColor = WalletColors.PaperPure
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(text = category.iconKey, style = WalletTypography.DisplayL)
                        Column {
                            Text(text = category.name, style = WalletTypography.TitleM, color = WalletColors.TextPrimary)
                            Text(text = category.type.name, style = WalletTypography.LabelS, color = WalletColors.TextMuted)
                        }
                    }

                    category.monthlyBudget?.let { budget ->
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Monthly Budget", style = WalletTypography.LabelM, color = WalletColors.TextMuted)
                                Text(
                                    text = "$${String.format(Locale.US, "%.2f", category.currentPeriodSpent)} / $${String.format(Locale.US, "%.2f", budget)}",
                                    style = WalletTypography.LabelM,
                                    color = WalletColors.TextPrimary
                                )
                            }

                            // Progress Track
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(WalletShapes.Pill)
                                    .background(WalletColors.Paper)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize(category.budgetProgressRatio)
                                        .clip(WalletShapes.Pill)
                                        .background(
                                            if (category.budgetProgressRatio >= 1.0f) WalletColors.Coral
                                            else WalletColors.MintChip
                                        )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryCardItem(
    category: Category,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isExpense = category.type == TransactionType.EXPENSE
    val accentColor = if (isExpense) WalletColors.Coral else WalletColors.MintChip

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(WalletShapes.CardMedium)
            .background(WalletColors.Paper)
            .border(1.dp, WalletColors.CardBorder, WalletShapes.CardMedium)
            .clickable { onClick() }
            .padding(14.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = category.iconKey, style = WalletTypography.TitleM)
                }

                Box(
                    modifier = Modifier
                        .clip(WalletShapes.Pill)
                        .background(accentColor.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (isExpense) "Expense" else "Income",
                        style = WalletTypography.LabelS,
                        color = accentColor
                    )
                }
            }

            Text(
                text = category.name,
                style = WalletTypography.TitleM,
                color = WalletColors.TextPrimary
            )

            // Budget Progress Bar if budget is set
            category.monthlyBudget?.let { budget ->
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Spent $${String.format(Locale.US, "%.0f", category.currentPeriodSpent)} / $${String.format(Locale.US, "%.0f", budget)}",
                        style = WalletTypography.LabelS,
                        color = WalletColors.TextMuted
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(WalletShapes.Pill)
                            .background(WalletColors.Canvas.copy(alpha = 0.3f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize(category.budgetProgressRatio)
                                .clip(WalletShapes.Pill)
                                .background(
                                    if (category.budgetProgressRatio >= 1.0f) WalletColors.Coral
                                    else WalletColors.MintChip
                                )
                        )
                    }
                }
            }
        }
    }
}
