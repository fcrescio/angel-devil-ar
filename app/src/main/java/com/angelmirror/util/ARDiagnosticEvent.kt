package com.angelmirror.util

sealed class ARDiagnosticEvent(val timestampMs: Long = System.currentTimeMillis()) {
    data class TrackingState(val state: String, val faceCount: Int) : ARDiagnosticEvent()
    data class PlacementUpdate(val horizontal: Float, val vertical: Float, val depth: Float) : ARDiagnosticEvent()
    data class AnimationIntent(val intent: String) : ARDiagnosticEvent()
}
