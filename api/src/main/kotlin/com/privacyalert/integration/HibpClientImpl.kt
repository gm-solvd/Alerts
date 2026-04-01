package com.privacyalert.integration

import com.privacyalert.config.AppProperties
import com.privacyalert.domain.model.AppException
import com.privacyalert.domain.model.KnownBreach
import com.privacyalert.domain.repository.BreachDatabaseRepository
import com.privacyalert.domain.repository.BreachedCredentialRepository
import com.privacyalert.domain.service.BreachResult
import com.privacyalert.domain.service.BreachScanner
import com.privacyalert.domain.service.DataTypeNormalizer
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientException
import java.security.MessageDigest
import java.time.LocalDate

@ConditionalOnProperty(name = ["app.hibp.enabled"], havingValue = "true", matchIfMissing = false)
@Component
class HibpClientImpl(
    private val appProperties: AppProperties,
    private val breachDatabaseRepository: BreachDatabaseRepository,
    private val breachedCredentialRepository: BreachedCredentialRepository,
    private val dataTypeNormalizer: DataTypeNormalizer,
) : BreachScanner {
    private val log = LoggerFactory.getLogger(HibpClientImpl::class.java)

    private val restClient =
        RestClient
            .builder()
            .baseUrl(appProperties.hibp.baseUrl)
            .defaultHeader("hibp-api-key", appProperties.hibp.apiKey)
            .defaultHeader("user-agent", "PrivacyAlertSystem")
            .build()

    override fun scanEmail(email: String): List<BreachResult> =
        try {
            val response =
                restClient
                    .get()
                    .uri("/breachedaccount/{email}?truncateResponse=false", email)
                    .retrieve()
                    .onStatus({ it == HttpStatus.NOT_FOUND }) { _, _ -> }
                    .body(Array<HibpBreachResponse>::class.java)

            val results =
                response?.map {
                    BreachResult(
                        name = it.Name,
                        domain = it.Domain,
                        breachDate = it.BreachDate,
                        dataClasses = it.DataClasses,
                    )
                } ?: emptyList()

            cacheResults(email, results)
            results
        } catch (e: RestClientException) {
            throw AppException.ExternalServiceException("HIBP", e.message ?: "Unknown error")
        }

    override fun scanPhone(phone: String): List<BreachResult> = emptyList()

    private fun cacheResults(
        email: String,
        results: List<BreachResult>,
    ) {
        if (results.isEmpty()) return
        val emailHash = sha256(email.lowercase())

        for (result in results) {
            try {
                val knownBreach =
                    breachDatabaseRepository.findByName(result.name)
                        ?: breachDatabaseRepository.save(
                            KnownBreach(
                                name = result.name,
                                domain = result.domain,
                                breachDate = parseDate(result.breachDate),
                                dataClasses = dataTypeNormalizer.normalize(result.dataClasses),
                                sourceUrl = "haveibeenpwned",
                            ),
                        )

                if (!breachedCredentialRepository.existsByEmailHashAndBreachId(emailHash, knownBreach.id)) {
                    breachedCredentialRepository.saveCredential(emailHash, knownBreach.id)
                }
            } catch (e: Exception) {
                log.warn("Failed to cache HIBP result for breach {}: {}", result.name, e.message)
            }
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

private data class HibpBreachResponse(
    val Name: String,
    val Domain: String,
    val BreachDate: String,
    val DataClasses: List<String>,
)
