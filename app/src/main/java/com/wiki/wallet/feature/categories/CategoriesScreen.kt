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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wiki.wallet.core.database.entity.TransactionType
import com.wiki.wallet.core.designsystem.theme.ThemeManager
import com.wiki.wallet.core.designsystem.theme.WalletColors
import com.wiki.wallet.core.designsystem.theme.WalletShapes
import com.wiki.wallet.core.designsystem.theme.WalletTypography
import com.wiki.wallet.core.util.CurrencyManager
import com.wiki.wallet.domain.model.Category

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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Top Bar Row
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
                        .clickable { onEvent(CategoriesUiEvent.OnBackClicked) },
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
                    text = "Category Budgets",
                    style = WalletTypography.TitleM,
                    color = textColor
                )

                Spacer(modifier = Modifier.width(44.dp))
            }

            // Grid of Categories
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.categories) { category ->
                    CategoryCardItem(
                        category = category,
                        onClick = { onEvent(CategoriesUiEvent.OnCategoryClick(category)) },
                        cardBg = cardBg,
                        textColor = textColor,
                        borderColor = borderColor
                    )
                }
            }
        }
    }

    // Modal Bottom Sheet for Category Budget Detail & Customization
    if (uiState.isDetailBottomSheetOpen && uiState.selectedCategory != null) {
        val category = uiState.selectedCategory
        val isIncome = category.type == TransactionType.INCOME

        ModalBottomSheet(
            onDismissRequest = { onEvent(CategoriesUiEvent.OnDismissDetail) },
            sheetState = rememberModalBottomSheetState(),
            containerColor = cardBg
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(
                                if (isIncome) WalletColors.MintChip.copy(alpha = 0.15f)
                                else WalletColors.Coral.copy(alpha = 0.15f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = category.iconKey, style = WalletTypography.DisplayL)
                    }

                    Column {
                        Text(text = category.name, style = WalletTypography.TitleM, color = textColor)
                        Text(
                            text = if (isIncome) "Income Category" else "Expense Category",
                            style = WalletTypography.BodyM,
                            color = WalletColors.TextMuted
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(WalletShapes.CardMedium)
                        .background(bgColor)
                        .border(1.dp, borderColor, WalletShapes.CardMedium)
                        .padding(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Spent this month", style = WalletTypography.BodyM, color = WalletColors.TextMuted)
                            Text(
                                text = CurrencyManager.format(category.currentPeriodSpent),
                                style = WalletTypography.TitleM,
                                color = if (isIncome) WalletColors.MintChip else WalletColors.Coral
                            )
                        }

                        if (!isIncome) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "Monthly Budget", style = WalletTypography.BodyM, color = WalletColors.TextMuted)

                                if (uiState.isEditingBudget) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        BasicTextField(
                                            value = uiState.editedBudgetText,
                                            onValueChange = { onEvent(CategoriesUiEvent.OnBudgetTextChanged(it)) },
                                            textStyle = WalletTypography.TitleM.copy(color = textColor),
                                            singleLine = true,
                                            cursorBrush = SolidColor(WalletColors.Coral),
                                            modifier = Modifier
                                                .width(100.dp)
                                                .clip(WalletShapes.CardMedium)
                                                .background(cardBg)
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clip(CircleShape)
                                                .background(WalletColors.Coral)
                                                .clickable { onEvent(CategoriesUiEvent.OnSaveBudgetClicked) },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Save",
                                                tint = Color.White,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                } else {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = if (category.monthlyBudget != null) CurrencyManager.format(category.monthlyBudget) else "Not Set",
                                            style = WalletTypography.TitleM,
                                            color = textColor
                                        )
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Edit Budget",
                                            tint = WalletColors.Coral,
                                            modifier = Modifier
                                                .size(16.dp)
                                                .clickable { onEvent(CategoriesUiEvent.OnToggleEditBudget) }
                                        )
                                    }
                                }
                            }

                            if (category.monthlyBudget != null) {
                                val ratio = (category.currentPeriodSpent / category.monthlyBudget).coerceIn(0.0, 1.0).toFloat()
                                LinearProgressIndicator(
                                    progress = { ratio },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(WalletShapes.Pill),
                                    color = if (ratio > 0.9f) WalletColors.Coral else WalletColors.MintChip,
                                    trackColor = borderColor
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
    cardBg: Color,
    textColor: Color,
    borderColor: Color
) {
    val isIncome = category.type == TransactionType.INCOME
    val accentColor = if (isIncome) WalletColors.MintChip else WalletColors.Coral

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(WalletShapes.CardMedium)
            .background(cardBg)
            .border(1.dp, borderColor, WalletShapes.CardMedium)
            .clickable { onClick() }
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
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
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (isIncome) "IN" else "OUT",
                        style = WalletTypography.LabelS,
                        color = accentColor
                    )
                }
            }

            Text(
                text = category.name,
                style = WalletTypography.TitleM,
                color = textColor,
                maxLines = 1
            )

            Text(
                text = CurrencyManager.format(category.currentPeriodSpent),
                style = WalletTypography.LabelM,
                color = accentColor
            )
        }
    }
}
