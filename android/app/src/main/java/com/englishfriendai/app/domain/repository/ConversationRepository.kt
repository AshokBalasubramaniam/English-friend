package com.englishfriendai.app.domain.repository

import com.englishfriendai.app.domain.model.Conversation
import com.englishfriendai.app.domain.model.ConversationMode
import com.englishfriendai.app.domain.model.Message
import kotlinx.coroutines.flow.Flow

interface ConversationRepository {

    /** Offline-first: emits cached conversations immediately, then refreshes from the backend. */
    fun getConversations(): Flow<List<Conversation>>

    fun getConversation(conversationId: String): Flow<Conversation?>

    /** Streams AI reply chunks/completions for a sent message via the realtime socket channel. */
    fun sendMessage(
        conversationId: String?,
        englishText: String,
        mode: ConversationMode
    ): Flow<Message>

    suspend fun deleteConversation(conversationId: String): Result<Unit>

    suspend fun uploadAudio(filePath: String): Result<String>

    /** Placeholder for a future "export as PDF/TXT" feature. */
    suspend fun exportTranscript(conversationId: String): Result<Nothing>
}
