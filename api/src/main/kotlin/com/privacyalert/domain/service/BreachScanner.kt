package com.privacyalert.domain.service

data class BreachResult(
    val name: String,
    val domain: String,
    val breachDate: String,
    val dataClasses: List<String>,
)

interface BreachScanner {
    fun scanEmail(email: String): List<BreachResult>
}
