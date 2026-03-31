package com.privacyalert.data.repository

import com.privacyalert.data.entity.DataBrokerSiteEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface DataBrokerSiteJpaRepository : JpaRepository<DataBrokerSiteEntity, UUID> {

    fun findAllByActiveTrue(): List<DataBrokerSiteEntity>
}
