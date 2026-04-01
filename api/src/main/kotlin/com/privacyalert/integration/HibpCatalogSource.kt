package com.privacyalert.integration

import com.fasterxml.jackson.annotation.JsonProperty
import com.privacyalert.config.AppProperties
import com.privacyalert.domain.model.KnownBreach
import com.privacyalert.domain.service.BreachCatalogSource
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientException
import java.time.LocalDate

@ConditionalOnProperty(name = ["app.hibp.enabled"], havingValue = "true", matchIfMissing = false)
@Component
class HibpCatalogSource(
    appProperties: AppProperties,
) : BreachCatalogSource {
    private val log = LoggerFactory.getLogger(HibpCatalogSource::class.java)

    private val restClient =
        RestClient
            .builder()
            .baseUrl(appProperties.hibp.baseUrl)
            .defaultHeader("user-agent", "PrivacyAlertSystem")
            .build()

    override fun sourceName(): String = "haveibeenpwned"

    override fun fetchCatalog(): List<KnownBreach> =
        try {
            val response =
                restClient
                    .get()
                    .uri("/breaches")
                    .retrieve()
                    .body(Array<HibpBreachCatalogEntry>::class.java)

            response
                ?.filter { !it.isFabricated && !it.isRetired }
                ?.mapNotNull { entry ->
                    try {
                        KnownBreach(
                            name = entry.name,
                            domain = entry.domain,
                            breachDate = parseDate(entry.breachDate),
                            dataClasses = entry.dataClasses,
                            recordCount = entry.pwnCount,
                            sourceUrl = sourceName(),
                        )
                    } catch (e: Exception) {
                        log.warn("Failed to parse HIBP breach entry: {}", entry.name, e)
                        null
                    }
                } ?: emptyList()
        } catch (e: RestClientException) {
            log.error("Failed to fetch HIBP breach catalog", e)
            emptyList()
        }

    private fun parseDate(dateStr: String?): LocalDate? =
        try {
            dateStr?.takeIf { it.isNotBlank() }?.let { LocalDate.parse(it.take(10)) }
        } catch (e: Exception) {
            null
        }
}

private data class HibpBreachCatalogEntry(
    @JsonProperty("Name") val name: String,
    @JsonProperty("Title") val title: String?,
    @JsonProperty("Domain") val domain: String?,
    @JsonProperty("BreachDate") val breachDate: String?,
    @JsonProperty("PwnCount") val pwnCount: Long?,
    @JsonProperty("DataClasses") val dataClasses: List<String>,
    @JsonProperty("IsVerified") val isVerified: Boolean,
    @JsonProperty("IsSensitive") val isSensitive: Boolean,
    @JsonProperty("IsFabricated") val isFabricated: Boolean,
    @JsonProperty("IsRetired") val isRetired: Boolean,
    @JsonProperty("Description") val description: String?,
    @JsonProperty("LogoPath") val logoPath: String?,
)
