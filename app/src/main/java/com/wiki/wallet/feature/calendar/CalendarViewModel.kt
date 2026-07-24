package com.wiki.wallet.feature.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wiki.wallet.domain.model.Transaction
import com.wiki.wallet.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

data class CalendarUiState(
    val currentMonthLabel: String = "July 2026",
    val selectedDayOfMonth: Int = Calendar.getInstance().get(Calendar.DAY_OF_MONTH),
    val totalDaysInMonth: Int = 31,
    val billTransactions: List<Transaction> = emptyList(),
    val dayBillCountMap: Map<Int, Int> = emptyMap(),
    val selectedFilter: String = "All" // "All", "Due", "Paid"
)

sealed interface CalendarUiEvent {
    data class OnDaySelected(val day: Int) : CalendarUiEvent
    data class OnFilterSelected(val filter: String) : CalendarUiEvent
    data class OnMarkAsPaidClicked(val transactionId: String) : CalendarUiEvent
    data object OnBackClicked : CalendarUiEvent
}

class CalendarViewModel(
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    init {
        val cal = Calendar.getInstance()
        val monthFormat = java.text.SimpleDateFormat("MMMM yyyy", java.util.Locale.getDefault())
        val monthLabel = monthFormat.format(cal.time)
        val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

        _uiState.update {
            it.copy(
                currentMonthLabel = monthLabel,
                totalDaysInMonth = daysInMonth
            )
        }

        transactionRepository.observeAllTransactions()
            .onEach { allTxs ->
                val bills = allTxs.filter { it.isBill || it.isRecurring || it.dueDate != null }
                val countMap = mutableMapOf<Int, Int>()

                bills.forEach { tx ->
                    val txCal = Calendar.getInstance().apply { timeInMillis = tx.dueDate ?: tx.date }
                    if (txCal.get(Calendar.MONTH) == cal.get(Calendar.MONTH)) {
                        val day = txCal.get(Calendar.DAY_OF_MONTH)
                        countMap[day] = (countMap[day] ?: 0) + 1
                    }
                }

                _uiState.update {
                    it.copy(
                        billTransactions = bills,
                        dayBillCountMap = countMap
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    fun onEvent(event: CalendarUiEvent) {
        when (event) {
            is CalendarUiEvent.OnDaySelected -> {
                _uiState.update { it.copy(selectedDayOfMonth = event.day) }
            }
            is CalendarUiEvent.OnFilterSelected -> {
                _uiState.update { it.copy(selectedFilter = event.filter) }
            }
            is CalendarUiEvent.OnMarkAsPaidClicked -> {
                viewModelScope.launch {
                    transactionRepository.markBillAsPaid(event.transactionId)
                }
            }
            CalendarUiEvent.OnBackClicked -> {}
        }
    }
}
