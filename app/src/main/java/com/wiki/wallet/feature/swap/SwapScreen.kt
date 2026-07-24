package com.wiki.wallet.feature.swap

import android.widget.Toast
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wiki.wallet.core.database.entity.TransactionType
import com.wiki.wallet.core.designsystem.components.PillButton
import com.wiki.wallet.core.designsystem.components.PillButtonVariant
import com.wiki.wallet.core.designsystem.theme.ThemeManager
import com.wiki.wallet.core.designsystem.theme.WalletColors
import com.wiki.wallet.core.designsystem.theme.WalletShapes
import com.wiki.wallet.core.designsystem.theme.WalletTypography
import com.wiki.wallet.domain.model.Account
import com.wiki.wallet.domain.model.Category

@Composable
fun SwapRoute(
    onNavigateBack: () -> Unit,
    viewModel: SwapViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(viewModel.effect) {
        viewModel.effect.collect { effect ->
            when (effect) {
                SwapUiEffect.SaveSuccess -> {
                    Toast.makeText(context, if (uiState.isEditMode) "Transaction updated!" else "Transaction saved!", Toast.LENGTH_SHORT).show()
                    onNavigateBack()
                }
                SwapUiEffect.DeleteSuccess -> {
                    Toast.makeText(context, "Transaction deleted!", Toast.LENGTH_SHORT).show()
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Top Navigation Bar
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
                        .clickable { onEvent(SwapUiEvent.OnBackClicked) },
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
                    text = if (uiState.isEditMode) "Edit Transaction" else "Add Transaction",
                    style = WalletTypography.TitleM,
                    color = textColor
                )

                Spacer(modifier = Modifier.width(44.dp))
            }

            // Income / Expense Toggle Segment
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(WalletShapes.Pill)
                    .background(cardBg)
                    .padding(4.dp)
            ) {
                TypeSegmentButton(
                    text = "Expense (−)",
                    isSelected = uiState.type == TransactionType.EXPENSE,
                    selectedColor = WalletColors.Coral,
                    unselectedTextColor = textColor,
                    onClick = { onEvent(SwapUiEvent.OnTypeChanged(TransactionType.EXPENSE)) },
                    modifier = Modifier.weight(1f)
                )

                TypeSegmentButton(
                    text = "Income (+)",
                    isSelected = uiState.type == TransactionType.INCOME,
                    selectedColor = WalletColors.MintChip,
                    unselectedTextColor = textColor,
                    onClick = { onEvent(SwapUiEvent.OnTypeChanged(TransactionType.INCOME)) },
                    modifier = Modifier.weight(1f)
                )
            }

            // Amount Input Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(WalletShapes.CardLarge)
                    .background(WalletColors.Ink)
                    .padding(20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Amount",
                        style = WalletTypography.BodyM,
                        color = WalletColors.TextMuted
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (uiState.type == TransactionType.INCOME) "+$" else "-$",
                            style = WalletTypography.DisplayXL,
                            color = if (uiState.type == TransactionType.INCOME) WalletColors.MintChip else WalletColors.Coral
                        )

                        BasicTextField(
                            value = uiState.amountText,
                            onValueChange = { onEvent(SwapUiEvent.OnAmountChanged(it)) },
                            textStyle = WalletTypography.DisplayXL.copy(
                                color = WalletColors.TextOnDark
                            ),
                            singleLine = true,
                            cursorBrush = SolidColor(WalletColors.Coral),
                            decorationBox = { innerTextField ->
                                Box {
                                    if (uiState.amountText.isEmpty()) {
                                        Text(
                                            text = "0.00",
                                            style = WalletTypography.DisplayXL,
                                            color = WalletColors.TextMutedDark
                                        )
                                    }
                                    innerTextField()
                                }
                            }
                        )
                    }
                }
            }

            // Category Selector Card
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Category",
                    style = WalletTypography.LabelM,
                    color = WalletColors.TextMuted
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(WalletShapes.CardMedium)
                        .background(cardBg)
                        .border(1.dp, borderColor, WalletShapes.CardMedium)
                        .clickable { onEvent(SwapUiEvent.OnCategoryPickerToggle(true)) }
                        .padding(16.dp)
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
                            Text(
                                text = uiState.selectedCategory?.iconKey ?: "📁",
                                style = WalletTypography.TitleM
                            )
                            Text(
                                text = uiState.selectedCategory?.name ?: "Select Category",
                                style = WalletTypography.TitleM,
                                color = textColor
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Select Category",
                            tint = textColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            // Account Selection Chips (with High-Contrast Dark Mode Highlight)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Source Account",
                    style = WalletTypography.LabelM,
                    color = WalletColors.TextMuted
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    uiState.accounts.forEach { account ->
                        val isSelected = account.id == uiState.selectedAccount?.id
                        AccountChip(
                            account = account,
                            isSelected = isSelected,
                            cardBg = cardBg,
                            textColor = textColor,
                            borderColor = borderColor,
                            onClick = { onEvent(SwapUiEvent.OnAccountSelected(account)) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Recurring Bill / Subscription Toggle Section
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
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = "Bill",
                            tint = WalletColors.Coral,
                            modifier = Modifier.size(22.dp)
                        )
                        Column {
                            Text(
                                text = "Set as Scheduled Bill / Subscription",
                                style = WalletTypography.TitleM,
                                color = textColor
                            )
                            Text(
                                text = "Will appear on the Bill Calendar for due tracking",
                                style = WalletTypography.LabelS,
                                color = WalletColors.TextMuted
                            )
                        }
                    }

                    Switch(
                        checked = uiState.isBill,
                        onCheckedChange = { onEvent(SwapUiEvent.OnIsBillToggled(it)) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = WalletColors.Coral
                        )
                    )
                }
            }

            // Optional Note Input
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Note (Optional)",
                    style = WalletTypography.LabelM,
                    color = WalletColors.TextMuted
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(WalletShapes.CardMedium)
                        .background(cardBg)
                        .border(1.dp, borderColor, WalletShapes.CardMedium)
                        .padding(14.dp)
                ) {
                    BasicTextField(
                        value = uiState.noteText,
                        onValueChange = { onEvent(SwapUiEvent.OnNoteChanged(it)) },
                        textStyle = WalletTypography.BodyM.copy(color = textColor),
                        singleLine = true,
                        cursorBrush = SolidColor(WalletColors.Coral),
                        decorationBox = { innerTextField ->
                            Box {
                                if (uiState.noteText.isEmpty()) {
                                    Text(
                                        text = "Add note or subscription name...",
                                        style = WalletTypography.BodyM,
                                        color = WalletColors.TextMuted
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Primary Save / Log Button
            PillButton(
                text = if (uiState.isExecuting) "Saving..."
                else if (uiState.isEditMode) "Save Changes"
                else "Log Transaction",
                onClick = { onEvent(SwapUiEvent.OnSaveClicked) },
                enabled = uiState.isCtaEnabled && !uiState.isExecuting,
                variant = PillButtonVariant.Primary,
                height = 56.dp,
                modifier = Modifier.fillMaxWidth()
            )

            // Delete Button (only present in edit mode)
            if (uiState.isEditMode) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(WalletShapes.Pill)
                        .background(WalletColors.Coral.copy(alpha = 0.12f))
                        .clickable { onEvent(SwapUiEvent.OnDeleteClicked) }
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = WalletColors.Coral,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Delete Transaction",
                            style = WalletTypography.TitleM,
                            color = WalletColors.Coral
                        )
                    }
                }
            }
        }
    }

    // Modal Bottom Sheet for Category Picker
    if (uiState.isCategoryPickerOpen) {
        ModalBottomSheet(
            onDismissRequest = { onEvent(SwapUiEvent.OnCategoryPickerToggle(false)) },
            sheetState = rememberModalBottomSheetState(),
            containerColor = cardBg
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
                    color = textColor
                )

                val availableCats = uiState.categories.filter { it.type == uiState.type }

                LazyColumn(
                    modifier = Modifier.height(300.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(availableCats) { category ->
                        val isSelected = category.id == uiState.selectedCategory?.id

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(WalletShapes.CardMedium)
                                .background(if (isSelected) WalletColors.InkElevated else bgColor)
                                .clickable { onEvent(SwapUiEvent.OnCategorySelected(category)) }
                                .padding(14.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = category.iconKey,
                                    style = WalletTypography.TitleM
                                )
                                Text(
                                    text = category.name,
                                    style = WalletTypography.TitleM,
                                    color = if (isSelected) WalletColors.TextOnDark else textColor
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
private fun TypeSegmentButton(
    text: String,
    isSelected: Boolean,
    selectedColor: Color,
    unselectedTextColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(WalletShapes.Pill)
            .background(if (isSelected) selectedColor else Color.Transparent)
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = WalletTypography.TitleM,
            color = if (isSelected) Color.White else unselectedTextColor
        )
    }
}

@Composable
private fun AccountChip(
    account: Account,
    isSelected: Boolean,
    cardBg: Color,
    textColor: Color,
    borderColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // High-contrast contrast highlight in dark mode: selected chip uses vibrant Coral container
    val containerBg = if (isSelected) WalletColors.Coral else cardBg
    val contentColor = if (isSelected) Color.White else textColor
    val borderCol = if (isSelected) WalletColors.Coral else borderColor

    Box(
        modifier = modifier
            .clip(WalletShapes.CardMedium)
            .background(containerBg)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = borderCol,
                shape = WalletShapes.CardMedium
            )
            .clickable { onClick() }
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(text = account.iconKey, style = WalletTypography.BodyM)
            Text(
                text = account.name,
                style = WalletTypography.LabelM,
                color = contentColor,
                maxLines = 1
            )
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
