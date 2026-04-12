package com.angelmirror.character

import com.angelmirror.tracking.FacePose

data class ShoulderPlacementOffset(
    val horizontalMeters: Float = 0.28f,
    val verticalMeters: Float = -0.08f,
    val depthMeters: Float = -0.18f,
)

data class ShoulderPlacement(
    val x: Float,
    val y: Float,
    val z: Float,
)

object ShoulderPlacementSolver {
    fun solve(
        facePose: FacePose,
        offset: ShoulderPlacementOffset = ShoulderPlacementOffset(),
    ): ShoulderPlacement {
        return ShoulderPlacement(
            x = facePose.centerX + offset.horizontalMeters,
            y = facePose.centerY + offset.verticalMeters,
            z = facePose.centerZ + offset.depthMeters,
        )
    }
}
