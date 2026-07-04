package com.englishfriendai.app.presentation.vocabulary

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.englishfriendai.app.domain.model.VocabularyItem
import com.englishfriendai.app.presentation.common.AppTopBar
import com.englishfriendai.app.presentation.common.EmptyState
import com.englishfriendai.app.presentation.common.ErrorView
import com.englishfriendai.app.presentation.common.LoadingIndicator

@Composable
fun VocabularyScreen(viewModel: VocabularyViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(topBar = { AppTopBar(title = "Vocabulary") }) { paddingValues ->
        when (val state = uiState) {
            is VocabularyUiState.Loading -> LoadingIndicator(modifier = Modifier.padding(paddingValues))
            is VocabularyUiState.Error -> ErrorView(message = state.message, modifier = Modifier.padding(paddingValues))
            is VocabularyUiState.Empty -> EmptyState(
                title = "No vocabulary yet",
                subtitle = "New words you learn during conversations will appear here.",
                icon = Icons.Default.MenuBook,
                modifier = Modifier.padding(paddingValues)
            )
            is VocabularyUiState.Content -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp)
            ) {
                items(state.items, key = { it.id }) { item -> VocabularyCard(item) }
            }
        }
    }
}

@Composable
private fun VocabularyCard(item: VocabularyItem) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        androidx.compose.foundation.layout.Column(modifier = Modifier.padding(16.dp)) {
            Text(text = item.word, style = MaterialTheme.typography.titleMedium)
            Text(text = item.pronunciation, style = MaterialTheme.typography.labelSmall)
            Text(text = item.meaning, style = MaterialTheme.typography.bodyMedium)
            Text(
                text = item.tamilMeaning,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "e.g. ${item.usageExample}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
