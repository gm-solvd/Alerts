package com.privacyalert.domain.service

import com.privacyalert.domain.model.Alert
import com.privacyalert.domain.model.ScanResult
import com.privacyalert.domain.model.Severity
import com.privacyalert.domain.model.ThreatCategory
import com.privacyalert.domain.model.UserScanProfile
import com.privacyalert.domain.repository.AlertRepository
import com.privacyalert.domain.repository.ScanResultRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.Optional
import java.util.UUID

class ScanServiceTest {
    private val alertRepository = mockk<AlertRepository>()
    private val breachScanner = mockk<BreachScanner>()
    private val piiExposureScanner = mockk<PiiExposureScanner>()
    private val identityExposureScanner = mockk<IdentityExposureScanner>()
    private val socialFootprintScanner = mockk<SocialFootprintScanner>()
    private val scoreService = mockk<ScoreService>()
    private val dataTypeNormalizer = DataTypeNormalizer()
    private val breachRiskClassifier = BreachRiskClassifier()
    private val scanResultRepository = mockk<ScanResultRepository>(relaxed = true)
    private val emailReputationScanner = mockk<EmailReputationScanner>()
    private val service =
        ScanService(
            alertRepository,
            breachScanner,
            piiExposureScanner,
            identityExposureScanner,
            socialFootprintScanner,
            scoreService,
            dataTypeNormalizer,
            breachRiskClassifier,
            scanResultRepository,
            Optional.of(emailReputationScanner),
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
    fun `breachScan creates alerts for each breach found with dynamic severity`() {
        val breaches =
            listOf(
                BreachResult("LinkedIn", "linkedin.com", "2012-05-05", listOf("Email addresses", "Passwords", "Phone numbers")),
                BreachResult("Adobe", "adobe.com", "2013-10-04", listOf("Email addresses", "Passwords", "Phone numbers")),
            )

        every { breachScanner.scanEmail("user@example.com") } returns breaches
        every { breachScanner.scanPhone("+1234567890") } returns emptyList()
        every { alertRepository.save(any()) } answers { firstArg() }
        every { scoreService.recalculate(userId) } returns mockk()

        val alerts = service.breachScan(userId, profile)

        assertEquals(2, alerts.size)
        assertTrue(alerts.all { it.category == ThreatCategory.DATA_BREACH })
        // Passwords(10) + Email addresses(4) + Phone numbers(7) = 21 → HIGH
        assertTrue(alerts.all { it.severity == Severity.HIGH })
        assertTrue(alerts[0].title.contains("LinkedIn"))
        assertTrue(alerts[1].title.contains("Adobe"))
        verify { scoreService.recalculate(userId) }
    }

    @Test
    fun `breachScan assigns CRITICAL severity when critical-tier data exposed`() {
        val breaches =
            listOf(
                BreachResult("MegaBreach", "mega.com", "2024-01-01", listOf("Passwords", "Social security numbers", "Credit cards")),
            )

        every { breachScanner.scanEmail("user@example.com") } returns breaches
        every { breachScanner.scanPhone("+1234567890") } returns emptyList()
        every { alertRepository.save(any()) } answers { firstArg() }
        every { scoreService.recalculate(userId) } returns mockk()

        val alerts = service.breachScan(userId, profile)

        assertEquals(1, alerts.size)
        assertEquals(Severity.CRITICAL, alerts[0].severity)
    }

    @Test
    fun `breachScan assigns LOW severity when only low-tier data exposed`() {
        val breaches =
            listOf(
                BreachResult("MinorBreach", "minor.com", "2024-01-01", listOf("Genders", "Photos")),
            )

        every { breachScanner.scanEmail("user@example.com") } returns breaches
        every { breachScanner.scanPhone("+1234567890") } returns emptyList()
        every { alertRepository.save(any()) } answers { firstArg() }
        every { scoreService.recalculate(userId) } returns mockk()

        val alerts = service.breachScan(userId, profile)

        assertEquals(1, alerts.size)
        assertEquals(Severity.LOW, alerts[0].severity)
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

    @Test
    fun `breachScan sets credential_exposed tag when Passwords in dataClasses`() {
        val breaches =
            listOf(
                BreachResult("LinkedIn", "linkedin.com", "2012-05-05", listOf("Email addresses", "Passwords")),
            )
        val alertSlot = slot<Alert>()

        every { breachScanner.scanEmail("user@example.com") } returns breaches
        every { breachScanner.scanPhone("+1234567890") } returns emptyList()
        every { alertRepository.save(capture(alertSlot)) } answers { firstArg() }
        every { scoreService.recalculate(userId) } returns mockk()

        val alerts = service.breachScan(userId, profile)

        assertEquals(1, alerts.size)
        assertTrue(alerts[0].tags.contains("credential_exposed"))
    }

    @Test
    fun `breachScan does not set credential_exposed tag when no Passwords exposed`() {
        val breaches =
            listOf(
                BreachResult("MinorBreach", "minor.com", "2024-01-01", listOf("Email addresses")),
            )

        every { breachScanner.scanEmail("user@example.com") } returns breaches
        every { breachScanner.scanPhone("+1234567890") } returns emptyList()
        every { alertRepository.save(any()) } answers { firstArg() }
        every { scoreService.recalculate(userId) } returns mockk()

        val alerts = service.breachScan(userId, profile)

        assertEquals(1, alerts.size)
        assertTrue(alerts[0].tags.isEmpty())
    }

    @Test
    fun `breachScan populates findingsJson with structured findings`() {
        val breaches =
            listOf(
                BreachResult("LinkedIn", "linkedin.com", "2012-05-05", listOf("Email addresses", "Passwords")),
            )
        val scanResultSlot = slot<ScanResult>()

        every { breachScanner.scanEmail("user@example.com") } returns breaches
        every { breachScanner.scanPhone("+1234567890") } returns emptyList()
        every { alertRepository.save(any()) } answers { firstArg() }
        every { scoreService.recalculate(userId) } returns mockk()
        every { scanResultRepository.save(capture(scanResultSlot)) } answers { firstArg() }

        service.breachScan(userId, profile)

        val saved = scanResultSlot.captured
        assertEquals(1, saved.findingsJson.size)
        assertEquals("breach", saved.findingsJson[0].type)
        assertEquals("LinkedIn", saved.findingsJson[0].name)
        assertEquals("2012-05-05", saved.findingsJson[0].date)
        assertTrue(saved.findingsJson[0].dataClasses.isNotEmpty())
        assertNotNull(saved.findingsJson[0].severity)
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

    @Test
    fun `identityScan populates findingsJson with structured findings`() {
        val results =
            listOf(
                IdentityExposureResult("Spokeo", "https://spokeo.com/john", "John Doe", listOf("name", "email", "phone")),
            )
        val scanResultSlot = slot<ScanResult>()

        every { identityExposureScanner.scan("user@example.com", "John Doe") } returns results
        every { alertRepository.save(any()) } answers { firstArg() }
        every { scoreService.recalculate(userId) } returns mockk()
        every { scanResultRepository.save(capture(scanResultSlot)) } answers { firstArg() }

        service.identityScan(userId, profile)

        val saved = scanResultSlot.captured
        assertEquals(1, saved.findingsJson.size)
        assertEquals("identity", saved.findingsJson[0].type)
        assertEquals("Spokeo", saved.findingsJson[0].name)
        assertEquals("https://spokeo.com/john", saved.findingsJson[0].sourceUrl)
        assertEquals("HIGH", saved.findingsJson[0].severity)
        assertEquals(listOf("name", "email", "phone"), saved.findingsJson[0].exposedFields)
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

    @Test
    fun `piiExposureScan populates findingsJson with structured findings`() {
        val results =
            listOf(
                PiiExposureResult("DataBroker", "https://databroker.com/j", listOf("phone", "address"), "John at 123 Main"),
            )
        val scanResultSlot = slot<ScanResult>()

        every { piiExposureScanner.scan(profile) } returns results
        every { alertRepository.save(any()) } answers { firstArg() }
        every { scoreService.recalculate(userId) } returns mockk()
        every { scanResultRepository.save(capture(scanResultSlot)) } answers { firstArg() }

        service.piiExposureScan(userId, profile)

        val saved = scanResultSlot.captured
        assertEquals(1, saved.findingsJson.size)
        assertEquals("pii", saved.findingsJson[0].type)
        assertEquals("DataBroker", saved.findingsJson[0].name)
        assertEquals("https://databroker.com/j", saved.findingsJson[0].sourceUrl)
        assertEquals("HIGH", saved.findingsJson[0].severity)
        assertEquals(listOf("phone", "address"), saved.findingsJson[0].exposedFields)
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

    @Test
    fun `socialFootprintScan populates findingsJson with structured findings`() {
        val results =
            listOf(
                SocialFootprintResult("Twitter", "https://twitter.com/johndoe", "johndoe", listOf("bio", "location", "links")),
            )
        val scanResultSlot = slot<ScanResult>()

        every { socialFootprintScanner.scan("user@example.com", "John Doe", "johndoe") } returns results
        every { alertRepository.save(any()) } answers { firstArg() }
        every { scoreService.recalculate(userId) } returns mockk()
        every { scanResultRepository.save(capture(scanResultSlot)) } answers { firstArg() }

        service.socialFootprintScan(userId, profile, "johndoe")

        val saved = scanResultSlot.captured
        assertEquals(1, saved.findingsJson.size)
        assertEquals("social", saved.findingsJson[0].type)
        assertEquals("Twitter", saved.findingsJson[0].name)
        assertEquals("https://twitter.com/johndoe", saved.findingsJson[0].sourceUrl)
        assertEquals("MEDIUM", saved.findingsJson[0].severity)
        assertEquals(listOf("bio", "location", "links"), saved.findingsJson[0].exposedFields)
    }

    // ── emailReputationScan ───────────────────────────────────────────────

    @Test
    fun `emailReputationScan creates HIGH alert when reputation is none`() {
        val result =
            EmailReputationResult(
                reputation = "none",
                suspicious = false,
                credentialsLeaked = true,
                darkWebAppearances = 3,
                dataBreachCount = 5,
                profilesFound = 2,
            )

        every { emailReputationScanner.scan("user@example.com") } returns result
        every { alertRepository.save(any()) } answers { firstArg() }
        every { scoreService.recalculate(userId) } returns mockk()

        val alerts = service.emailReputationScan(userId, profile)

        assertEquals(1, alerts.size)
        assertEquals(Severity.HIGH, alerts[0].severity)
        assertEquals(ThreatCategory.IDENTITY_EXPOSURE, alerts[0].category)
        assertTrue(alerts[0].tags.contains("email_reputation"))
        assertTrue(alerts[0].tags.contains("credential_exposed"))
    }

    @Test
    fun `emailReputationScan creates HIGH alert when suspicious`() {
        val result =
            EmailReputationResult(
                reputation = "low",
                suspicious = true,
                credentialsLeaked = false,
                darkWebAppearances = 0,
                dataBreachCount = 0,
                profilesFound = 0,
            )

        every { emailReputationScanner.scan("user@example.com") } returns result
        every { alertRepository.save(any()) } answers { firstArg() }
        every { scoreService.recalculate(userId) } returns mockk()

        val alerts = service.emailReputationScan(userId, profile)

        assertEquals(1, alerts.size)
        assertEquals(Severity.HIGH, alerts[0].severity)
    }

    @Test
    fun `emailReputationScan creates MEDIUM alert when reputation is low`() {
        val result =
            EmailReputationResult(
                reputation = "low",
                suspicious = false,
                credentialsLeaked = false,
                darkWebAppearances = 0,
                dataBreachCount = 1,
                profilesFound = 0,
            )

        every { emailReputationScanner.scan("user@example.com") } returns result
        every { alertRepository.save(any()) } answers { firstArg() }
        every { scoreService.recalculate(userId) } returns mockk()

        val alerts = service.emailReputationScan(userId, profile)

        assertEquals(1, alerts.size)
        assertEquals(Severity.MEDIUM, alerts[0].severity)
    }

    @Test
    fun `emailReputationScan creates LOW alert when reputation is medium`() {
        val result =
            EmailReputationResult(
                reputation = "medium",
                suspicious = false,
                credentialsLeaked = false,
                darkWebAppearances = 0,
                dataBreachCount = 0,
                profilesFound = 1,
            )

        every { emailReputationScanner.scan("user@example.com") } returns result
        every { alertRepository.save(any()) } answers { firstArg() }

        val alerts = service.emailReputationScan(userId, profile)

        assertEquals(1, alerts.size)
        assertEquals(Severity.LOW, alerts[0].severity)
        verify(exactly = 0) { scoreService.recalculate(any()) }
    }

    @Test
    fun `emailReputationScan skips alert when reputation is high`() {
        val result =
            EmailReputationResult(
                reputation = "high",
                suspicious = false,
                credentialsLeaked = false,
                darkWebAppearances = 0,
                dataBreachCount = 0,
                profilesFound = 0,
            )

        every { emailReputationScanner.scan("user@example.com") } returns result

        val alerts = service.emailReputationScan(userId, profile)

        assertTrue(alerts.isEmpty())
        verify(exactly = 0) { alertRepository.save(any()) }
    }

    @Test
    fun `emailReputationScan returns empty when scanner returns null`() {
        every { emailReputationScanner.scan("user@example.com") } returns null

        val alerts = service.emailReputationScan(userId, profile)

        assertTrue(alerts.isEmpty())
    }

    @Test
    fun `emailReputationScan returns empty when scanner is absent`() {
        val serviceWithoutEmailRep =
            ScanService(
                alertRepository,
                breachScanner,
                piiExposureScanner,
                identityExposureScanner,
                socialFootprintScanner,
                scoreService,
                dataTypeNormalizer,
                breachRiskClassifier,
                scanResultRepository,
                Optional.empty(),
            )

        val alerts = serviceWithoutEmailRep.emailReputationScan(userId, profile)

        assertTrue(alerts.isEmpty())
    }

    @Test
    fun `emailReputationScan populates findingsJson with structured finding`() {
        val result =
            EmailReputationResult(
                reputation = "none",
                suspicious = false,
                credentialsLeaked = true,
                darkWebAppearances = 2,
                dataBreachCount = 3,
                profilesFound = 1,
            )
        val scanResultSlot = slot<ScanResult>()

        every { emailReputationScanner.scan("user@example.com") } returns result
        every { alertRepository.save(any()) } answers { firstArg() }
        every { scoreService.recalculate(userId) } returns mockk()
        every { scanResultRepository.save(capture(scanResultSlot)) } answers { firstArg() }

        service.emailReputationScan(userId, profile)

        val saved = scanResultSlot.captured
        assertEquals(1, saved.findingsJson.size)
        assertEquals("reputation", saved.findingsJson[0].type)
        assertEquals("EmailRep", saved.findingsJson[0].name)
        assertTrue(saved.findingsJson[0].credentialExposed)
    }

    // ── fullScan ────────────────────────────────────────────────────────

    @Test
    fun `fullScan aggregates results from all five scans`() {
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
        val emailRepResult =
            EmailReputationResult(
                reputation = "low",
                suspicious = false,
                credentialsLeaked = false,
                darkWebAppearances = 0,
                dataBreachCount = 1,
                profilesFound = 0,
            )

        every { breachScanner.scanEmail("user@example.com") } returns breaches
        every { breachScanner.scanPhone("+1234567890") } returns emptyList()
        every { identityExposureScanner.scan("user@example.com", "John Doe") } returns identityResults
        every { piiExposureScanner.scan(profile) } returns piiResults
        every { socialFootprintScanner.scan("user@example.com", "John Doe", "johndoe") } returns socialResults
        every { emailReputationScanner.scan("user@example.com") } returns emailRepResult
        every { alertRepository.save(any()) } answers { firstArg() }
        every { scoreService.recalculate(userId) } returns mockk()

        val alerts = service.fullScan(userId, profile, "johndoe")

        assertEquals(5, alerts.size)
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
        every { emailReputationScanner.scan("clean@example.com") } returns null

        val alerts = service.fullScan(userId, emptyProfile, null)

        assertTrue(alerts.isEmpty())
        verify(exactly = 0) { scoreService.recalculate(any()) }
    }
}
