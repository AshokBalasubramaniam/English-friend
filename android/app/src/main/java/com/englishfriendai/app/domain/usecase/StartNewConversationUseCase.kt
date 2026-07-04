package com.englishfriendai.app.domain.usecase

import com.englishfriendai.app.domain.model.ConversationMode
import com.englishfriendai.app.domain.model.Message
import com.englishfriendai.app.domain.repository.ConversationRepository
import javax.inject.Inject

class StartNewConversationUseCase @Inject constructor(
    private val conversationRepository: ConversationRepository
) {
    suspend operator fun invoke(mode: ConversationMode): Result<Pair<String, Message?>> =
        conversationRepository.startNewConversation(mode)
}
