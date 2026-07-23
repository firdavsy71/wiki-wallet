package com.wiki.wallet.core.di

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.wiki.wallet.core.database.WalletDatabase
import com.wiki.wallet.data.repository.AccountRepositoryImpl
import com.wiki.wallet.data.repository.CategoryRepositoryImpl
import com.wiki.wallet.data.repository.TransactionRepositoryImpl
import com.wiki.wallet.domain.repository.AccountRepository
import com.wiki.wallet.domain.repository.CategoryRepository
import com.wiki.wallet.domain.repository.TransactionRepository
import com.wiki.wallet.domain.usecase.AddTransactionUseCase
import com.wiki.wallet.domain.usecase.GetCategoriesUseCase
import com.wiki.wallet.domain.usecase.GetDashboardSummaryUseCase
import com.wiki.wallet.feature.categories.CategoriesViewModel
import com.wiki.wallet.feature.dashboard.DashboardViewModel
import com.wiki.wallet.feature.history.HistoryViewModel
import com.wiki.wallet.feature.settings.SettingsViewModel
import com.wiki.wallet.feature.swap.SwapViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class AppContainer(private val context: Context) {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    val walletDatabase: WalletDatabase by lazy {
        WalletDatabase.getDatabase(context, applicationScope)
    }

    val transactionRepository: TransactionRepository by lazy {
        TransactionRepositoryImpl(
            transactionDao = walletDatabase.transactionDao(),
            categoryDao = walletDatabase.categoryDao(),
            accountDao = walletDatabase.accountDao()
        )
    }

    val categoryRepository: CategoryRepository by lazy {
        CategoryRepositoryImpl(
            categoryDao = walletDatabase.categoryDao()
        )
    }

    val accountRepository: AccountRepository by lazy {
        AccountRepositoryImpl(
            accountDao = walletDatabase.accountDao(),
            transactionDao = walletDatabase.transactionDao()
        )
    }

    val getDashboardSummaryUseCase: GetDashboardSummaryUseCase by lazy {
        GetDashboardSummaryUseCase(
            transactionRepository = transactionRepository,
            accountRepository = accountRepository
        )
    }

    val addTransactionUseCase: AddTransactionUseCase by lazy {
        AddTransactionUseCase(
            transactionRepository = transactionRepository
        )
    }

    val getCategoriesUseCase: GetCategoriesUseCase by lazy {
        GetCategoriesUseCase(
            categoryRepository = categoryRepository,
            transactionRepository = transactionRepository
        )
    }

    val dashboardViewModelFactory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DashboardViewModel(
                getDashboardSummaryUseCase = getDashboardSummaryUseCase,
                accountRepository = accountRepository
            ) as T
        }
    }

    val swapViewModelFactory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SwapViewModel(
                addTransactionUseCase = addTransactionUseCase,
                getCategoriesUseCase = getCategoriesUseCase,
                accountRepository = accountRepository
            ) as T
        }
    }

    val historyViewModelFactory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HistoryViewModel(
                transactionRepository = transactionRepository
            ) as T
        }
    }

    val categoriesViewModelFactory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CategoriesViewModel(
                getCategoriesUseCase = getCategoriesUseCase
            ) as T
        }
    }

    val settingsViewModelFactory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SettingsViewModel(
                accountRepository = accountRepository
            ) as T
        }
    }
}
