package com.angelmirror.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import com.angelmirror.interaction.CompanionSignal
import java.util.Locale

class AndroidVoiceCommandRecognizer(
    context: Context,
    private val onStateChanged: (VoiceRecognitionState) -> Unit,
    private val onCommand: (CompanionSignal) -> Unit,
) : RecognitionListener {
    private val appContext = context.applicationContext
    private val recognizer = if (SpeechRecognizer.isRecognitionAvailable(appContext)) {
        SpeechRecognizer.createSpeechRecognizer(appContext).also {
            it.setRecognitionListener(this)
        }
    } else {
        null
    }

    fun startListening() {
        val recognizer = recognizer
        if (recognizer == null) {
            onStateChanged(VoiceRecognitionState.Unavailable("speech recognizer unavailable"))
            return
        }

        onStateChanged(VoiceRecognitionState.Listening)
        recognizer.startListening(recognitionIntent())
    }

    fun destroy() {
        recognizer?.destroy()
    }

    override fun onReadyForSpeech(params: Bundle?) {
        onStateChanged(VoiceRecognitionState.Listening)
    }

    override fun onBeginningOfSpeech() = Unit

    override fun onRmsChanged(rmsdB: Float) = Unit

    override fun onBufferReceived(buffer: ByteArray?) = Unit

    override fun onEndOfSpeech() = Unit

    override fun onError(error: Int) {
        onStateChanged(VoiceRecognitionState.Error(error.toVoiceErrorMessage()))
    }

    override fun onResults(results: Bundle?) {
        val candidates = results
            ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            .orEmpty()
        val command = VoiceCommandParser.parse(candidates)
        if (command != null) {
            onStateChanged(
                VoiceRecognitionState.Heard(
                    transcript = command.transcript,
                    actionId = command.actionId,
                ),
            )
            onCommand(command.signal)
        } else {
            onStateChanged(
                VoiceRecognitionState.NoMatch(
                    transcript = candidates.firstOrNull().orEmpty(),
                ),
            )
        }
    }

    override fun onPartialResults(partialResults: Bundle?) = Unit

    override fun onEvent(eventType: Int, params: Bundle?) = Unit

    private fun recognitionIntent(): Intent {
        return Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM,
            )
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().toLanguageTag())
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, MaxRecognitionResults)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
        }
    }

    private fun Int.toVoiceErrorMessage(): String {
        return when (this) {
            SpeechRecognizer.ERROR_AUDIO -> "audio capture failed"
            SpeechRecognizer.ERROR_CLIENT -> "client error"
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "missing microphone permission"
            SpeechRecognizer.ERROR_NETWORK -> "network error"
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "network timeout"
            SpeechRecognizer.ERROR_NO_MATCH -> "no speech match"
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "recognizer busy"
            SpeechRecognizer.ERROR_SERVER -> "server error"
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "speech timeout"
            else -> "unknown error $this"
        }
    }

    private companion object {
        const val MaxRecognitionResults = 5
    }
}
