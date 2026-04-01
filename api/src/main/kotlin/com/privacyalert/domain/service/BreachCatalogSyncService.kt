package com.privacyalert.domain.service

import com.privacyalert.domain.repository.BreachDatabaseRepository
import org.slf4j.LoggerFactory

class BreachCatalogSyncService(
    private val sources: List<BreachCatalogSource>,
    private val breachDatabaseRepository: BreachDatabaseRepository,
    private val dataTypeNormalizer: DataTypeNormalizer,
) {
    private val log = LoggerFactory.getLogger(BreachCatalogSyncService::class.java)

    fun sync(): SyncResult {
        val existingNames = breachDatabaseRepository.findAllNames().toMutableSet()
        var inserted = 0
        var skipped = 0
        var errors = 0

        for (source in sources) {
            val catalog =
                try {
                    source.fetchCatalog()
                } catch (e: Exception) {
                    log.error("Failed to fetch catalog from {}", source.sourceName(), e)
                    errors++
                    continue
                }

            log.info("Fetched {} breaches from {}", catalog.size, source.sourceName())

            for (breach in catalog) {
                if (breach.name in existingNames) {
                    skipped++
                    continue
                }

                try {
                    val normalized =
                        breach.copy(
                            dataClasses = dataTypeNormalizer.normalize(breach.dataClasses),
                        )
                    breachDatabaseRepository.save(normalized)
                    existingNames.add(normalized.name)
                    inserted++
                } catch (e: Exception) {
                    log.warn("Failed to save breach: {}", breach.name, e)
                    errors++
                }
            }
        }

        log.info("Catalog sync complete. Inserted: {}, Skipped: {}, Errors: {}", inserted, skipped, errors)
        return SyncResult(inserted = inserted, skipped = skipped, errors = errors)
    }
}

data class SyncResult(
    val inserted: Int,
    val skipped: Int,
    val errors: Int,
)
