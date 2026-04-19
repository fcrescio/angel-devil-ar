package com.angelmirror.voice

sealed interface VoiceRecognitionState {
    val summary: String

    data object Idle : VoiceRecognitionState {
        override val summary: String = "voice: idle"
    }

    data object Listening : VoiceRecognitionState {
        override val summary: String = "voice: listening"
    }

    data class Heard(
        val transcript: String,
        val actionId: String,
    ) : VoiceRecognitionState {
        override val summary: String = "voice: heard \"$transcript\" -> $actionId"
    }

    data class NoMatch(
        val transcript: String,
    ) : VoiceRecognitionState {
        override val summary: String = "voice: no match \"$transcript\""
    }

    data class Unavailable(
        val reason: String,
    ) : VoiceRecognitionState {
        override val summary: String = "voice: unavailable $reason"
    }

    data class Error(
        val reason: String,
    ) : VoiceRecognitionState {
        override val summary: String = "voice: error $reason"
    }
}
