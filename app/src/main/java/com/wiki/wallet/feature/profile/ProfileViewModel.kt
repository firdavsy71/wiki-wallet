package com.wiki.wallet.feature.profile

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wiki.wallet.core.util.CurrencyManager
import com.wiki.wallet.domain.repository.AccountRepository
import com.wiki.wallet.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.update

data class ProfileUiState(
    val userName: String = "Apex User",
    val isEditingName: Boolean = false,
    val editedNameText: String = "",
    val totalAccountsCount: Int = 0,
    val totalTransactionsCount: Int = 0,
    val totalNetWorth: Double = 0.0,
    val currencySymbol: String = "$",
    val memberSinceYear: String = "2026"
)

sealed interface ProfileUiEvent {
    data class OnNameChanged(val text: String) : ProfileUiEvent
    data object OnToggleEditName : ProfileUiEvent
    data object OnSaveNameClicked : ProfileUiEvent
    data object OnBackClicked : ProfileUiEvent
}

class ProfileViewModel(
    private val context: Context,
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        val prefs = context.getSharedPreferences("apexbudget_prefs", Context.MODE_PRIVATE)
        val savedName = prefs.getString("user_name", "Apex User") ?: "Apex User"

        _uiState.update { it.copy(userName = savedName, editedNameText = savedName) }

        combine(
            accountRepository.observeAllAccounts(),
            transactionRepository.observeAllTransactions(),
            CurrencyManager.currentCurrencySymbol
        ) { accounts, transactions, symbol ->
            val netWorth = accounts.sumOf { it.currentBalance }
            _uiState.update {
                it.copy(
                    totalAccountsCount = accounts.size,
                    totalTransactionsCount = transactions.size,
                    totalNetWorth = netWorth,
                    currencySymbol = symbol
                )
            }
        }.launchIn(viewModelScope)
    }

    fun onEvent(event: ProfileUiEvent) {
        when (event) {
            is ProfileUiEvent.OnNameChanged -> {
                _uiState.update { it.copy(editedNameText = event.text) }
            }
            ProfileUiEvent.OnToggleEditName -> {
                _uiState.update { it.copy(isEditingName = !it.isEditingName) }
            }
            ProfileUiEvent.OnSaveNameClicked -> {
                val newName = _uiState.value.editedNameText.ifBlank { "Apex User" }
                val prefs = context.getSharedPreferences("apexbudget_prefs", Context.MODE_PRIVATE)
                prefs.edit().putString("user_name", newName).apply()
                _uiState.update {
                    it.copy(
                        userName = newName,
                        isEditingName = false
                    )
                }
            }
            ProfileUiEvent.OnBackClicked -> {}
        }
    }
}
