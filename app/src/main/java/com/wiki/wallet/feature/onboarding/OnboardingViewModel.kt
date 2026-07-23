package com.wiki.wallet.feature.onboarding

import android.content.Context
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class OnboardingPage(
    val title: String,
    val description: String,
    val iconKey: String
)

data class OnboardingUiState(
    val currentPageIndex: Int = 0,
    val pages: List<OnboardingPage> = listOf(
        OnboardingPage(
            title = "Welcome to ApexBudget",
            description = "Your modern, local-first personal financial manager. Take total control of your cash flow with complete privacy.",
            iconKey = "⚡"
        ),
        OnboardingPage(
            title = "Track Income & Expenses",
            description = "Easily log money in and money out across all your accounts. View real-time daily, weekly, and monthly net balance trends.",
            iconKey = "📊"
        ),
        OnboardingPage(
            title = "Smart Category Budgets",
            description = "Set monthly spending limits for categories like Food, Transport, and Shopping. Monitor progress with clean visual indicators.",
            iconKey = "🎯"
        )
    )
)

sealed interface OnboardingUiEvent {
    data object OnNextPage : OnboardingUiEvent
    data object OnSkip : OnboardingUiEvent
}

class OnboardingViewModel(private val context: Context) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun onEvent(event: OnboardingUiEvent, onComplete: () -> Unit) {
        when (event) {
            OnboardingUiEvent.OnNextPage -> {
                val nextIndex = _uiState.value.currentPageIndex + 1
                if (nextIndex < _uiState.value.pages.size) {
                    _uiState.update { it.copy(currentPageIndex = nextIndex) }
                } else {
                    markOnboardingComplete(onComplete)
                }
            }
            OnboardingUiEvent.OnSkip -> {
                markOnboardingComplete(onComplete)
            }
        }
    }

    private fun markOnboardingComplete(onComplete: () -> Unit) {
        val prefs = context.getSharedPreferences("apexbudget_prefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("onboarding_completed", true).apply()
        onComplete()
    }
}
