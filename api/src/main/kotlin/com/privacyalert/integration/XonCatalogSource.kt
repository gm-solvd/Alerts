package com.privacyalert.integration

// Uses the XposedOrNot API (https://xposedornot.com), licensed under MIT.
// https://github.com/XposedOrNot/XposedOrNot-API

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

@ConditionalOnProperty(name = ["app.xon.enabled"], havingValue = "true", matchIfMissing = false)
@Component
class XonCatalogSource(
    appProperties: AppProperties,
) : BreachCatalogSource {
    private val log = LoggerFactory.getLogger(XonCatalogSource::class.java)

    private val restClient =
        RestClient
            .builder()
            .baseUrl(appProperties.xon.baseUrl)
            .defaultHeader("user-agent", "PrivacyAlertSystem")
            .build()

    override fun sourceName(): String = "xposedornot"

    override fun fetchCatalog(): List<KnownBreach> =
        try {
            val response =
                restClient
                    .get()
                    .uri("/v1/breaches")
                    .retrieve()
                    .body(XonBreachesResponse::class.java)

            response?.exposedBreaches?.mapNotNull { entry ->
                try {
                    KnownBreach(
                        name = entry.breachId,
                        domain = entry.domain,
                        breachDate = parseDate(entry.breachedDate),
                        dataClasses = entry.exposedData ?: emptyList(),
                        recordCount = entry.exposedRecords,
                        sourceUrl = entry.referenceUrl ?: sourceName(),
                    )
                } catch (e: Exception) {
                    log.warn("Failed to parse XON breach entry: {}", entry.breachId, e)
                    null
                }
            } ?: emptyList()
        } catch (e: RestClientException) {
            log.error("Failed to fetch XON breach catalog", e)
            emptyList()
        }

    private fun parseDate(dateStr: String?): LocalDate? =
        try {
            dateStr?.takeIf { it.isNotBlank() }?.let { LocalDate.parse(it.take(10)) }
        } catch (e: Exception) {
            null
        }
}

private data class XonBreachesResponse(
    @JsonProperty("Exposed Breaches") val exposedBreaches: List<XonBreachCatalogEntry>?,
)

private data class XonBreachCatalogEntry(
    @JsonProperty("breachID") val breachId: String,
    @JsonProperty("breachedDate") val breachedDate: String?,
    val domain: String?,
    val industry: String?,
    @JsonProperty("exposedData") val exposedData: List<String>?,
    @JsonProperty("exposedRecords") val exposedRecords: Long?,
    @JsonProperty("passwordRisk") val passwordRisk: String?,
    val verified: Boolean?,
    val sensitive: Boolean?,
    @JsonProperty("exposureDescription") val exposureDescription: String?,
    @JsonProperty("referenceURL") val referenceUrl: String?,
    val logo: String?,
)
