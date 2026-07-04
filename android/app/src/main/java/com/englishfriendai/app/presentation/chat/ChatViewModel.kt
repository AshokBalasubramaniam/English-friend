package com.englishfriendai.app.presentation.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.englishfriendai.app.core.audio.AudioPlayer
import com.englishfriendai.app.domain.model.ConversationMode
import com.englishfriendai.app.domain.model.Message
import com.englishfriendai.app.domain.usecase.SendMessageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

data class ChatUiState(
    val conversationId: String? = null,
    val messages: List<Message> = emptyList(),
    val mode: ConversationMode = ConversationMode.ENGLISH,
    val isSending: Boolean = false,
    val isRecording: Boolean = false,
    val inputText: String = "",
    val errorMessage: String? = null
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val sendMessageUseCase: SendMessageUseCase,
    private val audioPlayer: AudioPlayer
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    fun onInputTextChanged(text: String) {
        _uiState.value = _uiState.value.copy(inputText = text)
    }

    fun onModeSelected(mode: ConversationMode) {
        _uiState.value = _uiState.value.copy(mode = mode)
    }

    fun onMicToggle() {
        _uiState.value = _uiState.value.copy(isRecording = !_uiState.value.isRecording)
        // TODO: wire SpeechRecognizerManager.startListening()/stopListening() here and feed
        // partial/final transcripts into onInputTextChanged().
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
    }

    companion object {
        private const val STREAMING_ID = "streaming"
    }
}
