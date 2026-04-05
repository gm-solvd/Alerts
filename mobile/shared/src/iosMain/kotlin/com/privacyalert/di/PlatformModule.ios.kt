package com.privacyalert.di

import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.privacyalert.db.PrivacyAlertDb
import com.privacyalert.presentation.preferences.OnboardingPreferences
import org.koin.dsl.module

actual val platformModule = module {
    single {
        val driver = NativeSqliteDriver(
            schema = PrivacyAlertDb.Schema,
            name = "privacy_alert.db",
        )
        PrivacyAlertDb(driver)
    }
    single { OnboardingPreferences() }
}
