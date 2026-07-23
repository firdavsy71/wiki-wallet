package com.wiki.wallet.feature.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wiki.wallet.core.database.entity.TransactionType
import com.wiki.wallet.domain.model.Account
import com.wiki.wallet.domain.model.Transaction
import com.wiki.wallet.domain.repository.AccountRepository
import com.wiki.wallet.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update

data class AccountDetailUiState(
    val account: Account? = null,
    val transactions: List<Transaction> = emptyList(),
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val isLoading: Boolean = true
)

sealed interface AccountDetailUiEvent {
    data object OnBackClicked : AccountDetailUiEvent
}

class AccountDetailViewModel(
    private val accountId: String,
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AccountDetailUiState())
    val uiState: StateFlow<AccountDetailUiState> = _uiState.asStateFlow()

    init {
        combine(
            accountRepository.observeAllAccounts(),
            transactionRepository.observeAllTransactions()
        ) { accounts, allTransactions ->
            val account = accounts.firstOrNull { it.id == accountId }
            val accountTransactions = allTransactions.filter { it.accountId == accountId }
            val income = accountTransactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
            val expense = accountTransactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }

            _uiState.update {
                it.copy(
                    account = account,
                    transactions = accountTransactions,
                    totalIncome = income,
                    totalExpense = expense,
                    isLoading = false
                )
            }
        }.launchIn(viewModelScope)
    }

    fun onEvent(event: AccountDetailUiEvent) {
        when (event) {
            AccountDetailUiEvent.OnBackClicked -> {}
        }
    }
}
