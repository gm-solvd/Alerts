package com.privacyalert.data.repository

import com.privacyalert.data.entity.UserEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface UserJpaRepository : JpaRepository<UserEntity, UUID> {
    fun findByEmail(email: String): UserEntity?

    fun findByOauthProviderAndOauthSubject(
        provider: String,
        subject: String,
    ): UserEntity?

    fun existsByEmail(email: String): Boolean
}
