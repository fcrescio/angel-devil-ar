package com.angelmirror.character

import kotlin.math.PI
import kotlin.math.sin

class CharacterProceduralAnimator {
    fun poseAt(
        elapsedSeconds: Float,
        intent: CharacterAnimationIntent,
    ): CharacterAnimationPose {
        return when (intent) {
            CharacterAnimationIntent.Appearing -> appear(elapsedSeconds)
            CharacterAnimationIntent.Idle -> idle(elapsedSeconds)
            CharacterAnimationIntent.Greeting -> greeting(elapsedSeconds)
            CharacterAnimationIntent.Searching -> searching(elapsedSeconds)
            CharacterAnimationIntent.Blocked -> blocked(elapsedSeconds)
            CharacterAnimationIntent.Calming -> calming(elapsedSeconds)
            CharacterAnimationIntent.Paused -> CharacterAnimationPose()
        }
    }

    private fun appear(elapsedSeconds: Float): CharacterAnimationPose {
        val pulse = sin(elapsedSeconds * TwoPi * 1.8f)
        return CharacterAnimationPose(
            verticalOffsetMeters = 0.010f + (pulse * 0.006f),
            pitchDegrees = -3.0f + (pulse * 2.0f),
            rollDegrees = pulse * 3.0f,
        )
    }

    private fun idle(elapsedSeconds: Float): CharacterAnimationPose {
        val breath = sin(elapsedSeconds * TwoPi * 0.75f)
        val sway = sin(elapsedSeconds * TwoPi * 0.37f)
        return CharacterAnimationPose(
            verticalOffsetMeters = breath * 0.008f,
            yawDegrees = sway * 4.0f,
            rollDegrees = breath * 2.0f,
        )
    }

    private fun greeting(elapsedSeconds: Float): CharacterAnimationPose {
        val wave = sin(elapsedSeconds * TwoPi * 1.2f)
        val bounce = sin(elapsedSeconds * TwoPi * 2.4f)
        return CharacterAnimationPose(
            verticalOffsetMeters = 0.014f + (bounce * 0.006f),
            pitchDegrees = -6.0f + (bounce * 1.5f),
            yawDegrees = wave * 7.0f,
            rollDegrees = wave * 5.0f,
        )
    }

    private fun searching(elapsedSeconds: Float): CharacterAnimationPose {
        val scan = sin(elapsedSeconds * TwoPi * 0.95f)
        val bob = sin(elapsedSeconds * TwoPi * 1.4f)
        return CharacterAnimationPose(
            verticalOffsetMeters = bob * 0.006f,
            yawDegrees = scan * 12.0f,
            rollDegrees = -scan * 4.0f,
        )
    }

    private fun calming(elapsedSeconds: Float): CharacterAnimationPose {
        val breath = sin(elapsedSeconds * TwoPi * 0.42f)
        return CharacterAnimationPose(
            verticalOffsetMeters = -0.004f + (breath * 0.003f),
            pitchDegrees = 3.0f,
            yawDegrees = breath * 1.2f,
            rollDegrees = breath * 0.8f,
        )
    }

    private fun blocked(elapsedSeconds: Float): CharacterAnimationPose {
        val twitch = sin(elapsedSeconds * TwoPi * 2.4f)
        return CharacterAnimationPose(
            verticalOffsetMeters = twitch * 0.004f,
            pitchDegrees = 6.0f,
            rollDegrees = twitch * 6.0f,
        )
    }

    private companion object {
        const val TwoPi = (PI * 2.0).toFloat()
    }
}
