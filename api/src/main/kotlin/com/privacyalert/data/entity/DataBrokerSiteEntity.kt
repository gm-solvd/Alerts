package com.privacyalert.data.entity

import com.privacyalert.domain.model.DataBrokerSite
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

@Entity
@Table(name = "data_broker_sites")
class DataBrokerSiteEntity(
    @Id
    @get:JvmName("getEntityId")
    val id: UUID = UUID.randomUUID(),
    @Column(nullable = false)
    val name: String = "",
    @Column(nullable = false)
    val baseUrl: String = "",
    val searchUrlTpl: String? = null,
    val resultSelector: String? = null,
    @Column(name = "pii_fields", columnDefinition = "TEXT[]")
    @JdbcTypeCode(SqlTypes.ARRAY)
    var piiFields: List<String> = emptyList(),
    @Column(nullable = false)
    val active: Boolean = true,
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

fun DataBrokerSiteEntity.toDomain(): DataBrokerSite =
    DataBrokerSite(
        id = id,
        name = name,
        baseUrl = baseUrl,
        searchUrlTemplate = searchUrlTpl,
        resultSelector = resultSelector,
        piiFields = piiFields,
        active = active,
    )

fun DataBrokerSite.toEntity(): DataBrokerSiteEntity =
    DataBrokerSiteEntity(
        id = id,
        name = name,
        baseUrl = baseUrl,
        searchUrlTpl = searchUrlTemplate,
        resultSelector = resultSelector,
        piiFields = piiFields,
        active = active,
    )
