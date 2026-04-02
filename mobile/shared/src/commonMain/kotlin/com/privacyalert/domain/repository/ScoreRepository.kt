package com.privacyalert.domain.repository

import com.privacyalert.domain.model.PageResult
import com.privacyalert.domain.model.ScoreRecord

interface ScoreRepository {
    suspend fun getCurrentScore(): ScoreRecord
    suspend fun getScoreHistory(page: Int = 0, size: Int = 20): PageResult<ScoreRecord>
}
