package com.privacyalert.config

import com.privacyalert.domain.service.PasswordEncoder
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Component

@Component
class BcryptPasswordEncoderAdapter : PasswordEncoder {
    private val bcrypt = BCryptPasswordEncoder(12)

    override fun hash(rawPassword: String): String = bcrypt.encode(rawPassword)

    override fun matches(
        rawPassword: String,
        encodedPassword: String,
    ): Boolean = bcrypt.matches(rawPassword, encodedPassword)
}
