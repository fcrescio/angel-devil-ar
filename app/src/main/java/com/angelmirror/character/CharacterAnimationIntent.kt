package com.angelmirror.character

import com.angelmirror.interaction.CompanionInteractionState
import com.angelmirror.interaction.CompanionMood

enum class CharacterAnimationIntent {
    Appearing,
    Idle,
    Greeting,
    Searching,
    Blocked,
    Calming,
    Paused,
}

data class CharacterAnimationDirective(
    val intent: CharacterAnimationIntent,
    val intensity: Int = DefaultIntensity,
) {
    val clampedIntensity: Int = intensity.coerceIn(MinIntensity, MaxIntensity)

    companion object {
        const val DefaultIntensity = 1
        const val MinIntensity = 1
        const val MaxIntensity = 3
    }
}

object CharacterAnimationIntentMapper {
    fun fromMood(mood: CompanionMood): CharacterAnimationIntent = when (mood) {
        CompanionMood.WarmingUp -> CharacterAnimationIntent.Appearing
        CompanionMood.Present -> CharacterAnimationIntent.Idle
        CompanionMood.Greeting -> CharacterAnimationIntent.Greeting
        CompanionMood.Searching -> CharacterAnimationIntent.Searching
        CompanionMood.Blocked -> CharacterAnimationIntent.Blocked
        CompanionMood.Calming -> CharacterAnimationIntent.Calming
        CompanionMood.Paused -> CharacterAnimationIntent.Paused
    }

    fun fromInteractionState(state: CompanionInteractionState): CharacterAnimationDirective {
        return CharacterAnimationDirective(
            intent = fromMood(state.cue.mood),
            intensity = state.manualActionStreak.coerceAtLeast(CharacterAnimationDirective.DefaultIntensity),
        )
    }
}
