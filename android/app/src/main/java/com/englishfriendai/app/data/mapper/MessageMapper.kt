package com.englishfriendai.app.data.mapper

import com.englishfriendai.app.data.local.db.CorrectionEntity
import com.englishfriendai.app.data.local.db.MessageEntity
import com.englishfriendai.app.data.remote.dto.MessageDto
import com.englishfriendai.app.domain.model.Message
import com.englishfriendai.app.domain.model.Sender
import java.time.Instant

fun MessageDto.toDomain(): Message = Message(
    id = id,
    conversationId = conversationId,
    sender = sender.toSender(),
    englishText = englishText,
    tamilTranslation = tamilTranslation,
    // createdAt is an ISO-8601 string (raw Mongoose Date JSON), not epoch millis.
    timestamp = runCatching { Instant.parse(createdAt).toEpochMilli() }
        .getOrDefault(System.currentTimeMillis()),
    correction = correction?.toDomain(),
    audioUrl = audioUrl
)

fun MessageEntity.toDomain(correctionEntity: CorrectionEntity?): Message = Message(
    id = id,
    conversationId = conversationId,
    sender = sender.toSender(),
    englishText = englishText,
    tamilTranslation = tamilTranslation,
    timestamp = timestamp,
    correction = correctionEntity?.toDomain(),
    audioUrl = audioUrl
)

fun Message.toEntity(): MessageEntity = MessageEntity(
    id = id,
    conversationId = conversationId,
    sender = sender.name,
    englishText = englishText,
    tamilTranslation = tamilTranslation,
    timestamp = timestamp,
    audioUrl = audioUrl
)

private fun String.toSender(): Sender =
    Sender.entries.find { it.name.equals(this, ignoreCase = true) } ?: Sender.USER
