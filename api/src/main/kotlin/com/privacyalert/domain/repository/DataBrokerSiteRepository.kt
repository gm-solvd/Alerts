package com.privacyalert.domain.repository

import com.privacyalert.domain.model.DataBrokerSite
import java.util.UUID

interface DataBrokerSiteRepository {
    fun findAllActive(): List<DataBrokerSite>

    fun findById(id: UUID): DataBrokerSite?
}
