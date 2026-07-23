package com.wiki.wallet.feature.swap

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wiki.wallet.core.database.entity.TransactionType
import com.wiki.wallet.domain.model.Account
import com.wiki.wallet.domain.model.Category
import com.wiki.wallet.domain.repository.AccountRepository
import com.wiki.wallet.domain.usecase.AddTransactionUseCase
import com.wiki.wallet.domain.usecase.GetCategoriesUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SwapUiState(
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
    val budgetProgressRatio: Float = 0.0f
)

sealed interface SwapUiEvent {
    data class OnTypeChanged(val type: TransactionType) : SwapUiEvent
    data class OnAmountChanged(val text: String) : SwapUiEvent
    data class OnNoteChanged(val text: String) : SwapUiEvent
    data class OnCategorySelected(val category: Category) : SwapUiEvent
    data class OnAccountSelected(val account: Account) : SwapUiEvent
    data class OnCategoryPickerToggle(val isOpen: Boolean) : SwapUiEvent
    data object OnSaveClicked : SwapUiEvent
    data object OnBackClicked : SwapUiEvent
}

sealed interface SwapUiEffect {
    data object SaveSuccess : SwapUiEffect
    data class Error(val message: String) : SwapUiEffect
    data class Toast(val text: String) : SwapUiEffect
}

class SwapViewModel(
    private val addTransactionUseCase: AddTransactionUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val accountRepository: AccountRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SwapUiState())
    val uiState: StateFlow<SwapUiState> = _uiState.asStateFlow()

    private val _effectChannel = Channel<SwapUiEffect>()
    val effect = _effectChannel.receiveAsFlow()

    init {
        combine(
            getCategoriesUseCase(),
            accountRepository.observeAllAccounts()
        ) { categories, accounts ->
            val expenseCategories = categories.filter { it.type == _uiState.value.type }
            val defaultCat = expenseCategories.firstOrNull() ?: categories.firstOrNull()
            val defaultAcc = accounts.firstOrNull()

            _uiState.update { state ->
                state.copy(
                    categories = categories,
                    accounts = accounts,
                    selectedCategory = state.selectedCategory ?: defaultCat,
                    selectedAccount = state.selectedAccount ?: defaultAcc,
                    budgetProgressRatio = defaultCat?.budgetProgressRatio ?: 0f
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
                addTransactionUseCase(
                    amount = amount,
                    type = state.type,
                    categoryId = category.id,
                    accountId = account.id,
                    note = state.noteText.ifBlank { null }
                )
            }.onSuccess {
                _uiState.update { it.copy(isExecuting = false, isCtaEnabled = true) }
                _effectChannel.send(SwapUiEffect.SaveSuccess)
            }.onFailure { err ->
                _uiState.update { it.copy(isExecuting = false, isCtaEnabled = true) }
                _effectChannel.send(SwapUiEffect.Error(err.message ?: "Failed to save transaction"))
            }
        }
    }
}
