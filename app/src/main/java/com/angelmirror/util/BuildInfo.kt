package com.angelmirror.util

import com.angelmirror.BuildConfig

object BuildInfo {
    const val BootstrapPhase = "M0"
    val GitShortSha: String = BuildConfig.GIT_SHORT_SHA
    val VersionName: String = BuildConfig.VERSION_NAME
    val DisplayBuild: String = "build $GitShortSha"
}
