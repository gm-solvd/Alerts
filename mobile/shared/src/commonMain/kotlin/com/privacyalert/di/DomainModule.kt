package com.privacyalert.di

import com.privacyalert.domain.usecase.alert.GetAlertDetailUseCase
import com.privacyalert.domain.usecase.alert.GetAlertsUseCase
import com.privacyalert.domain.usecase.alert.ResolveAlertUseCase
import com.privacyalert.domain.usecase.auth.CheckAuthUseCase
import com.privacyalert.domain.usecase.auth.LoginUseCase
import com.privacyalert.domain.usecase.auth.LogoutUseCase
import com.privacyalert.domain.usecase.auth.RegisterUseCase
import com.privacyalert.domain.usecase.mitigation.CompleteMitigationUseCase
import com.privacyalert.domain.usecase.mitigation.GetMitigationsByAlertUseCase
import com.privacyalert.domain.usecase.mitigation.GetMitigationsUseCase
import com.privacyalert.domain.usecase.scan.FullScanUseCase
import com.privacyalert.domain.usecase.score.GetScoreHistoryUseCase
import com.privacyalert.domain.usecase.score.GetScoreUseCase
import org.koin.dsl.module

val domainModule = module {
    factory { RegisterUseCase(get()) }
    factory { LoginUseCase(get()) }
    factory { LogoutUseCase(get()) }
    factory { CheckAuthUseCase(get()) }
    factory { FullScanUseCase(get()) }
    factory { GetAlertsUseCase(get()) }
    factory { GetAlertDetailUseCase(get()) }
    factory { ResolveAlertUseCase(get()) }
    factory { GetScoreUseCase(get()) }
    factory { GetScoreHistoryUseCase(get()) }
    factory { GetMitigationsUseCase(get()) }
    factory { GetMitigationsByAlertUseCase(get()) }
    factory { CompleteMitigationUseCase(get()) }
}
