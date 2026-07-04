package com.englishfriendai.app.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.englishfriendai.app.core.util.DateBucket
import com.englishfriendai.app.core.util.DateUtils
import com.englishfriendai.app.domain.model.Conversation
import com.englishfriendai.app.domain.usecase.DeleteConversationUseCase
import com.englishfriendai.app.domain.usecase.GetConversationsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class HistoryUiState {
    data object Loading : HistoryUiState()
    data class Content(val grouped: Map<DateBucket, List<Conversation>>) : HistoryUiState()
    data object Empty : HistoryUiState()
    data class Error(val message: String) : HistoryUiState()
}

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val getConversationsUseCase: GetConversationsUseCase,
    private val deleteConversationUseCase: DeleteConversationUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<HistoryUiState>(HistoryUiState.Loading)
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        observeConversations()
    }

    private fun observeConversations() {
        getConversationsUseCase()
            .onEach { conversations ->
                _uiState.value = if (conversations.isEmpty()) {
                    HistoryUiState.Empty
                } else {
                    HistoryUiState.Content(
                        conversations.groupBy { DateUtils.bucketFor(it.updatedAt) }
                    )
                }
            }
            .catch { throwable ->
                _uiState.value = HistoryUiState.Error(throwable.message ?: "Failed to load history")
            }
            .launchIn(viewModelScope)
    }

    fun deleteConversation(conversationId: String) {
        viewModelScope.launch {
            deleteConversationUseCase(conversationId)
        }
    }
}
