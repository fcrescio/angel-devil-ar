package com.angelmirror.permissions

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

object MicrophonePermissionChecker {
    fun check(context: Context): MicrophonePermissionState {
        return if (
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            MicrophonePermissionState.Granted
        } else {
            MicrophonePermissionState.Denied
        }
    }
}
