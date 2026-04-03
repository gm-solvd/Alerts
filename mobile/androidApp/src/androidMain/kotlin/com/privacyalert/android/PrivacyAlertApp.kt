package com.privacyalert.android

import android.app.Application
import com.privacyalert.di.appModules
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class PrivacyAlertApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@PrivacyAlertApp)
            modules(appModules)
        }
    }
}
