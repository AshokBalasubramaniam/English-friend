package com.englishfriendai.app.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.englishfriendai.app.core.util.Constants
import com.englishfriendai.app.core.worker.DailyReminderWorker
import com.englishfriendai.app.data.local.datastore.UserPreferences
import com.englishfriendai.app.data.local.datastore.UserPreferencesDataStore
import com.englishfriendai.app.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferencesDataStore: UserPreferencesDataStore,
    private val authRepository: AuthRepository,
    private val workManager: WorkManager
) : ViewModel() {

    val preferences: StateFlow<UserPreferences> = userPreferencesDataStore.userPreferencesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserPreferences(
                sessionToken = null,
                languageMode = "ENGLISH",
                isDarkMode = false,
                streakDays = 0,
                remindersEnabled = true
            )
        )

    fun setDarkMode(enabled: Boolean) {
        viewModelScope.launch { userPreferencesDataStore.saveDarkMode(enabled) }
    }

    fun setLanguageMode(mode: String) {
        viewModelScope.launch { userPreferencesDataStore.saveLanguageMode(mode) }
    }

    fun setRemindersEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesDataStore.saveRemindersEnabled(enabled)
            if (enabled) scheduleDailyReminder() else cancelDailyReminder()
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            cancelDailyReminder()
        }
    }

    private fun scheduleDailyReminder() {
        val request = PeriodicWorkRequestBuilder<DailyReminderWorker>(24, TimeUnit.HOURS).build()
        workManager.enqueueUniquePeriodicWork(
            Constants.WORK_DAILY_REMINDER,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    private fun cancelDailyReminder() {
        workManager.cancelUniqueWork(Constants.WORK_DAILY_REMINDER)
    }
}
