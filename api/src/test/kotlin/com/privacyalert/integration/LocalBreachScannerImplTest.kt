package com.privacyalert.integration

import com.privacyalert.domain.model.KnownBreach
import com.privacyalert.domain.repository.BreachDatabaseRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDate

class LocalBreachScannerImplTest {

    private val breachDatabaseRepository = mockk<BreachDatabaseRepository>()
    private val scanner = LocalBreachScannerImpl(breachDatabaseRepository)

    @Test
    fun `scanEmail hashes email with SHA-256 lowercase and queries repository`() {
        every { breachDatabaseRepository.findBreachesByEmailHash(any()) } returns emptyList()

        scanner.scanEmail("test@example.com")

        val expectedHash = "973dfe463ec85785f5f95af5ba3906eedb2d931c24e69824a89ea65dba4e813b"
        verify { breachDatabaseRepository.findBreachesByEmailHash(expectedHash) }
    }

    @Test
    fun `scanEmail maps KnownBreach to BreachResult correctly`() {
        val breach = KnownBreach(
            name = "LinkedIn",
            domain = "linkedin.com",
            breachDate = LocalDate.of(2012, 5, 5),
            dataClasses = listOf("Emails", "Passwords"),
        )
        every { breachDatabaseRepository.findBreachesByEmailHash(any()) } returns listOf(breach)

        val results = scanner.scanEmail("test@example.com")

        assertEquals(1, results.size)
        assertEquals("LinkedIn", results[0].name)
        assertEquals("linkedin.com", results[0].domain)
        assertEquals("2012-05-05", results[0].breachDate)
        assertEquals(listOf("Emails", "Passwords"), results[0].dataClasses)
    }

    @Test
    fun `scanEmail maps null domain to empty string and null breachDate to Unknown`() {
        val breach = KnownBreach(
            name = "UnknownBreach",
            domain = null,
            breachDate = null,
            dataClasses = emptyList(),
        )
        every { breachDatabaseRepository.findBreachesByEmailHash(any()) } returns listOf(breach)

        val results = scanner.scanEmail("test@example.com")

        assertEquals(1, results.size)
        assertEquals("", results[0].domain)
        assertEquals("Unknown", results[0].breachDate)
    }

    @Test
    fun `scanPhone hashes phone with SHA-256 and queries repository`() {
        every { breachDatabaseRepository.findBreachesByPhoneHash(any()) } returns emptyList()

        scanner.scanPhone("+1234567890")

        verify { breachDatabaseRepository.findBreachesByPhoneHash(any()) }
    }

    @Test
    fun `scanPhone returns empty list for blank phone`() {
        val results = scanner.scanPhone("   ")

        assertTrue(results.isEmpty())
        verify(exactly = 0) { breachDatabaseRepository.findBreachesByPhoneHash(any()) }
    }

    @Test
    fun `scanPhone returns mapped results when breaches found`() {
        val breach = KnownBreach(
            name = "Facebook",
            domain = "facebook.com",
            breachDate = LocalDate.of(2019, 4, 1),
            dataClasses = listOf("Phone numbers"),
        )
        every { breachDatabaseRepository.findBreachesByPhoneHash(any()) } returns listOf(breach)

        val results = scanner.scanPhone("+1234567890")

        assertEquals(1, results.size)
        assertEquals("Facebook", results[0].name)
    }
}
