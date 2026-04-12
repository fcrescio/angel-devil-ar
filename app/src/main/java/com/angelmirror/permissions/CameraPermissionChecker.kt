package com.angelmirror.permissions

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

object CameraPermissionChecker {
    fun check(context: Context): CameraPermissionState {
        return if (
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            CameraPermissionState.Granted
        } else {
            CameraPermissionState.Denied
        }
    }
}
