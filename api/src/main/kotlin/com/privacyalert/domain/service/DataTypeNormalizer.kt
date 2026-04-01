package com.privacyalert.domain.service

// Normalization logic inspired by XposedOrNot (MIT license)
// https://github.com/XposedOrNot/XposedOrNot-API

class DataTypeNormalizer {
    fun normalize(rawDataTypes: List<String>): List<String> =
        rawDataTypes
            .flatMap { splitEntry(it) }
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .flatMap { expandCompound(it) }
            .map { resolveAlias(it) }
            .distinct()

    private fun splitEntry(entry: String): List<String> = entry.split(";", ",").map { it.trim() }.filter { it.isNotBlank() }

    private fun expandCompound(value: String): List<String> = COMPOUND_FIXES[value] ?: listOf(value)

    private fun resolveAlias(value: String): String = ALIASES[value] ?: value

    companion object {
        private val ALIASES =
            mapOf(
                "Name" to "Names",
                "Username" to "Usernames",
                "User names" to "Usernames",
                "Email addresse" to "Email addresses",
                "Email" to "Email addresses",
                "Emails" to "Email addresses",
                "Password" to "Passwords",
                "Drink habits" to "Drinking Habits",
                "Passwords history" to "Historical Passwords",
                "Password history" to "Historical Passwords",
                "Sexual preferences" to "Sexual Orientations",
                "Credit card" to "Credit cards",
                "Credit Card Info" to "Credit cards",
                "Credit card details" to "Credit cards",
                "Phone number" to "Phone numbers",
                "Physical address" to "Physical addresses",
                "Date of birth" to "Dates of birth",
                "Government ID" to "Government issued IDs",
                "Government IDs" to "Government issued IDs",
                "SSN" to "Social security numbers",
                "Social Security Numbers" to "Social security numbers",
                "Bank account" to "Bank Account Numbers",
                "IP address" to "IP addresses",
                "MAC address" to "MAC Addresses",
                "Auth tokens" to "Auth Tokens",
                "Security questions" to "Security questions and answers",
                "Security Questions and Answers" to "Security questions and answers",
                "Private messages" to "Private Messages",
                "Chat logs" to "Chat Logs",
                "GPS coordinates" to "GPS Coordinates",
                "Personal health data" to "Personal Health Data",
            )

        private val COMPOUND_FIXES =
            mapOf(
                "GendersDates of birth" to listOf("Genders", "Dates of birth"),
                "PasswordsUsernames" to listOf("Passwords", "Usernames"),
                "Email addressesPasswords" to listOf("Email addresses", "Passwords"),
                "NamesEmail addresses" to listOf("Names", "Email addresses"),
            )
    }
}
