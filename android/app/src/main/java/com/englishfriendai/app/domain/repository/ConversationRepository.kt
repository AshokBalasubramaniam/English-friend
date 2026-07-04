package com.englishfriendai.app.domain.repository

import com.englishfriendai.app.domain.model.Conversation
import com.englishfriendai.app.domain.model.ConversationMode
import com.englishfriendai.app.domain.model.Message
import kotlinx.coroutines.flow.Flow

interface ConversationRepository {

    /** Offline-first: emits cached conversations immediately, then refreshes from the backend. */
    fun getConversations(): Flow<List<Conversation>>

    fun getConversation(conversationId: String): Flow<Conversation?>

    /**
     * Eagerly creates a new conversation (e.g. when the Chat screen opens with no active
     * conversation yet) and returns the AI's personalized opening greeting, if the backend
     * generated one — so the chat never starts on a blank screen waiting for the user to
     * speak first. Returns the new conversation id alongside the greeting (which may be
     * null if greeting generation failed server-side).
     */
    suspend fun startNewConversation(mode: ConversationMode): Result<Pair<String, Message?>>

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
