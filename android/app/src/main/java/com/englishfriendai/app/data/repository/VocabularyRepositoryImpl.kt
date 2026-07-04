package com.englishfriendai.app.data.repository

import com.englishfriendai.app.core.network.ApiService
import com.englishfriendai.app.data.local.db.VocabularyDao
import com.englishfriendai.app.data.mapper.toDomain
import com.englishfriendai.app.data.mapper.toEntity
import com.englishfriendai.app.di.IoDispatcher
import com.englishfriendai.app.domain.model.VocabularyItem
import com.englishfriendai.app.domain.repository.VocabularyRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VocabularyRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val vocabularyDao: VocabularyDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : VocabularyRepository {

    override fun getVocabulary(): Flow<List<VocabularyItem>> = vocabularyDao.observeAll()
        .onStart {
            try {
                val remote = apiService.getVocabulary()
                vocabularyDao.upsertAll(remote.map { it.toEntity() })
            } catch (_: Exception) {
                // Offline-first: keep serving the last cached vocabulary list.
            }
        }
        .map { entities -> entities.map { it.toDomain() } }
        .flowOn(ioDispatcher)
}
