package com.englishfriendai.app.presentation.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.englishfriendai.app.domain.model.ConversationMode
import com.englishfriendai.app.presentation.chat.components.ModeSelector
import com.englishfriendai.app.presentation.common.AppTopBar

@Composable
fun SettingsScreen(
    onLoggedOut: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val preferences by viewModel.preferences.collectAsState()

    Scaffold(topBar = { AppTopBar(title = "Settings") }) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            SettingRow(label = "Dark mode") {
                Switch(checked = preferences.isDarkMode, onCheckedChange = viewModel::setDarkMode)
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            Text(text = "Language mode", style = MaterialTheme.typography.titleMedium)
            ModeSelector(
                selectedMode = ConversationMode.entries.find { it.name == preferences.languageMode }
                    ?: ConversationMode.ENGLISH,
                onModeSelected = { viewModel.setLanguageMode(it.name) },
                modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            SettingRow(label = "Daily practice reminders") {
                Switch(checked = preferences.remindersEnabled, onCheckedChange = viewModel::setRemindersEnabled)
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            SettingRow(label = "AI asks me questions") {
                Switch(checked = preferences.aiAsksQuestions, onCheckedChange = viewModel::setAiAsksQuestions)
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            Text(text = "AI voice", style = MaterialTheme.typography.titleMedium)
            Row(
                modifier = Modifier.padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = preferences.voiceGender == "FEMALE",
                    onClick = { viewModel.setVoiceGender("FEMALE") },
                    label = { Text("Female") }
                )
                FilterChip(
                    selected = preferences.voiceGender == "MALE",
                    onClick = { viewModel.setVoiceGender("MALE") },
                    label = { Text("Male") }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.VolumeDown, contentDescription = null)
                Slider(
                    value = preferences.voiceVolume,
                    onValueChange = viewModel::setVoiceVolume,
                    valueRange = 0f..1f,
                    modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                )
                Icon(Icons.Filled.VolumeUp, contentDescription = null)
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            Button(
                onClick = {
                    viewModel.logout()
                    onLoggedOut()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Log out")
            }
        }
    }
}

@Composable
private fun SettingRow(label: String, control: @Composable () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
        control()
    }
}
