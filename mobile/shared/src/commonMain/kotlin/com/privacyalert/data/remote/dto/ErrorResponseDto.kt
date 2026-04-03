package com.privacyalert.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ErrorResponseDto(
    val code: String,
    val message: String,
    val timestamp: String? = null,
)
