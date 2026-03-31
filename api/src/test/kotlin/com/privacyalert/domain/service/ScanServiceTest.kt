package com.privacyalert.domain.service

import com.privacyalert.domain.model.Severity
import com.privacyalert.domain.model.ThreatCategory
import com.privacyalert.domain.model.UserScanProfile
import com.privacyalert.domain.repository.AlertRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.UUID

class ScanServiceTest {
    private val alertRepository = mockk<AlertRepository>()
    private val breachScanner = mockk<BreachScanner>()
    private val piiExposureScanner = mockk<PiiExposureScanner>()
    private val identityExposureScanner = mockk<IdentityExposureScanner>()
    private val socialFootprintScanner = mockk<SocialFootprintScanner>()
    private val scoreService = mockk<ScoreService>()
    private val service =
        ScanService(
            alertRepository,
            breachScanner,
            piiExposureScanner,
            identityExposureScanner,
            socialFootprintScanner,
            scoreService,
        )

    private val userId = UUID.randomUUID()
    private val profile =
        UserScanProfile(
            email = "user@example.com",
            phoneNumber = "+1234567890",
            fullName = "John Doe",
            homeAddress = "123 Main St",
        )

    // ── breachScan ──────────────────────────────────────────────────────

    @Test
    fun `breachScan creates alerts for each breach found`() {
        val breaches =
            listOf(
                BreachResult("LinkedIn", "linkedin.com", "2012-05-05", listOf("Emails", "Passwords")),
                BreachResult("Adobe", "adobe.com", "2013-10-04", listOf("Emails", "Passwords", "Usernames")),
            )

        every { breachScanner.scanEmail("user@example.com") } returns breaches
        every { breachScanner.scanPhone("+1234567890") } returns emptyList()
        every { alertRepository.save(any()) } answers { firstArg() }
        every { scoreService.recalculate(userId) } returns mockk()

        val alerts = service.breachScan(userId, profile)

        assertEquals(2, alerts.size)
        assertTrue(alerts.all { it.category == ThreatCategory.DATA_BREACH })
        assertTrue(alerts.all { it.severity == Severity.CRITICAL })
        assertTrue(alerts[0].title.contains("LinkedIn"))
        assertTrue(alerts[1].title.contains("Adobe"))
        verify { scoreService.recalculate(userId) }
    }

    @Test
    fun `breachScan returns empty list and skips recalculation when no breaches found`() {
        val cleanProfile = UserScanProfile(email = "clean@example.com")

        every { breachScanner.scanEmail("clean@example.com") } returns emptyList()

        val alerts = service.breachScan(userId, cleanProfile)

        assertTrue(alerts.isEmpty())
        verify(exactly = 0) { scoreService.recalculate(any()) }
    }

    @Test
    fun `breachScan merges email and phone breaches and deduplicates by name`() {
        val emailBreaches =
            listOf(
                BreachResult("LinkedIn", "linkedin.com", "2012-05-05", listOf("Emails")),
            )
        val phoneBreaches =
            listOf(
                BreachResult("LinkedIn", "linkedin.com", "2012-05-05", listOf("Emails")),
                BreachResult("Facebook", "facebook.com", "2019-04-01", listOf("Phone numbers")),
            )

        every { breachScanner.scanEmail("user@example.com") } returns emailBreaches
        every { breachScanner.scanPhone("+1234567890") } returns phoneBreaches
        every { alertRepository.save(any()) } answers { firstArg() }
        every { scoreService.recalculate(userId) } returns mockk()

        val alerts = service.breachScan(userId, profile)

        assertEquals(2, alerts.size)
        assertTrue(alerts.any { it.title.contains("LinkedIn") })
        assertTrue(alerts.any { it.title.contains("Facebook") })
    }

    @Test
    fun `breachScan skips phone scan when phoneNumber is null`() {
        val emailOnlyProfile = UserScanProfile(email = "user@example.com")

        every { breachScanner.scanEmail("user@example.com") } returns emptyList()

        service.breachScan(userId, emailOnlyProfile)

        verify(exactly = 0) { breachScanner.scanPhone(any()) }
    }

    // ── identityScan ────────────────────────────────────────────────────

    @Test
    fun `identityScan creates alerts from scanner results`() {
        val results =
            listOf(
                IdentityExposureResult("Spokeo", "https://spokeo.com/john", "John Doe", listOf("name", "email", "phone")),
            )

        every { identityExposureScanner.scan("user@example.com", "John Doe") } returns results
        every { alertRepository.save(any()) } answers { firstArg() }
        every { scoreService.recalculate(userId) } returns mockk()

        val alerts = service.identityScan(userId, profile)

        assertEquals(1, alerts.size)
        assertEquals(ThreatCategory.IDENTITY_EXPOSURE, alerts[0].category)
        assertTrue(alerts[0].title.contains("Spokeo"))
        verify { scoreService.recalculate(userId) }
    }

    @Test
    fun `identityScan assigns HIGH severity when 3 or more fields exposed`() {
        val results =
            listOf(
                IdentityExposureResult("Spokeo", "https://spokeo.com/john", null, listOf("name", "email", "phone")),
            )

        every { identityExposureScanner.scan("user@example.com", "John Doe") } returns results
        every { alertRepository.save(any()) } answers { firstArg() }
        every { scoreService.recalculate(userId) } returns mockk()

        val alerts = service.identityScan(userId, profile)

        assertEquals(Severity.HIGH, alerts[0].severity)
    }

    @Test
    fun `identityScan assigns MEDIUM severity when fewer than 3 fields exposed`() {
        val results =
            listOf(
                IdentityExposureResult("Spokeo", "https://spokeo.com/john", null, listOf("name", "email")),
            )

        every { identityExposureScanner.scan("user@example.com", "John Doe") } returns results
        every { alertRepository.save(any()) } answers { firstArg() }
        every { scoreService.recalculate(userId) } returns mockk()

        val alerts = service.identityScan(userId, profile)

        assertEquals(Severity.MEDIUM, alerts[0].severity)
    }

    @Test
    fun `identityScan returns empty list when no results found`() {
        every { identityExposureScanner.scan("user@example.com", "John Doe") } returns emptyList()

        val alerts = service.identityScan(userId, profile)

        assertTrue(alerts.isEmpty())
        verify(exactly = 0) { scoreService.recalculate(any()) }
    }

    // ── piiExposureScan ─────────────────────────────────────────────────

    @Test
    fun `piiExposureScan creates alerts from scanner results`() {
        val results =
            listOf(
                PiiExposureResult("DataBroker", "https://databroker.com/j", listOf("phone", "address"), "John at 123 Main"),
            )

        every { piiExposureScanner.scan(profile) } returns results
        every { alertRepository.save(any()) } answers { firstArg() }
        every { scoreService.recalculate(userId) } returns mockk()

        val alerts = service.piiExposureScan(userId, profile)

        assertEquals(1, alerts.size)
        assertEquals(ThreatCategory.TRACKER_EXPOSURE, alerts[0].category)
        assertTrue(alerts[0].title.contains("DataBroker"))
        verify { scoreService.recalculate(userId) }
    }

    @Test
    fun `piiExposureScan assigns HIGH severity when phone and address exposed`() {
        val results =
            listOf(
                PiiExposureResult("DataBroker", "https://db.com/j", listOf("phone", "address")),
            )

        every { piiExposureScanner.scan(profile) } returns results
        every { alertRepository.save(any()) } answers { firstArg() }
        every { scoreService.recalculate(userId) } returns mockk()

        val alerts = service.piiExposureScan(userId, profile)

        assertEquals(Severity.HIGH, alerts[0].severity)
    }

    @Test
    fun `piiExposureScan assigns MEDIUM severity when name and address exposed`() {
        val results =
            listOf(
                PiiExposureResult("DataBroker", "https://db.com/j", listOf("name", "address")),
            )

        every { piiExposureScanner.scan(profile) } returns results
        every { alertRepository.save(any()) } answers { firstArg() }
        every { scoreService.recalculate(userId) } returns mockk()

        val alerts = service.piiExposureScan(userId, profile)

        assertEquals(Severity.MEDIUM, alerts[0].severity)
    }

    @Test
    fun `piiExposureScan assigns LOW severity when only email exposed`() {
        val results =
            listOf(
                PiiExposureResult("DataBroker", "https://db.com/j", listOf("email")),
            )

        every { piiExposureScanner.scan(profile) } returns results
        every { alertRepository.save(any()) } answers { firstArg() }
        every { scoreService.recalculate(userId) } returns mockk()

        val alerts = service.piiExposureScan(userId, profile)

        assertEquals(Severity.LOW, alerts[0].severity)
    }

    @Test
    fun `piiExposureScan returns empty list when no results found`() {
        every { piiExposureScanner.scan(profile) } returns emptyList()

        val alerts = service.piiExposureScan(userId, profile)

        assertTrue(alerts.isEmpty())
        verify(exactly = 0) { scoreService.recalculate(any()) }
    }

    // ── socialFootprintScan ─────────────────────────────────────────────

    @Test
    fun `socialFootprintScan creates alerts from scanner results`() {
        val results =
            listOf(
                SocialFootprintResult("Twitter", "https://twitter.com/johndoe", "johndoe", listOf("bio", "location", "links")),
            )

        every { socialFootprintScanner.scan("user@example.com", "John Doe", "johndoe") } returns results
        every { alertRepository.save(any()) } answers { firstArg() }
        every { scoreService.recalculate(userId) } returns mockk()

        val alerts = service.socialFootprintScan(userId, profile, "johndoe")

        assertEquals(1, alerts.size)
        assertEquals(ThreatCategory.SOCIAL_FOOTPRINT, alerts[0].category)
        assertTrue(alerts[0].title.contains("Twitter"))
        verify { scoreService.recalculate(userId) }
    }

    @Test
    fun `socialFootprintScan assigns MEDIUM severity when 3 or more public info found`() {
        val results =
            listOf(
                SocialFootprintResult("Twitter", "https://twitter.com/jd", "jd", listOf("bio", "location", "links")),
            )

        every { socialFootprintScanner.scan("user@example.com", "John Doe", "jd") } returns results
        every { alertRepository.save(any()) } answers { firstArg() }
        every { scoreService.recalculate(userId) } returns mockk()

        val alerts = service.socialFootprintScan(userId, profile, "jd")

        assertEquals(Severity.MEDIUM, alerts[0].severity)
    }

    @Test
    fun `socialFootprintScan assigns LOW severity when fewer than 3 public info found`() {
        val results =
            listOf(
                SocialFootprintResult("Twitter", "https://twitter.com/jd", "jd", listOf("bio")),
            )

        every { socialFootprintScanner.scan("user@example.com", "John Doe", null) } returns results
        every { alertRepository.save(any()) } answers { firstArg() }
        every { scoreService.recalculate(userId) } returns mockk()

        val alerts = service.socialFootprintScan(userId, profile, null)

        assertEquals(Severity.LOW, alerts[0].severity)
    }

    @Test
    fun `socialFootprintScan returns empty list when no results found`() {
        every { socialFootprintScanner.scan("user@example.com", "John Doe", null) } returns emptyList()

        val alerts = service.socialFootprintScan(userId, profile, null)

        assertTrue(alerts.isEmpty())
        verify(exactly = 0) { scoreService.recalculate(any()) }
    }

    // ── fullScan ────────────────────────────────────────────────────────

    @Test
    fun `fullScan aggregates results from all four scans`() {
        val breaches =
            listOf(
                BreachResult("LinkedIn", "linkedin.com", "2012-05-05", listOf("Emails")),
            )
        val identityResults =
            listOf(
                IdentityExposureResult("Spokeo", "https://spokeo.com/j", null, listOf("name", "email")),
            )
        val piiResults =
            listOf(
                PiiExposureResult("DataBroker", "https://db.com/j", listOf("email")),
            )
        val socialResults =
            listOf(
                SocialFootprintResult("Twitter", "https://twitter.com/jd", "jd", listOf("bio")),
            )

        every { breachScanner.scanEmail("user@example.com") } returns breaches
        every { breachScanner.scanPhone("+1234567890") } returns emptyList()
        every { identityExposureScanner.scan("user@example.com", "John Doe") } returns identityResults
        every { piiExposureScanner.scan(profile) } returns piiResults
        every { socialFootprintScanner.scan("user@example.com", "John Doe", "johndoe") } returns socialResults
        every { alertRepository.save(any()) } answers { firstArg() }
        every { scoreService.recalculate(userId) } returns mockk()

        val alerts = service.fullScan(userId, profile, "johndoe")

        assertEquals(4, alerts.size)
        assertTrue(alerts.any { it.category == ThreatCategory.DATA_BREACH })
        assertTrue(alerts.any { it.category == ThreatCategory.IDENTITY_EXPOSURE })
        assertTrue(alerts.any { it.category == ThreatCategory.TRACKER_EXPOSURE })
        assertTrue(alerts.any { it.category == ThreatCategory.SOCIAL_FOOTPRINT })
    }

    @Test
    fun `fullScan returns empty list when all scans find nothing`() {
        val emptyProfile = UserScanProfile(email = "clean@example.com")

        every { breachScanner.scanEmail("clean@example.com") } returns emptyList()
        every { identityExposureScanner.scan("clean@example.com", null) } returns emptyList()
        every { piiExposureScanner.scan(emptyProfile) } returns emptyList()
        every { socialFootprintScanner.scan("clean@example.com", null, null) } returns emptyList()

        val alerts = service.fullScan(userId, emptyProfile, null)

        assertTrue(alerts.isEmpty())
        verify(exactly = 0) { scoreService.recalculate(any()) }
    }
}
