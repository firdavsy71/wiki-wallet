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

@Database(
    entities = [
        TransactionEntity::class,
        CategoryEntity::class,
        AccountEntity::class
    ],
    version = 3,
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
    // Seed default clean account with 0 balance
    val defaultAccounts = listOf(
        AccountEntity(
            id = "acc_card",
            name = "Main Card",
            startingBalance = 0.0,
            currency = "USD",
            iconKey = "💳"
        )
    )
    db.accountDao().insertAccounts(defaultAccounts)

    val defaultCategories = listOf(
        // Expense categories
        CategoryEntity(id = "cat_food", name = "Food & Dining", type = TransactionType.EXPENSE, iconKey = "🍔", colorToken = "Coral", monthlyBudget = 500.0),
        CategoryEntity(id = "cat_transport", name = "Transport", type = TransactionType.EXPENSE, iconKey = "🚗", colorToken = "Coral", monthlyBudget = 200.0),
        CategoryEntity(id = "cat_housing", name = "Housing & Utilities", type = TransactionType.EXPENSE, iconKey = "🏠", colorToken = "Coral", monthlyBudget = 1000.0),
        CategoryEntity(id = "cat_shopping", name = "Shopping", type = TransactionType.EXPENSE, iconKey = "🛍️", colorToken = "Coral", monthlyBudget = 300.0),
        CategoryEntity(id = "cat_entertainment", name = "Entertainment", type = TransactionType.EXPENSE, iconKey = "🍿", colorToken = "Coral", monthlyBudget = 150.0),
        CategoryEntity(id = "cat_health", name = "Health & Medical", type = TransactionType.EXPENSE, iconKey = "🏥", colorToken = "Coral", monthlyBudget = 100.0),

        // Income categories
        CategoryEntity(id = "cat_salary", name = "Salary", type = TransactionType.INCOME, iconKey = "💼", colorToken = "MintChip", monthlyBudget = null),
        CategoryEntity(id = "cat_freelance", name = "Freelance", type = TransactionType.INCOME, iconKey = "💻", colorToken = "MintChip", monthlyBudget = null),
        CategoryEntity(id = "cat_investments", name = "Investments", type = TransactionType.INCOME, iconKey = "📈", colorToken = "MintChip", monthlyBudget = null),
        CategoryEntity(id = "cat_gifts", name = "Gifts & Bonuses", type = TransactionType.INCOME, iconKey = "🎁", colorToken = "MintChip", monthlyBudget = null)
    )
    db.categoryDao().insertCategories(defaultCategories)
}
