package com.englishfriendai.app.core.audio

import android.content.Context
import android.speech.tts.TextToSpeech
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * On-device fallback TTS used when a backend-generated voice reply audio URL isn't available
 * (e.g. offline mode, or before the AI service produces audio for a text chunk).
 */
@Singleton
class TextToSpeechManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private var textToSpeech: TextToSpeech? = null
    private var isReady = false

    fun initialize(onReady: () -> Unit = {}) {
        textToSpeech = TextToSpeech(context) { status ->
            isReady = status == TextToSpeech.SUCCESS
            if (isReady) {
                textToSpeech?.language = Locale.US
                onReady()
            }
        }
    }

    fun speak(text: String, utteranceId: String = text.hashCode().toString()) {
        if (!isReady) return
        textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
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
