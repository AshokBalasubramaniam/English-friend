package com.englishfriendai.app.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ProgressDto(
    @SerializedName("streak_days") val streakDays: Int,
    @SerializedName("total_practice_minutes") val totalPracticeMinutes: Int,
    @SerializedName("conversations_completed") val conversationsCompleted: Int,
    @SerializedName("weekly_scores") val weeklyScores: List<ProgressScoreDto>,
    @SerializedName("latest_score") val latestScore: ProgressScoreDto?
)
