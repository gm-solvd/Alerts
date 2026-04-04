package com.privacyalert.android

import android.app.Application

/**
 * Minimal Application for Robolectric tests.
 * Does NOT start Koin — screenshot tests render stateless composables only.
 */
class TestApplication : Application()
