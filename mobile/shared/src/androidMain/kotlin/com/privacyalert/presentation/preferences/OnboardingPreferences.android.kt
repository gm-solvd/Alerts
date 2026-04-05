package com.privacyalert.presentation.preferences

import android.content.Context

actual class OnboardingPreferences(context: Context) {
    private val prefs = context.getSharedPreferences("onboarding", Context.MODE_PRIVATE)

    actual fun hasCompletedOnboarding(): Boolean = prefs.getBoolean("completed", false)

    actual fun setOnboardingCompleted() {
        prefs.edit().putBoolean("completed", true).apply()
    }
}
