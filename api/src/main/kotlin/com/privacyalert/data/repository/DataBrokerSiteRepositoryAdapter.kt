package com.privacyalert.data.repository

import com.privacyalert.data.entity.toDomain
import com.privacyalert.domain.model.DataBrokerSite
import com.privacyalert.domain.repository.DataBrokerSiteRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class DataBrokerSiteRepositoryAdapter(
    private val jpa: DataBrokerSiteJpaRepository,
) : DataBrokerSiteRepository {
    override fun findAllActive(): List<DataBrokerSite> = jpa.findAllByActiveTrue().map { it.toDomain() }

    override fun findById(id: UUID): DataBrokerSite? = jpa.findByIdOrNull(id)?.toDomain()
}
