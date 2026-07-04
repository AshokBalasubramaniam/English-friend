package com.englishfriendai.app.presentation.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.englishfriendai.app.R
import com.englishfriendai.app.presentation.chat.components.MessageBubble
import com.englishfriendai.app.presentation.chat.components.MicButton
import com.englishfriendai.app.presentation.chat.components.ModeSelector
import com.englishfriendai.app.presentation.common.AppTopBar
import kotlinx.coroutines.launch

private data class Suggestion(val emoji: String, val text: String)

private val SUGGESTIONS = listOf(
    Suggestion("👋", "Hello"),
    Suggestion("☕", "How are you?"),
    Suggestion("💼", "Tell me about your work")
)

@Composable
fun ChatScreen(
    onNavigateBack: (() -> Unit)? = null,
    viewModel: ChatViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val streakDays by viewModel.streakDays.collectAsState()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            coroutineScope.launch { listState.animateScrollToItem(uiState.messages.lastIndex) }
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = stringResource(R.string.chat_title),
                onBackClick = onNavigateBack,
                actions = { StreakBadge(days = streakDays) }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            ModeSelector(
                selectedMode = uiState.mode,
                onModeSelected = viewModel::onModeSelected,
                modifier = Modifier.fillMaxWidth().padding(12.dp)
            )

            if (uiState.messages.isEmpty()) {
                ChatHero(modifier = Modifier.weight(1f))
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.messages, key = { it.id }) { message ->
                        MessageBubble(message = message)
                    }
                }
            }

            if (uiState.showSuggestions) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    items(SUGGESTIONS) { suggestion ->
                        SuggestionChip(
                            onClick = { viewModel.sendSuggestion(suggestion.text) },
                            label = { Text("${suggestion.emoji} ${suggestion.text}") },
                            colors = SuggestionChipDefaults.suggestionChipColors()
                        )
                    }
                }
            }

            uiState.errorMessage?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = uiState.inputText,
                    onValueChange = viewModel::onInputTextChanged,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(28.dp),
                    placeholder = { Text(stringResource(R.string.chat_input_hint)) },
                    singleLine = true
                )

                MicButton(isRecording = uiState.isRecording, onClick = viewModel::onMicToggle)

                FilledIconButton(onClick = viewModel::sendMessage, enabled = !uiState.isSending) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send")
                }
            }
        }
    }
}

/** Streak badge (e.g. "🔥 7") shown in the top bar — mirrors the dashboard's daily streak. */
@Composable
private fun StreakBadge(days: Int) {
    if (days <= 0) return
    Row(
        modifier = Modifier
            .padding(end = 12.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.errorContainer)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.Whatshot,
            contentDescription = "Streak",
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = days.toString(),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onErrorContainer
        )
    }
}

/**
 * Illustrated empty-state shown before the AI's opening greeting has arrived (or if it
 * failed to generate) — a friendly avatar plus a generic placeholder. Once the real,
 * personalized greeting loads, this is replaced by an actual chat bubble.
 */
@Composable
private fun ChatHero(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.SmartToy,
                contentDescription = null,
                modifier = Modifier.size(52.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        Text(
            text = "Hi there! 👋",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 16.dp)
        )
        Text(
            text = "I'm your AI conversation partner.\nSay hello to start practicing!",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}
