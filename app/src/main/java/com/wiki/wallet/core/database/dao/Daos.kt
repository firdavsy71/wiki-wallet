package com.wiki.wallet.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.wiki.wallet.core.database.entity.AccountEntity
import com.wiki.wallet.core.database.entity.CategoryEntity
import com.wiki.wallet.core.database.entity.TransactionEntity
import com.wiki.wallet.core.database.entity.TransactionType
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun observeAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions ORDER BY date DESC")
    suspend fun getAllTransactions(): List<TransactionEntity>

    @Query("SELECT * FROM transactions WHERE date >= :startTimeAndMillis AND date <= :endTimeAndMillis ORDER BY date DESC")
    fun observeTransactionsInDateRange(startTimeAndMillis: Long, endTimeAndMillis: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE categoryId = :categoryId ORDER BY date DESC")
    fun observeTransactionsByCategory(categoryId: String): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(tx: TransactionEntity): Long

    @Delete
    suspend fun deleteTransaction(tx: TransactionEntity): Int
}

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories")
    fun observeAllCategories(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories")
    suspend fun getAllCategories(): List<CategoryEntity>

    @Query("SELECT * FROM categories WHERE type = :type")
    fun observeCategoriesByType(type: TransactionType): Flow<List<CategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<CategoryEntity>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity): Long

    @Query("UPDATE categories SET monthlyBudget = :budget WHERE id = :categoryId")
    suspend fun updateMonthlyBudget(categoryId: String, budget: Double?): Int
}

@Dao
interface AccountDao {
    @Query("SELECT * FROM accounts")
    fun observeAllAccounts(): Flow<List<AccountEntity>>

    @Query("SELECT * FROM accounts")
    suspend fun getAllAccounts(): List<AccountEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccounts(accounts: List<AccountEntity>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: AccountEntity): Long

    @Delete
    suspend fun deleteAccount(account: AccountEntity): Int
}
