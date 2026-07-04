package com.englishfriendai.app.domain.model

enum class DifficultyLevel { BEGINNER, INTERMEDIATE, ADVANCED }

data class Correction(
    val original: String,
    val corrected: String,
    val reason: String,
    val example: String,
    val difficultyLevel: DifficultyLevel
)
