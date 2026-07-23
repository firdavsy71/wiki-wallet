package com.wiki.wallet.feature.account

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
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
import com.wiki.wallet.core.designsystem.components.PillButton
import com.wiki.wallet.core.designsystem.components.PillButtonVariant
import com.wiki.wallet.core.designsystem.theme.WalletColors
import com.wiki.wallet.core.designsystem.theme.WalletShapes
import com.wiki.wallet.core.designsystem.theme.WalletTypography

@Composable
fun EditAccountRoute(
    onNavigateBack: () -> Unit,
    viewModel: EditAccountViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(viewModel.effect) {
        viewModel.effect.collect { effect ->
            when (effect) {
                EditAccountUiEffect.SaveSuccess -> {
                    Toast.makeText(context, if (uiState.isEditMode) "Account updated!" else "Account created!", Toast.LENGTH_SHORT).show()
                    onNavigateBack()
                }
                EditAccountUiEffect.DeleteSuccess -> {
                    Toast.makeText(context, "Account deleted!", Toast.LENGTH_SHORT).show()
                    onNavigateBack()
                }
                is EditAccountUiEffect.Error -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    EditAccountScreen(
        uiState = uiState,
        onEvent = { event ->
            if (event is EditAccountUiEvent.OnBackClicked) {
                onNavigateBack()
            } else {
                viewModel.onEvent(event)
            }
        },
        modifier = modifier
    )
}

@Composable
fun EditAccountScreen(
    uiState: EditAccountUiState,
    onEvent: (EditAccountUiEvent) -> Unit,
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
            // Header Row
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
                        .clickable { onEvent(EditAccountUiEvent.OnBackClicked) },
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
                    text = if (uiState.isEditMode) "Edit Account" else "New Account",
                    style = WalletTypography.TitleM,
                    color = WalletColors.TextPrimary
                )

                Spacer(modifier = Modifier.width(44.dp))
            }

            // Icon Picker Row
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = "Account Icon", style = WalletTypography.LabelM, color = WalletColors.TextMuted)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    uiState.availableIcons.forEach { icon ->
                        val isSelected = icon == uiState.selectedIcon
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) WalletColors.Ink else WalletColors.Paper)
                                .clickable { onEvent(EditAccountUiEvent.OnIconSelected(icon)) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = icon, style = WalletTypography.TitleM)
                        }
                    }
                }
            }

            // Name Field
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(text = "Account Name", style = WalletTypography.LabelM, color = WalletColors.TextMuted)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(WalletShapes.CardMedium)
                        .background(WalletColors.Paper)
                        .border(1.dp, WalletColors.CardBorder, WalletShapes.CardMedium)
                        .padding(14.dp)
                ) {
                    BasicTextField(
                        value = uiState.nameText,
                        onValueChange = { onEvent(EditAccountUiEvent.OnNameChanged(it)) },
                        textStyle = WalletTypography.BodyM.copy(color = WalletColors.TextPrimary),
                        singleLine = true,
                        cursorBrush = SolidColor(WalletColors.Coral),
                        decorationBox = { innerTextField ->
                            Box {
                                if (uiState.nameText.isEmpty()) {
                                    Text(text = "e.g. Main Card, Savings Vault...", style = WalletTypography.BodyM, color = WalletColors.TextMuted)
                                }
                                innerTextField()
                            }
                        }
                    )
                }
            }

            // Starting Balance Field
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(text = "Starting Balance", style = WalletTypography.LabelM, color = WalletColors.TextMuted)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(WalletShapes.CardMedium)
                        .background(WalletColors.Paper)
                        .border(1.dp, WalletColors.CardBorder, WalletShapes.CardMedium)
                        .padding(14.dp)
                ) {
                    BasicTextField(
                        value = uiState.startingBalanceText,
                        onValueChange = { onEvent(EditAccountUiEvent.OnStartingBalanceChanged(it)) },
                        textStyle = WalletTypography.BodyM.copy(color = WalletColors.TextPrimary),
                        singleLine = true,
                        cursorBrush = SolidColor(WalletColors.Coral),
                        decorationBox = { innerTextField ->
                            Box {
                                if (uiState.startingBalanceText.isEmpty()) {
                                    Text(text = "0.00", style = WalletTypography.BodyM, color = WalletColors.TextMuted)
                                }
                                innerTextField()
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Save CTA Button
            PillButton(
                text = if (uiState.isExecuting) "Saving..." else if (uiState.isEditMode) "Save Account Details" else "Create Account",
                onClick = { onEvent(EditAccountUiEvent.OnSaveClicked) },
                enabled = uiState.isCtaEnabled && !uiState.isExecuting,
                variant = PillButtonVariant.Primary,
                height = 56.dp,
                modifier = Modifier.fillMaxWidth()
            )

            // Delete Button (if editing)
            if (uiState.isEditMode) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(WalletShapes.Pill)
                        .background(WalletColors.Coral.copy(alpha = 0.12f))
                        .clickable { onEvent(EditAccountUiEvent.OnDeleteClicked) }
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
                            text = "Delete Account",
                            style = WalletTypography.TitleM,
                            color = WalletColors.Coral
                        )
                    }
                }
            }
        }
    }
}
