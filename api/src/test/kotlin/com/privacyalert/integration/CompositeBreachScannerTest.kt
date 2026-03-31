package com.privacyalert.integration

import com.privacyalert.domain.service.BreachResult
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.Optional

class CompositeBreachScannerTest {
    private val localBreachScanner = mockk<LocalBreachScannerImpl>()
    private val pasteMonitorClient = mockk<PasteMonitorClient>()
    private val hibpClient = mockk<HibpClientImpl>()

    @Test
    fun `scanEmail combines results from all three sources and deduplicates by name`() {
        val localResult = BreachResult("LinkedIn", "linkedin.com", "2012-05-05", listOf("Emails"))
        val pasteResult = BreachResult("PasteExposure", "pastebin.com", "2024-01-01", listOf("Emails"))
        val hibpResult = BreachResult("Adobe", "adobe.com", "2013-10-04", listOf("Emails", "Passwords"))

        every { localBreachScanner.scanEmail("user@example.com") } returns listOf(localResult)
        every { pasteMonitorClient.scanEmail("user@example.com") } returns listOf(pasteResult)
        every { hibpClient.scanEmail("user@example.com") } returns listOf(hibpResult)

        val scanner = CompositeBreachScanner(localBreachScanner, pasteMonitorClient, Optional.of(hibpClient))

        val results = scanner.scanEmail("user@example.com")

        assertEquals(3, results.size)
        assertTrue(results.any { it.name == "LinkedIn" })
        assertTrue(results.any { it.name == "PasteExposure" })
        assertTrue(results.any { it.name == "Adobe" })
    }

    @Test
    fun `scanEmail works when HIBP client is absent`() {
        val localResult = BreachResult("LinkedIn", "linkedin.com", "2012-05-05", listOf("Emails"))

        every { localBreachScanner.scanEmail("user@example.com") } returns listOf(localResult)
        every { pasteMonitorClient.scanEmail("user@example.com") } returns emptyList()

        val scanner = CompositeBreachScanner(localBreachScanner, pasteMonitorClient, Optional.empty())

        val results = scanner.scanEmail("user@example.com")

        assertEquals(1, results.size)
        assertEquals("LinkedIn", results[0].name)
    }

    @Test
    fun `scanEmail deduplicates when local and paste return same breach name`() {
        val localResult = BreachResult("LinkedIn", "linkedin.com", "2012-05-05", listOf("Emails"))
        val pasteResult = BreachResult("LinkedIn", "linkedin.com", "2012-05-05", listOf("Emails"))

        every { localBreachScanner.scanEmail("user@example.com") } returns listOf(localResult)
        every { pasteMonitorClient.scanEmail("user@example.com") } returns listOf(pasteResult)

        val scanner = CompositeBreachScanner(localBreachScanner, pasteMonitorClient, Optional.empty())

        val results = scanner.scanEmail("user@example.com")

        assertEquals(1, results.size)
        assertEquals("LinkedIn", results[0].name)
    }

    @Test
    fun `scanPhone combines local and paste results but not HIBP`() {
        val localResult = BreachResult("Facebook", "facebook.com", "2019-04-01", listOf("Phone numbers"))
        val pasteResult = BreachResult("PasteExposure", "pastebin.com", "2024-01-01", listOf("Phone numbers"))

        every { localBreachScanner.scanPhone("+1234567890") } returns listOf(localResult)
        every { pasteMonitorClient.scanPhone("+1234567890") } returns listOf(pasteResult)

        val scanner = CompositeBreachScanner(localBreachScanner, pasteMonitorClient, Optional.of(hibpClient))

        val results = scanner.scanPhone("+1234567890")

        assertEquals(2, results.size)
        verify(exactly = 0) { hibpClient.scanPhone(any()) }
    }

    @Test
    fun `scanPhone deduplicates by name`() {
        val localResult = BreachResult("Facebook", "facebook.com", "2019-04-01", listOf("Phone numbers"))
        val pasteResult = BreachResult("Facebook", "facebook.com", "2019-04-01", listOf("Phone numbers"))

        every { localBreachScanner.scanPhone("+1234567890") } returns listOf(localResult)
        every { pasteMonitorClient.scanPhone("+1234567890") } returns listOf(pasteResult)

        val scanner = CompositeBreachScanner(localBreachScanner, pasteMonitorClient, Optional.empty())

        val results = scanner.scanPhone("+1234567890")

        assertEquals(1, results.size)
    }
}
