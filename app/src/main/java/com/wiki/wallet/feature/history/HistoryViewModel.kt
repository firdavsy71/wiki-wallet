package com.wiki.wallet.feature.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wiki.wallet.core.database.entity.TransactionType
import com.wiki.wallet.domain.model.Transaction
import com.wiki.wallet.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class TransactionFilter {
    ALL, INCOME, EXPENSE
}

data class DayGroupedTransactions(
    val dateLabel: String,
    val dayTotalNet: Double,
    val transactions: List<Transaction>
)

data class HistoryUiState(
    val selectedFilter: TransactionFilter = TransactionFilter.ALL,
    val groupedTransactions: List<DayGroupedTransactions> = emptyList(),
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0
)

sealed interface HistoryUiEvent {
    data class OnFilterSelected(val filter: TransactionFilter) : HistoryUiEvent
    data object OnBackClicked : HistoryUiEvent
}

class HistoryViewModel(
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _selectedFilter = MutableStateFlow(TransactionFilter.ALL)
    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        combine(
            transactionRepository.observeAllTransactions(),
            _selectedFilter
        ) { transactions, filter ->
            val filtered = when (filter) {
                TransactionFilter.ALL -> transactions
                TransactionFilter.INCOME -> transactions.filter { it.type == TransactionType.INCOME }
                TransactionFilter.EXPENSE -> transactions.filter { it.type == TransactionType.EXPENSE }
            }

            val dateFormat = SimpleDateFormat("EEEE, MMM d, yyyy", Locale.getDefault())
            val groupedMap = filtered.groupBy { dateFormat.format(Date(it.date)) }

            val groupedList = groupedMap.map { (dateLabel, txs) ->
                val dayIncome = txs.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
                val dayExpense = txs.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
                val dayNet = dayIncome - dayExpense

                DayGroupedTransactions(
                    dateLabel = dateLabel,
                    dayTotalNet = dayNet,
                    transactions = txs
                )
            }

            val income = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
            val expense = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }

            _uiState.update {
                it.copy(
                    selectedFilter = filter,
                    groupedTransactions = groupedList,
                    totalIncome = income,
                    totalExpense = expense
                )
            }
        }.launchIn(viewModelScope)
    }

    fun onEvent(event: HistoryUiEvent) {
        when (event) {
            is HistoryUiEvent.OnFilterSelected -> {
                _selectedFilter.value = event.filter
            }
            HistoryUiEvent.OnBackClicked -> {}
        }
    }
}
