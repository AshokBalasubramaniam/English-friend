package com.englishfriendai.app.domain.model

data class VocabularyItem(
    val id: String,
    val word: String,
    val meaning: String,
    val tamilMeaning: String,
    val pronunciation: String,
    val usageExample: String,
    val learnedAt: Long
)
