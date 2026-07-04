package com.englishfriendai.app.data.mapper

import com.englishfriendai.app.data.remote.dto.ProgressDto
import com.englishfriendai.app.domain.model.Progress

fun ProgressDto.toDomain(): Progress = Progress(
    streakDays = streakDays,
    totalPracticeMinutes = totalPracticeMinutes,
    conversationsCompleted = conversationsCompleted,
    weeklyScores = weeklyScores.map { it.toDomain() },
    latestScore = latestScore?.toDomain()
)
