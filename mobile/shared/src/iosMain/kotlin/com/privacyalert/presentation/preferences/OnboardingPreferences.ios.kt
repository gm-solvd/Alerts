package com.privacyalert.presentation.preferences

import platform.Foundation.NSUserDefaults

actual class OnboardingPreferences {
    private val defaults = NSUserDefaults.standardUserDefaults

    actual fun hasCompletedOnboarding(): Boolean = defaults.boolForKey("onboarding_completed")

    actual fun setOnboardingCompleted() {
        defaults.setBool(true, forKey = "onboarding_completed")
    }
}
