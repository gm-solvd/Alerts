package com.privacyalert.integration

import com.privacyalert.domain.repository.BreachDatabaseRepository
import com.privacyalert.domain.service.BreachResult
import com.privacyalert.domain.service.BreachScanner
import org.springframework.stereotype.Component
import java.security.MessageDigest

@Component
class LocalBreachScannerImpl(
    private val breachDatabaseRepository: BreachDatabaseRepository,
) : BreachScanner {

    override fun scanEmail(email: String): List<BreachResult> {
        val hash = sha256(email.lowercase())
        return breachDatabaseRepository.findBreachesByEmailHash(hash).map { breach ->
            BreachResult(
                name = breach.name,
                domain = breach.domain ?: "",
                breachDate = breach.breachDate?.toString() ?: "Unknown",
                dataClasses = breach.dataClasses,
            )
        }
    }

    override fun scanPhone(phone: String): List<BreachResult> {
        if (phone.isBlank()) return emptyList()
        val hash = sha256(phone)
        return breachDatabaseRepository.findBreachesByPhoneHash(hash).map { breach ->
            BreachResult(
                name = breach.name,
                domain = breach.domain ?: "",
                breachDate = breach.breachDate?.toString() ?: "Unknown",
                dataClasses = breach.dataClasses,
            )
        }
    }

    private fun sha256(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        return digest.digest(input.toByteArray()).joinToString("") { "%02x".format(it) }
    }
}
