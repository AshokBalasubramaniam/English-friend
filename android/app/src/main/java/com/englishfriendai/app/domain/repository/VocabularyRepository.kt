package com.englishfriendai.app.domain.repository

import com.englishfriendai.app.domain.model.VocabularyItem
import kotlinx.coroutines.flow.Flow

interface VocabularyRepository {

    /** Offline-first: local Room cache first, then refreshed from the backend. */
    fun getVocabulary(): Flow<List<VocabularyItem>>
}
