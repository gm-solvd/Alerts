package com.privacyalert.integration

// Uses the XposedOrNot API (https://xposedornot.com), licensed under MIT.
// https://github.com/XposedOrNot/XposedOrNot-API

import com.fasterxml.jackson.annotation.JsonProperty
import com.privacyalert.config.AppProperties
import com.privacyalert.domain.model.KnownBreach
import com.privacyalert.domain.repository.BreachDatabaseRepository
import com.privacyalert.domain.repository.BreachedCredentialRepository
import com.privacyalert.domain.service.BreachResult
import com.privacyalert.domain.service.BreachScanner
import com.privacyalert.domain.service.DataTypeNormalizer
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientException
import java.security.MessageDigest
import java.time.LocalDate

@ConditionalOnProperty(name = ["app.xon.enabled"], havingValue = "true", matchIfMissing = false)
@Component
class XonEmailScannerImpl(
    appProperties: AppProperties,
    private val breachDatabaseRepository: BreachDatabaseRepository,
    private val breachedCredentialRepository: BreachedCredentialRepository,
    private val dataTypeNormalizer: DataTypeNormalizer,
) : BreachScanner {
    private val log = LoggerFactory.getLogger(XonEmailScannerImpl::class.java)

    private val restClient =
        RestClient
            .builder()
            .baseUrl(appProperties.xon.baseUrl)
            .defaultHeader("user-agent", "PrivacyAlertSystem")
            .build()

    override fun scanEmail(email: String): List<BreachResult> {
        return try {
            val response =
                restClient
                    .get()
                    .uri("/v1/check-email/{email}?details=true", email)
                    .retrieve()
                    .body(XonCheckEmailResponse::class.java)

            if (response?.error != null) return emptyList()

            val emailHash = sha256(email.lowercase())
            val results = mutableListOf<BreachResult>()

            for (detail in response?.breachDetails.orEmpty()) {
                val knownBreach = ensureBreachExists(detail)
                cacheCredentialLink(emailHash, knownBreach)

                results.add(
                    BreachResult(
                        name = knownBreach.name,
                        domain = knownBreach.domain ?: "",
                        breachDate = knownBreach.breachDate?.toString() ?: "Unknown",
                        dataClasses = knownBreach.dataClasses,
                    ),
                )
            }
            results
        } catch (e: RestClientException) {
            log.warn("XON email scan failed for {}: {}", email, e.message)
            emptyList()
        }
    }

    override fun scanPhone(phone: String): List<BreachResult> = emptyList()

    private fun ensureBreachExists(detail: XonBreachDetail): KnownBreach =
        breachDatabaseRepository.findByName(detail.name)
            ?: breachDatabaseRepository.save(
                KnownBreach(
                    name = detail.name,
                    domain = detail.company?.name,
                    breachDate = parseDate(detail.breachDate),
                    dataClasses = dataTypeNormalizer.normalize(detail.exposedData ?: emptyList()),
                    recordCount = detail.recordsExposed,
                    sourceUrl = "xposedornot",
                ),
            )

    private fun cacheCredentialLink(
        emailHash: String,
        breach: KnownBreach,
    ) {
        if (!breachedCredentialRepository.existsByEmailHashAndBreachId(emailHash, breach.id)) {
            breachedCredentialRepository.saveCredential(emailHash, breach.id)
        }
    }

    private fun parseDate(dateStr: String?): LocalDate? =
        try {
            dateStr?.takeIf { it.isNotBlank() }?.let { LocalDate.parse(it.take(10)) }
        } catch (e: Exception) {
            null
        }

    private fun sha256(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        return digest.digest(input.toByteArray()).joinToString("") { "%02x".format(it) }
    }
}

private data class XonCheckEmailResponse(
    @JsonProperty("Error") val error: String? = null,
    val breaches: List<List<String>>? = null,
    val email: String? = null,
    val status: String? = null,
    @JsonProperty("breach_details") val breachDetails: List<XonBreachDetail>? = null,
)

private data class XonBreachDetail(
    val name: String,
    @JsonProperty("records_exposed") val recordsExposed: Long? = null,
    @JsonProperty("breach_date") val breachDate: String? = null,
    val company: XonCompany? = null,
    @JsonProperty("exposed_data") val exposedData: List<String>? = null,
)

private data class XonCompany(
    val name: String? = null,
)
