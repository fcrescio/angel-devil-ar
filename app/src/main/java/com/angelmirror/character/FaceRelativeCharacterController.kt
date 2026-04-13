package com.angelmirror.character

import com.google.ar.core.AugmentedFace
import com.google.ar.core.Frame
import com.google.ar.core.Pose
import com.google.ar.core.TrackingState
import io.github.sceneview.math.Position
import io.github.sceneview.node.ModelNode

class FaceRelativeCharacterController(
    private val modelNode: ModelNode,
    private val offset: ShoulderPlacementOffset = ShoulderPlacementOffset(),
) {
    private var lastPlacement: ShoulderPlacement? = null

    fun update(frame: Frame): Boolean {
        val face = frame.getUpdatedTrackables(AugmentedFace::class.java)
            .firstOrNull { it.trackingState == TrackingState.TRACKING }
            ?: return false

        val pose = face.centerPose.compose(
            Pose.makeTranslation(
                offset.horizontalMeters,
                offset.verticalMeters,
                offset.depthMeters,
            ),
        )
        val target = ShoulderPlacement(
            x = pose.tx(),
            y = pose.ty(),
            z = pose.tz(),
        )
        val smoothed = smooth(target)

        modelNode.position = Position(
            x = smoothed.x,
            y = smoothed.y,
            z = smoothed.z,
        )

        return true
    }

    private fun smooth(target: ShoulderPlacement): ShoulderPlacement {
        val previous = lastPlacement
        val next = if (previous == null) {
            target
        } else {
            ShoulderPlacement(
                x = previous.x.lerp(target.x),
                y = previous.y.lerp(target.y),
                z = previous.z.lerp(target.z),
            )
        }
        lastPlacement = next
        return next
    }

    private fun Float.lerp(target: Float): Float {
        return this + ((target - this) * SmoothingFactor)
    }

    private companion object {
        const val SmoothingFactor = 0.35f
    }
}
