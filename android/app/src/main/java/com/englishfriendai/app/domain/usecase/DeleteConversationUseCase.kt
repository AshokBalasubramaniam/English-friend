package com.englishfriendai.app.domain.usecase

import com.englishfriendai.app.domain.repository.ConversationRepository
import javax.inject.Inject

class DeleteConversationUseCase @Inject constructor(
    private val conversationRepository: ConversationRepository
) {
    suspend operator fun invoke(conversationId: String): Result<Unit> =
        conversationRepository.deleteConversation(conversationId)
}
