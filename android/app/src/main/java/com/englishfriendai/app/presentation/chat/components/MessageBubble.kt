package com.englishfriendai.app.presentation.chat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.englishfriendai.app.core.util.DateUtils
import com.englishfriendai.app.domain.model.Message
import com.englishfriendai.app.domain.model.Sender
import com.englishfriendai.app.presentation.theme.AiBubbleDark
import com.englishfriendai.app.presentation.theme.AiBubbleLight
import com.englishfriendai.app.presentation.theme.UserBubbleDark
import com.englishfriendai.app.presentation.theme.UserBubbleLight

/**
 * Chat bubble. Per the product spec this app deliberately flips the usual messaging-app
 * convention: the learner's own (USER) messages are pinned to the LEFT, and the AI tutor's
 * (AI) replies appear on the RIGHT — the AI acts as the "other" conversation partner the
 * layout visually foregrounds.
 */
@Composable
fun MessageBubble(
    message: Message,
    modifier: Modifier = Modifier,
    onCorrectionClick: (() -> Unit)? = null
) {
    val isUser = message.sender == Sender.USER
    val bubbleColor = if (isUser) {
        if (isSystemInDarkThemeCompat()) UserBubbleDark else UserBubbleLight
    } else {
        if (isSystemInDarkThemeCompat()) AiBubbleDark else AiBubbleLight
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.Start else Arrangement.End
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(bubbleColor)
                .padding(12.dp),
            horizontalAlignment = if (isUser) Alignment.Start else Alignment.End
        ) {
            Text(text = message.englishText, style = MaterialTheme.typography.bodyLarge)

            message.tamilTranslation?.takeIf { it.isNotBlank() }?.let { tamil ->
                Text(
                    text = tamil,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = DateUtils.formatTime(message.timestamp),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (message.correction != null && onCorrectionClick != null) {
                Text(
                    text = "View correction",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .clickable(onClick = onCorrectionClick)
                )
            }
        }
    }
}

@Composable
private fun isSystemInDarkThemeCompat(): Boolean = androidx.compose.foundation.isSystemInDarkTheme()
