package com.privacyalert.android.preferences

import android.content.Context

class OnboardingPreferences(context: Context) {
    private val prefs = context.getSharedPreferences("onboarding", Context.MODE_PRIVATE)

    fun hasCompletedOnboarding(): Boolean = prefs.getBoolean("completed", false)

    fun setOnboardingCompleted() {
        prefs.edit().putBoolean("completed", true).apply()
    }
}
