package com.privacyalert.integration

import com.privacyalert.config.AppProperties
import com.privacyalert.domain.model.AppException
import com.privacyalert.domain.service.BreachResult
import com.privacyalert.domain.service.BreachScanner
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientException

@ConditionalOnProperty(name = ["app.hibp.enabled"], havingValue = "true", matchIfMissing = false)
@Component
class HibpClientImpl(
    private val appProperties: AppProperties,
) : BreachScanner {
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

            response?.map {
                BreachResult(
                    name = it.Name,
                    domain = it.Domain,
                    breachDate = it.BreachDate,
                    dataClasses = it.DataClasses,
                )
            } ?: emptyList()
        } catch (e: RestClientException) {
            throw AppException.ExternalServiceException("HIBP", e.message ?: "Unknown error")
        }

    override fun scanPhone(phone: String): List<BreachResult> = emptyList()
}

private data class HibpBreachResponse(
    val Name: String,
    val Domain: String,
    val BreachDate: String,
    val DataClasses: List<String>,
)
