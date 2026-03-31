package com.privacyalert.data.repository

import com.privacyalert.data.entity.toDomain
import com.privacyalert.data.entity.toEntity
import com.privacyalert.domain.model.User
import com.privacyalert.domain.repository.UserRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class UserRepositoryAdapter(
    private val jpa: UserJpaRepository,
) : UserRepository {
    override fun findById(id: UUID): User? = jpa.findById(id).orElse(null)?.toDomain()

    override fun findByEmail(email: String): User? = jpa.findByEmail(email)?.toDomain()

    override fun findByOauthProviderAndOauthSubject(
        provider: String,
        subject: String,
    ): User? = jpa.findByOauthProviderAndOauthSubject(provider, subject)?.toDomain()

    override fun save(user: User): User = jpa.save(user.toEntity()).toDomain()

    override fun existsByEmail(email: String): Boolean = jpa.existsByEmail(email)

    override fun findAll(pageable: Pageable): Page<User> = jpa.findAll(pageable).map { it.toDomain() }

    override fun count(): Long = jpa.count()

    override fun deleteById(id: UUID) = jpa.deleteById(id)
}
