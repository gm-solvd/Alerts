package com.privacyalert.integration

import com.privacyalert.domain.model.AppException
import com.privacyalert.domain.service.OAuthUserInfo
import com.privacyalert.domain.service.OAuthVerifier
import org.springframework.stereotype.Component

@Component
class OAuthVerifierImpl : OAuthVerifier {

    override fun verify(provider: String, idToken: String): OAuthUserInfo {
        // TODO: Implement actual Google/Apple ID token verification
        // For Google: verify with Google's public keys via https://www.googleapis.com/oauth2/v3/tokeninfo
        // For Apple: verify with Apple's public keys via https://appleid.apple.com/auth/keys
        throw AppException.ExternalServiceException(
            provider,
            "OAuth verification not yet implemented for provider: $provider",
        )
    }
}
