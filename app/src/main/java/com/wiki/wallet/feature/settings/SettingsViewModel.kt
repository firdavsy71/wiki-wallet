package com.wiki.wallet.feature.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wiki.wallet.core.database.WalletDatabase
import com.wiki.wallet.core.util.CurrencyManager
import com.wiki.wallet.domain.model.Account
import com.wiki.wallet.domain.repository.AccountRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CurrencyItem(
    val code: String,
    val name: String,
    val symbol: String,
    val flag: String
)

data class SettingsUiState(
    val selectedCurrency: String = "USD",
    val searchQuery: String = "",
    val isCurrencyPickerOpen: Boolean = false,
    val isResetDialogOpen: Boolean = false,
    val isSecurityLockEnabled: Boolean = false,
    val isDailyReminderEnabled: Boolean = true,
    val selectedTheme: String = "Dark Ink",
    val accounts: List<Account> = emptyList(),
    val appVersion: String = "1.2.2",
    val currencies: List<CurrencyItem> = listOf(
        CurrencyItem("USD", "United States Dollar", "$", "🇺🇸"),
        CurrencyItem("EUR", "Euro", "€", "🇪🇺"),
        CurrencyItem("GBP", "British Pound", "£", "🇬🇧"),
        CurrencyItem("JPY", "Japanese Yen", "¥", "🇯🇵"),
        CurrencyItem("AUD", "Australian Dollar", "$", "🇦🇺"),
        CurrencyItem("CAD", "Canadian Dollar", "$", "🇨🇦"),
        CurrencyItem("CHF", "Swiss Franc", "CHF", "🇨🇭"),
        CurrencyItem("CNY", "Chinese Yuan", "¥", "🇨🇳"),
        CurrencyItem("HKD", "Hong Kong Dollar", "$", "🇭🇰"),
        CurrencyItem("NZD", "New Zealand Dollar", "$", "🇳🇿"),
        CurrencyItem("SEK", "Swedish Krona", "kr", "🇸🇪"),
        CurrencyItem("KRW", "South Korean Won", "₩", "🇰🇷"),
        CurrencyItem("SGD", "Singapore Dollar", "$", "🇸🇬"),
        CurrencyItem("NOK", "Norwegian Krone", "kr", "🇳🇴"),
        CurrencyItem("MXN", "Mexican Peso", "$", "🇲🇽"),
        CurrencyItem("INR", "Indian Rupee", "₹", "🇮🇳"),
        CurrencyItem("RUB", "Russian Ruble", "₽", "🇷🇺"),
        CurrencyItem("BRL", "Brazilian Real", "R$", "🇧🇷"),
        CurrencyItem("ZAR", "South African Rand", "R", "🇿🇦"),
        CurrencyItem("TRY", "Turkish Lira", "₺", "🇹🇷"),
        CurrencyItem("TWD", "New Taiwan Dollar", "NT$", "🇹🇼"),
        CurrencyItem("AED", "United Arab Emirates Dirham", "AED", "🇦🇪"),
        CurrencyItem("SAR", "Saudi Riyal", "SAR", "🇸🇦"),
        CurrencyItem("THB", "Thai Baht", "฿", "🇹🇭"),
        CurrencyItem("IDR", "Indonesian Rupiah", "Rp", "🇮🇩"),
        CurrencyItem("MYR", "Malaysian Ringgit", "RM", "🇲🇾"),
        CurrencyItem("PHP", "Philippine Peso", "₱", "🇵🇭"),
        CurrencyItem("VND", "Vietnamese Dong", "₫", "🇻🇳"),
        CurrencyItem("PLN", "Polish Zloty", "zł", "🇵🇱"),
        CurrencyItem("EGP", "Egyptian Pound", "EGP", "🇪🇬"),
        CurrencyItem("PKR", "Pakistani Rupee", "Rs", "🇵🇰"),
        CurrencyItem("BDT", "Bangladeshi Taka", "৳", "🇧🇩"),
        CurrencyItem("NGN", "Nigerian Naira", "₦", "🇳🇬"),
        CurrencyItem("UAH", "Ukrainian Hryvnia", "₴", "🇺🇦"),
        CurrencyItem("UZS", "Uzbekistani Som", "so'm", "🇺🇿"),
        CurrencyItem("KZT", "Kazakhstani Tenge", "₸", "🇰🇿")
    )
)

sealed interface SettingsUiEvent {
    data class OnSearchQueryChanged(val query: String) : SettingsUiEvent
    data class OnCurrencySelected(val currencyCode: String) : SettingsUiEvent
    data class OnCurrencyPickerToggle(val isOpen: Boolean) : SettingsUiEvent
    data class OnThemeSelected(val theme: String) : SettingsUiEvent
    data class OnSecurityLockToggle(val enabled: Boolean) : SettingsUiEvent
    data class OnDailyReminderToggle(val enabled: Boolean) : SettingsUiEvent
    data class OnResetDialogToggle(val isOpen: Boolean) : SettingsUiEvent
    data object OnConfirmResetData : SettingsUiEvent
    data object OnBackClicked : SettingsUiEvent
}

class SettingsViewModel(
    private val context: Context,
    private val accountRepository: AccountRepository,
    private val walletDatabase: WalletDatabase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState(selectedCurrency = CurrencyManager.currentCurrencyCode.value))
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        combine(
            CurrencyManager.currentCurrencyCode,
            accountRepository.observeAllAccounts()
        ) { currencyCode, accounts ->
            _uiState.update {
                it.copy(
                    selectedCurrency = currencyCode,
                    accounts = accounts
                )
            }
        }.launchIn(viewModelScope)
    }

    fun onEvent(event: SettingsUiEvent) {
        when (event) {
            is SettingsUiEvent.OnSearchQueryChanged -> {
                _uiState.update { it.copy(searchQuery = event.query) }
            }
            is SettingsUiEvent.OnCurrencySelected -> {
                CurrencyManager.setCurrency(context, event.currencyCode)
                _uiState.update {
                    it.copy(
                        selectedCurrency = event.currencyCode,
                        isCurrencyPickerOpen = false
                    )
                }
            }
            is SettingsUiEvent.OnCurrencyPickerToggle -> {
                _uiState.update { it.copy(isCurrencyPickerOpen = event.isOpen) }
            }
            is SettingsUiEvent.OnThemeSelected -> {
                _uiState.update { it.copy(selectedTheme = event.theme) }
            }
            is SettingsUiEvent.OnSecurityLockToggle -> {
                _uiState.update { it.copy(isSecurityLockEnabled = event.enabled) }
            }
            is SettingsUiEvent.OnDailyReminderToggle -> {
                _uiState.update { it.copy(isDailyReminderEnabled = event.enabled) }
            }
            is SettingsUiEvent.OnResetDialogToggle -> {
                _uiState.update { it.copy(isResetDialogOpen = event.isOpen) }
            }
            SettingsUiEvent.OnConfirmResetData -> {
                resetDatabase()
            }
            SettingsUiEvent.OnBackClicked -> {}
        }
    }

    private fun resetDatabase() {
        viewModelScope.launch(Dispatchers.IO) {
            walletDatabase.clearAllTables()
            _uiState.update { it.copy(isResetDialogOpen = false) }
        }
    }
}
