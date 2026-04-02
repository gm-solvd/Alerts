package com.privacyalert.domain.usecase.score

import com.privacyalert.domain.model.PageResult
import com.privacyalert.domain.model.ScoreRecord
import com.privacyalert.domain.repository.ScoreRepository
import com.privacyalert.domain.util.safeApiCall

class GetScoreHistoryUseCase(private val scoreRepository: ScoreRepository) {
    suspend operator fun invoke(page: Int = 0, size: Int = 20): Result<PageResult<ScoreRecord>> =
        safeApiCall { scoreRepository.getScoreHistory(page, size) }
}
