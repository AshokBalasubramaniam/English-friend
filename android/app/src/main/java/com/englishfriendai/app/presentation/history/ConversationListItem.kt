package com.englishfriendai.app.presentation.history

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.englishfriendai.app.core.util.DateUtils
import com.englishfriendai.app.domain.model.Conversation

@Composable
fun ConversationListItem(
    conversation: Conversation,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    ListItem(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        headlineContent = { Text(conversation.title) },
        supportingContent = {
            Column {
                Text(DateUtils.formatDate(conversation.updatedAt))
                conversation.score?.let { score ->
                    Text("Overall score: ${score.overall.toInt()}%")
                }
            }
        },
        trailingContent = {
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete conversation")
            }
        }
    )
}
