package com.englishfriendai.app.presentation.chat.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.englishfriendai.app.R
import com.englishfriendai.app.domain.model.ConversationMode

/** Lets the learner switch the conversation's language mode: English / Tamil+English / Tamil. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModeSelector(
    selectedMode: ConversationMode,
    onModeSelected: (ConversationMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val options = listOf(
        ConversationMode.ENGLISH to stringResource(R.string.mode_english),
        ConversationMode.TAMIL_ENGLISH to stringResource(R.string.mode_tamil_english),
        ConversationMode.TAMIL to stringResource(R.string.mode_tamil)
    )

    SingleChoiceSegmentedButtonRow(modifier = modifier) {
        options.forEachIndexed { index, (mode, label) ->
            SegmentedButton(
                selected = selectedMode == mode,
                onClick = { onModeSelected(mode) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size)
            ) {
                Text(label)
            }
        }
    }
}
