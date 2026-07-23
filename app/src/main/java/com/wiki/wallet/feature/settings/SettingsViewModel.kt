package com.wiki.wallet.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wiki.wallet.domain.model.Account
import com.wiki.wallet.domain.repository.AccountRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update

data class SettingsUiState(
    val selectedCurrency: String = "USD",
    val accounts: List<Account> = emptyList(),
    val appVersion: String = "1.1.0",
    val availableCurrencies: List<String> = listOf("USD", "EUR", "GBP", "JPY", "CAD", "AUD")
)

sealed interface SettingsUiEvent {
    data class OnCurrencySelected(val currency: String) : SettingsUiEvent
    data object OnBackClicked : SettingsUiEvent
}

class SettingsViewModel(
    private val accountRepository: AccountRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        accountRepository.observeAllAccounts()
            .onEach { accounts ->
                _uiState.update { it.copy(accounts = accounts) }
            }
            .launchIn(viewModelScope)
    }

    fun onEvent(event: SettingsUiEvent) {
        when (event) {
            is SettingsUiEvent.OnCurrencySelected -> {
                _uiState.update { it.copy(selectedCurrency = event.currency) }
            }
            SettingsUiEvent.OnBackClicked -> {}
        }
    }
}
