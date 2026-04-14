package com.angelmirror.character

import com.google.ar.core.AugmentedFace
import com.google.ar.core.Frame
import com.google.ar.core.Pose
import com.google.ar.core.TrackingState
import dev.romainguy.kotlin.math.Float3
import io.github.sceneview.math.Position
import io.github.sceneview.node.ModelNode

class FaceRelativeCharacterController(
    private val modelNode: ModelNode,
    private val profile: CharacterPlacementProfile = CharacterPlacementProfiles.Default,
    initialAnimationIntent: CharacterAnimationIntent = CharacterAnimationIntent.Idle,
    private val animator: CharacterProceduralAnimator = CharacterProceduralAnimator(),
    private val onDebugStateChanged: (CharacterPlacementDebugState) -> Unit = {},
) {
    private var lastPlacement: ShoulderPlacement? = null
    private var firstAnimationTimestampNanos: Long? = null
    private val offset = profile.offset

    var animationIntent: CharacterAnimationIntent = initialAnimationIntent

    fun update(frame: Frame): Boolean {
        val face = frame.getUpdatedTrackables(AugmentedFace::class.java)
            .firstOrNull { it.trackingState == TrackingState.TRACKING }
            ?: run {
                onDebugStateChanged(debugState(tracking = false))
                return false
            }

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
        val animationPose = animator.poseAt(
            elapsedSeconds = animationElapsedSeconds(frame.timestamp),
            intent = animationIntent,
        )

        modelNode.position = Position(
            x = smoothed.x,
            y = smoothed.y + animationPose.verticalOffsetMeters,
            z = smoothed.z,
        )
        modelNode.rotation = Float3(
            animationPose.pitchDegrees,
            animationPose.yawDegrees,
            animationPose.rollDegrees,
        )
        onDebugStateChanged(debugState(tracking = true))

        return true
    }

    private fun debugState(tracking: Boolean): CharacterPlacementDebugState {
        return CharacterPlacementDebugState(
            profileName = profile.name,
            offset = offset,
            latestPlacement = lastPlacement,
            tracking = tracking,
            animationIntent = animationIntent,
        )
    }

    private fun animationElapsedSeconds(timestampNanos: Long): Float {
        val firstTimestamp = firstAnimationTimestampNanos ?: timestampNanos.also {
            firstAnimationTimestampNanos = it
        }
        return ((timestampNanos - firstTimestamp).coerceAtLeast(0L) / NanosPerSecond).toFloat()
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
        const val NanosPerSecond = 1_000_000_000.0
    }
}
