package com.privacyalert.domain.repository

import com.privacyalert.domain.model.User
import java.util.UUID

interface UserRepository {
    fun findById(id: UUID): User?
    fun findByEmail(email: String): User?
    fun findByOauthProviderAndOauthSubject(provider: String, subject: String): User?
    fun save(user: User): User
    fun existsByEmail(email: String): Boolean
}
