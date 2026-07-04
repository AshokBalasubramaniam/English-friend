package com.englishfriendai.app.domain.usecase

import com.englishfriendai.app.domain.model.Progress
import com.englishfriendai.app.domain.repository.ProgressRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetProgressUseCase @Inject constructor(
    private val progressRepository: ProgressRepository
) {
    operator fun invoke(): Flow<Progress> = progressRepository.getProgress()
}
