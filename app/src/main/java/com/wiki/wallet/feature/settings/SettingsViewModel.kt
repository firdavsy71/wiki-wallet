package com.wiki.wallet.feature.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wiki.wallet.core.database.WalletDatabase
import com.wiki.wallet.core.designsystem.theme.ThemeManager
import com.wiki.wallet.core.util.CurrencyManager
import com.wiki.wallet.domain.model.CurrencyItem
import com.wiki.wallet.domain.repository.AccountRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class SettingsUiState(
    val selectedCurrency: String = "USD",
    val currencies: List<CurrencyItem> = CurrencyManager.availableCurrencies,
    val isCurrencyPickerOpen: Boolean = false,
    val searchQuery: String = "",
    val selectedTheme: String = "Dark Ink",
    val isSecurityLockEnabled: Boolean = false,
    val isDailyReminderEnabled: Boolean = true,
    val isResetDialogOpen: Boolean = false,
    val exportStatusMessage: String? = null,
    val appVersion: String = "2.0.0"
)

sealed interface SettingsUiEvent {
    data class OnCurrencySelected(val currencyCode: String) : SettingsUiEvent
    data class OnCurrencyPickerToggle(val isOpen: Boolean) : SettingsUiEvent
    data class OnSearchQueryChanged(val query: String) : SettingsUiEvent
    data class OnThemeSelected(val themeMode: String) : SettingsUiEvent
    data class OnSecurityLockToggle(val enabled: Boolean) : SettingsUiEvent
    data class OnDailyReminderToggle(val enabled: Boolean) : SettingsUiEvent
    data class OnResetDialogToggle(val isOpen: Boolean) : SettingsUiEvent
    data object OnExportCsvClicked : SettingsUiEvent
    data object OnConfirmResetData : SettingsUiEvent
    data object OnBackClicked : SettingsUiEvent
}

class SettingsViewModel(
    private val context: Context,
    private val accountRepository: AccountRepository,
    private val walletDatabase: WalletDatabase
) : ViewModel() {

    private val prefs = context.getSharedPreferences("apexbudget_prefs", Context.MODE_PRIVATE)

    private val _uiState = MutableStateFlow(
        SettingsUiState(
            selectedCurrency = CurrencyManager.getCurrentCurrencyCode(context),
            selectedTheme = prefs.getString("app_theme_mode", "Dark Ink") ?: "Dark Ink",
            isSecurityLockEnabled = prefs.getBoolean("security_lock_enabled", false),
            isDailyReminderEnabled = prefs.getBoolean("daily_reminder_enabled", true)
        )
    )
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun onEvent(event: SettingsUiEvent) {
        when (event) {
            is SettingsUiEvent.OnCurrencySelected -> {
                CurrencyManager.setCurrencyCode(context, event.currencyCode)
                _uiState.update { it.copy(selectedCurrency = event.currencyCode, isCurrencyPickerOpen = false) }
            }
            is SettingsUiEvent.OnCurrencyPickerToggle -> {
                _uiState.update { it.copy(isCurrencyPickerOpen = event.isOpen) }
            }
            is SettingsUiEvent.OnSearchQueryChanged -> {
                _uiState.update { it.copy(searchQuery = event.query) }
            }
            is SettingsUiEvent.OnThemeSelected -> {
                ThemeManager.setThemeMode(context, event.themeMode)
                _uiState.update { it.copy(selectedTheme = event.themeMode) }
            }
            is SettingsUiEvent.OnSecurityLockToggle -> {
                prefs.edit().putBoolean("security_lock_enabled", event.enabled).apply()
                _uiState.update { it.copy(isSecurityLockEnabled = event.enabled) }
            }
            is SettingsUiEvent.OnDailyReminderToggle -> {
                prefs.edit().putBoolean("daily_reminder_enabled", event.enabled).apply()
                _uiState.update { it.copy(isDailyReminderEnabled = event.enabled) }
            }
            is SettingsUiEvent.OnResetDialogToggle -> {
                _uiState.update { it.copy(isResetDialogOpen = event.isOpen) }
            }
            SettingsUiEvent.OnExportCsvClicked -> exportTransactionsCsv()
            SettingsUiEvent.OnConfirmResetData -> clearAllData()
            SettingsUiEvent.OnBackClicked -> {}
        }
    }

    private fun exportTransactionsCsv() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val txs = walletDatabase.transactionDao().getAllTransactions()
                val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                val csvBuilder = StringBuilder()
                csvBuilder.append("ID,Amount,Type,CategoryId,AccountId,Note,Date,IsPaid\n")

                txs.forEach { tx ->
                    val dateStr = dateFormat.format(Date(tx.date))
                    csvBuilder.append("${tx.id},${tx.amount},${tx.type},${tx.categoryId},${tx.accountId},\"${tx.note ?: ""}\",\"$dateStr\",${tx.isPaid}\n")
                }

                val exportFile = File(context.getExternalFilesDir(null), "ApexBudget_Export_${System.currentTimeMillis()}.csv")
                exportFile.writeText(csvBuilder.toString())

                _uiState.update { it.copy(exportStatusMessage = "CSV exported to: ${exportFile.name}") }
            } catch (e: Exception) {
                _uiState.update { it.copy(exportStatusMessage = "Export failed: ${e.message}") }
            }
        }
    }

    private fun clearAllData() {
        viewModelScope.launch(Dispatchers.IO) {
            walletDatabase.transactionDao().deleteAllTransactions()
            val accounts = accountRepository.observeAllAccounts().firstOrNull() ?: emptyList()
            accounts.forEach { accountRepository.deleteAccount(it) }

            accountRepository.addAccount(
                com.wiki.wallet.domain.model.Account(
                    id = "acc_card",
                    name = "Main Card",
                    startingBalance = 0.0,
                    currentBalance = 0.0,
                    currency = "USD",
                    iconKey = "💳",
                    displayOrder = 0,
                    type = com.wiki.wallet.core.database.entity.AccountType.BANK
                )
            )

            _uiState.update { it.copy(isResetDialogOpen = false) }
        }
    }
}
