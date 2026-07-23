package com.wiki.wallet.feature.onboarding

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wiki.wallet.core.designsystem.components.PillButton
import com.wiki.wallet.core.designsystem.components.PillButtonVariant
import com.wiki.wallet.core.designsystem.theme.WalletColors
import com.wiki.wallet.core.designsystem.theme.WalletShapes
import com.wiki.wallet.core.designsystem.theme.WalletTypography

@Composable
fun OnboardingRoute(
    onComplete: () -> Unit,
    viewModel: OnboardingViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    OnboardingScreen(
        uiState = uiState,
        onEvent = { event -> viewModel.onEvent(event, onComplete) },
        modifier = modifier
    )
}

@Composable
fun OnboardingScreen(
    uiState: OnboardingUiState,
    onEvent: (OnboardingUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentPage = uiState.pages.getOrNull(uiState.currentPageIndex) ?: uiState.pages.first()
    val isLastPage = uiState.currentPageIndex == uiState.pages.lastIndex

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(WalletColors.Ink)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Bar with Skip Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                if (!isLastPage) {
                    Text(
                        text = "Skip",
                        style = WalletTypography.LabelM,
                        color = WalletColors.TextMuted,
                        modifier = Modifier
                            .clickable { onEvent(OnboardingUiEvent.OnSkip) }
                            .padding(8.dp)
                    )
                }
            }

            // Main Content Area
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Large Icon Circle
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(WalletColors.InkElevated),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = currentPage.iconKey,
                        style = WalletTypography.DisplayXL
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = currentPage.title,
                    style = WalletTypography.DisplayXL,
                    color = WalletColors.TextOnDark
                )

                Text(
                    text = currentPage.description,
                    style = WalletTypography.BodyM,
                    color = WalletColors.TextMuted,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            // Bottom Indicators and Action Button
            Column(
                verticalArrangement = Arrangement.spacedBy(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Page Indicator Dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    uiState.pages.forEachIndexed { index, _ ->
                        val isSelected = index == uiState.currentPageIndex
                        Box(
                            modifier = Modifier
                                .height(8.dp)
                                .width(if (isSelected) 24.dp else 8.dp)
                                .clip(WalletShapes.Pill)
                                .background(if (isSelected) WalletColors.Coral else WalletColors.TextMutedDark)
                        )
                    }
                }

                // CTA Button
                PillButton(
                    text = if (isLastPage) "Get Started 🚀" else "Continue",
                    onClick = { onEvent(OnboardingUiEvent.OnNextPage) },
                    variant = PillButtonVariant.Primary,
                    height = 56.dp,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
