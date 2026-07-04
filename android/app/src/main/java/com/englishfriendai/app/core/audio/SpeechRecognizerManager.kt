package com.englishfriendai.app.core.audio

import android.content.Context
import android.content.Intent
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.util.Locale
import javax.inject.Inject

/** Result of a single speech-to-text pass, streamed to the MicButton / ChatViewModel. */
sealed class SpeechRecognitionResult {
    data class PartialResult(val text: String) : SpeechRecognitionResult()
    data class FinalResult(val text: String) : SpeechRecognitionResult()
    data class Error(val message: String) : SpeechRecognitionResult()
}

interface SpeechRecognizerManager {
    fun startListening(languageTag: String = Locale.ENGLISH.toLanguageTag()): Flow<SpeechRecognitionResult>
    fun stopListening()
}

/**
 * Default implementation backed by the platform [SpeechRecognizer]. Requires
 * `RECORD_AUDIO` (declared in the manifest) and should be started only after that
 * runtime permission has been granted by the caller.
 */
class SpeechRecognizerManagerImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : SpeechRecognizerManager {

    private var recognizer: SpeechRecognizer? = null

    override fun startListening(languageTag: String): Flow<SpeechRecognitionResult> = callbackFlow {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            trySend(SpeechRecognitionResult.Error("Speech recognition not available on this device"))
            close()
            return@callbackFlow
        }

        val speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).also { recognizer = it }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageTag)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }

        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: android.os.Bundle?) = Unit
            override fun onBeginningOfSpeech() = Unit
            override fun onRmsChanged(rmsdB: Float) = Unit
            override fun onBufferReceived(buffer: ByteArray?) = Unit
            override fun onEndOfSpeech() = Unit

            override fun onError(error: Int) {
                trySend(SpeechRecognitionResult.Error("Speech recognizer error code $error"))
            }

            override fun onResults(results: android.os.Bundle?) {
                val text = results
                    ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.firstOrNull()
                    .orEmpty()
                trySend(SpeechRecognitionResult.FinalResult(text))
            }

            override fun onPartialResults(partialResults: android.os.Bundle?) {
                val text = partialResults
                    ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.firstOrNull()
                    .orEmpty()
                trySend(SpeechRecognitionResult.PartialResult(text))
            }

            override fun onEvent(eventType: Int, params: android.os.Bundle?) = Unit
        })

        speechRecognizer.startListening(intent)

        awaitClose {
            speechRecognizer.stopListening()
            speechRecognizer.destroy()
            recognizer = null
        }
    }

    override fun stopListening() {
        recognizer?.stopListening()
    }
}
