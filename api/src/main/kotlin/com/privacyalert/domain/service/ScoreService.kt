package com.privacyalert.domain.service

import com.privacyalert.domain.model.Alert
import com.privacyalert.domain.model.ScoreRecord
import com.privacyalert.domain.repository.AlertRepository
import com.privacyalert.domain.repository.ScoreRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.util.UUID
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.roundToInt

@Service
class ScoreService(
    private val alertRepository: AlertRepository,
    private val scoreRepository: ScoreRepository,
) {

    fun getCurrent(userId: UUID): ScoreRecord {
        return scoreRepository.findLatestByUserId(userId)
            ?: recalculate(userId)
    }

    fun getHistory(userId: UUID, pageable: Pageable): Page<ScoreRecord> =
        scoreRepository.findAllByUserId(userId, pageable)

    fun recalculate(userId: UUID): ScoreRecord {
        val unresolvedAlerts = alertRepository.findAllUnresolvedByUserId(userId)
        val score = calculate(unresolvedAlerts)
        return scoreRepository.save(
            ScoreRecord(userId = userId, score = score),
        )
    }

    companion object {
        /**
         * CVSS-inspired deduction model.
         * Score = max(0, 100 - SUM(penalty * weight * decay))
         *
         * decay(n) = 1 + ln(n) for diminishing returns on multiple alerts per category.
         */
        fun calculate(unresolvedAlerts: List<Alert>): Int {
            if (unresolvedAlerts.isEmpty()) return 100

            val totalDeduction = unresolvedAlerts
                .groupBy { it.category }
                .entries
                .sumOf { (category, alerts) ->
                    val penalty = alerts.first().severity.penalty
                    val weight = category.weight
                    val count = alerts.size
                    val decay = 1.0 + ln(count.toDouble())
                    penalty * weight * decay
                }

            return max(0, (100 - totalDeduction).roundToInt())
        }
    }
}
