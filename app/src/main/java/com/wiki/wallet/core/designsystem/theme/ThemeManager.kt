package com.wiki.wallet.core.designsystem.theme

import android.content.Context
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object ThemeManager {
    private const val PREFS_NAME = "apexbudget_prefs"
    private const val KEY_THEME_MODE = "selected_theme_mode"

    private val _themeMode = MutableStateFlow("Dark Ink")
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    private val _isDarkTheme = MutableStateFlow(true)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    fun init(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedMode = prefs.getString(KEY_THEME_MODE, "Dark Ink") ?: "Dark Ink"
        setThemeModeInternal(context, savedMode)
    }

    fun setThemeMode(context: Context, mode: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_THEME_MODE, mode).apply()
        setThemeModeInternal(context, mode)
    }

    private fun setThemeModeInternal(context: Context, mode: String) {
        _themeMode.value = mode
        _isDarkTheme.value = when (mode) {
            "Light Paper" -> false
            "Dark Ink" -> true
            else -> true
        }
    }

    val backgroundColor: Color
        get() = if (_isDarkTheme.value) Color(0xFF141416) else Color(0xFFFAFAF8)

    val cardColor: Color
        get() = if (_isDarkTheme.value) Color(0xFF1C1C1E) else Color(0xFFF2F1EF)

    val cardElevatedColor: Color
        get() = if (_isDarkTheme.value) Color(0xFF2A2A2C) else Color(0xFFFAFAF8)

    val textColorPrimary: Color
        get() = if (_isDarkTheme.value) Color(0xFFF5F5F5) else Color(0xFF141416)

    val cardBorderColor: Color
        get() = if (_isDarkTheme.value) Color(0x33FFFFFF) else Color(0x0D000000)
}
