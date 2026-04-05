package com.privacyalert.di

import org.koin.core.context.startKoin

fun startKoinApp() {
    startKoin {
        modules(appModules)
    }
}
