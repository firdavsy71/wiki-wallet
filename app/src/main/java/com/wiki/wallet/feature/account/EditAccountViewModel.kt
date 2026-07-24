package com.wiki.wallet.feature.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wiki.wallet.core.database.entity.AccountType
import com.wiki.wallet.domain.model.Account
import com.wiki.wallet.domain.repository.AccountRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class EditAccountUiState(
    val isEditMode: Boolean = false,
    val accountId: String? = null,
    val nameText: String = "",
    val startingBalanceText: String = "",
    val selectedIcon: String = "💳",
    val selectedType: AccountType = AccountType.BANK,
    val availableIcons: List<String> = listOf("💳", "🏦", "💵", "🏠", "🚗", "💼", "📈", "💎", "💳"),
    val isExecuting: Boolean = false
) {
    val isCtaEnabled: Boolean
        get() = nameText.isNotBlank() && startingBalanceText.toDoubleOrNull() != null
}

sealed interface EditAccountUiEvent {
    data class OnNameChanged(val name: String) : EditAccountUiEvent
    data class OnStartingBalanceChanged(val balance: String) : EditAccountUiEvent
    data class OnIconSelected(val icon: String) : EditAccountUiEvent
    data class OnTypeSelected(val type: AccountType) : EditAccountUiEvent
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

    private val _uiState = MutableStateFlow(EditAccountUiState(isEditMode = accountId != null, accountId = accountId))
    val uiState: StateFlow<EditAccountUiState> = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<EditAccountUiEffect>()
    val effect: SharedFlow<EditAccountUiEffect> = _effect.asSharedFlow()

    init {
        if (accountId != null) {
            viewModelScope.launch {
                val accounts = accountRepository.observeAllAccounts().firstOrNull() ?: emptyList()
                val target = accounts.firstOrNull { it.id == accountId }
                if (target != null) {
                    _uiState.update {
                        it.copy(
                            nameText = target.name,
                            startingBalanceText = target.startingBalance.toString(),
                            selectedIcon = target.iconKey,
                            selectedType = target.type
                        )
                    }
                }
            }
        }
    }

    fun onEvent(event: EditAccountUiEvent) {
        when (event) {
            is EditAccountUiEvent.OnNameChanged -> {
                _uiState.update { it.copy(nameText = event.name) }
            }
            is EditAccountUiEvent.OnStartingBalanceChanged -> {
                _uiState.update { it.copy(startingBalanceText = event.balance) }
            }
            is EditAccountUiEvent.OnIconSelected -> {
                _uiState.update { it.copy(selectedIcon = event.icon) }
            }
            is EditAccountUiEvent.OnTypeSelected -> {
                _uiState.update { it.copy(selectedType = event.type) }
            }
            EditAccountUiEvent.OnSaveClicked -> saveAccount()
            EditAccountUiEvent.OnDeleteClicked -> deleteAccount()
            EditAccountUiEvent.OnBackClicked -> {}
        }
    }

    private fun saveAccount() {
        val state = _uiState.value
        val name = state.nameText.trim()
        val balance = state.startingBalanceText.toDoubleOrNull() ?: 0.0

        if (name.isBlank()) {
            viewModelScope.launch { _effect.emit(EditAccountUiEffect.Error("Account name cannot be empty.")) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isExecuting = true) }
            try {
                if (state.isEditMode && state.accountId != null) {
                    val existing = accountRepository.observeAllAccounts().firstOrNull()?.firstOrNull { it.id == state.accountId }
                    if (existing != null) {
                        val updated = existing.copy(
                            name = name,
                            startingBalance = balance,
                            iconKey = state.selectedIcon,
                            type = state.selectedType
                        )
                        accountRepository.updateAccount(updated)
                    }
                } else {
                    val newAccount = Account(
                        id = UUID.randomUUID().toString(),
                        name = name,
                        startingBalance = balance,
                        currentBalance = balance,
                        currency = "USD",
                        iconKey = state.selectedIcon,
                        displayOrder = 99,
                        type = state.selectedType
                    )
                    accountRepository.addAccount(newAccount)
                }
                _effect.emit(EditAccountUiEffect.SaveSuccess)
            } catch (e: Exception) {
                _effect.emit(EditAccountUiEffect.Error(e.message ?: "Failed to save account."))
            } finally {
                _uiState.update { it.copy(isExecuting = false) }
            }
        }
    }

    private fun deleteAccount() {
        val state = _uiState.value
        if (!state.isEditMode || state.accountId == null) return

        viewModelScope.launch {
            _uiState.update { it.copy(isExecuting = true) }
            try {
                val existing = accountRepository.observeAllAccounts().firstOrNull()?.firstOrNull { it.id == state.accountId }
                if (existing != null) {
                    accountRepository.deleteAccount(existing)
                    _effect.emit(EditAccountUiEffect.DeleteSuccess)
                }
            } catch (e: Exception) {
                _effect.emit(EditAccountUiEffect.Error(e.message ?: "Failed to delete account."))
            } finally {
                _uiState.update { it.copy(isExecuting = false) }
            }
        }
    }
}
