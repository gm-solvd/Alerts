package com.privacyalert.data.repository

import com.privacyalert.data.entity.toDomain
import com.privacyalert.data.entity.toEntity
import com.privacyalert.domain.model.KnownBreach
import com.privacyalert.domain.repository.BreachDatabaseRepository
import org.springframework.stereotype.Repository

@Repository
class BreachDatabaseRepositoryAdapter(
    private val knownBreachJpa: KnownBreachJpaRepository,
    private val breachedCredentialJpa: BreachedCredentialJpaRepository,
) : BreachDatabaseRepository {
    override fun findBreachesByEmailHash(emailSha256: String): List<KnownBreach> {
        val credentials = breachedCredentialJpa.findAllByEmailSha256(emailSha256)
        val breachIds = credentials.map { it.breachId }.distinct()
        return knownBreachJpa.findAllById(breachIds).map { it.toDomain() }
    }

    override fun findBreachesByPhoneHash(phoneSha256: String): List<KnownBreach> {
        val credentials = breachedCredentialJpa.findAllByPhoneSha256(phoneSha256)
        val breachIds = credentials.map { it.breachId }.distinct()
        return knownBreachJpa.findAllById(breachIds).map { it.toDomain() }
    }

    override fun findByName(name: String): KnownBreach? = knownBreachJpa.findByName(name)?.toDomain()

    override fun findAllNames(): Set<String> = knownBreachJpa.findAllNames().toSet()

    override fun save(breach: KnownBreach): KnownBreach = knownBreachJpa.save(breach.toEntity()).toDomain()
}
