package com.englishfriendai.app.data.mapper

import com.englishfriendai.app.data.local.db.CorrectionEntity
import com.englishfriendai.app.data.remote.dto.CorrectionDto
import com.englishfriendai.app.domain.model.Correction
import com.englishfriendai.app.domain.model.DifficultyLevel

fun CorrectionDto.toDomain(): Correction = Correction(
    original = original,
    corrected = corrected,
    reason = reason,
    example = example,
    difficultyLevel = difficultyLevel.toDifficultyLevel()
)

fun CorrectionEntity.toDomain(): Correction = Correction(
    original = original,
    corrected = corrected,
    reason = reason,
    example = example,
    difficultyLevel = difficultyLevel.toDifficultyLevel()
)

fun Correction.toEntity(messageId: String): CorrectionEntity = CorrectionEntity(
    messageId = messageId,
    original = original,
    corrected = corrected,
    reason = reason,
    example = example,
    difficultyLevel = difficultyLevel.name
)

private fun String.toDifficultyLevel(): DifficultyLevel =
    DifficultyLevel.entries.find { it.name.equals(this, ignoreCase = true) } ?: DifficultyLevel.BEGINNER
