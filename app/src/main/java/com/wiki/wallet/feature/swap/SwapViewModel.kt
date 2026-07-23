package com.wiki.wallet.feature.swap

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wiki.wallet.core.database.entity.TransactionType
import com.wiki.wallet.domain.model.Account
import com.wiki.wallet.domain.model.Category
import com.wiki.wallet.domain.model.Transaction
import com.wiki.wallet.domain.repository.AccountRepository
import com.wiki.wallet.domain.repository.TransactionRepository
import com.wiki.wallet.domain.usecase.AddTransactionUseCase
import com.wiki.wallet.domain.usecase.GetCategoriesUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SwapUiState(
    val transactionId: String? = null,
    val isEditMode: Boolean = false,
    val type: TransactionType = TransactionType.EXPENSE,
    val amountText: String = "",
    val noteText: String = "",
    val selectedCategory: Category? = null,
    val selectedAccount: Account? = null,
    val categories: List<Category> = emptyList(),
    val accounts: List<Account> = emptyList(),
    val isCategoryPickerOpen: Boolean = false,
    val isExecuting: Boolean = false,
    val isCtaEnabled: Boolean = false,
    val budgetProgressRatio: Float = 0.0f,
    val existingTransaction: Transaction? = null
)

sealed interface SwapUiEvent {
    data class OnTypeChanged(val type: TransactionType) : SwapUiEvent
    data class OnAmountChanged(val text: String) : SwapUiEvent
    data class OnNoteChanged(val text: String) : SwapUiEvent
    data class OnCategorySelected(val category: Category) : SwapUiEvent
    data class OnAccountSelected(val account: Account) : SwapUiEvent
    data class OnCategoryPickerToggle(val isOpen: Boolean) : SwapUiEvent
    data object OnSaveClicked : SwapUiEvent
    data object OnDeleteClicked : SwapUiEvent
    data object OnBackClicked : SwapUiEvent
}

sealed interface SwapUiEffect {
    data object SaveSuccess : SwapUiEffect
    data object DeleteSuccess : SwapUiEffect
    data class Error(val message: String) : SwapUiEffect
    data class Toast(val text: String) : SwapUiEffect
}

class SwapViewModel(
    private val transactionId: String? = null,
    private val addTransactionUseCase: AddTransactionUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SwapUiState(transactionId = transactionId, isEditMode = transactionId != null))
    val uiState: StateFlow<SwapUiState> = _uiState.asStateFlow()

    private val _effectChannel = Channel<SwapUiEffect>()
    val effect = _effectChannel.receiveAsFlow()

    init {
        combine(
            getCategoriesUseCase(),
            accountRepository.observeAllAccounts(),
            transactionRepository.observeAllTransactions()
        ) { categories, accounts, allTransactions ->
            val existingTx = if (transactionId != null) allTransactions.firstOrNull { it.id == transactionId } else null
            
            val activeType = existingTx?.type ?: _uiState.value.type
            val matchingCat = existingTx?.let { tx -> categories.firstOrNull { it.id == tx.categoryId } }
                ?: categories.firstOrNull { it.type == activeType }
            val matchingAcc = existingTx?.let { tx -> accounts.firstOrNull { it.id == tx.accountId } }
                ?: accounts.firstOrNull()

            _uiState.update { state ->
                state.copy(
                    type = activeType,
                    amountText = if (state.amountText.isEmpty() && existingTx != null) String.format(java.util.Locale.US, "%.2f", existingTx.amount) else state.amountText,
                    noteText = if (state.noteText.isEmpty() && existingTx != null) (existingTx.note ?: "") else state.noteText,
                    categories = categories,
                    accounts = accounts,
                    selectedCategory = state.selectedCategory ?: matchingCat,
                    selectedAccount = state.selectedAccount ?: matchingAcc,
                    budgetProgressRatio = matchingCat?.budgetProgressRatio ?: 0f,
                    existingTransaction = existingTx
                )
            }
            validateForm()
        }.launchIn(viewModelScope)
    }

    fun onEvent(event: SwapUiEvent) {
        when (event) {
            is SwapUiEvent.OnTypeChanged -> {
                val matchingCat = _uiState.value.categories.firstOrNull { it.type == event.type }
                _uiState.update {
                    it.copy(
                        type = event.type,
                        selectedCategory = matchingCat,
                        budgetProgressRatio = matchingCat?.budgetProgressRatio ?: 0f
                    )
                }
                validateForm()
            }
            is SwapUiEvent.OnAmountChanged -> {
                _uiState.update { it.copy(amountText = event.text) }
                validateForm()
            }
            is SwapUiEvent.OnNoteChanged -> {
                _uiState.update { it.copy(noteText = event.text) }
            }
            is SwapUiEvent.OnCategorySelected -> {
                _uiState.update {
                    it.copy(
                        selectedCategory = event.category,
                        isCategoryPickerOpen = false,
                        budgetProgressRatio = event.category.budgetProgressRatio
                    )
                }
                validateForm()
            }
            is SwapUiEvent.OnAccountSelected -> {
                _uiState.update { it.copy(selectedAccount = event.account) }
                validateForm()
            }
            is SwapUiEvent.OnCategoryPickerToggle -> {
                _uiState.update { it.copy(isCategoryPickerOpen = event.isOpen) }
            }
            SwapUiEvent.OnSaveClicked -> {
                saveTransaction()
            }
            SwapUiEvent.OnDeleteClicked -> {
                deleteTransaction()
            }
            SwapUiEvent.OnBackClicked -> {}
        }
    }

    private fun validateForm() {
        val state = _uiState.value
        val amount = state.amountText.toDoubleOrNull()
        val isValid = amount != null && amount > 0.0 && state.selectedCategory != null && state.selectedAccount != null
        _uiState.update { it.copy(isCtaEnabled = isValid) }
    }

    private fun saveTransaction() {
        val state = _uiState.value
        val amount = state.amountText.toDoubleOrNull() ?: return
        val category = state.selectedCategory ?: return
        val account = state.selectedAccount ?: return

        _uiState.update { it.copy(isExecuting = true, isCtaEnabled = false) }

        viewModelScope.launch {
            runCatching {
                if (state.isEditMode && state.existingTransaction != null) {
                    val existing = state.existingTransaction
                    transactionRepository.updateTransaction(
                        id = existing.id,
                        amount = amount,
                        type = state.type,
                        categoryId = category.id,
                        accountId = account.id,
                        note = state.noteText.ifBlank { null },
                        dateMillis = existing.date,
                        createdAt = existing.createdAt
                    )
                } else {
                    addTransactionUseCase(
                        amount = amount,
                        type = state.type,
                        categoryId = category.id,
                        accountId = account.id,
                        note = state.noteText.ifBlank { null }
                    )
                }
            }.onSuccess {
                _uiState.update { it.copy(isExecuting = false, isCtaEnabled = true) }
                _effectChannel.send(SwapUiEffect.SaveSuccess)
            }.onFailure { err ->
                _uiState.update { it.copy(isExecuting = false, isCtaEnabled = true) }
                _effectChannel.send(SwapUiEffect.Error(err.message ?: "Failed to save transaction"))
            }
        }
    }

    private fun deleteTransaction() {
        val existing = _uiState.value.existingTransaction ?: return
        _uiState.update { it.copy(isExecuting = true, isCtaEnabled = false) }

        viewModelScope.launch {
            runCatching {
                transactionRepository.deleteTransaction(existing)
            }.onSuccess {
                _uiState.update { it.copy(isExecuting = false, isCtaEnabled = true) }
                _effectChannel.send(SwapUiEffect.DeleteSuccess)
            }.onFailure { err ->
                _uiState.update { it.copy(isExecuting = false, isCtaEnabled = true) }
                _effectChannel.send(SwapUiEffect.Error(err.message ?: "Failed to delete transaction"))
            }
        }
    }
}
