package com.englishfriendai.app.presentation.history

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.englishfriendai.app.core.util.DateBucket
import com.englishfriendai.app.presentation.common.EmptyState
import com.englishfriendai.app.presentation.common.ErrorView
import com.englishfriendai.app.presentation.common.LoadingIndicator

private fun DateBucket.label(): String = when (this) {
    DateBucket.TODAY -> "Today"
    DateBucket.YESTERDAY -> "Yesterday"
    DateBucket.LAST_WEEK -> "Last Week"
    DateBucket.LAST_MONTH -> "Last Month"
    DateBucket.OLDER -> "Older"
}

@Composable
fun HistoryScreen(
    onConversationClick: (String) -> Unit,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold { paddingValues ->
        when (val state = uiState) {
            is HistoryUiState.Loading -> LoadingIndicator(modifier = Modifier.padding(paddingValues))
            is HistoryUiState.Error -> ErrorView(message = state.message, modifier = Modifier.padding(paddingValues))
            is HistoryUiState.Empty -> EmptyState(
                title = "No conversations yet",
                subtitle = "Your practice sessions will show up here.",
                icon = Icons.Default.History,
                modifier = Modifier.padding(paddingValues)
            )
            is HistoryUiState.Content -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(paddingValues)
                ) {
                    DateBucket.entries.forEach { bucket ->
                        val conversations = state.grouped[bucket].orEmpty()
                        if (conversations.isNotEmpty()) {
                            item {
                                Text(
                                    text = bucket.label(),
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                            items(conversations, key = { it.id }) { conversation ->
                                ConversationListItem(
                                    conversation = conversation,
                                    onClick = { onConversationClick(conversation.id) },
                                    onDelete = { viewModel.deleteConversation(conversation.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
