package com.privacyalert.integration

import com.privacyalert.config.AppProperties
import com.privacyalert.domain.service.EmailReputationResult
import com.privacyalert.domain.service.EmailReputationScanner
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientException

@ConditionalOnProperty(name = ["app.email-rep.enabled"], havingValue = "true", matchIfMissing = false)
@Component
class EmailRepClientImpl(
    appProperties: AppProperties,
) : EmailReputationScanner {
    private val log = LoggerFactory.getLogger(EmailRepClientImpl::class.java)

    private val restClient =
        RestClient
            .builder()
            .baseUrl(appProperties.emailRep.baseUrl)
            .defaultHeader("user-agent", "PrivacyAlert/1.0")
            .apply {
                val key = appProperties.emailRep.apiKey
                if (key.isNotBlank()) {
                    it.defaultHeader("Key", key)
                }
            }.build()

    override fun scan(email: String): EmailReputationResult? =
        try {
            val response =
                restClient
                    .get()
                    .uri("/{email}", email)
                    .retrieve()
                    .onStatus({ it == HttpStatus.TOO_MANY_REQUESTS }) { _, _ ->
                        log.warn("EmailRep rate limit reached")
                    }
                    .body(EmailRepResponse::class.java)

            response?.toResult()
        } catch (e: RestClientException) {
            log.warn("EmailRep scan failed: {}", e.message)
            null
        }
}

private data class EmailRepResponse(
    val reputation: String = "none",
    val suspicious: Boolean = false,
    val references: Int = 0,
    val details: EmailRepDetails = EmailRepDetails(),
) {
    fun toResult(): EmailReputationResult =
        EmailReputationResult(
            reputation = reputation,
            suspicious = suspicious,
            credentialsLeaked = details.credentials_leaked,
            darkWebAppearances = details.dark_web_appearances,
            dataBreachCount = details.data_breach,
            profilesFound = details.profiles_found,
        )
}

private data class EmailRepDetails(
    val credentials_leaked: Boolean = false,
    val dark_web_appearances: Int = 0,
    val data_breach: Int = 0,
    val profiles_found: Int = 0,
)
