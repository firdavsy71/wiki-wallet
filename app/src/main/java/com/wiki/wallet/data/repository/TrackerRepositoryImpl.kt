package com.wiki.wallet.data.repository

import com.wiki.wallet.core.database.dao.AccountDao
import com.wiki.wallet.core.database.dao.CategoryDao
import com.wiki.wallet.core.database.dao.TransactionDao
import com.wiki.wallet.core.database.entity.AccountEntity
import com.wiki.wallet.core.database.entity.CategoryEntity
import com.wiki.wallet.core.database.entity.TransactionEntity
import com.wiki.wallet.core.database.entity.TransactionType
import com.wiki.wallet.domain.model.Account
import com.wiki.wallet.domain.model.Category
import com.wiki.wallet.domain.model.Transaction
import com.wiki.wallet.domain.repository.AccountRepository
import com.wiki.wallet.domain.repository.CategoryRepository
import com.wiki.wallet.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import java.util.UUID

class TransactionRepositoryImpl(
    private val transactionDao: TransactionDao,
    private val categoryDao: CategoryDao,
    private val accountDao: AccountDao
) : TransactionRepository {

    override fun observeAllTransactions(): Flow<List<Transaction>> {
        return combine(
            transactionDao.observeAllTransactions(),
            categoryDao.observeAllCategories(),
            accountDao.observeAllAccounts()
        ) { txEntities, catEntities, accEntities ->
            val categoryMap = catEntities.associateBy { it.id }
            val accountMap = accEntities.associateBy { it.id }

            txEntities.map { entity ->
                val category = categoryMap[entity.categoryId]
                val account = accountMap[entity.accountId]

                Transaction(
                    id = entity.id,
                    amount = entity.amount,
                    type = entity.type,
                    categoryId = entity.categoryId,
                    categoryName = category?.name ?: "General",
                    categoryIcon = category?.iconKey ?: "💰",
                    categoryColorToken = category?.colorToken ?: "Coral",
                    accountId = entity.accountId,
                    accountName = account?.name ?: "Account",
                    note = entity.note,
                    date = entity.date,
                    createdAt = entity.createdAt,
                    isRecurring = entity.isRecurring,
                    recurrenceRule = entity.recurrenceRule,
                    receiptImagePath = entity.receiptImagePath,
                    isBill = entity.isBill,
                    dueDate = entity.dueDate,
                    isPaid = entity.isPaid
                )
            }
        }
    }

    override fun observeTransactionsInDateRange(
        startTimeMillis: Long,
        endTimeMillis: Long
    ): Flow<List<Transaction>> {
        return observeAllTransactions().map { list ->
            list.filter { it.date in startTimeMillis..endTimeMillis }
        }
    }

    override suspend fun addTransaction(
        amount: Double,
        type: TransactionType,
        categoryId: String,
        accountId: String,
        note: String?,
        dateMillis: Long,
        receiptImagePath: String?,
        isBill: Boolean,
        dueDate: Long?,
        isPaid: Boolean
    ) {
        val entity = TransactionEntity(
            id = UUID.randomUUID().toString(),
            amount = amount,
            type = type,
            categoryId = categoryId,
            accountId = accountId,
            note = note,
            date = dateMillis,
            createdAt = System.currentTimeMillis(),
            receiptImagePath = receiptImagePath,
            isBill = isBill,
            dueDate = dueDate,
            isPaid = isPaid
        )
        transactionDao.insertTransaction(entity)
    }

    override suspend fun updateTransaction(
        id: String,
        amount: Double,
        type: TransactionType,
        categoryId: String,
        accountId: String,
        note: String?,
        dateMillis: Long,
        createdAt: Long,
        receiptImagePath: String?,
        isBill: Boolean,
        dueDate: Long?,
        isPaid: Boolean
    ) {
        val entity = TransactionEntity(
            id = id,
            amount = amount,
            type = type,
            categoryId = categoryId,
            accountId = accountId,
            note = note,
            date = dateMillis,
            createdAt = createdAt,
            receiptImagePath = receiptImagePath,
            isBill = isBill,
            dueDate = dueDate,
            isPaid = isPaid
        )
        transactionDao.insertTransaction(entity)
    }

    override suspend fun deleteTransaction(transaction: Transaction) {
        val entity = TransactionEntity(
            id = transaction.id,
            amount = transaction.amount,
            type = transaction.type,
            categoryId = transaction.categoryId,
            accountId = transaction.accountId,
            note = transaction.note,
            date = transaction.date,
            createdAt = transaction.createdAt,
            receiptImagePath = transaction.receiptImagePath,
            isBill = transaction.isBill,
            dueDate = transaction.dueDate,
            isPaid = transaction.isPaid
        )
        transactionDao.deleteTransaction(entity)
    }

    override suspend fun markBillAsPaid(transactionId: String) {
        val txs = transactionDao.getAllTransactions()
        val target = txs.firstOrNull { it.id == transactionId } ?: return
        val updated = target.copy(isPaid = true)
        transactionDao.insertTransaction(updated)
    }
}

class CategoryRepositoryImpl(
    private val categoryDao: CategoryDao
) : CategoryRepository {

    override fun observeAllCategories(): Flow<List<Category>> {
        return categoryDao.observeAllCategories().map { list ->
            list.map { entity ->
                Category(
                    id = entity.id,
                    name = entity.name,
                    type = entity.type,
                    iconKey = entity.iconKey,
                    colorToken = entity.colorToken,
                    monthlyBudget = entity.monthlyBudget,
                    parentCategoryId = entity.parentCategoryId
                )
            }
        }
    }

    override fun observeCategoriesByType(type: TransactionType): Flow<List<Category>> {
        return categoryDao.observeCategoriesByType(type).map { list ->
            list.map { entity ->
                Category(
                    id = entity.id,
                    name = entity.name,
                    type = entity.type,
                    iconKey = entity.iconKey,
                    colorToken = entity.colorToken,
                    monthlyBudget = entity.monthlyBudget,
                    parentCategoryId = entity.parentCategoryId
                )
            }
        }
    }

    override suspend fun addCategory(category: Category) {
        categoryDao.insertCategory(
            CategoryEntity(
                id = category.id,
                name = category.name,
                type = category.type,
                iconKey = category.iconKey,
                colorToken = category.colorToken,
                monthlyBudget = category.monthlyBudget,
                parentCategoryId = category.parentCategoryId
            )
        )
    }

    override suspend fun updateMonthlyBudget(categoryId: String, budget: Double?) {
        categoryDao.updateMonthlyBudget(categoryId, budget)
    }
}

class AccountRepositoryImpl(
    private val accountDao: AccountDao,
    private val transactionDao: TransactionDao
) : AccountRepository {

    override fun observeAllAccounts(): Flow<List<Account>> {
        return combine(
            accountDao.observeAllAccounts(),
            transactionDao.observeAllTransactions()
        ) { accounts, txs ->
            accounts
                .sortedBy { it.displayOrder }
                .map { account ->
                    val accountTxs = txs.filter { it.accountId == account.id && it.isPaid }
                    val income = accountTxs.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
                    val expense = accountTxs.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
                    val current = account.startingBalance + income - expense

                    Account(
                        id = account.id,
                        name = account.name,
                        startingBalance = account.startingBalance,
                        currentBalance = current,
                        currency = account.currency,
                        iconKey = account.iconKey,
                        displayOrder = account.displayOrder,
                        type = account.type
                    )
                }
        }
    }

    override suspend fun addAccount(account: Account) {
        accountDao.insertAccount(
            AccountEntity(
                id = account.id,
                name = account.name,
                startingBalance = account.startingBalance,
                currency = account.currency,
                iconKey = account.iconKey,
                displayOrder = account.displayOrder,
                type = account.type
            )
        )
    }

    override suspend fun updateAccount(account: Account) {
        accountDao.insertAccount(
            AccountEntity(
                id = account.id,
                name = account.name,
                startingBalance = account.startingBalance,
                currency = account.currency,
                iconKey = account.iconKey,
                displayOrder = account.displayOrder,
                type = account.type
            )
        )
    }

    override suspend fun deleteAccount(account: Account) {
        accountDao.deleteAccount(
            AccountEntity(
                id = account.id,
                name = account.name,
                startingBalance = account.startingBalance,
                currency = account.currency,
                iconKey = account.iconKey,
                displayOrder = account.displayOrder,
                type = account.type
            )
        )
    }

    override suspend fun reorderAccounts(accounts: List<Account>) {
        val updatedEntities = accounts.mapIndexed { index, account ->
            AccountEntity(
                id = account.id,
                name = account.name,
                startingBalance = account.startingBalance,
                currency = account.currency,
                iconKey = account.iconKey,
                displayOrder = index,
                type = account.type
            )
        }
        accountDao.insertAccounts(updatedEntities)
    }
}
