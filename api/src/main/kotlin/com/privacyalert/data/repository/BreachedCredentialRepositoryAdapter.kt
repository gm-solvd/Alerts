package com.privacyalert.data.repository

import com.privacyalert.data.entity.BreachedCredentialEntity
import com.privacyalert.domain.repository.BreachedCredentialRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class BreachedCredentialRepositoryAdapter(
    private val breachedCredentialJpa: BreachedCredentialJpaRepository,
) : BreachedCredentialRepository {
    override fun existsByEmailHashAndBreachId(
        emailSha256: String,
        breachId: UUID,
    ): Boolean = breachedCredentialJpa.existsByEmailSha256AndBreachId(emailSha256, breachId)

    override fun saveCredential(
        emailSha256: String,
        breachId: UUID,
    ) {
        breachedCredentialJpa.save(
            BreachedCredentialEntity(
                breachId = breachId,
                emailSha256 = emailSha256,
            ),
        )
    }
}
