package com.englishfriendai.app.domain.usecase

import com.englishfriendai.app.domain.model.Conversation
import com.englishfriendai.app.domain.repository.ConversationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetConversationsUseCase @Inject constructor(
    private val conversationRepository: ConversationRepository
) {
    operator fun invoke(): Flow<List<Conversation>> = conversationRepository.getConversations()
}
