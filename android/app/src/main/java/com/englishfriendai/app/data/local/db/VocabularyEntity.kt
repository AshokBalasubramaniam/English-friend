package com.englishfriendai.app.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vocabulary")
data class VocabularyEntity(
    @PrimaryKey val id: String,
    val word: String,
    val meaning: String,
    val tamilMeaning: String,
    val pronunciation: String,
    val usageExample: String,
    val learnedAt: Long
)
