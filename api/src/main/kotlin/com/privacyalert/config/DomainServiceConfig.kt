package com.privacyalert.config

import com.privacyalert.domain.repository.BreachDatabaseRepository
import com.privacyalert.domain.service.BreachCatalogSource
import com.privacyalert.domain.service.BreachCatalogSyncService
import com.privacyalert.domain.service.BreachRiskClassifier
import com.privacyalert.domain.service.DataTypeNormalizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class DomainServiceConfig {
    @Bean
    fun dataTypeNormalizer(): DataTypeNormalizer = DataTypeNormalizer()

    @Bean
    fun breachRiskClassifier(): BreachRiskClassifier = BreachRiskClassifier()

    @Bean
    fun breachCatalogSyncService(
        sources: List<BreachCatalogSource>,
        breachDatabaseRepository: BreachDatabaseRepository,
        dataTypeNormalizer: DataTypeNormalizer,
    ): BreachCatalogSyncService =
        BreachCatalogSyncService(
            sources = sources,
            breachDatabaseRepository = breachDatabaseRepository,
            dataTypeNormalizer = dataTypeNormalizer,
        )
}
