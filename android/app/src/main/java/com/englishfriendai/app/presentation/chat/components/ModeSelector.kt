package com.englishfriendai.app.presentation.chat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.englishfriendai.app.R
import com.englishfriendai.app.domain.model.ConversationMode

private data class ModeOption(val mode: ConversationMode, val label: String, val icon: ImageVector? = null, val glyph: String? = null)

/** Lets the learner switch the conversation's language mode: English / Tamil+English / Tamil. */
@Composable
fun ModeSelector(
    selectedMode: ConversationMode,
    onModeSelected: (ConversationMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val options = listOf(
        ModeOption(ConversationMode.ENGLISH, stringResource(R.string.mode_english), icon = Icons.Filled.Language),
        ModeOption(ConversationMode.TAMIL_ENGLISH, stringResource(R.string.mode_tamil_english), icon = Icons.Filled.Translate),
        ModeOption(ConversationMode.TAMIL, stringResource(R.string.mode_tamil), glyph = "அ")
    )

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { option ->
            ModeCard(
                option = option,
                selected = option.mode == selectedMode,
                onClick = { onModeSelected(option.mode) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ModeCard(
    option: ModeOption,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val background = if (selected) {
        Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.tertiary))
    } else {
        Brush.linearGradient(
            listOf(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.surfaceVariant)
        )
    }
    val contentColor = if (selected) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Column(
        modifier = modifier
            .aspectRatio(1.35f)
            .clip(RoundedCornerShape(16.dp))
            .background(background)
            .clickable(onClick = onClick)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (option.icon != null) {
            Icon(imageVector = option.icon, contentDescription = null, tint = contentColor)
        } else if (option.glyph != null) {
            Text(text = option.glyph, fontSize = 24.sp, color = contentColor, fontWeight = FontWeight.Bold)
        }
        Text(
            text = option.label,
            style = MaterialTheme.typography.labelLarge,
            color = contentColor,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}
