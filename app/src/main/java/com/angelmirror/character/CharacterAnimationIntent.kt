package com.angelmirror.character

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
}
