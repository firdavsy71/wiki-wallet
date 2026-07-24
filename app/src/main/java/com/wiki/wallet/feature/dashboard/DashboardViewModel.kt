package com.wiki.wallet.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wiki.wallet.core.designsystem.components.BarChartItem
import com.wiki.wallet.core.database.entity.TransactionType
import com.wiki.wallet.core.util.CurrencyManager
import com.wiki.wallet.domain.model.Account
import com.wiki.wallet.domain.model.TimePeriod
import com.wiki.wallet.domain.model.Transaction
import com.wiki.wallet.domain.repository.AccountRepository
import com.wiki.wallet.domain.usecase.GetDashboardSummaryUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DashboardUiState(
    val userName: String = "Apex User",
    val currencySymbol: String = "$",
    val netBalanceText: String = "$0.00",
    val periodIncomeText: String = "$0.00",
    val periodExpenseText: String = "$0.00",
    val savingsRateText: String = "0.0%",
    val selectedPeriod: TimePeriod = TimePeriod.WEEKLY,
    val chartItems: List<BarChartItem> = emptyList(),
    val accounts: List<Account> = emptyList(),
    val recentTransactions: List<Transaction> = emptyList(),
    val isReorderModalOpen: Boolean = false,
    val isLoading: Boolean = false
)

sealed interface DashboardUiEvent {
    data class OnPeriodSelected(val period: TimePeriod) : DashboardUiEvent
    data class OnAccountClick(val accountId: String) : DashboardUiEvent
    data class OnMoveAccountUp(val accountId: String) : DashboardUiEvent
    data class OnMoveAccountDown(val accountId: String) : DashboardUiEvent
    data class OnReorderModalToggle(val isOpen: Boolean) : DashboardUiEvent
    data class OnTransactionClick(val transactionId: String) : DashboardUiEvent
    data object OnNavigateToAddTransaction : DashboardUiEvent
    data object OnNavigateToHistory : DashboardUiEvent
    data object OnNavigateToCategories : DashboardUiEvent
    data object OnNavigateToSettings : DashboardUiEvent
    data object OnNavigateToCalendar : DashboardUiEvent
}

class DashboardViewModel(
    private val getDashboardSummaryUseCase: GetDashboardSummaryUseCase,
    private val accountRepository: AccountRepository
) : ViewModel() {

    private val _selectedPeriod = MutableStateFlow(TimePeriod.WEEKLY)

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        combine(
            _selectedPeriod.flatMapLatest { period -> getDashboardSummaryUseCase(period) },
            accountRepository.observeAllAccounts(),
            CurrencyManager.currentCurrencySymbol
        ) { summary, accounts, symbol ->
            val chartBarItems = summary.chartItems.mapIndexed { idx, item ->
                BarChartItem(
                    dayLabel = item.dayLabel,
                    valueRatio = item.ratioOfMax,
                    incomeAmount = if (item.netAmount > 0) item.netAmount else 0.0,
                    expenseAmount = if (item.netAmount < 0) kotlin.math.abs(item.netAmount) else 0.0,
                    isActive = idx == summary.chartItems.lastIndex,
                    isPositive = item.isNetPositive,
                    deltaChipText = if (item.netAmount != 0.0) {
                        val sign = if (item.netAmount > 0) "+" else "−"
                        "$sign${CurrencyManager.format(kotlin.math.abs(item.netAmount), symbol)}"
                    } else null,
                    isDeltaPositive = item.isNetPositive
                )
            }

            _uiState.update { state ->
                state.copy(
                    currencySymbol = symbol,
                    netBalanceText = CurrencyManager.format(summary.netBalance, symbol),
                    periodIncomeText = CurrencyManager.format(summary.periodIncome, symbol),
                    periodExpenseText = CurrencyManager.format(summary.periodExpense, symbol),
                    savingsRateText = String.format(java.util.Locale.US, "%.1f%%", summary.savingsRatePercent),
                    selectedPeriod = summary.selectedPeriod,
                    chartItems = chartBarItems,
                    accounts = accounts,
                    recentTransactions = summary.recentTransactions
                )
            }
        }.launchIn(viewModelScope)
    }

    fun onEvent(event: DashboardUiEvent) {
        when (event) {
            is DashboardUiEvent.OnPeriodSelected -> {
                _selectedPeriod.value = event.period
            }
            is DashboardUiEvent.OnReorderModalToggle -> {
                _uiState.update { it.copy(isReorderModalOpen = event.isOpen) }
            }
            is DashboardUiEvent.OnMoveAccountUp -> {
                moveAccount(event.accountId, -1)
            }
            is DashboardUiEvent.OnMoveAccountDown -> {
                moveAccount(event.accountId, 1)
            }
            else -> {}
        }
    }

    private fun moveAccount(accountId: String, direction: Int) {
        val currentAccounts = _uiState.value.accounts.toMutableList()
        val index = currentAccounts.indexOfFirst { it.id == accountId }
        if (index == -1) return

        val newIndex = index + direction
        if (newIndex in 0..currentAccounts.lastIndex) {
            val item = currentAccounts.removeAt(index)
            currentAccounts.add(newIndex, item)
            viewModelScope.launch {
                accountRepository.reorderAccounts(currentAccounts)
            }
        }
    }
}
