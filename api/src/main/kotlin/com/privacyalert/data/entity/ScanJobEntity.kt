package com.privacyalert.data.entity

import com.privacyalert.domain.model.ScanJob
import com.privacyalert.domain.model.ScanJobStatus
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.PostLoad
import jakarta.persistence.PostPersist
import jakarta.persistence.Table
import jakarta.persistence.Transient
import org.springframework.data.domain.Persistable
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "scan_jobs")
class ScanJobEntity(
    @Id
    @get:JvmName("getEntityId")
    val id: UUID = UUID.randomUUID(),
    @Column(nullable = false)
    val userId: UUID = UUID.randomUUID(),
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val status: ScanJobStatus = ScanJobStatus.PENDING,
    val progress: String? = null,
    val totalAlerts: Int? = null,
    val errorMessage: String? = null,
    val startedAt: Instant? = null,
    val completedAt: Instant? = null,
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

fun ScanJobEntity.toDomain(): ScanJob =
    ScanJob(
        id = id,
        userId = userId,
        status = status,
        progress = progress,
        totalAlerts = totalAlerts,
        errorMessage = errorMessage,
        startedAt = startedAt,
        completedAt = completedAt,
        createdAt = createdAt,
    )

fun ScanJob.toEntity(): ScanJobEntity =
    ScanJobEntity(
        id = id,
        userId = userId,
        status = status,
        progress = progress,
        totalAlerts = totalAlerts,
        errorMessage = errorMessage,
        startedAt = startedAt,
        completedAt = completedAt,
        createdAt = createdAt,
    )
