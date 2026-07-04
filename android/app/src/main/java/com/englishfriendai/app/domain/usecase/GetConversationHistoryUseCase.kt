package com.englishfriendai.app.domain.usecase

import com.englishfriendai.app.domain.model.Conversation
import com.englishfriendai.app.domain.repository.ConversationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/** Fetches the full message history for a single conversation, e.g. when re-opening it from History. */
class GetConversationHistoryUseCase @Inject constructor(
    private val conversationRepository: ConversationRepository
) {
    operator fun invoke(conversationId: String): Flow<Conversation?> =
        conversationRepository.getConversation(conversationId)
}
