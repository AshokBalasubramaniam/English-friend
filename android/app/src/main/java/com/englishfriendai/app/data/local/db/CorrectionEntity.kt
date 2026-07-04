package com.englishfriendai.app.data.local.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "corrections",
    foreignKeys = [
        ForeignKey(
            entity = MessageEntity::class,
            parentColumns = ["id"],
            childColumns = ["messageId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("messageId", unique = true)]
)
data class CorrectionEntity(
    @PrimaryKey val messageId: String,
    val original: String,
    val corrected: String,
    val reason: String,
    val example: String,
    val difficultyLevel: String
)
