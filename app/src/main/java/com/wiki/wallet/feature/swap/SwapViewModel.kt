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
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SwapUiState(
    val isEditMode: Boolean = false,
    val transactionId: String? = null,
    val amountText: String = "",
    val type: TransactionType = TransactionType.EXPENSE,
    val selectedCategory: Category? = null,
    val selectedAccount: Account? = null,
    val noteText: String = "",
    val categories: List<Category> = emptyList(),
    val accounts: List<Account> = emptyList(),
    val isCategoryPickerOpen: Boolean = false,
    val isBill: Boolean = false,
    val dueDateMillis: Long? = null,
    val isExecuting: Boolean = false,
    val originalCreatedAt: Long = System.currentTimeMillis()
) {
    val isCtaEnabled: Boolean
        get() = amountText.toDoubleOrNull() != null &&
                (amountText.toDoubleOrNull() ?: 0.0) > 0.0 &&
                selectedCategory != null &&
                selectedAccount != null
}

sealed interface SwapUiEvent {
    data class OnAmountChanged(val amount: String) : SwapUiEvent
    data class OnTypeChanged(val type: TransactionType) : SwapUiEvent
    data class OnCategorySelected(val category: Category) : SwapUiEvent
    data class OnAccountSelected(val account: Account) : SwapUiEvent
    data class OnNoteChanged(val note: String) : SwapUiEvent
    data class OnIsBillToggled(val isBill: Boolean) : SwapUiEvent
    data class OnDueDateChanged(val millis: Long) : SwapUiEvent
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

    private val _uiState = MutableStateFlow(SwapUiState(isEditMode = transactionId != null, transactionId = transactionId))
    val uiState: StateFlow<SwapUiState> = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<SwapUiEffect>()
    val effect: SharedFlow<SwapUiEffect> = _effect.asSharedFlow()

    init {
        accountRepository.observeAllAccounts()
            .onEach { accountsList ->
                _uiState.update { state ->
                    val defaultAccount = state.selectedAccount ?: accountsList.firstOrNull()
                    state.copy(
                        accounts = accountsList,
                        selectedAccount = defaultAccount
                    )
                }
            }
            .launchIn(viewModelScope)

        getCategoriesUseCase()
            .onEach { categoriesList ->
                _uiState.update { state ->
                    val filtered = categoriesList.filter { it.type == state.type }
                    val defaultCategory = state.selectedCategory ?: filtered.firstOrNull()
                    state.copy(
                        categories = categoriesList,
                        selectedCategory = defaultCategory
                    )
                }
            }
            .launchIn(viewModelScope)

        if (transactionId != null) {
            viewModelScope.launch {
                val allTxs = transactionRepository.observeAllTransactions().firstOrNull() ?: emptyList()
                val target = allTxs.firstOrNull { it.id == transactionId }
                if (target != null) {
                    val allAccounts = accountRepository.observeAllAccounts().firstOrNull() ?: emptyList()
                    val allCategories = getCategoriesUseCase().firstOrNull() ?: emptyList()

                    _uiState.update {
                        it.copy(
                            amountText = target.amount.toString(),
                            type = target.type,
                            selectedCategory = allCategories.firstOrNull { cat -> cat.id == target.categoryId },
                            selectedAccount = allAccounts.firstOrNull { acc -> acc.id == target.accountId },
                            noteText = target.note ?: "",
                            isBill = target.isBill,
                            dueDateMillis = target.dueDate,
                            originalCreatedAt = target.createdAt
                        )
                    }
                }
            }
        }
    }

    fun onEvent(event: SwapUiEvent) {
        when (event) {
            is SwapUiEvent.OnAmountChanged -> {
                _uiState.update { it.copy(amountText = event.amount) }
            }
            is SwapUiEvent.OnTypeChanged -> {
                _uiState.update { state ->
                    val filtered = state.categories.filter { it.type == event.type }
                    state.copy(
                        type = event.type,
                        selectedCategory = filtered.firstOrNull()
                    )
                }
            }
            is SwapUiEvent.OnCategorySelected -> {
                _uiState.update { it.copy(selectedCategory = event.category, isCategoryPickerOpen = false) }
            }
            is SwapUiEvent.OnAccountSelected -> {
                _uiState.update { it.copy(selectedAccount = event.account) }
            }
            is SwapUiEvent.OnNoteChanged -> {
                _uiState.update { it.copy(noteText = event.note) }
            }
            is SwapUiEvent.OnIsBillToggled -> {
                _uiState.update { it.copy(isBill = event.isBill) }
            }
            is SwapUiEvent.OnDueDateChanged -> {
                _uiState.update { it.copy(dueDateMillis = event.millis) }
            }
            is SwapUiEvent.OnCategoryPickerToggle -> {
                _uiState.update { it.copy(isCategoryPickerOpen = event.isOpen) }
            }
            SwapUiEvent.OnSaveClicked -> saveTransaction()
            SwapUiEvent.OnDeleteClicked -> deleteTransaction()
            SwapUiEvent.OnBackClicked -> {}
        }
    }

    private fun saveTransaction() {
        val state = _uiState.value
        val amount = state.amountText.toDoubleOrNull() ?: return
        val category = state.selectedCategory ?: return
        val account = state.selectedAccount ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isExecuting = true) }
            try {
                if (state.isEditMode && state.transactionId != null) {
                    transactionRepository.updateTransaction(
                        id = state.transactionId,
                        amount = amount,
                        type = state.type,
                        categoryId = category.id,
                        accountId = account.id,
                        note = state.noteText.ifBlank { null },
                        dateMillis = System.currentTimeMillis(),
                        createdAt = state.originalCreatedAt,
                        isBill = state.isBill,
                        dueDate = state.dueDateMillis ?: System.currentTimeMillis(),
                        isPaid = !state.isBill
                    )
                } else {
                    addTransactionUseCase(
                        amount = amount,
                        type = state.type,
                        categoryId = category.id,
                        accountId = account.id,
                        note = state.noteText.ifBlank { null },
                        dateMillis = System.currentTimeMillis()
                    )
                }
                _effect.emit(SwapUiEffect.SaveSuccess)
            } catch (e: Exception) {
                _effect.emit(SwapUiEffect.Error(e.message ?: "Failed to save transaction."))
            } finally {
                _uiState.update { it.copy(isExecuting = false) }
            }
        }
    }

    private fun deleteTransaction() {
        val state = _uiState.value
        if (!state.isEditMode || state.transactionId == null) return

        viewModelScope.launch {
            _uiState.update { it.copy(isExecuting = true) }
            try {
                val allTxs = transactionRepository.observeAllTransactions().firstOrNull() ?: emptyList()
                val target = allTxs.firstOrNull { it.id == state.transactionId }
                if (target != null) {
                    transactionRepository.deleteTransaction(target)
                    _effect.emit(SwapUiEffect.DeleteSuccess)
                }
            } catch (e: Exception) {
                _effect.emit(SwapUiEffect.Error(e.message ?: "Failed to delete transaction."))
            } finally {
                _uiState.update { it.copy(isExecuting = false) }
            }
        }
    }
}
