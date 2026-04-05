package com.privacyalert.domain.service

data class EmailReputationResult(
    val reputation: String,
    val suspicious: Boolean,
    val credentialsLeaked: Boolean,
    val darkWebAppearances: Int,
    val dataBreachCount: Int,
    val profilesFound: Int,
)

interface EmailReputationScanner {
    fun scan(email: String): EmailReputationResult?
}
