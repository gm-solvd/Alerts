package com.privacyalert.domain.usecase.score

import com.privacyalert.domain.model.ScoreRecord
import com.privacyalert.domain.repository.ScoreRepository
import com.privacyalert.domain.util.safeFlow
import kotlinx.coroutines.flow.Flow

class GetScoreUseCase(private val scoreRepository: ScoreRepository) {
    operator fun invoke(): Flow<Result<ScoreRecord>> =
        safeFlow { scoreRepository.getCurrentScore() }
}
