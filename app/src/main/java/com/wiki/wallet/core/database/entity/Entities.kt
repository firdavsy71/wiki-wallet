package com.wiki.wallet.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TransactionType {
    INCOME, EXPENSE
}

enum class AccountType {
    BANK, CASH, CREDIT_CARD, ASSET, DEBT
}

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey val id: String,
    val amount: Double,           // positive double; sign derived from type
    val type: TransactionType,    // INCOME or EXPENSE
    val categoryId: String,
    val accountId: String,
    val note: String?,
    val date: Long,               // epoch millis
    val createdAt: Long,
    val isRecurring: Boolean = false,
    val recurrenceRule: String? = null, // RRULE-style string, nullable
    val receiptImagePath: String? = null,
    val isBill: Boolean = false,
    val dueDate: Long? = null,
    val isPaid: Boolean = true
)

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val id: String,
    val name: String,
    val type: TransactionType,
    val iconKey: String,          // e.g. "🍔", "🚗", "🏠", "💼"
    val colorToken: String,       // WalletColors token name: "Coral", "MintChip", etc.
    val monthlyBudget: Double? = null, // null = no budget set for this category
    val parentCategoryId: String? = null
)

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey val id: String,
    val name: String,             // e.g. "Cash Wallet", "Checking Card", "Savings Account"
    val startingBalance: Double,
    val currency: String = "USD", // ISO 4217, default USD
    val iconKey: String = "💳",
    val displayOrder: Int = 0,
    val type: AccountType = AccountType.BANK
)
