package com.privacyalert.api.controller

import com.ninjasquad.springmockk.MockkBean
import com.privacyalert.config.JwtAuthFilter
import com.privacyalert.config.SecurityConfig
import com.privacyalert.domain.model.DataBrokerCategory
import com.privacyalert.domain.model.DataBrokerSite
import com.privacyalert.domain.repository.DataBrokerSiteRepository
import com.privacyalert.domain.service.JwtProvider
import io.mockk.every
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import java.util.UUID

@WebMvcTest(DataBrokerController::class)
@Import(SecurityConfig::class, JwtAuthFilter::class)
class DataBrokerControllerTest(
    @Autowired val mockMvc: MockMvc,
) {
    @MockkBean
    lateinit var dataBrokerSiteRepository: DataBrokerSiteRepository

    @MockkBean
    lateinit var jwtProvider: JwtProvider

    private val userId = UUID.randomUUID()

    private fun authenticateAs(userId: UUID) {
        every { jwtProvider.validateAndExtractUserId("test-token") } returns userId
    }

    // ── GET /data-brokers ───────────────────────────────────────────────

    @Test
    fun `GET data-brokers returns 200 with list of active brokers`() {
        authenticateAs(userId)
        val brokers =
            listOf(
                DataBrokerSite(
                    name = "Experian",
                    baseUrl = "https://www.experian.com",
                    piiFields = listOf("name", "address", "ssn"),
                    category = DataBrokerCategory.CREDIT_BUREAU,
                    privacyPolicyUrl = "https://www.experian.com/privacy/",
                    dataAccessUrl = "https://www.experian.com/consumer-products/free-credit-report",
                ),
                DataBrokerSite(
                    name = "Acxiom",
                    baseUrl = "https://www.acxiom.com",
                    piiFields = listOf("name", "email", "demographics"),
                    category = DataBrokerCategory.MARKETING_DATA,
                ),
            )
        every { dataBrokerSiteRepository.findAllActive() } returns brokers

        mockMvc
            .get("/api/v1/data-brokers") {
                header("Authorization", "Bearer test-token")
            }.andExpect {
                status { isOk() }
                jsonPath("$.length()") { value(2) }
                jsonPath("$[0].name") { value("Experian") }
                jsonPath("$[0].category") { value("CREDIT_BUREAU") }
                jsonPath("$[0].privacyPolicyUrl") { value("https://www.experian.com/privacy/") }
                jsonPath("$[1].name") { value("Acxiom") }
                jsonPath("$[1].category") { value("MARKETING_DATA") }
            }
    }

    @Test
    fun `GET data-brokers returns 401 without authentication`() {
        every { jwtProvider.validateAndExtractUserId(any()) } returns null

        mockMvc
            .get("/api/v1/data-brokers")
            .andExpect {
                status { isUnauthorized() }
            }
    }

    // ── GET /data-brokers/{id} ──────────────────────────────────────────

    @Test
    fun `GET data-broker by id returns 200 with broker details`() {
        authenticateAs(userId)
        val brokerId = UUID.randomUUID()
        val broker =
            DataBrokerSite(
                id = brokerId,
                name = "LexisNexis",
                baseUrl = "https://www.lexisnexis.com",
                piiFields = listOf("name", "address", "court_records"),
                category = DataBrokerCategory.DATA_AGGREGATOR,
                privacyPolicyUrl = "https://www.lexisnexis.com/en-us/privacy/",
                dataAccessUrl = "https://consumer.risk.lexisnexis.com/request",
            )
        every { dataBrokerSiteRepository.findById(brokerId) } returns broker

        mockMvc
            .get("/api/v1/data-brokers/$brokerId") {
                header("Authorization", "Bearer test-token")
            }.andExpect {
                status { isOk() }
                jsonPath("$.id") { value(brokerId.toString()) }
                jsonPath("$.name") { value("LexisNexis") }
                jsonPath("$.category") { value("DATA_AGGREGATOR") }
                jsonPath("$.dataAccessUrl") { value("https://consumer.risk.lexisnexis.com/request") }
            }
    }

    @Test
    fun `GET data-broker by id returns 404 when not found`() {
        authenticateAs(userId)
        val unknownId = UUID.randomUUID()
        every { dataBrokerSiteRepository.findById(unknownId) } returns null

        mockMvc
            .get("/api/v1/data-brokers/$unknownId") {
                header("Authorization", "Bearer test-token")
            }.andExpect {
                status { isNotFound() }
            }
    }
}
