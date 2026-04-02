package com.privacyalert.di

import com.privacyalert.presentation.viewmodel.AlertDetailViewModel
import com.privacyalert.presentation.viewmodel.AlertsViewModel
import com.privacyalert.presentation.viewmodel.AuthViewModel
import com.privacyalert.presentation.viewmodel.DashboardViewModel
import com.privacyalert.presentation.viewmodel.MitigationsViewModel
import com.privacyalert.presentation.viewmodel.OnboardingViewModel
import com.privacyalert.presentation.viewmodel.ScanViewModel
import org.koin.dsl.module

val viewModelModule = module {
    factory { AuthViewModel(get(), get(), get(), get()) }
    factory { OnboardingViewModel(get(), get()) }
    factory { DashboardViewModel(get(), get()) }
    factory { AlertsViewModel(get()) }
    factory { params -> AlertDetailViewModel(params.get(), get(), get(), get()) }
    factory { ScanViewModel(get()) }
    factory { MitigationsViewModel(get(), get()) }
}
