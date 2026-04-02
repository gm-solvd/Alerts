package com.privacyalert.domain.usecase.score

import com.privacyalert.domain.model.ScoreRecord
import com.privacyalert.domain.repository.ScoreRepository
import com.privacyalert.domain.util.safeApiCall

class GetScoreUseCase(private val scoreRepository: ScoreRepository) {
    suspend operator fun invoke(): Result<ScoreRecord> =
        safeApiCall { scoreRepository.getCurrentScore() }
}
