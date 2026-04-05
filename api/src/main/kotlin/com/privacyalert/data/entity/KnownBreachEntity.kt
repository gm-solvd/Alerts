package com.privacyalert.data.entity

import com.privacyalert.domain.model.KnownBreach
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.PostLoad
import jakarta.persistence.PostPersist
import jakarta.persistence.Table
import jakarta.persistence.Transient
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import org.springframework.data.domain.Persistable
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

@Entity
@Table(name = "known_breaches")
class KnownBreachEntity(
    @Id
    @get:JvmName("getEntityId")
    val id: UUID = UUID.randomUUID(),
    @Column(nullable = false)
    val name: String = "",
    val domain: String? = null,
    val breachDate: LocalDate? = null,
    @Column(name = "data_classes", columnDefinition = "text[]")
    @JdbcTypeCode(SqlTypes.ARRAY)
    var dataClasses: Array<String> = emptyArray(),
    val recordCount: Long? = null,
    val sourceUrl: String? = null,
    @Column(nullable = false)
    val ingestedAt: Instant = Instant.now(),
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

fun KnownBreachEntity.toDomain(): KnownBreach =
    KnownBreach(
        id = id,
        name = name,
        domain = domain,
        breachDate = breachDate,
        dataClasses = dataClasses.toList(),
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
        dataClasses = dataClasses.toTypedArray(),
        recordCount = recordCount,
        sourceUrl = sourceUrl,
        ingestedAt = ingestedAt,
    )
