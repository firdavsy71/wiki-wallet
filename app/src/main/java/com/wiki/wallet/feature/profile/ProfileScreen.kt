package com.wiki.wallet.feature.profile

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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wiki.wallet.core.designsystem.theme.WalletColors
import com.wiki.wallet.core.designsystem.theme.WalletShapes
import com.wiki.wallet.core.designsystem.theme.WalletTypography
import com.wiki.wallet.core.util.CurrencyManager

@Composable
fun ProfileRoute(
    onNavigateBack: () -> Unit,
    viewModel: ProfileViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ProfileScreen(
        uiState = uiState,
        onEvent = { event ->
            if (event is ProfileUiEvent.OnBackClicked) {
                onNavigateBack()
            } else {
                viewModel.onEvent(event)
            }
        },
        modifier = modifier
    )
}

@Composable
fun ProfileScreen(
    uiState: ProfileUiState,
    onEvent: (ProfileUiEvent) -> Unit,
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
                        .clickable { onEvent(ProfileUiEvent.OnBackClicked) },
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
                    text = "Profile & Account",
                    style = WalletTypography.TitleM,
                    color = WalletColors.TextPrimary
                )

                Spacer(modifier = Modifier.width(44.dp))
            }

            // User Avatar Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(WalletShapes.CardLarge)
                    .background(WalletColors.Ink)
                    .padding(24.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(WalletColors.InkElevated),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Avatar",
                            tint = WalletColors.MintChip,
                            modifier = Modifier.size(44.dp)
                        )
                    }

                    if (uiState.isEditingName) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            BasicTextField(
                                value = uiState.editedNameText,
                                onValueChange = { onEvent(ProfileUiEvent.OnNameChanged(it)) },
                                textStyle = WalletTypography.TitleM.copy(color = WalletColors.TextOnDark),
                                singleLine = true,
                                cursorBrush = SolidColor(WalletColors.Coral),
                                modifier = Modifier
                                    .clip(WalletShapes.CardMedium)
                                    .background(WalletColors.InkElevated)
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(WalletColors.MintChip)
                                    .clickable { onEvent(ProfileUiEvent.OnSaveNameClicked) },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Save",
                                    tint = WalletColors.Ink,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = uiState.userName,
                                style = WalletTypography.DisplayL,
                                color = WalletColors.TextOnDark
                            )
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Name",
                                tint = WalletColors.TextMuted,
                                modifier = Modifier
                                    .size(18.dp)
                                    .clickable { onEvent(ProfileUiEvent.OnToggleEditName) }
                            )
                        }
                    }

                    Text(
                        text = "ApexBudget Premium User • Active",
                        style = WalletTypography.BodyM,
                        color = WalletColors.TextMuted
                    )
                }
            }

            // Financial Summary Section
            Text(text = "Financial Overview", style = WalletTypography.TitleM, color = WalletColors.TextPrimary)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ProfileStatCard(
                    title = "Total Net Worth",
                    value = CurrencyManager.format(uiState.totalNetWorth),
                    modifier = Modifier.weight(1f)
                )
                ProfileStatCard(
                    title = "Accounts",
                    value = "${uiState.totalAccountsCount} Active",
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ProfileStatCard(
                    title = "Total Activity",
                    value = "${uiState.totalTransactionsCount} Logs",
                    modifier = Modifier.weight(1f)
                )
                ProfileStatCard(
                    title = "Member Since",
                    value = uiState.memberSinceYear,
                    modifier = Modifier.weight(1f)
                )
            }

            // Security Badge
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(WalletShapes.CardMedium)
                    .background(WalletColors.Paper)
                    .border(1.dp, WalletColors.CardBorder, WalletShapes.CardMedium)
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "Shield",
                        tint = WalletColors.MintChip,
                        modifier = Modifier.size(24.dp)
                    )
                    Column {
                        Text(text = "100% Offline & Private", style = WalletTypography.TitleM, color = WalletColors.TextPrimary)
                        Text(text = "No telemetry or external cloud sync enabled.", style = WalletTypography.BodyM, color = WalletColors.TextMuted)
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileStatCard(
    title: String,
    value: String,
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
            Text(text = title, style = WalletTypography.LabelM, color = WalletColors.TextMuted)
            Text(text = value, style = WalletTypography.TitleM, color = WalletColors.TextPrimary)
        }
    }
}
