package com.englishfriendai.app.presentation.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.englishfriendai.app.domain.model.ConversationScore
import com.englishfriendai.app.presentation.common.AppTopBar
import com.englishfriendai.app.presentation.common.ErrorView
import com.englishfriendai.app.presentation.common.LoadingIndicator

@Composable
fun DashboardScreen(viewModel: DashboardViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(topBar = { AppTopBar(title = "Dashboard") }) { paddingValues ->
        when (val state = uiState) {
            is DashboardUiState.Loading -> LoadingIndicator(modifier = Modifier.padding(paddingValues))
            is DashboardUiState.Error -> ErrorView(message = state.message, modifier = Modifier.padding(paddingValues))
            is DashboardUiState.Content -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                StatCard(label = "Current streak", value = "${state.progress.streakDays} days")
                StatCard(label = "Total practice time", value = "${state.progress.totalPracticeMinutes} min")
                StatCard(label = "Conversations completed", value = "${state.progress.conversationsCompleted}")

                Text(
                    text = "Weekly score trend",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 24.dp, bottom = 12.dp)
                )
                WeeklyScoreBars(scores = state.progress.weeklyScores)
            }
        }
    }
}

@Composable
private fun StatCard(label: String, value: String) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, style = MaterialTheme.typography.bodyLarge)
            Text(value, style = MaterialTheme.typography.titleMedium)
        }
    }
}

/**
 * Simple bar-row visualization of weekly overall scores — deliberately not using a charting
 * library for this scaffold, per spec. Each row's width is proportional to [ConversationScore.overall].
 */
@Composable
private fun WeeklyScoreBars(scores: List<ConversationScore>) {
    if (scores.isEmpty()) {
        Text(
            text = "No sessions scored yet this week.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        return
    }

    Column {
        scores.forEachIndexed { index, score ->
            val fraction = (score.overall / 100f).coerceIn(0f, 1f)
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                Text(
                    text = "Day ${index + 1}",
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.width(56.dp)
                )
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(16.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    androidx.compose.foundation.layout.Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction)
                            .fillMaxSize()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primary)
                    )
                }
            }
        }
    }
}
