package com.englishfriendai.app.domain.usecase

import com.englishfriendai.app.domain.model.ConversationMode
import com.englishfriendai.app.domain.model.Message
import com.englishfriendai.app.domain.repository.ConversationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SendMessageUseCase @Inject constructor(
    private val conversationRepository: ConversationRepository
) {
    operator fun invoke(
        conversationId: String?,
        englishText: String,
        mode: ConversationMode
    ): Flow<Message> = conversationRepository.sendMessage(conversationId, englishText, mode)
}
