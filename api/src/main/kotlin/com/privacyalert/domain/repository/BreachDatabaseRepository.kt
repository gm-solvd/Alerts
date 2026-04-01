package com.privacyalert.domain.repository

import com.privacyalert.domain.model.KnownBreach

interface BreachDatabaseRepository {
    fun findBreachesByEmailHash(emailSha256: String): List<KnownBreach>

    fun findBreachesByPhoneHash(phoneSha256: String): List<KnownBreach>

    fun findByName(name: String): KnownBreach?

    fun findAllNames(): Set<String>

    fun save(breach: KnownBreach): KnownBreach
}
