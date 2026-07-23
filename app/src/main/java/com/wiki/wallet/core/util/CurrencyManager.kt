package com.wiki.wallet.core.util

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

object CurrencyManager {
    private const val PREFS_NAME = "apexbudget_prefs"
    private const val KEY_CURRENCY_CODE = "selected_currency_code"
    private const val KEY_CURRENCY_SYMBOL = "selected_currency_symbol"

    private val symbolMap = mapOf(
        "USD" to "$",
        "EUR" to "€",
        "GBP" to "£",
        "JPY" to "¥",
        "AUD" to "$",
        "CAD" to "$",
        "CHF" to "CHF",
        "CNY" to "¥",
        "HKD" to "$",
        "NZD" to "$",
        "SEK" to "kr",
        "KRW" to "₩",
        "SGD" to "$",
        "NOK" to "kr",
        "MXN" to "$",
        "INR" to "₹",
        "RUB" to "₽",
        "BRL" to "R$",
        "ZAR" to "R",
        "TRY" to "₺",
        "TWD" to "NT$",
        "AED" to "AED",
        "SAR" to "SAR",
        "THB" to "฿",
        "IDR" to "Rp",
        "MYR" to "RM",
        "PHP" to "₱",
        "VND" to "₫",
        "PLN" to "zł",
        "EGP" to "EGP",
        "PKR" to "Rs",
        "BDT" to "৳",
        "NGN" to "₦",
        "UAH" to "₴",
        "UZS" to "so'm",
        "KZT" to "₸"
    )

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
