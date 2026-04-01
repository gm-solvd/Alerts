package com.privacyalert.config

import com.privacyalert.domain.service.BreachCatalogSyncService
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled

@ConditionalOnProperty(name = ["app.catalog-sync.enabled"], havingValue = "true", matchIfMissing = false)
@Configuration
@EnableScheduling
class BreachCatalogScheduler(
    private val breachCatalogSyncService: BreachCatalogSyncService,
) {
    private val log = LoggerFactory.getLogger(BreachCatalogScheduler::class.java)

    @Scheduled(cron = "\${app.catalog-sync.cron}")
    fun refreshCatalog() {
        log.info("Starting scheduled breach catalog sync")
        val result = breachCatalogSyncService.sync()
        log.info(
            "Catalog sync complete — Inserted: {}, Skipped: {}, Errors: {}",
            result.inserted,
            result.skipped,
            result.errors,
        )
    }
}
