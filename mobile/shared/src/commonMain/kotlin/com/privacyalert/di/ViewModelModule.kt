package com.privacyalert.di

import com.privacyalert.presentation.viewmodel.AlertDetailViewModel
import com.privacyalert.presentation.viewmodel.AlertsViewModel
import com.privacyalert.presentation.viewmodel.AuthViewModel
import com.privacyalert.presentation.viewmodel.DashboardViewModel
import com.privacyalert.presentation.viewmodel.OnboardingViewModel
import org.koin.dsl.module

val viewModelModule = module {
    factory { AuthViewModel(get(), get(), get(), get(), get()) }
    factory { OnboardingViewModel(get(), get()) }
    factory { DashboardViewModel(get(), get(), get(), get(), get(), get(), get()) }
    factory { AlertsViewModel(get(), get()) }
    factory { params -> AlertDetailViewModel(params.get(), get(), get(), get(), get()) }
}
