package com.privacyalert.data.entity

import com.privacyalert.domain.model.KnownBreach
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

@Entity
@Table(name = "known_breaches")
class KnownBreachEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID = UUID.randomUUID(),
    @Column(nullable = false)
    val name: String = "",
    val domain: String? = null,
    val breachDate: LocalDate? = null,
    @Column(name = "data_classes", columnDefinition = "TEXT[]")
    @JdbcTypeCode(SqlTypes.ARRAY)
    var dataClasses: List<String> = emptyList(),
    val recordCount: Long? = null,
    val sourceUrl: String? = null,
    @Column(nullable = false)
    val ingestedAt: Instant = Instant.now(),
)

fun KnownBreachEntity.toDomain(): KnownBreach =
    KnownBreach(
        id = id,
        name = name,
        domain = domain,
        breachDate = breachDate,
        dataClasses = dataClasses,
        recordCount = recordCount,
        sourceUrl = sourceUrl,
        ingestedAt = ingestedAt,
    )

fun KnownBreach.toEntity(): KnownBreachEntity =
    KnownBreachEntity(
        id = id,
        name = name,
        domain = domain,
        breachDate = breachDate,
        dataClasses = dataClasses,
        recordCount = recordCount,
        sourceUrl = sourceUrl,
        ingestedAt = ingestedAt,
    )
