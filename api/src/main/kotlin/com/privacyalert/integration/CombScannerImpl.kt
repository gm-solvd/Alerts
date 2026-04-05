package com.privacyalert.integration

import com.privacyalert.config.AppProperties
import com.privacyalert.domain.service.BreachResult
import com.privacyalert.domain.service.BreachScanner
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientException

@ConditionalOnProperty(name = ["app.comb.enabled"], havingValue = "true", matchIfMissing = false)
@Component
class CombScannerImpl(
    appProperties: AppProperties,
) : BreachScanner {
    private val log = LoggerFactory.getLogger(CombScannerImpl::class.java)

    private val restClient =
        RestClient
            .builder()
            .baseUrl(appProperties.comb.baseUrl)
            .defaultHeader("user-agent", "PrivacyAlert/1.0")
            .build()

    override fun scanEmail(email: String): List<BreachResult> =
        try {
            val response =
                restClient
                    .get()
                    .uri("/comb?query={email}&start=0&limit=1", email)
                    .retrieve()
                    .body(CombResponse::class.java)

            if (response != null && response.count > 0) {
                listOf(
                    BreachResult(
                        name = "COMB Credential Database",
                        domain = "proxynova.com",
                        breachDate = "Aggregated",
                        dataClasses = listOf("Passwords", "Email addresses"),
                    ),
                )
            } else {
                emptyList()
            }
        } catch (e: RestClientException) {
            log.warn("COMB scan failed for email: {}", e.message)
            emptyList()
        }

    override fun scanPhone(phone: String): List<BreachResult> = emptyList()
}

private data class CombResponse(
    val count: Int = 0,
    val lines: List<String> = emptyList(),
)
