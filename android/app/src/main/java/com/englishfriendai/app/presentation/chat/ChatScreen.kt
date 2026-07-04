package com.englishfriendai.app.presentation.chat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.englishfriendai.app.R
import com.englishfriendai.app.presentation.chat.components.MessageBubble
import com.englishfriendai.app.presentation.chat.components.MicButton
import com.englishfriendai.app.presentation.chat.components.ModeSelector
import com.englishfriendai.app.presentation.common.AppTopBar
import com.englishfriendai.app.presentation.common.EmptyState
import kotlinx.coroutines.launch

@Composable
fun ChatScreen(
    onNavigateBack: (() -> Unit)? = null,
    viewModel: ChatViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            coroutineScope.launch { listState.animateScrollToItem(uiState.messages.lastIndex) }
        }
    }

    Scaffold(
        topBar = { AppTopBar(title = stringResource(R.string.chat_title), onBackClick = onNavigateBack) }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            ModeSelector(
                selectedMode = uiState.mode,
                onModeSelected = viewModel::onModeSelected,
                modifier = Modifier.fillMaxWidth().padding(12.dp)
            )

            if (uiState.messages.isEmpty()) {
                EmptyState(
                    title = "Start practicing",
                    subtitle = "Say hello to begin your conversation with your AI friend.",
                    icon = Icons.Default.Chat,
                    modifier = Modifier.weight(1f)
                )
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.messages, key = { it.id }) { message ->
                        MessageBubble(message = message)
                    }
                }
            }

            uiState.errorMessage?.let { error ->
                androidx.compose.material3.Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(horizontal = 12.dp)
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
                    placeholder = { androidx.compose.material3.Text(stringResource(R.string.chat_input_hint)) },
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
