package com.privacyalert.domain.repository

import com.privacyalert.domain.model.ScoreRecord
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.UUID

interface ScoreRepository {
    fun save(record: ScoreRecord): ScoreRecord
    fun findLatestByUserId(userId: UUID): ScoreRecord?
    fun findAllByUserId(userId: UUID, pageable: Pageable): Page<ScoreRecord>
}
