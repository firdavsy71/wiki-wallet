package com.wiki.wallet.feature.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wiki.wallet.domain.model.Account
import com.wiki.wallet.domain.repository.AccountRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class EditAccountUiState(
    val accountId: String? = null,
    val isEditMode: Boolean = false,
    val nameText: String = "",
    val startingBalanceText: String = "0.00",
    val selectedIcon: String = "💳",
    val selectedCurrency: String = "USD",
    val availableIcons: List<String> = listOf("💳", "💵", "🏦", "🪙", "💰", "📱", "💎", "⭐"),
    val isExecuting: Boolean = false,
    val isCtaEnabled: Boolean = false
)

sealed interface EditAccountUiEvent {
    data class OnNameChanged(val text: String) : EditAccountUiEvent
    data class OnStartingBalanceChanged(val text: String) : EditAccountUiEvent
    data class OnIconSelected(val icon: String) : EditAccountUiEvent
    data class OnCurrencySelected(val currency: String) : EditAccountUiEvent
    data object OnSaveClicked : EditAccountUiEvent
    data object OnDeleteClicked : EditAccountUiEvent
    data object OnBackClicked : EditAccountUiEvent
}

sealed interface EditAccountUiEffect {
    data object SaveSuccess : EditAccountUiEffect
    data object DeleteSuccess : EditAccountUiEffect
    data class Error(val message: String) : EditAccountUiEffect
}

class EditAccountViewModel(
    private val accountId: String? = null,
    private val accountRepository: AccountRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditAccountUiState(accountId = accountId, isEditMode = accountId != null))
    val uiState: StateFlow<EditAccountUiState> = _uiState.asStateFlow()

    private val _effectChannel = Channel<EditAccountUiEffect>()
    val effect = _effectChannel.receiveAsFlow()

    init {
        if (accountId != null) {
            accountRepository.observeAllAccounts()
                .onEach { accounts ->
                    val acc = accounts.firstOrNull { it.id == accountId }
                    if (acc != null) {
                        _uiState.update { state ->
                            state.copy(
                                nameText = if (state.nameText.isEmpty()) acc.name else state.nameText,
                                startingBalanceText = String.format(java.util.Locale.US, "%.2f", acc.startingBalance),
                                selectedIcon = acc.iconKey,
                                selectedCurrency = acc.currency
                            )
                        }
                        validateForm()
                    }
                }
                .launchIn(viewModelScope)
        }
    }

    fun onEvent(event: EditAccountUiEvent) {
        when (event) {
            is EditAccountUiEvent.OnNameChanged -> {
                _uiState.update { it.copy(nameText = event.text) }
                validateForm()
            }
            is EditAccountUiEvent.OnStartingBalanceChanged -> {
                _uiState.update { it.copy(startingBalanceText = event.text) }
                validateForm()
            }
            is EditAccountUiEvent.OnIconSelected -> {
                _uiState.update { it.copy(selectedIcon = event.icon) }
            }
            is EditAccountUiEvent.OnCurrencySelected -> {
                _uiState.update { it.copy(selectedCurrency = event.currency) }
            }
            EditAccountUiEvent.OnSaveClicked -> {
                saveAccount()
            }
            EditAccountUiEvent.OnDeleteClicked -> {
                deleteAccount()
            }
            EditAccountUiEvent.OnBackClicked -> {}
        }
    }

    private fun validateForm() {
        val state = _uiState.value
        val isValid = state.nameText.isNotBlank() && state.startingBalanceText.toDoubleOrNull() != null
        _uiState.update { it.copy(isCtaEnabled = isValid) }
    }

    private fun saveAccount() {
        val state = _uiState.value
        val name = state.nameText.trim()
        val startingBal = state.startingBalanceText.toDoubleOrNull() ?: 0.0

        if (name.isEmpty()) return

        _uiState.update { it.copy(isExecuting = true, isCtaEnabled = false) }

        viewModelScope.launch {
            runCatching {
                val account = Account(
                    id = state.accountId ?: UUID.randomUUID().toString(),
                    name = name,
                    startingBalance = startingBal,
                    currentBalance = startingBal,
                    currency = state.selectedCurrency,
                    iconKey = state.selectedIcon
                )
                if (state.isEditMode) {
                    accountRepository.updateAccount(account)
                } else {
                    accountRepository.addAccount(account)
                }
            }.onSuccess {
                _uiState.update { it.copy(isExecuting = false, isCtaEnabled = true) }
                _effectChannel.send(EditAccountUiEffect.SaveSuccess)
            }.onFailure { err ->
                _uiState.update { it.copy(isExecuting = false, isCtaEnabled = true) }
                _effectChannel.send(EditAccountUiEffect.Error(err.message ?: "Failed to save account"))
            }
        }
    }

    private fun deleteAccount() {
        val id = _uiState.value.accountId ?: return
        _uiState.update { it.copy(isExecuting = true, isCtaEnabled = false) }

        viewModelScope.launch {
            runCatching {
                val account = Account(
                    id = id,
                    name = _uiState.value.nameText,
                    startingBalance = 0.0,
                    currentBalance = 0.0
                )
                accountRepository.deleteAccount(account)
            }.onSuccess {
                _uiState.update { it.copy(isExecuting = false, isCtaEnabled = true) }
                _effectChannel.send(EditAccountUiEffect.DeleteSuccess)
            }.onFailure { err ->
                _uiState.update { it.copy(isExecuting = false, isCtaEnabled = true) }
                _effectChannel.send(EditAccountUiEffect.Error(err.message ?: "Failed to delete account"))
            }
        }
    }
}
