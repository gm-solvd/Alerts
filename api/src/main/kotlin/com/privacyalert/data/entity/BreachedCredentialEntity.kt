package com.privacyalert.data.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "breached_credentials")
class BreachedCredentialEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID = UUID.randomUUID(),

    @Column(nullable = false)
    val breachId: UUID = UUID.randomUUID(),

    val emailSha256: String? = null,

    val phoneSha256: String? = null,

    @Column(nullable = false)
    val createdAt: Instant = Instant.now(),
)
