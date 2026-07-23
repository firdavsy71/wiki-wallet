package com.wiki.wallet.feature.settings

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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wiki.wallet.core.designsystem.theme.WalletColors
import com.wiki.wallet.core.designsystem.theme.WalletShapes
import com.wiki.wallet.core.designsystem.theme.WalletTypography
import java.util.Locale

@Composable
fun SettingsRoute(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    SettingsScreen(
        uiState = uiState,
        onEvent = { event ->
            if (event is SettingsUiEvent.OnBackClicked) {
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
fun SettingsScreen(
    uiState: SettingsUiState,
    onEvent: (SettingsUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedItem = uiState.currencies.firstOrNull { it.code == uiState.selectedCurrency }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(WalletColors.PaperPure)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
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
                        .background(WalletColors.Paper)
                        .clickable { onEvent(SettingsUiEvent.OnBackClicked) },
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
                    text = "Settings",
                    style = WalletTypography.TitleM,
                    color = WalletColors.TextPrimary
                )

                Spacer(modifier = Modifier.width(44.dp))
            }

            // Section 1: Default Currency Selector Card
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Default Currency",
                    style = WalletTypography.TitleM,
                    color = WalletColors.TextPrimary
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(WalletShapes.CardMedium)
                        .background(WalletColors.Paper)
                        .border(1.dp, WalletColors.CardBorder, WalletShapes.CardMedium)
                        .clickable { onEvent(SettingsUiEvent.OnCurrencyPickerToggle(true)) }
                        .padding(16.dp)
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
                            Text(text = selectedItem?.flag ?: "🌐", style = WalletTypography.TitleM)
                            Column {
                                Text(
                                    text = "${selectedItem?.code} - ${selectedItem?.name ?: "Currency"}",
                                    style = WalletTypography.TitleM,
                                    color = WalletColors.TextPrimary
                                )
                                Text(
                                    text = "Symbol: ${selectedItem?.symbol ?: "$"}",
                                    style = WalletTypography.LabelS,
                                    color = WalletColors.TextMuted
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Select Currency",
                            tint = WalletColors.Ink,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            // Section 2: Account Overview
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Configured Accounts",
                    style = WalletTypography.TitleM,
                    color = WalletColors.TextPrimary
                )

                uiState.accounts.forEach { account ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(WalletShapes.CardMedium)
                            .background(WalletColors.Paper)
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
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(text = account.iconKey, style = WalletTypography.TitleM)
                                Text(text = account.name, style = WalletTypography.TitleM, color = WalletColors.TextPrimary)
                            }
                            Text(
                                text = "$${String.format(Locale.US, "%,.2f", account.currentBalance)}",
                                style = WalletTypography.TitleM,
                                color = WalletColors.TextPrimary
                            )
                        }
                    }
                }
            }

            // Section 3: Privacy & Security Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(WalletShapes.CardLarge)
                    .background(WalletColors.Ink)
                    .padding(18.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(WalletColors.InkElevated),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Security",
                            tint = WalletColors.MintChip,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "Local-First & Offline Storage",
                            style = WalletTypography.TitleM,
                            color = WalletColors.TextOnDark
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Your transactions are stored 100% locally on your device with no external server tracking.",
                            style = WalletTypography.BodyM,
                            color = WalletColors.TextMuted
                        )
                    }
                }
            }

            // Section 4: App Info Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(WalletShapes.CardMedium)
                    .background(WalletColors.Paper)
                    .border(1.dp, WalletColors.CardBorder, WalletShapes.CardMedium)
                    .padding(16.dp)
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
                            imageVector = Icons.Default.Info,
                            contentDescription = "Info",
                            tint = WalletColors.TextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(text = "App Version", style = WalletTypography.TitleM, color = WalletColors.TextPrimary)
                    }
                    Text(
                        text = "ApexBudget v${uiState.appVersion}",
                        style = WalletTypography.LabelM,
                        color = WalletColors.Coral
                    )
                }
            }
        }
    }

    // Modal Bottom Sheet for Currency Selection List
    if (uiState.isCurrencyPickerOpen) {
        ModalBottomSheet(
            onDismissRequest = { onEvent(SettingsUiEvent.OnCurrencyPickerToggle(false)) },
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
                    text = "Select Country Currency",
                    style = WalletTypography.TitleM,
                    color = WalletColors.TextPrimary
                )

                // Search Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(WalletShapes.CardMedium)
                        .background(WalletColors.Paper)
                        .padding(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = WalletColors.TextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                        BasicTextField(
                            value = uiState.searchQuery,
                            onValueChange = { onEvent(SettingsUiEvent.OnSearchQueryChanged(it)) },
                            textStyle = WalletTypography.BodyM.copy(color = WalletColors.TextPrimary),
                            singleLine = true,
                            cursorBrush = SolidColor(WalletColors.Coral),
                            decorationBox = { innerTextField ->
                                Box {
                                    if (uiState.searchQuery.isEmpty()) {
                                        Text(
                                            text = "Search by code or country name...",
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

                val filteredCurrencies = uiState.currencies.filter {
                    it.code.contains(uiState.searchQuery, ignoreCase = true) ||
                    it.name.contains(uiState.searchQuery, ignoreCase = true)
                }

                LazyColumn(
                    modifier = Modifier.height(350.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredCurrencies) { item ->
                        val isSelected = item.code == uiState.selectedCurrency

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(WalletShapes.CardMedium)
                                .background(if (isSelected) WalletColors.Ink else WalletColors.Paper)
                                .clickable { onEvent(SettingsUiEvent.OnCurrencySelected(item.code)) }
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
                                    Text(text = item.flag, style = WalletTypography.TitleM)
                                    Column {
                                        Text(
                                            text = "${item.code} - ${item.name}",
                                            style = WalletTypography.TitleM,
                                            color = if (isSelected) WalletColors.TextOnDark else WalletColors.TextPrimary
                                        )
                                        Text(
                                            text = "Symbol: ${item.symbol}",
                                            style = WalletTypography.LabelS,
                                            color = if (isSelected) WalletColors.TextMuted else WalletColors.TextMuted
                                        )
                                    }
                                }

                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = WalletColors.MintChip,
                                        modifier = Modifier.size(20.dp)
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
