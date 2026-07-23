package com.wiki.wallet.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wiki.wallet.domain.model.Account
import com.wiki.wallet.domain.repository.AccountRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update

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
    val accounts: List<Account> = emptyList(),
    val appVersion: String = "1.2.0",
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
    data object OnBackClicked : SettingsUiEvent
}

class SettingsViewModel(
    private val accountRepository: AccountRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        accountRepository.observeAllAccounts()
            .onEach { accounts ->
                _uiState.update { it.copy(accounts = accounts) }
            }
            .launchIn(viewModelScope)
    }

    fun onEvent(event: SettingsUiEvent) {
        when (event) {
            is SettingsUiEvent.OnSearchQueryChanged -> {
                _uiState.update { it.copy(searchQuery = event.query) }
            }
            is SettingsUiEvent.OnCurrencySelected -> {
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
            SettingsUiEvent.OnBackClicked -> {}
        }
    }
}
