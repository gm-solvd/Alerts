package com.privacyalert.domain.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class DataTypeNormalizerTest {
    private val normalizer = DataTypeNormalizer()

    @Test
    fun `normalize resolves known aliases`() {
        val result = normalizer.normalize(listOf("Username", "Email addresse", "Password"))

        assertEquals(listOf("Usernames", "Email addresses", "Passwords"), result)
    }

    @Test
    fun `normalize splits semicolon-delimited entries`() {
        val result = normalizer.normalize(listOf("Emails;Passwords;Usernames"))

        assertEquals(listOf("Email addresses", "Passwords", "Usernames"), result)
    }

    @Test
    fun `normalize splits comma-delimited entries`() {
        val result = normalizer.normalize(listOf("Names,Email addresses,Passwords"))

        assertEquals(listOf("Names", "Email addresses", "Passwords"), result)
    }

    @Test
    fun `normalize handles compound fixes`() {
        val result = normalizer.normalize(listOf("GendersDates of birth"))

        assertEquals(listOf("Genders", "Dates of birth"), result)
    }

    @Test
    fun `normalize deduplicates results`() {
        val result = normalizer.normalize(listOf("Email", "Emails", "Email addresses"))

        assertEquals(listOf("Email addresses"), result)
    }

    @Test
    fun `normalize trims whitespace`() {
        val result = normalizer.normalize(listOf("  Passwords  ", " Names "))

        assertEquals(listOf("Passwords", "Names"), result)
    }

    @Test
    fun `normalize returns empty list for empty input`() {
        val result = normalizer.normalize(emptyList())

        assertTrue(result.isEmpty())
    }

    @Test
    fun `normalize filters blank entries`() {
        val result = normalizer.normalize(listOf("", "  ", "Passwords"))

        assertEquals(listOf("Passwords"), result)
    }

    @Test
    fun `normalize passes through unrecognized types unchanged`() {
        val result = normalizer.normalize(listOf("SomeNewDataType"))

        assertEquals(listOf("SomeNewDataType"), result)
    }
}
