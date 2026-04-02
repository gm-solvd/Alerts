package com.privacyalert.data.entity

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.privacyalert.domain.model.ScanResult
import com.privacyalert.domain.model.StructuredFinding
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
import java.util.UUID

private val objectMapper = jacksonObjectMapper()

@Entity
@Table(name = "scan_results")
class ScanResultEntity(
    @Id
    @get:JvmName("getEntityId")
    val id: UUID = UUID.randomUUID(),
    @Column(nullable = false)
    val userId: UUID = UUID.randomUUID(),
    @Column(nullable = false)
    val scanType: String = "",
    @Column(nullable = false)
    val scanInput: String = "",
    @Column(nullable = false)
    val findings: String = "",
    @Column(nullable = false, columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    val findingsJson: String = "[]",
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

fun ScanResultEntity.toDomain(): ScanResult =
    ScanResult(
        id = id,
        userId = userId,
        scanType = scanType,
        scanInput = scanInput,
        findings = findings,
        findingsJson = objectMapper.readValue<List<StructuredFinding>>(findingsJson),
        createdAt = createdAt,
    )

fun ScanResult.toEntity(): ScanResultEntity =
    ScanResultEntity(
        id = id,
        userId = userId,
        scanType = scanType,
        scanInput = scanInput,
        findings = findings,
        findingsJson = objectMapper.writeValueAsString(findingsJson),
        createdAt = createdAt,
    )
