package com.englishfriendai.app.presentation.vocabulary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.englishfriendai.app.domain.model.VocabularyItem
import com.englishfriendai.app.domain.usecase.GetVocabularyUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

sealed class VocabularyUiState {
    data object Loading : VocabularyUiState()
    data class Content(val items: List<VocabularyItem>) : VocabularyUiState()
    data object Empty : VocabularyUiState()
    data class Error(val message: String) : VocabularyUiState()
}

@HiltViewModel
class VocabularyViewModel @Inject constructor(
    getVocabularyUseCase: GetVocabularyUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<VocabularyUiState>(VocabularyUiState.Loading)
    val uiState: StateFlow<VocabularyUiState> = _uiState.asStateFlow()

    init {
        getVocabularyUseCase()
            .onEach { items ->
                _uiState.value = if (items.isEmpty()) VocabularyUiState.Empty else VocabularyUiState.Content(items)
            }
            .catch { throwable ->
                _uiState.value = VocabularyUiState.Error(throwable.message ?: "Failed to load vocabulary")
            }
            .launchIn(viewModelScope)
    }
}
