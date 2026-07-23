package com.wiki.wallet.domain.usecase

import com.wiki.wallet.core.database.entity.TransactionType
import com.wiki.wallet.domain.model.Account
import com.wiki.wallet.domain.model.Category
import com.wiki.wallet.domain.model.TimePeriod
import com.wiki.wallet.domain.model.Transaction
import com.wiki.wallet.domain.repository.AccountRepository
import com.wiki.wallet.domain.repository.CategoryRepository
import com.wiki.wallet.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TrackerUseCasesTest {

    private val fakeTransactions = mutableListOf(
        Transaction(
            id = "tx1",
            amount = 1000.0,
            type = TransactionType.INCOME,
            categoryId = "cat_salary",
            categoryName = "Salary",
            categoryIcon = "💼",
            categoryColorToken = "MintChip",
            accountId = "acc_card",
            accountName = "Main Card",
            note = "Salary",
            date = System.currentTimeMillis(),
            createdAt = System.currentTimeMillis()
        ),
        Transaction(
            id = "tx2",
            amount = 200.0,
            type = TransactionType.EXPENSE,
            categoryId = "cat_food",
            categoryName = "Food & Dining",
            categoryIcon = "🍔",
            categoryColorToken = "Coral",
            accountId = "acc_card",
            accountName = "Main Card",
            note = "Groceries",
            date = System.currentTimeMillis(),
            createdAt = System.currentTimeMillis()
        )
    )

    private val fakeAccounts = listOf(
        Account(id = "acc_card", name = "Main Card", startingBalance = 500.0, currentBalance = 1300.0)
    )

    private val fakeTxRepository = object : TransactionRepository {
        override fun observeAllTransactions(): Flow<List<Transaction>> = flowOf(fakeTransactions)
        override fun observeTransactionsInDateRange(startTimeMillis: Long, endTimeMillis: Long): Flow<List<Transaction>> = flowOf(fakeTransactions)
        override suspend fun addTransaction(
            amount: Double,
            type: TransactionType,
            categoryId: String,
            accountId: String,
            note: String?,
            dateMillis: Long
        ) {
            fakeTransactions.add(
                Transaction(
                    id = "tx_${System.currentTimeMillis()}",
                    amount = amount,
                    type = type,
                    categoryId = categoryId,
                    categoryName = "Test",
                    categoryIcon = "💰",
                    categoryColorToken = "MintChip",
                    accountId = accountId,
                    accountName = "Main Card",
                    note = note,
                    date = dateMillis,
                    createdAt = System.currentTimeMillis()
                )
            )
        }
        override suspend fun deleteTransaction(transaction: Transaction) {
            fakeTransactions.remove(transaction)
        }
    }

    private val fakeAccountRepository = object : AccountRepository {
        override fun observeAllAccounts(): Flow<List<Account>> = flowOf(fakeAccounts)
        override suspend fun addAccount(account: Account) {}
    }

    @Test
    fun getDashboardSummary_calculatesNetBalanceAndSavingsRate() = runTest {
        val useCase = GetDashboardSummaryUseCase(fakeTxRepository, fakeAccountRepository)
        val summary = useCase(TimePeriod.WEEKLY).first()

        // Net balance = startingBalance (500) + income (1000) - expense (200) = 1300.0
        assertEquals(1300.0, summary.netBalance, 0.01)
        assertEquals(1000.0, summary.periodIncome, 0.01)
        assertEquals(200.0, summary.periodExpense, 0.01)
        // Savings rate = (1000 - 200) / 1000 = 80%
        assertEquals(80.0, summary.savingsRatePercent, 0.01)
    }

    @Test
    fun addTransactionUseCase_insertsTransactionSuccessfully() = runTest {
        val addTxUseCase = AddTransactionUseCase(fakeTxRepository)
        val initialSize = fakeTransactions.size

        addTxUseCase(
            amount = 150.0,
            type = TransactionType.EXPENSE,
            categoryId = "cat_food",
            accountId = "acc_card",
            note = "Dinner"
        )

        assertEquals(initialSize + 1, fakeTransactions.size)
        assertEquals(150.0, fakeTransactions.last().amount, 0.01)
    }
}
