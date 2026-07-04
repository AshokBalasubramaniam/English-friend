package com.englishfriendai.app.domain.usecase

import com.englishfriendai.app.domain.model.VocabularyItem
import com.englishfriendai.app.domain.repository.VocabularyRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetVocabularyUseCase @Inject constructor(
    private val vocabularyRepository: VocabularyRepository
) {
    operator fun invoke(): Flow<List<VocabularyItem>> = vocabularyRepository.getVocabulary()
}
