package com.englishfriendai.app.data.mapper

import com.englishfriendai.app.data.local.db.ConversationEntity
import com.englishfriendai.app.data.remote.dto.ConversationDto
import com.englishfriendai.app.data.remote.dto.ProgressScoreDto
import com.englishfriendai.app.domain.model.Conversation
import com.englishfriendai.app.domain.model.ConversationMode
import com.englishfriendai.app.domain.model.ConversationScore
import com.englishfriendai.app.domain.model.Message

// Matches the `mode` enum values in models/Conversation.js.
fun ConversationMode.toApiValue(): String = when (this) {
    ConversationMode.ENGLISH -> "english"
    ConversationMode.TAMIL_ENGLISH -> "tamil-english"
    ConversationMode.TAMIL -> "tamil"
}

fun ProgressScoreDto.toDomain(): ConversationScore = ConversationScore(
    grammar = grammar,
    vocabulary = vocabulary,
    fluency = fluency,
    confidence = confidence,
    pronunciation = pronunciation,
    overall = overall
)

fun ConversationDto.toEntity(): ConversationEntity = ConversationEntity(
    id = id,
    title = title,
    createdAt = createdAt,
    updatedAt = updatedAt,
    grammar = score?.grammar,
    vocabulary = score?.vocabulary,
    fluency = score?.fluency,
    confidence = score?.confidence,
    pronunciation = score?.pronunciation,
    overall = score?.overall
)

fun ConversationEntity.toDomain(messages: List<Message>): Conversation = Conversation(
    id = id,
    title = title,
    createdAt = createdAt,
    updatedAt = updatedAt,
    messages = messages,
    score = if (overall != null) {
        ConversationScore(
            grammar = grammar ?: 0f,
            vocabulary = vocabulary ?: 0f,
            fluency = fluency ?: 0f,
            confidence = confidence ?: 0f,
            pronunciation = pronunciation ?: 0f,
            overall = overall
        )
    } else null
)
