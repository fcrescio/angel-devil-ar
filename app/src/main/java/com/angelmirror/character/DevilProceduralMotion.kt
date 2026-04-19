package com.angelmirror.character

import kotlin.math.PI
import kotlin.math.floor
import kotlin.math.sin

data class DevilRigPose(
    val bodyScaleX: Float = 1.0f,
    val bodyScaleY: Float = 1.0f,
    val bodyScaleZ: Float = 1.0f,
    val jointRotations: List<DevilJointRotation> = emptyList(),
)

data class DevilJointRotation(
    val jointIndex: Int,
    val axis: DevilJointAxis,
    val degrees: Float,
)

enum class DevilJointAxis {
    X,
    Y,
    Z,
}

class DevilProceduralMotion {
    fun poseAt(
        elapsedSeconds: Float,
        directive: CharacterAnimationDirective,
    ): DevilRigPose {
        val intensity = directive.intensityScale()
        return when (directive.intent) {
            CharacterAnimationIntent.Appearing -> appearing(elapsedSeconds)
            CharacterAnimationIntent.Idle -> idle(elapsedSeconds)
            CharacterAnimationIntent.Greeting -> greeting(elapsedSeconds, intensity)
            CharacterAnimationIntent.Searching -> searching(elapsedSeconds)
            CharacterAnimationIntent.Blocked -> blocked(elapsedSeconds, intensity)
            CharacterAnimationIntent.Calming -> calming(elapsedSeconds, intensity)
            CharacterAnimationIntent.Paused -> DevilRigPose()
        }
    }

    fun poseAt(
        elapsedSeconds: Float,
        intent: CharacterAnimationIntent,
    ): DevilRigPose {
        return poseAt(
            elapsedSeconds = elapsedSeconds,
            directive = CharacterAnimationDirective(intent = intent),
        )
    }

    private fun appearing(elapsedSeconds: Float): DevilRigPose {
        if (elapsedSeconds >= AppearingDurationSeconds) {
            return idle(elapsedSeconds)
        }

        val progress = smoothStep(elapsedSeconds / AppearingDurationSeconds)
        val settle = sin(progress * PI.toFloat())
        return DevilRigPose(
            bodyScaleX = 0.92f + (progress * 0.08f),
            bodyScaleY = 0.86f + (progress * 0.14f) + (settle * 0.025f),
            bodyScaleZ = 0.92f + (progress * 0.08f),
            jointRotations = listOf(
                DevilJointRotation(RightWingRootJoint, DevilJointAxis.Z, -13.0f + (progress * 16.0f)),
                DevilJointRotation(RightWingTipJoint, DevilJointAxis.Z, -10.0f + (progress * 12.0f) + (settle * 2.0f)),
                DevilJointRotation(LeftWingRootJoint, DevilJointAxis.Z, 12.0f - (progress * 15.0f)),
                DevilJointRotation(LeftWingTipJoint, DevilJointAxis.Z, 9.0f - (progress * 11.0f) - (settle * 1.7f)),
                DevilJointRotation(TailRootJoint, DevilJointAxis.Z, -8.0f + (settle * 7.0f)),
                DevilJointRotation(TailMidJoint, DevilJointAxis.Z, -5.0f + (settle * 5.0f)),
                DevilJointRotation(TailTipJoint, DevilJointAxis.Z, settle * 6.0f),
            ),
        )
    }

    private fun idle(elapsedSeconds: Float): DevilRigPose {
        val breath = asymmetricBreath(elapsedSeconds * 0.62f)
        val wingCycle = elapsedSeconds * 0.72f
        val wing = asymmetricFlap(wrap01(wingCycle))
        val wingTip = asymmetricFlap(wrap01(wingCycle - WingTipLagCycles))
        val wingAccent = closingAccent(wrap01(wingCycle))
        val wingVariation = cycleVariation(wingCycle)
        val leftVariation = cycleVariation(wingCycle + 0.37f)
        val flutter = sin(elapsedSeconds * TwoPi * 2.6f) * wingAccent
        val tail = sin((elapsedSeconds * TwoPi * 0.55f) + 0.35f)
        val tailTip = sin((elapsedSeconds * TwoPi * 1.1f) + 1.4f)

        return DevilRigPose(
            bodyScaleX = 1.0f - (breath * 0.006f),
            bodyScaleY = 1.0f + (breath * 0.026f),
            bodyScaleZ = 1.0f - (breath * 0.004f),
            jointRotations = listOf(
                DevilJointRotation(RightWingRootJoint, DevilJointAxis.Z, wing * 9.0f * wingVariation),
                DevilJointRotation(RightWingTipJoint, DevilJointAxis.Z, (wingTip * 6.0f) + (flutter * 1.4f)),
                DevilJointRotation(LeftWingRootJoint, DevilJointAxis.Z, -wing * 8.4f * leftVariation),
                DevilJointRotation(LeftWingTipJoint, DevilJointAxis.Z, (-wingTip * 5.6f) - (flutter * 1.1f)),
                DevilJointRotation(TailRootJoint, DevilJointAxis.Z, tail * 7.0f),
                DevilJointRotation(TailMidJoint, DevilJointAxis.Z, (tail * 5.0f) + (tailTip * 2.0f)),
                DevilJointRotation(TailTipJoint, DevilJointAxis.Z, tailTip * 5.0f),
            ),
        )
    }

    private fun greeting(
        elapsedSeconds: Float,
        intensity: Float,
    ): DevilRigPose {
        val wave = sin(elapsedSeconds * TwoPi * 1.35f)
        val wingLift = smoothStep(0.65f + (wave * 0.35f))
        val tail = sin((elapsedSeconds * TwoPi * 1.15f) + 0.8f)
        return DevilRigPose(
            bodyScaleX = 0.99f,
            bodyScaleY = 1.04f + (wingLift * 0.012f),
            bodyScaleZ = 0.99f,
            jointRotations = listOf(
                DevilJointRotation(RightWingRootJoint, DevilJointAxis.Z, 10.0f + (wingLift * 11.0f * intensity)),
                DevilJointRotation(RightWingTipJoint, DevilJointAxis.Z, 6.0f + (wingLift * 9.0f * intensity)),
                DevilJointRotation(LeftWingRootJoint, DevilJointAxis.Z, -5.0f - (wingLift * 3.0f)),
                DevilJointRotation(LeftWingTipJoint, DevilJointAxis.Z, -3.5f - (wingLift * 2.0f)),
                DevilJointRotation(TailRootJoint, DevilJointAxis.Z, tail * 7.0f * intensity),
                DevilJointRotation(TailMidJoint, DevilJointAxis.Z, (tail * 4.5f * intensity) + (wingLift * 2.0f)),
                DevilJointRotation(TailTipJoint, DevilJointAxis.Z, sin(elapsedSeconds * TwoPi * 2.1f) * 5.0f * intensity),
            ),
        )
    }

    private fun searching(elapsedSeconds: Float): DevilRigPose {
        val scan = sin(elapsedSeconds * TwoPi * 1.05f)
        val wingTension = sin((elapsedSeconds * TwoPi * 0.9f) + 0.5f)
        val tail = sin((elapsedSeconds * TwoPi * 1.25f) + 1.1f)
        return DevilRigPose(
            bodyScaleX = 0.99f,
            bodyScaleY = 1.03f + (kotlin.math.abs(scan) * 0.01f),
            bodyScaleZ = 1.0f,
            jointRotations = listOf(
                DevilJointRotation(RightWingRootJoint, DevilJointAxis.Z, 5.0f + (wingTension * 4.5f)),
                DevilJointRotation(RightWingTipJoint, DevilJointAxis.Z, 4.0f + (wingTension * 3.0f)),
                DevilJointRotation(LeftWingRootJoint, DevilJointAxis.Z, -5.0f - (wingTension * 4.0f)),
                DevilJointRotation(LeftWingTipJoint, DevilJointAxis.Z, -4.0f - (wingTension * 2.8f)),
                DevilJointRotation(TailRootJoint, DevilJointAxis.Z, tail * 11.0f),
                DevilJointRotation(TailMidJoint, DevilJointAxis.Z, (tail * 7.0f) + (scan * 2.0f)),
                DevilJointRotation(TailTipJoint, DevilJointAxis.Z, sin(elapsedSeconds * TwoPi * 2.2f) * 7.0f),
            ),
        )
    }

    private fun calming(
        elapsedSeconds: Float,
        intensity: Float,
    ): DevilRigPose {
        val breath = sin(elapsedSeconds * TwoPi * 0.42f)
        val tail = sin((elapsedSeconds * TwoPi * 0.35f) + 1.0f)
        val settle = 1.0f / intensity
        return DevilRigPose(
            bodyScaleX = 1.0f,
            bodyScaleY = 0.985f - ((intensity - 1.0f) * 0.004f) + (breath * 0.008f * settle),
            bodyScaleZ = 1.0f,
            jointRotations = listOf(
                DevilJointRotation(RightWingRootJoint, DevilJointAxis.Z, -13.0f - ((intensity - 1.0f) * 2.0f) + (breath * 1.6f * settle)),
                DevilJointRotation(RightWingTipJoint, DevilJointAxis.Z, -7.0f - ((intensity - 1.0f) * 1.4f) + (breath * 1.0f * settle)),
                DevilJointRotation(LeftWingRootJoint, DevilJointAxis.Z, 12.0f + ((intensity - 1.0f) * 1.8f) - (breath * 1.4f * settle)),
                DevilJointRotation(LeftWingTipJoint, DevilJointAxis.Z, 6.6f + ((intensity - 1.0f) * 1.2f) - (breath * 0.9f * settle)),
                DevilJointRotation(TailRootJoint, DevilJointAxis.Z, tail * 3.0f * settle),
                DevilJointRotation(TailMidJoint, DevilJointAxis.Z, tail * 2.0f * settle),
                DevilJointRotation(TailTipJoint, DevilJointAxis.Z, tail * 1.5f * settle),
            ),
        )
    }

    private fun blocked(
        elapsedSeconds: Float,
        intensity: Float,
    ): DevilRigPose {
        val twitch = sin(elapsedSeconds * TwoPi * 2.4f)
        val tailSnap = sin((elapsedSeconds * TwoPi * 1.9f) + 0.7f)
        val sharpSnap = sin((elapsedSeconds * TwoPi * 4.6f) + 0.2f)
        return DevilRigPose(
            bodyScaleX = 1.01f + ((intensity - 1.0f) * 0.006f),
            bodyScaleY = 0.99f - ((intensity - 1.0f) * 0.008f),
            bodyScaleZ = 1.0f,
            jointRotations = listOf(
                DevilJointRotation(RightWingRootJoint, DevilJointAxis.Z, -8.0f + (twitch * 1.8f * intensity)),
                DevilJointRotation(RightWingTipJoint, DevilJointAxis.Z, -5.0f + (twitch * 1.2f * intensity)),
                DevilJointRotation(LeftWingRootJoint, DevilJointAxis.Z, 7.5f - (twitch * 1.6f * intensity)),
                DevilJointRotation(LeftWingTipJoint, DevilJointAxis.Z, 4.8f - (twitch * 1.1f * intensity)),
                DevilJointRotation(TailRootJoint, DevilJointAxis.Z, 5.5f + (tailSnap * 5.5f * intensity) + (sharpSnap * 2.0f * (intensity - 1.0f))),
                DevilJointRotation(TailMidJoint, DevilJointAxis.Z, 3.0f + (tailSnap * 4.2f * intensity) + (sharpSnap * 1.5f * (intensity - 1.0f))),
                DevilJointRotation(TailTipJoint, DevilJointAxis.Z, (tailSnap * 5.5f * intensity) + (sharpSnap * 2.2f * (intensity - 1.0f))),
            ),
        )
    }

    private fun asymmetricBreath(cycle: Float): Float {
        val wrapped = wrap01(cycle)
        return if (wrapped < BreathInhalePortion) {
            smoothStep(wrapped / BreathInhalePortion)
        } else {
            1.0f - (smoothStep((wrapped - BreathInhalePortion) / (1.0f - BreathInhalePortion)) * 2.0f)
        }
    }

    private fun asymmetricFlap(cycle: Float): Float {
        return if (cycle < WingOpenPortion) {
            -0.55f + (smoothStep(cycle / WingOpenPortion) * 1.25f)
        } else {
            0.70f - (smoothStep((cycle - WingOpenPortion) / (1.0f - WingOpenPortion)) * 1.25f)
        }
    }

    private fun closingAccent(cycle: Float): Float {
        return if (cycle < WingOpenPortion) {
            0.0f
        } else {
            sin(((cycle - WingOpenPortion) / (1.0f - WingOpenPortion)) * PI.toFloat())
        }
    }

    private fun cycleVariation(cycle: Float): Float {
        val cycleIndex = floor(cycle).toInt()
        val noise = wrap01(sin(cycleIndex * 12.9898f) * 43_758.547f)
        return 0.88f + (noise * 0.18f)
    }

    private fun smoothStep(value: Float): Float {
        val x = value.coerceIn(0.0f, 1.0f)
        return x * x * (3.0f - (2.0f * x))
    }

    private fun wrap01(value: Float): Float {
        return value - floor(value)
    }

    private fun CharacterAnimationDirective.intensityScale(): Float {
        return 1.0f + ((clampedIntensity - 1) * 0.35f)
    }

    companion object {
        const val RightWingRootJoint = 14
        const val RightWingTipJoint = 9
        const val LeftWingRootJoint = 27
        const val LeftWingTipJoint = 32
        const val TailRootJoint = 24
        const val TailMidJoint = 26
        const val TailTipJoint = 29

        private const val TwoPi = (PI * 2.0).toFloat()
        private const val AppearingDurationSeconds = 1.25f
        private const val BreathInhalePortion = 0.42f
        private const val WingOpenPortion = 0.68f
        private const val WingTipLagCycles = 0.09f
    }
}
