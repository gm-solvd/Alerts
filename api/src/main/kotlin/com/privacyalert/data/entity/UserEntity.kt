package com.privacyalert.data.entity

import com.privacyalert.domain.model.User
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "users")
class UserEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID = UUID.randomUUID(),

    @Column(nullable = false, unique = true)
    val email: String = "",

    val passwordHash: String? = null,

    val oauthProvider: String? = null,

    val oauthSubject: String? = null,

    @Column(nullable = false)
    val createdAt: Instant = Instant.now(),
)

fun UserEntity.toDomain(): User = User(
    id = id,
    email = email,
    passwordHash = passwordHash,
    oauthProvider = oauthProvider,
    oauthSubject = oauthSubject,
    createdAt = createdAt,
)

fun User.toEntity(): UserEntity = UserEntity(
    id = id,
    email = email,
    passwordHash = passwordHash,
    oauthProvider = oauthProvider,
    oauthSubject = oauthSubject,
    createdAt = createdAt,
)
