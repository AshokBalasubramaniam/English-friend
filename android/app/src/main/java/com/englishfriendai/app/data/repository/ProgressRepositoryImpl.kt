package com.englishfriendai.app.data.repository

import com.englishfriendai.app.core.network.ApiService
import com.englishfriendai.app.data.local.datastore.UserPreferencesDataStore
import com.englishfriendai.app.data.mapper.toDomain
import com.englishfriendai.app.di.IoDispatcher
import com.englishfriendai.app.domain.model.Progress
import com.englishfriendai.app.domain.repository.ProgressRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject
import javax.inject.Singleton

/**
 * TODO: back this with a dedicated Room entity (e.g. ProgressEntity) for true offline-first
 * caching. For this scaffold, the last-known streak from [UserPreferencesDataStore] is used as
 * an immediate placeholder value while the real snapshot loads from the backend.
 */
@Singleton
class ProgressRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val userPreferencesDataStore: UserPreferencesDataStore,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ProgressRepository {

    override fun getProgress(): Flow<Progress> = flow {
        val cachedStreak = userPreferencesDataStore.userPreferencesFlow.first().streakDays
        emit(
            Progress(
                streakDays = cachedStreak,
                totalPracticeMinutes = 0,
                conversationsCompleted = 0,
                weeklyScores = emptyList(),
                latestScore = null
            )
        )

        try {
            val remote = apiService.getProgress()
            userPreferencesDataStore.saveStreakDays(remote.streakDays)
            emit(remote.toDomain())
        } catch (_: Exception) {
            // Offline-first: keep showing the cached placeholder emitted above.
        }
    }.flowOn(ioDispatcher)
}
