package com.angelmirror.tracking

sealed interface FaceTrackingState {
    data object NotTracking : FaceTrackingState

    data class Tracking(
        val facePose: FacePose,
    ) : FaceTrackingState
}
