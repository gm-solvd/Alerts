package com.privacyalert.data.repository

import com.privacyalert.data.remote.api.ScoreApi
import com.privacyalert.domain.model.PageResult
import com.privacyalert.domain.model.ScoreRecord
import com.privacyalert.domain.repository.ScoreRepository

class ScoreRepositoryImpl(
    private val scoreApi: ScoreApi,
) : ScoreRepository {

    override suspend fun getCurrentScore(): ScoreRecord =
        scoreApi.getCurrentScore().toDomain()

    override suspend fun getScoreHistory(page: Int, size: Int): PageResult<ScoreRecord> =
        scoreApi.getScoreHistory(page, size).toDomain { it.toDomain() }
}
