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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
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

@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    onEvent: (SettingsUiEvent) -> Unit,
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

            // Section 1: Default Currency
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Default Currency",
                    style = WalletTypography.TitleM,
                    color = WalletColors.TextPrimary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    uiState.availableCurrencies.forEach { curr ->
                        val isSelected = curr == uiState.selectedCurrency
                        Box(
                            modifier = Modifier
                                .clip(WalletShapes.Pill)
                                .background(if (isSelected) WalletColors.Ink else WalletColors.Paper)
                                .border(1.dp, if (isSelected) WalletColors.Ink else WalletColors.CardBorder, WalletShapes.Pill)
                                .clickable { onEvent(SettingsUiEvent.OnCurrencySelected(curr)) }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                                Text(
                                    text = curr,
                                    style = WalletTypography.LabelM,
                                    color = if (isSelected) Color.White else WalletColors.TextPrimary
                                )
                            }
                        }
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
                        text = "v${uiState.appVersion}",
                        style = WalletTypography.LabelM,
                        color = WalletColors.Coral
                    )
                }
            }
        }
    }
}
