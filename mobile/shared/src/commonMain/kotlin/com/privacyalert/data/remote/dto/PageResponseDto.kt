package com.privacyalert.data.remote.dto

import com.privacyalert.domain.model.PageResult
import kotlinx.serialization.Serializable

@Serializable
data class PageResponseDto<T>(
    val content: List<T>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
) {
    fun <R> toDomain(mapper: (T) -> R): PageResult<R> = PageResult(
        content = content.map(mapper),
        page = page,
        size = size,
        totalElements = totalElements,
        totalPages = totalPages,
    )
}
