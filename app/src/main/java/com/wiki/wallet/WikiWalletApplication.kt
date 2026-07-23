package com.wiki.wallet

import android.app.Application
import com.wiki.wallet.core.di.AppContainer

class WikiWalletApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
