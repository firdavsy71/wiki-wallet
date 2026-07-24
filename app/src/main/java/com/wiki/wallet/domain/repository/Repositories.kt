package com.wiki.wallet.domain.repository

import com.wiki.wallet.core.database.entity.TransactionType
import com.wiki.wallet.domain.model.Account
import com.wiki.wallet.domain.model.Category
import com.wiki.wallet.domain.model.Transaction
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    fun observeAllTransactions(): Flow<List<Transaction>>
    fun observeTransactionsInDateRange(startTimeMillis: Long, endTimeMillis: Long): Flow<List<Transaction>>
    suspend fun addTransaction(
        amount: Double,
        type: TransactionType,
        categoryId: String,
        accountId: String,
        note: String?,
        dateMillis: Long
    )
    suspend fun updateTransaction(
        id: String,
        amount: Double,
        type: TransactionType,
        categoryId: String,
        accountId: String,
        note: String?,
        dateMillis: Long,
        createdAt: Long
    )
    suspend fun deleteTransaction(transaction: Transaction)
}

interface CategoryRepository {
    fun observeAllCategories(): Flow<List<Category>>
    fun observeCategoriesByType(type: TransactionType): Flow<List<Category>>
    suspend fun addCategory(category: Category)
    suspend fun updateMonthlyBudget(categoryId: String, budget: Double?)
}

interface AccountRepository {
    fun observeAllAccounts(): Flow<List<Account>>
    suspend fun addAccount(account: Account)
    suspend fun updateAccount(account: Account)
    suspend fun deleteAccount(account: Account)
    suspend fun reorderAccounts(accounts: List<Account>)
}
