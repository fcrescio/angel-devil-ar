package com.angelmirror.interaction

enum class CompanionMood {
    WarmingUp,
    Present,
    Greeting,
    Searching,
    Blocked,
    Calming,
    Paused,
}

data class CompanionCue(
    val id: String,
    val text: String,
    val mood: CompanionMood,
    val durationMillis: Long? = null,
)

data class CompanionInteractionState(
    val cue: CompanionCue = CompanionCues.WarmingUp,
    val voiceInputEnabled: Boolean = false,
    val llmResponseEnabled: Boolean = false,
    val eventId: Int = 0,
    val lastManualActionId: String? = null,
    val manualActionStreak: Int = 0,
) {
    val summary: String
        get() = buildString {
            append("interaction: ")
            append(cue.id)
            append('\n')
            append("mood: ")
            append(cue.mood)
            append('\n')
            append("event: ")
            append(eventId)
            if (manualActionStreak > 0) {
                append('\n')
                append("streak: ")
                append(manualActionStreak)
            }
    }
}

data class CompanionReactionExpiry(
    val eventId: Int,
    val cueId: String,
    val delayMillis: Long,
)

data class CompanionReactionResult(
    val state: CompanionInteractionState,
    val expiry: CompanionReactionExpiry? = null,
)

data class CompanionAction(
    val id: String,
    val label: String,
    val signal: CompanionSignal,
)

sealed interface CompanionSignal {
    data object ArSessionStarting : CompanionSignal
    data object CharacterPlaced : CompanionSignal
    data object FaceLost : CompanionSignal
    data object ArSessionPaused : CompanionSignal
    data object UserGreeted : CompanionSignal
    data object UserProvoked : CompanionSignal
    data object UserReassured : CompanionSignal
    data class CueExpired(
        val cueId: String,
    ) : CompanionSignal
    data class ArSessionFailed(
        val reason: String,
    ) : CompanionSignal
}

class CompanionReactionEngine(
    private val reducer: CompanionInteractionReducer = CompanionInteractionReducer,
) {
    fun dispatch(
        current: CompanionInteractionState,
        signal: CompanionSignal,
    ): CompanionReactionResult {
        val next = reducer.reduce(
            current = current,
            signal = signal,
        )
        return CompanionReactionResult(
            state = next,
            expiry = expiryFor(next),
        )
    }

    fun expiryFor(state: CompanionInteractionState): CompanionReactionExpiry? {
        val durationMillis = state.cue.durationMillis ?: return null
        return CompanionReactionExpiry(
            eventId = state.eventId,
            cueId = state.cue.id,
            delayMillis = durationMillis,
        )
    }
}

object CompanionActions {
    val Greet = CompanionAction(
        id = "greet",
        label = "Hey",
        signal = CompanionSignal.UserGreeted,
    )
    val Provoke = CompanionAction(
        id = "provoke",
        label = "Boo",
        signal = CompanionSignal.UserProvoked,
    )
    val Reassure = CompanionAction(
        id = "reassure",
        label = "Easy",
        signal = CompanionSignal.UserReassured,
    )
    val QuickActions = listOf(
        Greet,
        Provoke,
        Reassure,
    )
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

    fun greeted(streak: Int) = CompanionCue(
        id = "greeted",
        text = if (streak > 1) "Still here." else "I see you.",
        mood = CompanionMood.Greeting,
        durationMillis = 2_200L,
    )

    fun provoked(streak: Int) = CompanionCue(
        id = "provoked",
        text = when {
            streak >= 3 -> "Enough."
            streak == 2 -> "Again?"
            else -> "Careful."
        },
        mood = CompanionMood.Blocked,
        durationMillis = 2_800L,
    )

    fun reassured(streak: Int) = CompanionCue(
        id = "reassured",
        text = if (streak > 1) "Fine." else "Easy.",
        mood = CompanionMood.Calming,
        durationMillis = 2_000L,
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
            CompanionSignal.ArSessionStarting -> current.next(
                cue = CompanionCues.WarmingUp,
            )

            CompanionSignal.CharacterPlaced -> current.next(
                cue = CompanionCues.Present,
            )

            CompanionSignal.FaceLost -> current.next(
                cue = CompanionCues.FindFace,
            )

            CompanionSignal.ArSessionPaused -> current.next(
                cue = CompanionCues.Paused,
            )

            CompanionSignal.UserGreeted -> current.manual(
                actionId = CompanionActions.Greet.id,
                cueFactory = CompanionCues::greeted,
            )

            CompanionSignal.UserProvoked -> current.manual(
                actionId = CompanionActions.Provoke.id,
                cueFactory = CompanionCues::provoked,
            )

            CompanionSignal.UserReassured -> current.manual(
                actionId = CompanionActions.Reassure.id,
                cueFactory = CompanionCues::reassured,
            )

            is CompanionSignal.CueExpired -> {
                if (current.cue.id == signal.cueId && current.cue.durationMillis != null) {
                    current.next(cue = CompanionCues.Present)
                } else {
                    current
                }
            }

            is CompanionSignal.ArSessionFailed -> current.next(
                cue = CompanionCues.blocked(signal.reason),
            )
        }
    }

    private fun CompanionInteractionState.next(
        cue: CompanionCue,
    ): CompanionInteractionState {
        return CompanionInteractionState(
            cue = cue,
            voiceInputEnabled = false,
            llmResponseEnabled = false,
            eventId = eventId + 1,
            lastManualActionId = null,
            manualActionStreak = 0,
        )
    }

    private fun CompanionInteractionState.manual(
        actionId: String,
        cueFactory: (Int) -> CompanionCue,
    ): CompanionInteractionState {
        val streak = if (lastManualActionId == actionId) {
            (manualActionStreak + 1).coerceAtMost(MaxManualStreak)
        } else {
            1
        }
        return CompanionInteractionState(
            cue = cueFactory(streak),
            voiceInputEnabled = false,
            llmResponseEnabled = false,
            eventId = eventId + 1,
            lastManualActionId = actionId,
            manualActionStreak = streak,
        )
    }

    private const val MaxManualStreak = 3
}
