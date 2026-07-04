package com.englishfriendai.app.data.mapper

import com.englishfriendai.app.data.local.db.VocabularyEntity
import com.englishfriendai.app.data.remote.dto.VocabularyDto
import com.englishfriendai.app.domain.model.VocabularyItem

fun VocabularyDto.toEntity(): VocabularyEntity = VocabularyEntity(
    id = id,
    word = word,
    meaning = meaning,
    tamilMeaning = tamilMeaning,
    pronunciation = pronunciation,
    usageExample = usageExample,
    learnedAt = learnedAt
)

fun VocabularyEntity.toDomain(): VocabularyItem = VocabularyItem(
    id = id,
    word = word,
    meaning = meaning,
    tamilMeaning = tamilMeaning,
    pronunciation = pronunciation,
    usageExample = usageExample,
    learnedAt = learnedAt
)
