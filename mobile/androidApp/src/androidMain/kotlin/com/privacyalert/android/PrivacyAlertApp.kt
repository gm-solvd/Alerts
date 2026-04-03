package com.privacyalert.android

import android.app.Application
import com.privacyalert.android.preferences.OnboardingPreferences
import com.privacyalert.di.appModules
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.dsl.module

val androidUiModule = module {
    single { OnboardingPreferences(get()) }
}

class PrivacyAlertApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@PrivacyAlertApp)
            modules(appModules + androidUiModule)
        }
    }
}
