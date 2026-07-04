package com.englishfriendai.app.presentation.chat.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.englishfriendai.app.domain.model.VocabularyItem

/** Small chip summarizing a new vocabulary word learned within a conversation. */
@Composable
fun VocabularyChip(
    item: VocabularyItem,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    AssistChip(
        onClick = onClick,
        label = { Text(item.word) },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.MenuBook,
                contentDescription = null,
                modifier = Modifier.size(AssistChipDefaults.IconSize)
            )
        },
        modifier = modifier
    )
}
