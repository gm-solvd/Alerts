package com.privacyalert.data.entity

import com.privacyalert.domain.model.Mitigation
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "mitigations")
class MitigationEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID = UUID.randomUUID(),

    @Column(nullable = false)
    val alertId: UUID = UUID.randomUUID(),

    @Column(nullable = false)
    val title: String = "",

    @Column(nullable = false)
    val description: String = "",

    val actionUrl: String? = null,

    @Column(nullable = false)
    var completed: Boolean = false,

    var completedAt: Instant? = null,

    @Column(nullable = false)
    val createdAt: Instant = Instant.now(),
)

fun MitigationEntity.toDomain(): Mitigation = Mitigation(
    id = id,
    alertId = alertId,
    title = title,
    description = description,
    actionUrl = actionUrl,
    completed = completed,
    completedAt = completedAt,
    createdAt = createdAt,
)

fun Mitigation.toEntity(): MitigationEntity = MitigationEntity(
    id = id,
    alertId = alertId,
    title = title,
    description = description,
    actionUrl = actionUrl,
    completed = completed,
    completedAt = completedAt,
    createdAt = createdAt,
)
