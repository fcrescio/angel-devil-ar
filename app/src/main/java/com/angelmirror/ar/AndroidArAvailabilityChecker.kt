package com.angelmirror.ar

import android.content.Context
import com.google.ar.core.ArCoreApk

object AndroidArAvailabilityChecker {
    fun check(context: Context): ArAvailabilityState {
        return when (ArCoreApk.getInstance().checkAvailability(context)) {
            ArCoreApk.Availability.SUPPORTED_INSTALLED -> ArAvailabilityState.Ready
            ArCoreApk.Availability.SUPPORTED_APK_TOO_OLD,
            ArCoreApk.Availability.SUPPORTED_NOT_INSTALLED,
            -> ArAvailabilityState.NeedsInstall
            ArCoreApk.Availability.UNSUPPORTED_DEVICE_NOT_CAPABLE -> ArAvailabilityState.Unsupported
            ArCoreApk.Availability.UNKNOWN_CHECKING -> ArAvailabilityState.Checking
        }
    }
}
