package com.privacyalert.di

import com.privacyalert.data.local.SqlDelightTokenStorage
import com.privacyalert.data.local.TokenStorage
import com.privacyalert.data.remote.ApiClient
import com.privacyalert.data.remote.api.AlertApi
import com.privacyalert.data.remote.api.AuthApi
import com.privacyalert.data.remote.api.MitigationApi
import com.privacyalert.data.remote.api.ScanApi
import com.privacyalert.data.remote.api.ScoreApi
import com.privacyalert.data.repository.AlertRepositoryImpl
import com.privacyalert.data.repository.AuthRepositoryImpl
import com.privacyalert.data.repository.MitigationRepositoryImpl
import com.privacyalert.data.repository.ScanRepositoryImpl
import com.privacyalert.data.repository.ScoreRepositoryImpl
import com.privacyalert.domain.repository.AlertRepository
import com.privacyalert.domain.repository.AuthRepository
import com.privacyalert.domain.repository.MitigationRepository
import com.privacyalert.domain.repository.ScanRepository
import com.privacyalert.domain.repository.ScoreRepository
import io.ktor.client.HttpClient
import org.koin.dsl.module

val dataModule = module {
    // Token storage
    single<TokenStorage> { SqlDelightTokenStorage(get()) }

    // HTTP client
    single<HttpClient> { ApiClient.create(get()) }

    // API services
    single { AuthApi(get()) }
    single { AlertApi(get()) }
    single { ScanApi(get()) }
    single { ScoreApi(get()) }
    single { MitigationApi(get()) }

    // Repositories
    single<AuthRepository> { AuthRepositoryImpl(get(), get()) }
    single<AlertRepository> { AlertRepositoryImpl(get()) }
    single<ScanRepository> { ScanRepositoryImpl(get()) }
    single<ScoreRepository> { ScoreRepositoryImpl(get()) }
    single<MitigationRepository> { MitigationRepositoryImpl(get()) }
}
