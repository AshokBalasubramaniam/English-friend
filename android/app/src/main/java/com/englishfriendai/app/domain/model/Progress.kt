package com.englishfriendai.app.domain.model

/** Aggregated learner progress backing the Dashboard screen. */
data class Progress(
    val streakDays: Int,
    val totalPracticeMinutes: Int,
    val conversationsCompleted: Int,
    val weeklyScores: List<ConversationScore>,
    val latestScore: ConversationScore?
)
