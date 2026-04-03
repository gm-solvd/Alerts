package com.privacyalert.di

import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.privacyalert.db.PrivacyAlertDb
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

actual val platformModule = module {
    single {
        val driver = AndroidSqliteDriver(
            schema = PrivacyAlertDb.Schema,
            context = androidContext(),
            name = "privacy_alert.db",
        )
        PrivacyAlertDb(driver)
    }
}
