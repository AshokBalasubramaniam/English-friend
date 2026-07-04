package com.englishfriendai.app.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey val id: String,
    val title: String,
    val createdAt: Long,
    val updatedAt: Long,
    val grammar: Float? = null,
    val vocabulary: Float? = null,
    val fluency: Float? = null,
    val confidence: Float? = null,
    val pronunciation: Float? = null,
    val overall: Float? = null
)
