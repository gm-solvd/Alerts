package com.privacyalert.api.dto

import jakarta.validation.constraints.NotEmpty

data class PermissionAuditRequest(
    @field:NotEmpty
    val permissions: List<PermissionEntryDto>,
)

data class PermissionEntryDto(
    val name: String,
    val granted: Boolean,
)
