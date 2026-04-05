package com.privacyalert.domain.model

data class StructuredFinding(
    val type: String,
    val name: String,
    val sourceUrl: String? = null,
    val date: String? = null,
    val dataClasses: List<String> = emptyList(),
    val severity: String? = null,
    val recordCount: Long? = null,
    val exposedFields: List<String> = emptyList(),
    val credentialExposed: Boolean = false,
)
