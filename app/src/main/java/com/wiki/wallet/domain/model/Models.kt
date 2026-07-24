package com.wiki.wallet.domain.model

import com.wiki.wallet.core.database.entity.TransactionType

data class Transaction(
    val id: String,
    val amount: Double,
    val type: TransactionType,
    val categoryId: String,
    val categoryName: String,
    val categoryIcon: String,
    val categoryColorToken: String,
    val accountId: String,
    val accountName: String,
    val note: String?,
    val date: Long,
    val createdAt: Long,
    val isRecurring: Boolean = false,
    val recurrenceRule: String? = null
)

data class Category(
    val id: String,
    val name: String,
    val type: TransactionType,
    val iconKey: String,
    val colorToken: String,
    val monthlyBudget: Double? = null,
    val currentPeriodSpent: Double = 0.0
) {
    val budgetProgressRatio: Float
        get() {
            val budget = monthlyBudget ?: return 0f
            if (budget <= 0.0) return 0f
            return (currentPeriodSpent / budget).toFloat().coerceIn(0f, 1f)
        }
}

data class Account(
    val id: String,
    val name: String,
    val startingBalance: Double,
    val currentBalance: Double,
    val currency: String = "USD",
    val iconKey: String = "💳",
    val displayOrder: Int = 0
)

data class NetCashFlowItem(
    val dayLabel: String,
    val dateMillis: Long,
    val netAmount: Double,        // income - expense
    val ratioOfMax: Float,         // 0f..1f height ratio
    val isNetPositive: Boolean,    // true if net >= 0 (MintChip bar up), false if net < 0 (Coral bar down)
    val deltaPercentText: String? = null
)

enum class TimePeriod {
    WEEKLY, MONTHLY
}

data class DashboardSummary(
    val netBalance: Double,
    val periodIncome: Double,
    val periodExpense: Double,
    val savingsRatePercent: Double,
    val selectedPeriod: TimePeriod = TimePeriod.WEEKLY,
    val chartItems: List<NetCashFlowItem> = emptyList(),
    val recentTransactions: List<Transaction> = emptyList()
)
