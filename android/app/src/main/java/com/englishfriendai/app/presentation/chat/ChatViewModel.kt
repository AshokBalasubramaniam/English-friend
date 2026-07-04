package com.englishfriendai.app.presentation.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.englishfriendai.app.core.audio.AudioPlayer
import com.englishfriendai.app.core.audio.SpeechRecognitionResult
import com.englishfriendai.app.core.audio.SpeechRecognizerManager
import com.englishfriendai.app.core.audio.TextToSpeechManager
import com.englishfriendai.app.core.audio.VoiceGender
import com.englishfriendai.app.data.local.datastore.UserPreferencesDataStore
import com.englishfriendai.app.domain.model.ConversationMode
import com.englishfriendai.app.domain.model.Message
import com.englishfriendai.app.domain.model.Sender
import com.englishfriendai.app.domain.usecase.SendMessageUseCase
import com.englishfriendai.app.domain.usecase.StartNewConversationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChatUiState(
    val conversationId: String? = null,
    val messages: List<Message> = emptyList(),
    val mode: ConversationMode = ConversationMode.ENGLISH,
    val isSending: Boolean = false,
    val isStartingConversation: Boolean = false,
    val isRecording: Boolean = false,
    val inputText: String = "",
    val errorMessage: String? = null
) {
    /** Show the "say hello" quick-reply chips only before the user has actually replied. */
    val showSuggestions: Boolean get() = messages.none { it.sender == Sender.USER }
}

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val startNewConversationUseCase: StartNewConversationUseCase,
    private val sendMessageUseCase: SendMessageUseCase,
    private val audioPlayer: AudioPlayer,
    private val speechRecognizerManager: SpeechRecognizerManager,
    private val textToSpeechManager: TextToSpeechManager,
    private val userPreferencesDataStore: UserPreferencesDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    val streakDays: StateFlow<Int> = userPreferencesDataStore.userPreferencesFlow
        .map { it.streakDays }
        .stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = 0)

    private var listeningJob: Job? = null

    init {
        textToSpeechManager.initialize()
        startNewConversation()
    }

    /**
     * Eagerly creates a conversation and shows the AI's personalized opening greeting as
     * soon as the chat screen opens, rather than waiting for the user to speak first.
     */
    private fun startNewConversation() {
        _uiState.value = _uiState.value.copy(isStartingConversation = true)
        viewModelScope.launch {
            // Not using Result.onSuccess/onFailure here: their lambdas aren't `suspend`,
            // and speakAiMessage() below needs to be.
            val (conversationId, greeting) = startNewConversationUseCase(_uiState.value.mode)
                .getOrNull() ?: run {
                    // Non-fatal: the empty-state placeholder still lets the user type first,
                    // and sendMessage() falls back to creating a conversation lazily anyway.
                    _uiState.value = _uiState.value.copy(isStartingConversation = false)
                    return@launch
                }

            _uiState.value = _uiState.value.copy(
                conversationId = conversationId,
                messages = greeting?.let { listOf(it) } ?: _uiState.value.messages,
                isStartingConversation = false
            )
            greeting?.let { speakAiMessage(it.englishText) }
        }
    }

    fun onInputTextChanged(text: String) {
        _uiState.value = _uiState.value.copy(inputText = text)
    }

    fun onModeSelected(mode: ConversationMode) {
        _uiState.value = _uiState.value.copy(mode = mode)
    }

    /**
     * Starts/stops on-device speech recognition. The transcript is only sent as a message
     * once the user finishes speaking (final result) — partial results just live-update the
     * input field so the user can see it's picking up their speech.
     */
    fun onMicToggle() {
        if (_uiState.value.isRecording) {
            stopListening()
        } else {
            startListening()
        }
    }

    private fun startListening() {
        _uiState.value = _uiState.value.copy(isRecording = true)
        val languageTag = when (_uiState.value.mode) {
            ConversationMode.ENGLISH, ConversationMode.TAMIL_ENGLISH -> "en-US"
            ConversationMode.TAMIL -> "ta-IN"
        }
        listeningJob = speechRecognizerManager.startListening(languageTag)
            .onEach { result ->
                when (result) {
                    is SpeechRecognitionResult.PartialResult -> onInputTextChanged(result.text)
                    is SpeechRecognitionResult.FinalResult -> {
                        _uiState.value = _uiState.value.copy(isRecording = false)
                        if (result.text.isNotBlank()) {
                            onInputTextChanged(result.text)
                            sendMessage()
                        }
                        listeningJob?.cancel()
                    }
                    is SpeechRecognitionResult.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isRecording = false,
                            errorMessage = result.message
                        )
                        listeningJob?.cancel()
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    private fun stopListening() {
        speechRecognizerManager.stopListening()
        listeningJob?.cancel()
        _uiState.value = _uiState.value.copy(isRecording = false)
    }

    /** Quick-reply suggestion chips (e.g. "Hello", "How are you?") send immediately on tap. */
    fun sendSuggestion(text: String) {
        onInputTextChanged(text)
        sendMessage()
    }

    fun sendMessage() {
        val state = _uiState.value
        val text = state.inputText.trim()
        if (text.isBlank() || state.isSending) return

        _uiState.value = state.copy(inputText = "", isSending = true, errorMessage = null)

        sendMessageUseCase(state.conversationId, text, state.mode)
            .onEach { message ->
                appendOrReplaceStreamingMessage(message)
                message.audioUrl?.let { audioPlayer.play(it) }
                // Only speak the final AI reply, not each intermediate streaming chunk
                // (STREAMING_ID) — otherwise partial text would be spoken repeatedly/garbled.
                if (message.sender == Sender.AI && message.id != STREAMING_ID) {
                    speakAiMessage(message.englishText)
                }
            }
            .catch { throwable ->
                _uiState.value = _uiState.value.copy(
                    isSending = false,
                    errorMessage = throwable.message ?: "Failed to send message"
                )
            }
            .onCompletion { _uiState.value = _uiState.value.copy(isSending = false) }
            .launchIn(viewModelScope)
    }

    /** Reads an AI reply aloud (English text only) using the user's chosen voice/volume. */
    private suspend fun speakAiMessage(text: String) {
        val prefs = userPreferencesDataStore.userPreferencesFlow.first()
        val gender = runCatching { VoiceGender.valueOf(prefs.voiceGender) }.getOrDefault(VoiceGender.FEMALE)
        textToSpeechManager.setVoiceGender(gender)
        textToSpeechManager.speak(text, volume = prefs.voiceVolume)
    }

    private fun appendOrReplaceStreamingMessage(message: Message) {
        val current = _uiState.value.messages
        val withoutStreamingPlaceholder = current.filterNot { it.id == STREAMING_ID && message.id != STREAMING_ID }
        val replacedExisting = withoutStreamingPlaceholder.any { it.id == message.id }

        val updated = if (replacedExisting) {
            withoutStreamingPlaceholder.map { if (it.id == message.id) message else it }
        } else {
            withoutStreamingPlaceholder + message
        }

        _uiState.value = _uiState.value.copy(
            messages = updated,
            conversationId = _uiState.value.conversationId ?: message.conversationId
        )
    }

    override fun onCleared() {
        super.onCleared()
        audioPlayer.release()
        speechRecognizerManager.stopListening()
        textToSpeechManager.stop()
    }

    companion object {
        private const val STREAMING_ID = "streaming"
    }
}
