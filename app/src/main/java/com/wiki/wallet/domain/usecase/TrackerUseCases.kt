package com.wiki.wallet.domain.usecase

import com.wiki.wallet.core.database.entity.TransactionType
import com.wiki.wallet.domain.model.Account
import com.wiki.wallet.domain.model.Category
import com.wiki.wallet.domain.model.DashboardSummary
import com.wiki.wallet.domain.model.NetCashFlowItem
import com.wiki.wallet.domain.model.TimePeriod
import com.wiki.wallet.domain.model.Transaction
import com.wiki.wallet.domain.repository.AccountRepository
import com.wiki.wallet.domain.repository.CategoryRepository
import com.wiki.wallet.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.abs

class GetDashboardSummaryUseCase(
    private val transactionRepository: TransactionRepository,
    private val accountRepository: AccountRepository
) {
    operator fun invoke(selectedPeriod: TimePeriod): Flow<DashboardSummary> {
        return combine(
            transactionRepository.observeAllTransactions(),
            accountRepository.observeAllAccounts()
        ) { transactions, accounts ->
            val totalStartingBalance = accounts.sumOf { it.startingBalance }
            val totalIncomeAllTime = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
            val totalExpenseAllTime = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
            val netBalance = totalStartingBalance + totalIncomeAllTime - totalExpenseAllTime

            val cal = Calendar.getInstance()
            val now = cal.timeInMillis

            val periodStartMillis: Long = when (selectedPeriod) {
                TimePeriod.WEEKLY -> {
                    cal.add(Calendar.DAY_OF_YEAR, -6)
                    cal.set(Calendar.HOUR_OF_DAY, 0)
                    cal.set(Calendar.MINUTE, 0)
                    cal.set(Calendar.SECOND, 0)
                    cal.set(Calendar.MILLISECOND, 0)
                    cal.timeInMillis
                }
                TimePeriod.MONTHLY -> {
                    cal.add(Calendar.DAY_OF_YEAR, -29)
                    cal.set(Calendar.HOUR_OF_DAY, 0)
                    cal.set(Calendar.MINUTE, 0)
                    cal.set(Calendar.SECOND, 0)
                    cal.set(Calendar.MILLISECOND, 0)
                    cal.timeInMillis
                }
            }

            val periodTransactions = transactions.filter { it.date >= periodStartMillis }
            val periodIncome = periodTransactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
            val periodExpense = periodTransactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }

            val savingsRate = if (periodIncome > 0) {
                ((periodIncome - periodExpense) / periodIncome * 100.0).coerceIn(-100.0, 100.0)
            } else 0.0

            // Generate daily chart series (7 days for WEEKLY, 7 aggregated periods for MONTHLY)
            val chartItems = buildChartSeries(transactions, selectedPeriod)

            DashboardSummary(
                netBalance = netBalance,
                periodIncome = periodIncome,
                periodExpense = periodExpense,
                savingsRatePercent = savingsRate,
                selectedPeriod = selectedPeriod,
                chartItems = chartItems,
                recentTransactions = transactions.take(10)
            )
        }
    }

    private fun buildChartSeries(
        transactions: List<Transaction>,
        period: TimePeriod
    ): List<NetCashFlowItem> {
        val days = 7
        val cal = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("EEE", Locale.getDefault())

        val items = mutableListOf<NetCashFlowItem>()
        val dayValues = mutableListOf<Double>()

        for (i in (days - 1) downTo 0) {
            val dayCal = Calendar.getInstance()
            dayCal.add(Calendar.DAY_OF_YEAR, -i)

            val startOfDay = dayCal.apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis

            val endOfDay = dayCal.apply {
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }.timeInMillis

            val dayTx = transactions.filter { it.date in startOfDay..endOfDay }
            val dayIncome = dayTx.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
            val dayExpense = dayTx.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
            val netFlow = dayIncome - dayExpense

            dayValues.add(netFlow)
            val label = dateFormat.format(dayCal.time)

            items.add(
                NetCashFlowItem(
                    dayLabel = label,
                    dateMillis = startOfDay,
                    netAmount = netFlow,
                    ratioOfMax = 0f, // updated below
                    isNetPositive = netFlow >= 0
                )
            )
        }

        val maxMagnitude = dayValues.maxOfOrNull { abs(it) }?.takeIf { it > 0 } ?: 1.0

        return items.map { item ->
            val ratio = (abs(item.netAmount) / maxMagnitude).toFloat().coerceIn(0.1f, 1f)
            item.copy(ratioOfMax = ratio)
        }
    }
}

class AddTransactionUseCase(
    private val transactionRepository: TransactionRepository
) {
    suspend operator fun invoke(
        amount: Double,
        type: TransactionType,
        categoryId: String,
        accountId: String,
        note: String?,
        dateMillis: Long = System.currentTimeMillis()
    ) {
        require(amount > 0) { "Amount must be greater than 0" }
        transactionRepository.addTransaction(
            amount = amount,
            type = type,
            categoryId = categoryId,
            accountId = accountId,
            note = note,
            dateMillis = dateMillis
        )
    }
}

class GetCategoriesUseCase(
    private val categoryRepository: CategoryRepository,
    private val transactionRepository: TransactionRepository
) {
    operator fun invoke(): Flow<List<Category>> {
        return combine(
            categoryRepository.observeAllCategories(),
            transactionRepository.observeAllTransactions()
        ) { categories, transactions ->
            val cal = Calendar.getInstance()
            cal.set(Calendar.DAY_OF_MONTH, 1)
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            val startOfMonth = cal.timeInMillis

            categories.map { category ->
                val spent = transactions
                    .filter { it.categoryId == category.id && it.date >= startOfMonth }
                    .sumOf { it.amount }

                category.copy(currentPeriodSpent = spent)
            }
        }
    }
}
