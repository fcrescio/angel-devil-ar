package com.angelmirror.character

import kotlin.math.PI
import kotlin.math.sin

class CharacterProceduralAnimator {
    fun poseAt(
        elapsedSeconds: Float,
        directive: CharacterAnimationDirective,
    ): CharacterAnimationPose {
        val intensity = directive.intensityScale()
        return when (directive.intent) {
            CharacterAnimationIntent.Appearing -> appear(elapsedSeconds)
            CharacterAnimationIntent.Idle -> idle(elapsedSeconds)
            CharacterAnimationIntent.Greeting -> greeting(elapsedSeconds, intensity)
            CharacterAnimationIntent.Searching -> searching(elapsedSeconds)
            CharacterAnimationIntent.Blocked -> blocked(elapsedSeconds, intensity)
            CharacterAnimationIntent.Calming -> calming(elapsedSeconds, intensity)
            CharacterAnimationIntent.Paused -> CharacterAnimationPose()
        }
    }

    fun poseAt(
        elapsedSeconds: Float,
        intent: CharacterAnimationIntent,
    ): CharacterAnimationPose {
        return poseAt(
            elapsedSeconds = elapsedSeconds,
            directive = CharacterAnimationDirective(intent = intent),
        )
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

    private fun greeting(
        elapsedSeconds: Float,
        intensity: Float,
    ): CharacterAnimationPose {
        val wave = sin(elapsedSeconds * TwoPi * 1.2f)
        val bounce = sin(elapsedSeconds * TwoPi * 2.4f)
        return CharacterAnimationPose(
            verticalOffsetMeters = 0.012f + (intensity * 0.004f) + (bounce * 0.005f * intensity),
            pitchDegrees = -6.0f + (bounce * 1.5f),
            yawDegrees = wave * 6.0f * intensity,
            rollDegrees = wave * 4.5f * intensity,
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

    private fun calming(
        elapsedSeconds: Float,
        intensity: Float,
    ): CharacterAnimationPose {
        val breath = sin(elapsedSeconds * TwoPi * 0.42f)
        val settle = 1.0f / intensity
        return CharacterAnimationPose(
            verticalOffsetMeters = -0.004f - ((intensity - 1.0f) * 0.002f) + (breath * 0.003f * settle),
            pitchDegrees = 3.0f,
            yawDegrees = breath * 1.2f * settle,
            rollDegrees = breath * 0.8f * settle,
        )
    }

    private fun blocked(
        elapsedSeconds: Float,
        intensity: Float,
    ): CharacterAnimationPose {
        val twitch = sin(elapsedSeconds * TwoPi * 2.4f)
        val snap = sin(elapsedSeconds * TwoPi * 4.2f)
        return CharacterAnimationPose(
            verticalOffsetMeters = (twitch * 0.0035f * intensity) + (snap * 0.0015f * (intensity - 1.0f)),
            pitchDegrees = 5.0f + intensity,
            yawDegrees = snap * 2.0f * (intensity - 1.0f),
            rollDegrees = twitch * 5.5f * intensity,
        )
    }

    private fun CharacterAnimationDirective.intensityScale(): Float {
        return 1.0f + ((clampedIntensity - 1) * 0.35f)
    }

    private companion object {
        const val TwoPi = (PI * 2.0).toFloat()
    }
}
