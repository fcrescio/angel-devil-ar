package com.angelmirror.ar

import com.google.ar.core.Config
import com.google.ar.core.Session

object AugmentedFaceSessionConfig {
    val FrontCameraFeatures: Set<Session.Feature> = setOf(Session.Feature.FRONT_CAMERA)

    fun apply(@Suppress("UNUSED_PARAMETER") session: Session, config: Config) {
        config.augmentedFaceMode = Config.AugmentedFaceMode.MESH3D
        config.planeFindingMode = Config.PlaneFindingMode.DISABLED
        config.lightEstimationMode = Config.LightEstimationMode.DISABLED
    }
}
