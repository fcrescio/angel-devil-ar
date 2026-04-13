package com.angelmirror.interaction

enum class CompanionMood {
    WarmingUp,
    Present,
    Searching,
    Blocked,
    Paused,
}

data class CompanionCue(
    val id: String,
    val text: String,
    val mood: CompanionMood,
)

data class CompanionInteractionState(
    val cue: CompanionCue = CompanionCues.WarmingUp,
    val voiceInputEnabled: Boolean = false,
    val llmResponseEnabled: Boolean = false,
)

sealed interface CompanionSignal {
    data object ArSessionStarting : CompanionSignal
    data object CharacterPlaced : CompanionSignal
    data object FaceLost : CompanionSignal
    data object ArSessionPaused : CompanionSignal
    data class ArSessionFailed(
        val reason: String,
    ) : CompanionSignal
}

object CompanionCues {
    val WarmingUp = CompanionCue(
        id = "warming-up",
        text = "Starting mirror.",
        mood = CompanionMood.WarmingUp,
    )

    val Present = CompanionCue(
        id = "present",
        text = "I'm on your shoulder.",
        mood = CompanionMood.Present,
    )

    val FindFace = CompanionCue(
        id = "find-face",
        text = "Look at the camera.",
        mood = CompanionMood.Searching,
    )

    val Paused = CompanionCue(
        id = "paused",
        text = "AR is paused.",
        mood = CompanionMood.Paused,
    )

    fun blocked(reason: String): CompanionCue {
        return CompanionCue(
            id = "blocked",
            text = "AR is blocked: ${reason.ifBlank { "unknown reason" }}.",
            mood = CompanionMood.Blocked,
        )
    }
}

object CompanionInteractionReducer {
    fun reduce(
        current: CompanionInteractionState,
        signal: CompanionSignal,
    ): CompanionInteractionState {
        return when (signal) {
            CompanionSignal.ArSessionStarting -> current.copy(
                cue = CompanionCues.WarmingUp,
                voiceInputEnabled = false,
                llmResponseEnabled = false,
            )

            CompanionSignal.CharacterPlaced -> current.copy(
                cue = CompanionCues.Present,
                voiceInputEnabled = false,
                llmResponseEnabled = false,
            )

            CompanionSignal.FaceLost -> current.copy(
                cue = CompanionCues.FindFace,
                voiceInputEnabled = false,
                llmResponseEnabled = false,
            )

            CompanionSignal.ArSessionPaused -> current.copy(
                cue = CompanionCues.Paused,
                voiceInputEnabled = false,
                llmResponseEnabled = false,
            )

            is CompanionSignal.ArSessionFailed -> current.copy(
                cue = CompanionCues.blocked(signal.reason),
                voiceInputEnabled = false,
                llmResponseEnabled = false,
            )
        }
    }
}
