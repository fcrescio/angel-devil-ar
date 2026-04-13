package com.angelmirror.character

import com.angelmirror.interaction.CompanionMood

enum class CharacterAnimationIntent {
    Appearing,
    Idle,
    Searching,
    Blocked,
    Paused,
}

object CharacterAnimationIntentMapper {
    fun fromMood(mood: CompanionMood): CharacterAnimationIntent = when (mood) {
        CompanionMood.WarmingUp -> CharacterAnimationIntent.Appearing
        CompanionMood.Present -> CharacterAnimationIntent.Idle
        CompanionMood.Searching -> CharacterAnimationIntent.Searching
        CompanionMood.Blocked -> CharacterAnimationIntent.Blocked
        CompanionMood.Paused -> CharacterAnimationIntent.Paused
    }
}
