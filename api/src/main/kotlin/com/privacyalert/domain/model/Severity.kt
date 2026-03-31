package com.privacyalert.domain.model

enum class Severity(
    val penalty: Int,
) {
    CRITICAL(15),
    HIGH(10),
    MEDIUM(5),
    LOW(2),
}
