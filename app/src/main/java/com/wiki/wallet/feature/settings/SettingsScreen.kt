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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.wiki.wallet.core.designsystem.theme.ThemeManager
import com.wiki.wallet.core.designsystem.theme.WalletColors
import com.wiki.wallet.core.designsystem.theme.WalletShapes
import com.wiki.wallet.core.designsystem.theme.WalletTypography

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
                        .clickable { onEvent(SettingsUiEvent.OnBackClicked) },
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
                    text = "Settings",
                    style = WalletTypography.TitleM,
                    color = textColor
                )

                Spacer(modifier = Modifier.width(44.dp))
            }

            // Section 1: Default Currency Selector Card
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = "Default Currency", style = WalletTypography.TitleM, color = textColor)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(WalletShapes.CardMedium)
                        .background(cardBg)
                        .border(1.dp, borderColor, WalletShapes.CardMedium)
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
                                    color = textColor
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
                            tint = textColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            // Section 2: Appearance & Theme Card
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = "Appearance & Theme", style = WalletTypography.TitleM, color = textColor)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(WalletShapes.CardMedium)
                        .background(cardBg)
                        .border(1.dp, borderColor, WalletShapes.CardMedium)
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
                            Icon(imageVector = Icons.Default.Palette, contentDescription = "Theme", tint = textColor, modifier = Modifier.size(20.dp))
                            Text(text = "Color Palette", style = WalletTypography.TitleM, color = textColor)
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            ThemeChip("Dark Ink", uiState.selectedTheme == "Dark Ink", cardBg, textColor) {
                                onEvent(SettingsUiEvent.OnThemeSelected("Dark Ink"))
                            }
                            ThemeChip("Light Paper", uiState.selectedTheme == "Light Paper", cardBg, textColor) {
                                onEvent(SettingsUiEvent.OnThemeSelected("Light Paper"))
                            }
                        }
                    }
                }
            }

            // Section 3: Security & Notifications Toggles
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = "Security & Notifications", style = WalletTypography.TitleM, color = textColor)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(WalletShapes.CardMedium)
                        .background(cardBg)
                        .border(1.dp, borderColor, WalletShapes.CardMedium)
                        .padding(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Lock, contentDescription = "Lock", tint = textColor, modifier = Modifier.size(20.dp))
                                Text(text = "App Lock / Biometric", style = WalletTypography.TitleM, color = textColor)
                            }
                            Switch(
                                checked = uiState.isSecurityLockEnabled,
                                onCheckedChange = { onEvent(SettingsUiEvent.OnSecurityLockToggle(it)) },
                                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = WalletColors.Coral)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Notifications, contentDescription = "Reminder", tint = textColor, modifier = Modifier.size(20.dp))
                                Text(text = "Daily Logging Reminder", style = WalletTypography.TitleM, color = textColor)
                            }
                            Switch(
                                checked = uiState.isDailyReminderEnabled,
                                onCheckedChange = { onEvent(SettingsUiEvent.OnDailyReminderToggle(it)) },
                                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = WalletColors.MintChip)
                            )
                        }
                    }
                }
            }

            // Section 4: Data Management & Reset Card
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = "Data Management", style = WalletTypography.TitleM, color = textColor)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(WalletShapes.CardMedium)
                        .background(WalletColors.Coral.copy(alpha = 0.10f))
                        .border(1.dp, WalletColors.Coral.copy(alpha = 0.3f), WalletShapes.CardMedium)
                        .clickable { onEvent(SettingsUiEvent.OnResetDialogToggle(true)) }
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
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Reset", tint = WalletColors.Coral, modifier = Modifier.size(20.dp))
                            Column {
                                Text(text = "Clear All App Data", style = WalletTypography.TitleM, color = WalletColors.Coral)
                                Text(text = "Wipe all transactions & accounts to start clean.", style = WalletTypography.BodyM, color = WalletColors.TextMuted)
                            }
                        }
                    }
                }
            }

            // Section 5: App Info Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(WalletShapes.CardMedium)
                    .background(cardBg)
                    .border(1.dp, borderColor, WalletShapes.CardMedium)
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
                        Icon(imageVector = Icons.Default.Info, contentDescription = "Info", tint = WalletColors.TextMuted, modifier = Modifier.size(20.dp))
                        Text(text = "App Version", style = WalletTypography.TitleM, color = textColor)
                    }
                    Text(text = "ApexBudget v${uiState.appVersion}", style = WalletTypography.LabelM, color = WalletColors.Coral)
                }
            }
        }
    }

    // Modal Bottom Sheet for Currency Selection List
    if (uiState.isCurrencyPickerOpen) {
        ModalBottomSheet(
            onDismissRequest = { onEvent(SettingsUiEvent.OnCurrencyPickerToggle(false)) },
            sheetState = rememberModalBottomSheetState(),
            containerColor = cardBg
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(text = "Select Country Currency", style = WalletTypography.TitleM, color = textColor)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(WalletShapes.CardMedium)
                        .background(bgColor)
                        .border(1.dp, borderColor, WalletShapes.CardMedium)
                        .padding(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = WalletColors.TextMuted, modifier = Modifier.size(20.dp))
                        BasicTextField(
                            value = uiState.searchQuery,
                            onValueChange = { onEvent(SettingsUiEvent.OnSearchQueryChanged(it)) },
                            textStyle = WalletTypography.BodyM.copy(color = textColor),
                            singleLine = true,
                            cursorBrush = SolidColor(WalletColors.Coral),
                            decorationBox = { innerTextField ->
                                Box {
                                    if (uiState.searchQuery.isEmpty()) {
                                        Text(text = "Search by code or country name...", style = WalletTypography.BodyM, color = WalletColors.TextMuted)
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
                                .background(if (isSelected) WalletColors.InkElevated else bgColor)
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
                                            color = if (isSelected) WalletColors.TextOnDark else textColor
                                        )
                                        Text(
                                            text = "Symbol: ${item.symbol}",
                                            style = WalletTypography.LabelS,
                                            color = WalletColors.TextMuted
                                        )
                                    }
                                }

                                if (isSelected) {
                                    Icon(imageVector = Icons.Default.Check, contentDescription = "Selected", tint = WalletColors.MintChip, modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Reset Confirmation Dialog
    if (uiState.isResetDialogOpen) {
        AlertDialog(
            onDismissRequest = { onEvent(SettingsUiEvent.OnResetDialogToggle(false)) },
            title = { Text(text = "Reset All App Data?", style = WalletTypography.TitleM) },
            text = { Text(text = "This action will delete all logged transactions and accounts permanently. This cannot be undone.", style = WalletTypography.BodyM) },
            confirmButton = {
                TextButton(onClick = { onEvent(SettingsUiEvent.OnConfirmResetData) }) {
                    Text(text = "Clear All Data", color = WalletColors.Coral, style = WalletTypography.TitleM)
                }
            },
            dismissButton = {
                TextButton(onClick = { onEvent(SettingsUiEvent.OnResetDialogToggle(false)) }) {
                    Text(text = "Cancel", style = WalletTypography.LabelM)
                }
            }
        )
    }
}

@Composable
private fun ThemeChip(
    label: String,
    isSelected: Boolean,
    cardBg: Color,
    textColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(WalletShapes.Pill)
            .background(if (isSelected) WalletColors.Coral else cardBg)
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            style = WalletTypography.LabelS,
            color = if (isSelected) Color.White else textColor
        )
    }
}
