package com.privacyalert.data.repository

import com.privacyalert.data.entity.BreachedCredentialEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface BreachedCredentialJpaRepository : JpaRepository<BreachedCredentialEntity, UUID> {
    fun findAllByEmailSha256(emailSha256: String): List<BreachedCredentialEntity>

    fun findAllByPhoneSha256(phoneSha256: String): List<BreachedCredentialEntity>
}
