package com.privacyalert.api.dto

import org.springframework.data.domain.Page

data class PageResponse<T>(
    val content: List<T>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
)

fun <T, R> Page<T>.toPageResponse(mapper: (T) -> R): PageResponse<R> =
    PageResponse(
        content = content.map(mapper),
        page = number,
        size = size,
        totalElements = totalElements,
        totalPages = totalPages,
    )
