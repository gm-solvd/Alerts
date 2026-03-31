package com.privacyalert.domain.repository

import com.privacyalert.domain.model.DataBrokerSite

interface DataBrokerSiteRepository {
    fun findAllActive(): List<DataBrokerSite>
}
