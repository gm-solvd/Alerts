package com.privacyalert.data.entity

import com.privacyalert.domain.model.Alert
import com.privacyalert.domain.model.Severity
import com.privacyalert.domain.model.ThreatCategory
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "alerts")
class AlertEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID = UUID.randomUUID(),

    @Column(nullable = false)
    val userId: UUID = UUID.randomUUID(),

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val category: ThreatCategory = ThreatCategory.DATA_BREACH,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val severity: Severity = Severity.MEDIUM,

    @Column(nullable = false)
    val title: String = "",

    @Column(nullable = false)
    val description: String = "",

    @Column(nullable = false)
    var resolved: Boolean = false,

    var resolvedAt: Instant? = null,

    @Column(nullable = false)
    val createdAt: Instant = Instant.now(),
)

fun AlertEntity.toDomain(): Alert = Alert(
    id = id,
    userId = userId,
    category = category,
    severity = severity,
    title = title,
    description = description,
    resolved = resolved,
    resolvedAt = resolvedAt,
    createdAt = createdAt,
)

fun Alert.toEntity(): AlertEntity = AlertEntity(
    id = id,
    userId = userId,
    category = category,
    severity = severity,
    title = title,
    description = description,
    resolved = resolved,
    resolvedAt = resolvedAt,
    createdAt = createdAt,
)
