package com.privacyalert.data.entity

import com.privacyalert.domain.model.PasteFinding
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.PostLoad
import jakarta.persistence.PostPersist
import jakarta.persistence.Table
import jakarta.persistence.Transient
import org.springframework.data.domain.Persistable
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "paste_findings")
class PasteFindingEntity(
    @Id
    @get:JvmName("getEntityId")
    val id: UUID = UUID.randomUUID(),
    @Column(nullable = false)
    val source: String = "",
    @Column(nullable = false)
    val pasteUrl: String = "",
    val title: String? = null,
    val snippet: String? = null,
    @Column(nullable = false)
    val discoveredAt: Instant = Instant.now(),
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

fun PasteFindingEntity.toDomain(): PasteFinding =
    PasteFinding(
        id = id,
        source = source,
        pasteUrl = pasteUrl,
        title = title,
        snippet = snippet,
        discoveredAt = discoveredAt,
    )

fun PasteFinding.toEntity(): PasteFindingEntity =
    PasteFindingEntity(
        id = id,
        source = source,
        pasteUrl = pasteUrl,
        title = title,
        snippet = snippet,
        discoveredAt = discoveredAt,
    )
