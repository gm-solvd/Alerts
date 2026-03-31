package com.privacyalert.data.entity

import com.privacyalert.domain.model.ScanResult
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "scan_results")
class ScanResultEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID = UUID.randomUUID(),

    @Column(nullable = false)
    val userId: UUID = UUID.randomUUID(),

    @Column(nullable = false)
    val scanType: String = "",

    @Column(nullable = false)
    val scanInput: String = "",

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    val findings: String = "[]",

    @Column(nullable = false)
    val createdAt: Instant = Instant.now(),
)

fun ScanResultEntity.toDomain(): ScanResult = ScanResult(
    id = id,
    userId = userId,
    scanType = scanType,
    scanInput = scanInput,
    findings = findings,
    createdAt = createdAt,
)

fun ScanResult.toEntity(): ScanResultEntity = ScanResultEntity(
    id = id,
    userId = userId,
    scanType = scanType,
    scanInput = scanInput,
    findings = findings,
    createdAt = createdAt,
)
