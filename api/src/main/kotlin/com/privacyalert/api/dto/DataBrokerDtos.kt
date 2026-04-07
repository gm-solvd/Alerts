package com.privacyalert.api.dto

import com.privacyalert.domain.model.DataBrokerCategory
import com.privacyalert.domain.model.DataBrokerSite
import java.util.UUID

data class DataBrokerSiteResponse(
    val id: UUID,
    val name: String,
    val baseUrl: String,
    val piiFields: List<String>,
    val category: DataBrokerCategory,
    val privacyPolicyUrl: String?,
    val dataAccessUrl: String?,
    val active: Boolean,
)

fun DataBrokerSite.toResponse(): DataBrokerSiteResponse =
    DataBrokerSiteResponse(
        id = id,
        name = name,
        baseUrl = baseUrl,
        piiFields = piiFields,
        category = category,
        privacyPolicyUrl = privacyPolicyUrl,
        dataAccessUrl = dataAccessUrl,
        active = active,
    )
