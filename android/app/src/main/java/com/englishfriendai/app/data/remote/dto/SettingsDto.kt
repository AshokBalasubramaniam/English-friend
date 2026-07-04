package com.englishfriendai.app.data.remote.dto

import com.google.gson.annotations.SerializedName

// Only the fields this app currently reads/writes from Settings are modeled here — the
// backend's raw document also has darkMode/notificationsEnabled/languageMode, which Gson
// simply ignores since those are managed purely locally (DataStore) for now.
data class UpdateSettingsRequest(
    @SerializedName("aiAsksQuestions") val aiAsksQuestions: Boolean
)

data class SettingsEnvelope(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: SettingsData
)

data class SettingsData(
    @SerializedName("settings") val settings: SettingsDto
)

data class SettingsDto(
    @SerializedName("aiAsksQuestions") val aiAsksQuestions: Boolean = true
)
