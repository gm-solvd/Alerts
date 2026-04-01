package com.privacyalert.domain.service

import com.privacyalert.domain.model.Severity
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class BreachRiskClassifierTest {
    private val classifier = BreachRiskClassifier()

    @Test
    fun `classify returns CRITICAL when passwords and SSNs and credit cards exposed`() {
        // Passwords(10) + SSNs(10) + Credit cards(10) = 30 → CRITICAL
        val result = classifier.classify(listOf("Passwords", "Social security numbers", "Credit cards"))

        assertEquals(Severity.CRITICAL, result)
    }

    @Test
    fun `classify returns HIGH when phone numbers and addresses and DOB exposed`() {
        // Phone numbers(7) + Physical addresses(7) + Dates of birth(7) = 21 → HIGH
        val result = classifier.classify(listOf("Phone numbers", "Physical addresses", "Dates of birth"))

        assertEquals(Severity.HIGH, result)
    }

    @Test
    fun `classify returns MEDIUM when only emails and usernames exposed`() {
        val result = classifier.classify(listOf("Email addresses", "Usernames"))

        assertEquals(Severity.MEDIUM, result)
    }

    @Test
    fun `classify returns LOW when only photos and genders exposed`() {
        val result = classifier.classify(listOf("Photos", "Genders"))

        assertEquals(Severity.LOW, result)
    }

    @Test
    fun `classify returns LOW for empty data classes`() {
        val result = classifier.classify(emptyList())

        assertEquals(Severity.LOW, result)
    }

    @Test
    fun `classify uses default MEDIUM points for unknown data types`() {
        // 2 unknown types = 2 * 4 = 8 → MEDIUM threshold
        val result = classifier.classify(listOf("UnknownType1", "UnknownType2"))

        assertEquals(Severity.MEDIUM, result)
    }

    @Test
    fun `classify aggregates points across tiers correctly`() {
        // Passwords (10) + Phone numbers (7) + Genders (1) = 18 → HIGH
        val result = classifier.classify(listOf("Passwords", "Phone numbers", "Genders"))

        assertEquals(Severity.HIGH, result)
    }

    @Test
    fun `classify returns CRITICAL at threshold boundary`() {
        // 3 critical-tier items: 10 + 10 + 10 = 30 → exactly CRITICAL
        val result = classifier.classify(listOf("Passwords", "Credit cards", "Auth Tokens"))

        assertEquals(Severity.CRITICAL, result)
    }
}
