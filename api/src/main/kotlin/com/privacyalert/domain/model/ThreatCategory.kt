package com.privacyalert.domain.model

enum class ThreatCategory(
    val weight: Double,
) {
    DATA_BREACH(1.0),
    NETWORK_VULNERABILITY(0.9),
    IDENTITY_EXPOSURE(0.9),
    APP_OVERPERMISSIONS(0.7),
    TRACKER_EXPOSURE(0.6),
    DEVICE_HYGIENE(0.5),
    SOCIAL_FOOTPRINT(0.4),
}
