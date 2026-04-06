package com.privacyalert.presentation.preferences

expect class OnboardingPreferences {
    fun hasCompletedOnboarding(): Boolean
    fun setOnboardingCompleted()
}
