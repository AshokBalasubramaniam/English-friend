package com.englishfriendai.app.core.audio

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/** Simulated via pitch, since Android's TextToSpeech API doesn't reliably expose voice
 * gender metadata across devices/OEMs — a lower pitch reads as more male-sounding, a
 * higher pitch as more female-sounding, on the default synthesized voice most devices ship. */
enum class VoiceGender(val pitch: Float) { MALE(0.8f), FEMALE(1.15f) }

/**
 * On-device TTS (Android's built-in speech synthesizer) used to read AI replies aloud.
 * The backend has no voice-synthesis endpoint, so this is the app's only voice output path
 * for now, not just an offline fallback.
 */
@Singleton
class TextToSpeechManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private var textToSpeech: TextToSpeech? = null
    private var isReady = false
    private var gender = VoiceGender.FEMALE

    fun initialize(onReady: () -> Unit = {}) {
        if (textToSpeech != null) {
            if (isReady) onReady()
            return
        }
        textToSpeech = TextToSpeech(context) { status ->
            isReady = status == TextToSpeech.SUCCESS
            if (isReady) {
                textToSpeech?.language = Locale.US
                textToSpeech?.setPitch(gender.pitch)
                onReady()
            }
        }
    }

    fun setVoiceGender(newGender: VoiceGender) {
        gender = newGender
        textToSpeech?.setPitch(newGender.pitch)
    }

    fun speak(text: String, volume: Float = 1f, utteranceId: String = text.hashCode().toString()) {
        if (!isReady) return
        val params = Bundle().apply {
            putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, volume.coerceIn(0f, 1f))
        }
        textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
    }

    fun setLanguage(locale: Locale) {
        textToSpeech?.language = locale
    }

    fun stop() {
        textToSpeech?.stop()
    }

    fun release() {
        textToSpeech?.shutdown()
        textToSpeech = null
        isReady = false
    }
}
