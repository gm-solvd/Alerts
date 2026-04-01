package com.privacyalert.domain.service

import com.privacyalert.domain.model.KnownBreach
import com.privacyalert.domain.repository.BreachDatabaseRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class BreachCatalogSyncServiceTest {
    private val breachDatabaseRepository = mockk<BreachDatabaseRepository>()
    private val dataTypeNormalizer = DataTypeNormalizer()

    @Test
    fun `sync inserts new breaches from multiple sources`() {
        val source1 = mockk<BreachCatalogSource>()
        val source2 = mockk<BreachCatalogSource>()

        every { source1.sourceName() } returns "source1"
        every { source2.sourceName() } returns "source2"
        every { source1.fetchCatalog() } returns
            listOf(
                KnownBreach(name = "LinkedIn", dataClasses = listOf("Emails")),
            )
        every { source2.fetchCatalog() } returns
            listOf(
                KnownBreach(name = "Adobe", dataClasses = listOf("Passwords")),
            )
        every { breachDatabaseRepository.findAllNames() } returns emptySet()
        every { breachDatabaseRepository.save(any()) } answers { firstArg() }

        val service = BreachCatalogSyncService(listOf(source1, source2), breachDatabaseRepository, dataTypeNormalizer)
        val result = service.sync()

        assertEquals(2, result.inserted)
        assertEquals(0, result.skipped)
    }

    @Test
    fun `sync skips breaches that already exist`() {
        val source = mockk<BreachCatalogSource>()

        every { source.sourceName() } returns "test"
        every { source.fetchCatalog() } returns
            listOf(
                KnownBreach(name = "LinkedIn", dataClasses = listOf("Emails")),
                KnownBreach(name = "Adobe", dataClasses = listOf("Passwords")),
            )
        every { breachDatabaseRepository.findAllNames() } returns setOf("LinkedIn")
        every { breachDatabaseRepository.save(any()) } answers { firstArg() }

        val service = BreachCatalogSyncService(listOf(source), breachDatabaseRepository, dataTypeNormalizer)
        val result = service.sync()

        assertEquals(1, result.inserted)
        assertEquals(1, result.skipped)
    }

    @Test
    fun `sync deduplicates across sources by name`() {
        val source1 = mockk<BreachCatalogSource>()
        val source2 = mockk<BreachCatalogSource>()

        every { source1.sourceName() } returns "source1"
        every { source2.sourceName() } returns "source2"
        every { source1.fetchCatalog() } returns
            listOf(
                KnownBreach(name = "LinkedIn", dataClasses = listOf("Emails")),
            )
        every { source2.fetchCatalog() } returns
            listOf(
                KnownBreach(name = "LinkedIn", dataClasses = listOf("Emails", "Passwords")),
            )
        every { breachDatabaseRepository.findAllNames() } returns emptySet()
        every { breachDatabaseRepository.save(any()) } answers { firstArg() }

        val service = BreachCatalogSyncService(listOf(source1, source2), breachDatabaseRepository, dataTypeNormalizer)
        val result = service.sync()

        assertEquals(1, result.inserted)
        assertEquals(1, result.skipped)
    }

    @Test
    fun `sync continues when one source throws exception`() {
        val failingSource = mockk<BreachCatalogSource>()
        val workingSource = mockk<BreachCatalogSource>()

        every { failingSource.sourceName() } returns "failing"
        every { workingSource.sourceName() } returns "working"
        every { failingSource.fetchCatalog() } throws RuntimeException("API down")
        every { workingSource.fetchCatalog() } returns
            listOf(
                KnownBreach(name = "Adobe", dataClasses = listOf("Passwords")),
            )
        every { breachDatabaseRepository.findAllNames() } returns emptySet()
        every { breachDatabaseRepository.save(any()) } answers { firstArg() }

        val service = BreachCatalogSyncService(listOf(failingSource, workingSource), breachDatabaseRepository, dataTypeNormalizer)
        val result = service.sync()

        assertEquals(1, result.inserted)
        assertEquals(1, result.errors)
        verify(exactly = 1) { breachDatabaseRepository.save(any()) }
    }

    @Test
    fun `sync returns zeros when no sources configured`() {
        val service = BreachCatalogSyncService(emptyList(), breachDatabaseRepository, dataTypeNormalizer)
        every { breachDatabaseRepository.findAllNames() } returns emptySet()

        val result = service.sync()

        assertEquals(0, result.inserted)
        assertEquals(0, result.skipped)
        assertEquals(0, result.errors)
    }
}
