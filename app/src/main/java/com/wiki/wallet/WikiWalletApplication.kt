package com.wiki.wallet

import android.app.Application
import com.wiki.wallet.core.designsystem.theme.ThemeManager
import com.wiki.wallet.core.di.AppContainer
import com.wiki.wallet.core.util.CurrencyManager

class WikiWalletApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        CurrencyManager.init(this)
        ThemeManager.init(this)
        container = AppContainer(this)
    }
}
