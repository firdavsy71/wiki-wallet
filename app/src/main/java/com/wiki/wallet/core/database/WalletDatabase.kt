package com.wiki.wallet.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.wiki.wallet.core.database.dao.AccountDao
import com.wiki.wallet.core.database.dao.CategoryDao
import com.wiki.wallet.core.database.dao.TransactionDao
import com.wiki.wallet.core.database.entity.AccountEntity
import com.wiki.wallet.core.database.entity.CategoryEntity
import com.wiki.wallet.core.database.entity.TransactionEntity
import com.wiki.wallet.core.database.entity.TransactionType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

@Database(
    entities = [
        TransactionEntity::class,
        CategoryEntity::class,
        AccountEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class WalletDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun accountDao(): AccountDao

    companion object {
        @Volatile
        private var INSTANCE: WalletDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): WalletDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WalletDatabase::class.java,
                    "wiki_wallet.db"
                )
                .fallbackToDestructiveMigration()
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        INSTANCE?.let { database ->
                            scope.launch(Dispatchers.IO) {
                                seedInitialData(database)
                            }
                        }
                    }
                })
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

private suspend fun seedInitialData(db: WalletDatabase) {
    val defaultAccounts = listOf(
        AccountEntity(
            id = "acc_cash",
            name = "Cash Wallet",
            startingBalance = 450.0,
            currency = "USD",
            iconKey = "💵"
        ),
        AccountEntity(
            id = "acc_card",
            name = "Main Card",
            startingBalance = 2400.0,
            currency = "USD",
            iconKey = "💳"
        ),
        AccountEntity(
            id = "acc_savings",
            name = "Savings Vault",
            startingBalance = 5000.0,
            currency = "USD",
            iconKey = "🏦"
        )
    )
    db.accountDao().insertAccounts(defaultAccounts)

    val defaultCategories = listOf(
        // Expense categories
        CategoryEntity(id = "cat_food", name = "Food & Dining", type = TransactionType.EXPENSE, iconKey = "🍔", colorToken = "Coral", monthlyBudget = 600.0),
        CategoryEntity(id = "cat_transport", name = "Transport", type = TransactionType.EXPENSE, iconKey = "🚗", colorToken = "Coral", monthlyBudget = 250.0),
        CategoryEntity(id = "cat_housing", name = "Housing & Bills", type = TransactionType.EXPENSE, iconKey = "🏠", colorToken = "Coral", monthlyBudget = 1200.0),
        CategoryEntity(id = "cat_shopping", name = "Shopping", type = TransactionType.EXPENSE, iconKey = "🛍️", colorToken = "Coral", monthlyBudget = 400.0),
        CategoryEntity(id = "cat_entertainment", name = "Entertainment", type = TransactionType.EXPENSE, iconKey = "🍿", colorToken = "Coral", monthlyBudget = 200.0),
        CategoryEntity(id = "cat_health", name = "Health & Wellness", type = TransactionType.EXPENSE, iconKey = "🏥", colorToken = "Coral", monthlyBudget = 150.0),

        // Income categories
        CategoryEntity(id = "cat_salary", name = "Salary", type = TransactionType.INCOME, iconKey = "💼", colorToken = "MintChip", monthlyBudget = null),
        CategoryEntity(id = "cat_freelance", name = "Freelance", type = TransactionType.INCOME, iconKey = "💻", colorToken = "MintChip", monthlyBudget = null),
        CategoryEntity(id = "cat_investments", name = "Investments", type = TransactionType.INCOME, iconKey = "📈", colorToken = "MintChip", monthlyBudget = null),
        CategoryEntity(id = "cat_gifts", name = "Gifts & Bonuses", type = TransactionType.INCOME, iconKey = "🎁", colorToken = "MintChip", monthlyBudget = null)
    )
    db.categoryDao().insertCategories(defaultCategories)

    // Seed sample transactions for past 7 days
    val now = System.currentTimeMillis()
    val dayMillis = 86400000L

    val sampleTx = listOf(
        TransactionEntity(id = UUID.randomUUID().toString(), amount = 4200.0, type = TransactionType.INCOME, categoryId = "cat_salary", accountId = "acc_card", note = "Monthly Salary Deposit", date = now - dayMillis * 6, createdAt = now - dayMillis * 6),
        TransactionEntity(id = UUID.randomUUID().toString(), amount = 145.50, type = TransactionType.EXPENSE, categoryId = "cat_food", accountId = "acc_card", note = "Grocery Shopping", date = now - dayMillis * 5, createdAt = now - dayMillis * 5),
        TransactionEntity(id = UUID.randomUUID().toString(), amount = 45.00, type = TransactionType.EXPENSE, categoryId = "cat_transport", accountId = "acc_card", note = "Fuel Refill", date = now - dayMillis * 4, createdAt = now - dayMillis * 4),
        TransactionEntity(id = UUID.randomUUID().toString(), amount = 850.0, type = TransactionType.INCOME, categoryId = "cat_freelance", accountId = "acc_card", note = "UI Design Client Project", date = now - dayMillis * 3, createdAt = now - dayMillis * 3),
        TransactionEntity(id = UUID.randomUUID().toString(), amount = 89.99, type = TransactionType.EXPENSE, categoryId = "cat_shopping", accountId = "acc_card", note = "Headphones Purchase", date = now - dayMillis * 2, createdAt = now - dayMillis * 2),
        TransactionEntity(id = UUID.randomUUID().toString(), amount = 32.40, type = TransactionType.EXPENSE, categoryId = "cat_food", accountId = "acc_cash", note = "Dinner with Friends", date = now - dayMillis * 1, createdAt = now - dayMillis * 1),
        TransactionEntity(id = UUID.randomUUID().toString(), amount = 25.00, type = TransactionType.EXPENSE, categoryId = "cat_entertainment", accountId = "acc_card", note = "Movie Tickets", date = now, createdAt = now)
    )

    sampleTx.forEach { db.transactionDao().insertTransaction(it) }
}
