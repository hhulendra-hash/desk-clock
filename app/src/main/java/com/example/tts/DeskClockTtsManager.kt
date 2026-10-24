package com.example.tts

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

data class TtsUiState(
    val isInitialized: Boolean = false,
    val isMuted: Boolean = false,
    val isSpeaking: Boolean = false,
    val speechRate: Float = 1.0f,
    val lastSpokenAnnouncement: String = "Ready for voice announcements."
)

class DeskClockTtsManager(context: Context) {

    private val _state = MutableStateFlow(TtsUiState())
    val state: StateFlow<TtsUiState> = _state.asStateFlow()

    private var tts: TextToSpeech? = null

    init {
        try {
            tts = TextToSpeech(context.applicationContext) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    tts?.language = Locale.US
                    tts?.setSpeechRate(_state.value.speechRate)
                    tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                        override fun onStart(utteranceId: String?) {
                            _state.value = _state.value.copy(isSpeaking = true)
                        }

                        override fun onDone(utteranceId: String?) {
                            _state.value = _state.value.copy(isSpeaking = false)
                        }

                        @Deprecated("Deprecated in Java")
                        override fun onError(utteranceId: String?) {
                            _state.value = _state.value.copy(isSpeaking = false)
                        }
                    })
                    _state.value = _state.value.copy(isInitialized = true)
                }
            }
        } catch (_: Exception) {
            // Safe fallback on JVM unit tests / headless environments
            _state.value = _state.value.copy(isInitialized = false)
        }
    }

    fun speakAnnouncement(text: String, forceSpeakEvenIfMuted: Boolean = false) {
        if (text.isBlank()) return
        _state.value = _state.value.copy(lastSpokenAnnouncement = text)

        if (_state.value.isMuted && !forceSpeakEvenIfMuted) return

        try {
            if (_state.value.isInitialized) {
                tts?.setSpeechRate(_state.value.speechRate)
                tts?.speak(
                    text,
                    TextToSpeech.QUEUE_FLUSH,
                    null,
                    "chronostudy_tts_${System.currentTimeMillis()}"
                )
            }
        } catch (_: Exception) {
            // Ignore in test environments
        }
    }

    fun toggleMute() {
        val nextMuted = !_state.value.isMuted
        _state.value = _state.value.copy(isMuted = nextMuted)
        if (nextMuted) {
            stopSpeaking()
        }
    }

    fun setSpeechRate(rate: Float) {
        val clamped = rate.coerceIn(0.75f, 1.5f)
        _state.value = _state.value.copy(speechRate = clamped)
        try {
            tts?.setSpeechRate(clamped)
        } catch (_: Exception) {
        }
    }

    fun stopSpeaking() {
        try {
            tts?.stop()
        } catch (_: Exception) {
        }
        _state.value = _state.value.copy(isSpeaking = false)
    }

    fun shutdown() {
        try {
            tts?.stop()
            tts?.shutdown()
        } catch (_: Exception) {
        }
    }
}
