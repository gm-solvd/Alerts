package com.privacyalert.domain.usecase.score

import com.privacyalert.domain.model.PageResult
import com.privacyalert.domain.model.ScoreRecord
import com.privacyalert.domain.repository.ScoreRepository
import com.privacyalert.domain.util.safeFlow
import kotlinx.coroutines.flow.Flow

class GetScoreHistoryUseCase(private val scoreRepository: ScoreRepository) {
    operator fun invoke(page: Int = 0, size: Int = 20): Flow<Result<PageResult<ScoreRecord>>> =
        safeFlow { scoreRepository.getScoreHistory(page, size) }
}
