package com.privacyalert.domain.model

import java.util.UUID

data class DataBrokerSite(
    val id: UUID = UUID.randomUUID(),
    val name: String,
    val baseUrl: String,
    val searchUrlTemplate: String? = null,
    val resultSelector: String? = null,
    val piiFields: List<String>,
    val active: Boolean = true,
)
