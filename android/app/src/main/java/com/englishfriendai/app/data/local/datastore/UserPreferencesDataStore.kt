package com.englishfriendai.app.data.local.datastore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.englishfriendai.app.core.util.Constants
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = Constants.DATASTORE_NAME)

/**
 * Settings state that is fine to keep in plain (unencrypted) DataStore Preferences.
 *
 * Deliberately does NOT include a session/login-state field: the real session lives in
 * [com.englishfriendai.app.core.security.EncryptedPrefsManager] (the tokens actually used
 * for API auth). A previous version duplicated a "sessionToken" flag here for splash-screen
 * routing, but TokenAuthenticator's silent refresh only ever updated EncryptedPrefsManager,
 * so the two could drift out of sync - if EncryptedPrefsManager's tokens were ever cleared
 * (e.g. an invalid refresh) while this stale flag lingered, Splash would wrongly route
 * straight into the app with no usable session and no way to recover except a manual logout.
 */
data class UserPreferences(
    val languageMode: String,
    val isDarkMode: Boolean,
    val streakDays: Int,
    val remindersEnabled: Boolean,
    val aiAsksQuestions: Boolean,
    /** "MALE" or "FEMALE" — matches [com.englishfriendai.app.core.audio.VoiceGender].name. */
    val voiceGender: String,
    val voiceVolume: Float
)

@Singleton
class UserPreferencesDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private object Keys {
        val LANGUAGE_MODE = stringPreferencesKey("language_mode")
        val DARK_MODE = booleanPreferencesKey("dark_mode")
        val STREAK_DAYS = intPreferencesKey("streak_days")
        val REMINDERS_ENABLED = booleanPreferencesKey("reminders_enabled")
        val AI_ASKS_QUESTIONS = booleanPreferencesKey("ai_asks_questions")
        val VOICE_GENDER = stringPreferencesKey("voice_gender")
        val VOICE_VOLUME = floatPreferencesKey("voice_volume")
    }

    val userPreferencesFlow: Flow<UserPreferences> = context.dataStore.data.map { prefs ->
        UserPreferences(
            languageMode = prefs[Keys.LANGUAGE_MODE] ?: "ENGLISH",
            isDarkMode = prefs[Keys.DARK_MODE] ?: false,
            streakDays = prefs[Keys.STREAK_DAYS] ?: 0,
            remindersEnabled = prefs[Keys.REMINDERS_ENABLED] ?: true,
            aiAsksQuestions = prefs[Keys.AI_ASKS_QUESTIONS] ?: true,
            voiceGender = prefs[Keys.VOICE_GENDER] ?: "FEMALE",
            voiceVolume = prefs[Keys.VOICE_VOLUME] ?: 1f
        )
    }

    suspend fun saveLanguageMode(mode: String) {
        context.dataStore.edit { prefs -> prefs[Keys.LANGUAGE_MODE] = mode }
    }

    suspend fun saveDarkMode(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[Keys.DARK_MODE] = enabled }
    }

    suspend fun saveStreakDays(days: Int) {
        context.dataStore.edit { prefs -> prefs[Keys.STREAK_DAYS] = days }
    }

    suspend fun saveRemindersEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[Keys.REMINDERS_ENABLED] = enabled }
    }

    suspend fun saveAiAsksQuestions(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[Keys.AI_ASKS_QUESTIONS] = enabled }
    }

    suspend fun saveVoiceGender(gender: String) {
        context.dataStore.edit { prefs -> prefs[Keys.VOICE_GENDER] = gender }
    }

    suspend fun saveVoiceVolume(volume: Float) {
        context.dataStore.edit { prefs -> prefs[Keys.VOICE_VOLUME] = volume.coerceIn(0f, 1f) }
    }

    suspend fun clear() {
        context.dataStore.edit { it.clear() }
    }
}
