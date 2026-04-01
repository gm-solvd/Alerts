package com.privacyalert.data.entity

import com.privacyalert.domain.model.User
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.PostLoad
import jakarta.persistence.PostPersist
import jakarta.persistence.Table
import jakarta.persistence.Transient
import org.springframework.data.domain.Persistable
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

@Entity
@Table(name = "users")
class UserEntity(
    @Id
    @get:JvmName("getEntityId")
    val id: UUID = UUID.randomUUID(),
    @Column(nullable = false, unique = true)
    val email: String = "",
    val passwordHash: String? = null,
    val oauthProvider: String? = null,
    val oauthSubject: String? = null,
    val fullName: String? = null,
    val phoneNumber: String? = null,
    val homeAddress: String? = null,
    val dateOfBirth: LocalDate? = null,
    @Column(nullable = false)
    val createdAt: Instant = Instant.now(),
) : Persistable<UUID> {
    @Transient
    private var new: Boolean = true

    override fun getId(): UUID = id

    override fun isNew(): Boolean = new

    @PostLoad
    @PostPersist
    fun markNotNew() {
        new = false
    }
}

fun UserEntity.toDomain(): User =
    User(
        id = id,
        email = email,
        passwordHash = passwordHash,
        oauthProvider = oauthProvider,
        oauthSubject = oauthSubject,
        fullName = fullName,
        phoneNumber = phoneNumber,
        homeAddress = homeAddress,
        dateOfBirth = dateOfBirth,
        createdAt = createdAt,
    )

fun User.toEntity(): UserEntity =
    UserEntity(
        id = id,
        email = email,
        passwordHash = passwordHash,
        oauthProvider = oauthProvider,
        oauthSubject = oauthSubject,
        fullName = fullName,
        phoneNumber = phoneNumber,
        homeAddress = homeAddress,
        dateOfBirth = dateOfBirth,
        createdAt = createdAt,
    )
