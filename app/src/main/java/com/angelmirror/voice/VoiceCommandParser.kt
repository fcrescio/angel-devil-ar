package com.angelmirror.voice

import com.angelmirror.interaction.CompanionActions
import com.angelmirror.interaction.CompanionSignal
import java.util.Locale

data class VoiceCommandResult(
    val transcript: String,
    val signal: CompanionSignal,
    val actionId: String,
)

object VoiceCommandParser {
    fun parse(candidates: List<String>): VoiceCommandResult? {
        candidates.forEach { candidate ->
            val normalized = candidate.normalizedForCommand()
            val action = VoiceCommandMatchers.firstOrNull { matcher ->
                matcher.matches(normalized)
            }
            if (action != null) {
                return VoiceCommandResult(
                    transcript = candidate,
                    signal = action.signal,
                    actionId = action.actionId,
                )
            }
        }
        return null
    }

    private fun String.normalizedForCommand(): String {
        return lowercase(Locale.ROOT)
            .replace(NonCommandCharacters, " ")
            .split(' ')
            .filter { it.isNotBlank() }
            .joinToString(separator = " ")
    }

    private val NonCommandCharacters = Regex("[^a-z0-9àèéìòù]+")
}

private data class VoiceCommandMatcher(
    val actionId: String,
    val signal: CompanionSignal,
    val phrases: Set<String>,
) {
    fun matches(transcript: String): Boolean {
        return phrases.any { phrase ->
            transcript == phrase ||
                transcript.startsWith("$phrase ") ||
                transcript.endsWith(" $phrase") ||
                transcript.contains(" $phrase ")
        }
    }
}

private val VoiceCommandMatchers = listOf(
    VoiceCommandMatcher(
        actionId = CompanionActions.Greet.id,
        signal = CompanionSignal.UserGreeted,
        phrases = setOf(
            "hey",
            "ehi",
            "ciao",
            "hello",
            "salve",
        ),
    ),
    VoiceCommandMatcher(
        actionId = CompanionActions.Reassure.id,
        signal = CompanionSignal.UserReassured,
        phrases = setOf(
            "easy",
            "calmati",
            "calma",
            "tranquillo",
            "tranquilla",
            "piano",
            "buono",
        ),
    ),
    VoiceCommandMatcher(
        actionId = CompanionActions.Provoke.id,
        signal = CompanionSignal.UserProvoked,
        phrases = setOf(
            "boo",
            "buh",
            "bu",
            "spaventami",
            "paura",
        ),
    ),
)
