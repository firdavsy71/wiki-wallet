package com.wiki.wallet.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wiki.wallet.core.designsystem.components.BarChartItem
import com.wiki.wallet.core.database.entity.TransactionType
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
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update

data class DashboardUiState(
    val userName: String = "Wiki",
    val netBalanceText: String = "$0.00",
    val periodIncomeText: String = "$0.00",
    val periodExpenseText: String = "$0.00",
    val savingsRateText: String = "0.0%",
    val selectedPeriod: TimePeriod = TimePeriod.WEEKLY,
    val chartItems: List<BarChartItem> = emptyList(),
    val accounts: List<Account> = emptyList(),
    val recentTransactions: List<Transaction> = emptyList(),
    val isLoading: Boolean = false
)

sealed interface DashboardUiEvent {
    data class OnPeriodSelected(val period: TimePeriod) : DashboardUiEvent
    data class OnAccountClick(val accountId: String) : DashboardUiEvent
    data class OnTransactionClick(val transaction: Transaction) : DashboardUiEvent
    data object OnNavigateToAddTransaction : DashboardUiEvent
    data object OnNavigateToHistory : DashboardUiEvent
    data object OnNavigateToCategories : DashboardUiEvent
    data object OnNavigateToSettings : DashboardUiEvent
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
            accountRepository.observeAllAccounts()
        ) { summary, accounts ->
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
                        "$sign$${String.format(java.util.Locale.US, "%.0f", kotlin.math.abs(item.netAmount))}"
                    } else null,
                    isDeltaPositive = item.isNetPositive
                )
            }

            _uiState.update { state ->
                state.copy(
                    netBalanceText = formatCurrency(summary.netBalance),
                    periodIncomeText = formatCurrency(summary.periodIncome),
                    periodExpenseText = formatCurrency(summary.periodExpense),
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
            else -> {}
        }
    }

    private fun formatCurrency(amount: Double): String {
        val sign = if (amount < 0) "−$" else "$"
        val absAmount = kotlin.math.abs(amount)
        return "$sign${String.format(java.util.Locale.US, "%,.2f", absAmount)}"
    }
}
