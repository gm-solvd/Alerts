package com.privacyalert

import com.privacyalert.shared.BuildConfig

class AndroidPlatform : Platform {
    override val name: String = "Android ${android.os.Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()

actual val isDebugBuild: Boolean = BuildConfig.DEBUG
