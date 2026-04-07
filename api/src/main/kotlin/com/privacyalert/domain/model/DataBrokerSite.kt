package com.privacyalert.domain.model

import java.util.UUID

data class DataBrokerSite(
    val id: UUID = UUID.randomUUID(),
    val name: String,
    val baseUrl: String,
    val searchUrlTemplate: String? = null,
    val resultSelector: String? = null,
    val piiFields: List<String>,
    val category: DataBrokerCategory = DataBrokerCategory.PEOPLE_SEARCH,
    val privacyPolicyUrl: String? = null,
    val dataAccessUrl: String? = null,
    val active: Boolean = true,
)

enum class DataBrokerCategory {
    PEOPLE_SEARCH,
    CREDIT_BUREAU,
    MARKETING_DATA,
    DATA_AGGREGATOR,
}
