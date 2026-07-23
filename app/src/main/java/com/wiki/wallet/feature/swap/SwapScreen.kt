package com.wiki.wallet.feature.swap

import android.widget.Toast
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wiki.wallet.core.database.entity.TransactionType
import com.wiki.wallet.core.designsystem.components.DeltaChip
import com.wiki.wallet.core.designsystem.components.PillButton
import com.wiki.wallet.core.designsystem.components.PillButtonVariant
import com.wiki.wallet.core.designsystem.theme.WalletColors
import com.wiki.wallet.core.designsystem.theme.WalletShapes
import com.wiki.wallet.core.designsystem.theme.WalletTypography

@Composable
fun SwapRoute(
    onNavigateBack: () -> Unit,
    viewModel: SwapViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                SwapUiEffect.SaveSuccess -> {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    Toast.makeText(context, "Transaction saved! 🎉", Toast.LENGTH_SHORT).show()
                    onNavigateBack()
                }
                is SwapUiEffect.Error -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_LONG).show()
                }
                is SwapUiEffect.Toast -> {
                    Toast.makeText(context, effect.text, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    SwapScreen(
        uiState = uiState,
        onEvent = { event ->
            if (event is SwapUiEvent.OnBackClicked) {
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
fun SwapScreen(
    uiState: SwapUiState,
    onEvent: (SwapUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val accentColor = if (uiState.type == TransactionType.INCOME) WalletColors.MintChip else WalletColors.Coral

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(WalletColors.Ink)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
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
                        .background(Color.White)
                        .clickable { onEvent(SwapUiEvent.OnBackClicked) },
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
                    text = "Add Transaction",
                    style = WalletTypography.TitleM,
                    color = WalletColors.TextOnDark
                )

                Spacer(modifier = Modifier.width(44.dp))
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Income / Expense Segmented Control
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(WalletShapes.Pill)
                    .background(WalletColors.InkElevated)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                TypeSegmentButton(
                    text = "Expense",
                    isSelected = uiState.type == TransactionType.EXPENSE,
                    activeColor = WalletColors.Coral,
                    onClick = { onEvent(SwapUiEvent.OnTypeChanged(TransactionType.EXPENSE)) },
                    modifier = Modifier.weight(1f)
                )
                TypeSegmentButton(
                    text = "Income",
                    isSelected = uiState.type == TransactionType.INCOME,
                    activeColor = WalletColors.MintChip,
                    onClick = { onEvent(SwapUiEvent.OnTypeChanged(TransactionType.INCOME)) },
                    modifier = Modifier.weight(1f)
                )
            }

            // Account Selection Chips
            Text(
                text = "Select Account",
                style = WalletTypography.LabelS,
                color = WalletColors.TextMuted
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                uiState.accounts.forEach { account ->
                    val isSelected = account.id == uiState.selectedAccount?.id
                    Box(
                        modifier = Modifier
                            .clip(WalletShapes.Pill)
                            .background(if (isSelected) WalletColors.Paper else WalletColors.InkElevated)
                            .clickable { onEvent(SwapUiEvent.OnAccountSelected(account)) }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "${account.iconKey} ${account.name}",
                            style = WalletTypography.LabelS,
                            color = if (isSelected) WalletColors.Ink else WalletColors.TextOnDark
                        )
                    }
                }
            }

            // Amount Input Card (Top Card)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(WalletShapes.CardLarge)
                    .background(WalletColors.Paper)
                    .padding(20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = if (uiState.type == TransactionType.EXPENSE) "Spent Amount ($)" else "Received Amount ($)",
                        style = WalletTypography.LabelM,
                        color = WalletColors.TextMuted
                    )

                    BasicTextField(
                        value = uiState.amountText,
                        onValueChange = { onEvent(SwapUiEvent.OnAmountChanged(it)) },
                        textStyle = WalletTypography.DisplayL.copy(color = accentColor),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        cursorBrush = SolidColor(accentColor),
                        singleLine = true,
                        decorationBox = { innerTextField ->
                            Box {
                                if (uiState.amountText.isEmpty()) {
                                    Text(
                                        text = "0.00",
                                        style = WalletTypography.DisplayL,
                                        color = WalletColors.TextMuted
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )
                }
            }

            // Category Selector Card (Bottom Card)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(WalletShapes.CardLarge)
                    .background(accentColor)
                    .clickable { onEvent(SwapUiEvent.OnCategoryPickerToggle(true)) }
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Category",
                            style = WalletTypography.LabelM,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = uiState.selectedCategory?.let { "${it.iconKey} ${it.name}" } ?: "Select Category",
                                style = WalletTypography.TitleM,
                                color = Color.White
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Dropdown",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    if (uiState.selectedCategory?.monthlyBudget != null) {
                        DeltaChip(
                            text = "Budget $${String.format(java.util.Locale.US, "%.0f", uiState.selectedCategory.monthlyBudget)}",
                            isPositive = true,
                            backgroundColor = Color.White.copy(alpha = 0.2f),
                            textColor = Color.White
                        )
                    }
                }
            }

            // Note Input (Optional)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(WalletShapes.CardMedium)
                    .background(WalletColors.InkElevated)
                    .padding(14.dp)
            ) {
                BasicTextField(
                    value = uiState.noteText,
                    onValueChange = { onEvent(SwapUiEvent.OnNoteChanged(it)) },
                    textStyle = WalletTypography.BodyM.copy(color = WalletColors.TextOnDark),
                    singleLine = true,
                    decorationBox = { innerTextField ->
                        Box {
                            if (uiState.noteText.isEmpty()) {
                                Text(
                                    text = "Add note (optional e.g. Lunch at Cafe)...",
                                    style = WalletTypography.BodyM,
                                    color = WalletColors.TextMuted
                                )
                            }
                            innerTextField()
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Save CTA Button
            PillButton(
                text = if (uiState.isExecuting) "Saving..." else "Save Transaction",
                onClick = { onEvent(SwapUiEvent.OnSaveClicked) },
                variant = PillButtonVariant.Primary,
                enabled = uiState.isCtaEnabled && !uiState.isExecuting,
                height = 56.dp,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    // Modal Bottom Sheet for Category Picker
    if (uiState.isCategoryPickerOpen) {
        ModalBottomSheet(
            onDismissRequest = { onEvent(SwapUiEvent.OnCategoryPickerToggle(false)) },
            sheetState = rememberModalBottomSheetState(),
            containerColor = WalletColors.PaperPure
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Select Category",
                    style = WalletTypography.TitleM,
                    color = WalletColors.TextPrimary
                )

                val filteredCategories = uiState.categories.filter { it.type == uiState.type }

                LazyColumn(
                    modifier = Modifier.height(300.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredCategories) { category ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(WalletShapes.CardMedium)
                                .background(WalletColors.Paper)
                                .clickable { onEvent(SwapUiEvent.OnCategorySelected(category)) }
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
                                    Text(
                                        text = category.iconKey,
                                        style = WalletTypography.TitleM
                                    )
                                    Text(
                                        text = category.name,
                                        style = WalletTypography.TitleM,
                                        color = WalletColors.TextPrimary
                                    )
                                }

                                category.monthlyBudget?.let { budget ->
                                    Text(
                                        text = "Budget: $${String.format(java.util.Locale.US, "%.0f", budget)}",
                                        style = WalletTypography.LabelS,
                                        color = WalletColors.TextMuted
                                    )
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
private fun TypeSegmentButton(
    text: String,
    isSelected: Boolean,
    activeColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(WalletShapes.Pill)
            .background(if (isSelected) activeColor else Color.Transparent)
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = WalletTypography.LabelM,
            color = if (isSelected) Color.White else WalletColors.TextMuted
        )
    }
}

@Preview
@Composable
private fun SwapScreenPreview() {
    SwapScreen(
        uiState = SwapUiState(),
        onEvent = {}
    )
}
