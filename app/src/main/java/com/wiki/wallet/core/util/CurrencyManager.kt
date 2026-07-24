package com.wiki.wallet.core.util

import android.content.Context
import com.wiki.wallet.domain.model.CurrencyItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

object CurrencyManager {
    private const val PREFS_NAME = "apexbudget_prefs"
    private const val KEY_CURRENCY_CODE = "selected_currency_code"
    private const val KEY_CURRENCY_SYMBOL = "selected_currency_symbol"

    val availableCurrencies = listOf(
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
        CurrencyItem("AED", "UAE Dirham", "AED", "🇦🇪"),
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

    private val symbolMap = availableCurrencies.associate { it.code to it.symbol }

    private val _currentCurrencyCode = MutableStateFlow("USD")
    val currentCurrencyCode: StateFlow<String> = _currentCurrencyCode.asStateFlow()

    private val _currentCurrencySymbol = MutableStateFlow("$")
    val currentCurrencySymbol: StateFlow<String> = _currentCurrencySymbol.asStateFlow()

    fun init(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedCode = prefs.getString(KEY_CURRENCY_CODE, "USD") ?: "USD"
        val savedSymbol = prefs.getString(KEY_CURRENCY_SYMBOL, "$") ?: "$"

        _currentCurrencyCode.value = savedCode
        _currentCurrencySymbol.value = savedSymbol
    }

    fun getCurrentCurrencyCode(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_CURRENCY_CODE, "USD") ?: "USD"
    }

    fun setCurrencyCode(context: Context, code: String) {
        setCurrency(context, code)
    }

    fun setCurrency(context: Context, code: String) {
        val symbol = symbolMap[code] ?: "$"
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_CURRENCY_CODE, code)
            .putString(KEY_CURRENCY_SYMBOL, symbol)
            .apply()

        _currentCurrencyCode.value = code
        _currentCurrencySymbol.value = symbol
    }

    fun getSymbolForCode(code: String): String {
        return symbolMap[code] ?: "$"
    }

    fun format(amount: Double, symbol: String = _currentCurrencySymbol.value): String {
        val absAmount = kotlin.math.abs(amount)
        val formattedNumber = String.format(Locale.US, "%,.2f", absAmount)
        val sign = if (amount < 0) "−" else ""

        return if (symbol.length > 2) {
            "$sign$formattedNumber $symbol"
        } else {
            "$sign$symbol$formattedNumber"
        }
    }
}
