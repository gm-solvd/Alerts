package com.privacyalert.domain.service

import com.privacyalert.domain.model.Severity

// Risk tier classification inspired by XposedOrNot (MIT license)
// https://github.com/XposedOrNot/XposedOrNot-API

class BreachRiskClassifier {
    fun classify(dataClasses: List<String>): Severity {
        val score = dataClasses.sumOf { tierPoints(it) }
        return when {
            score >= 30 -> Severity.CRITICAL
            score >= 15 -> Severity.HIGH
            score >= 8 -> Severity.MEDIUM
            else -> Severity.LOW
        }
    }

    private fun tierPoints(dataType: String): Int =
        CRITICAL_TIER[dataType]
            ?: HIGH_TIER[dataType]
            ?: MEDIUM_TIER[dataType]
            ?: LOW_TIER[dataType]
            ?: DEFAULT_POINTS

    companion object {
        private const val DEFAULT_POINTS = 4

        private val CRITICAL_TIER =
            setToMap(
                10,
                "Passwords",
                "Historical Passwords",
                "Credit cards",
                "Bank Account Numbers",
                "Account balances",
                "Social security numbers",
                "Government issued IDs",
                "Passport numbers",
                "Mothers maiden names",
                "Security questions and answers",
                "Auth Tokens",
                "Mnemonic Phrases",
            )

        private val HIGH_TIER =
            setToMap(
                7,
                "Phone numbers",
                "Physical addresses",
                "Dates of birth",
                "Years of birth",
                "Income levels",
                "Private Messages",
                "Sexual Orientations",
                "Sexual Fetishes",
                "Partial credit card data",
                "Encrypted Keys",
                "Password Hints",
                "Personal Health Data",
                "HIV Statuses",
                "Medical Conditions",
                "Medications",
                "Psychological Conditions",
                "Physical Disabilities",
                "Email Messages",
                "Chat Logs",
                "GPS Coordinates",
            )

        private val MEDIUM_TIER =
            setToMap(
                4,
                "Names",
                "Usernames",
                "Email addresses",
                "IP addresses",
                "Employers",
                "Occupations",
                "Nationalities",
                "Ethnicities",
                "Education Levels",
                "Marital statuses",
                "Vehicle Details",
                "Vehicle Identification Numbers",
                "Licence Plates",
                "Ages",
                "Religions",
                "Political Views",
                "Races",
                "Spouses details",
                "MAC Addresses",
                "IMEI Numbers",
                "IMSI Numbers",
                "Social connections",
                "Instant Messenger Identities",
                "Employment Statuses",
                "Job titles",
                "Password Strengths",
            )

        private val LOW_TIER =
            setToMap(
                1,
                "Genders",
                "Places of Birth",
                "Photos",
                "Profile Photos",
                "Salutations",
                "Nicknames",
                "Social media profiles",
                "Avatars",
                "Apps Installed on Devices",
                "Buying Preferences",
                "Drinking Habits",
                "Drug Habits",
                "Eating Habits",
                "Living Costs",
                "Travel Habits",
                "Work Habits",
                "Professional Skills",
                "Spoken languages",
                "Time Zones",
                "Browsers",
                "Device information",
                "Device Serial Numbers",
                "Homepage URLs",
                "User Website URLs",
                "Website Activity",
                "Blood Types",
                "Body Measurements",
                "Physical Activity Levels",
                "Dietary Preferences",
                "Gambling Habits",
                "Smoking Habits",
                "Sleep Patterns",
                "Games Played",
                "Movies Watched",
                "Music Listened To",
                "Photos Uploaded",
                "Videos Watched",
            )

        private fun setToMap(
            points: Int,
            vararg types: String,
        ): Map<String, Int> = types.associateWith { points }
    }
}
