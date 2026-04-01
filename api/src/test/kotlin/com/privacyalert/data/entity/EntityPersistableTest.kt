package com.privacyalert.data.entity

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.UUID

/**
 * Regression tests for the Hibernate 6 StaleObjectStateException bug.
 *
 * Root cause: entities with @GeneratedValue(UUID) had pre-set IDs via toEntity().
 * Spring Data JPA's SimpleJpaRepository.save() checks isNew() to decide between
 * em.persist() (INSERT) and em.merge() (UPDATE). When isNew() returns false for
 * a brand-new entity, Hibernate 6 calls merge(), which throws StaleObjectStateException
 * because the row doesn't exist yet.
 *
 * Fix: implement Persistable<UUID> on each entity. New entities return isNew()=true
 * so persist() is used. @PostLoad/@PostPersist flips the flag to false so subsequent
 * saves on loaded entities correctly use merge().
 *
 * Bug 1: UserEntity — "create user form" failed with StaleObjectStateException
 * Bug 2: ScanResultEntity — "run scan" failed with StaleObjectStateException
 */
class EntityPersistableTest {
    // ── Bug 1: UserEntity ──────────────────────────────────────────────────────

    @Test
    fun `UserEntity isNew returns true on construction so persist is used for new users`() {
        val entity = UserEntity(id = UUID.randomUUID(), email = "test@example.com")
        assertTrue(entity.isNew())
    }

    @Test
    fun `UserEntity isNew returns false after markNotNew so merge is used for loaded users`() {
        val entity = UserEntity(id = UUID.randomUUID(), email = "test@example.com")
        entity.markNotNew()
        assertFalse(entity.isNew())
    }

    @Test
    fun `UserEntity getId returns the entity UUID`() {
        val id = UUID.randomUUID()
        val entity = UserEntity(id = id, email = "test@example.com")
        assertEquals(id, entity.getId())
    }

    // ── Bug 2: ScanResultEntity ────────────────────────────────────────────────

    @Test
    fun `ScanResultEntity isNew returns true on construction so persist is used on scan save`() {
        val entity =
            ScanResultEntity(
                id = UUID.randomUUID(),
                userId = UUID.randomUUID(),
                scanType = "breach",
                scanInput = "test@example.com",
                findings = "No breaches found",
            )
        assertTrue(entity.isNew())
    }

    @Test
    fun `ScanResultEntity isNew returns false after markNotNew so merge is used for loaded records`() {
        val entity =
            ScanResultEntity(
                id = UUID.randomUUID(),
                userId = UUID.randomUUID(),
                scanType = "breach",
                scanInput = "test@example.com",
                findings = "No breaches found",
            )
        entity.markNotNew()
        assertFalse(entity.isNew())
    }

    @Test
    fun `ScanResultEntity getId returns the entity UUID`() {
        val id = UUID.randomUUID()
        val entity =
            ScanResultEntity(
                id = id,
                userId = UUID.randomUUID(),
                scanType = "pii",
                scanInput = "test@example.com",
                findings = "No PII found",
            )
        assertEquals(id, entity.getId())
    }

    // ── Other affected entities ────────────────────────────────────────────────

    @Test
    fun `AlertEntity isNew returns true on construction`() {
        val entity = AlertEntity(id = UUID.randomUUID(), userId = UUID.randomUUID())
        assertTrue(entity.isNew())
    }

    @Test
    fun `ScoreHistoryEntity isNew returns true on construction`() {
        val entity = ScoreHistoryEntity(id = UUID.randomUUID(), userId = UUID.randomUUID(), score = 85)
        assertTrue(entity.isNew())
    }

    @Test
    fun `RefreshTokenEntity isNew returns true on construction`() {
        val entity = RefreshTokenEntity(id = UUID.randomUUID(), userId = UUID.randomUUID(), tokenHash = "hash")
        assertTrue(entity.isNew())
    }
}
