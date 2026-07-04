package com.englishfriendai.app.data.repository

import com.englishfriendai.app.core.network.ApiService
import com.englishfriendai.app.core.network.SocketManager
import com.englishfriendai.app.data.local.db.ConversationDao
import com.englishfriendai.app.data.local.db.ConversationEntity
import com.englishfriendai.app.data.local.db.CorrectionDao
import com.englishfriendai.app.data.local.db.MessageDao
import com.englishfriendai.app.data.mapper.toApiValue
import com.englishfriendai.app.data.mapper.toDomain
import com.englishfriendai.app.data.mapper.toEntity
import com.englishfriendai.app.data.remote.dto.ConversationDto
import com.englishfriendai.app.data.remote.dto.StartConversationRequest
import com.englishfriendai.app.di.IoDispatcher
import com.englishfriendai.app.domain.model.Conversation
import com.englishfriendai.app.domain.model.ConversationMode
import com.englishfriendai.app.domain.model.Message
import com.englishfriendai.app.domain.model.Sender
import com.englishfriendai.app.domain.repository.ConversationRepository
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.asRequestBody
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.transformWhile
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConversationRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val socketManager: SocketManager,
    private val conversationDao: ConversationDao,
    private val messageDao: MessageDao,
    private val correctionDao: CorrectionDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ConversationRepository {

    override fun getConversations(): Flow<List<Conversation>> = conversationDao.observeAll()
        .onStart { refreshConversationsFromRemote() }
        .map { entities -> entities.map { it.toDomain(emptyList()) } }
        .flowOn(ioDispatcher)

    override fun getConversation(conversationId: String): Flow<Conversation?> = combine(
        conversationDao.observeById(conversationId),
        messageDao.observeByConversation(conversationId)
    ) { conversationEntity, messageEntities -> conversationEntity to messageEntities }
        .onStart { refreshConversationFromRemote(conversationId) }
        .map { (conversationEntity, messageEntities) ->
            conversationEntity?.let { entity ->
                val corrections = correctionDao.getByMessageIds(messageEntities.map { it.id })
                    .associateBy { it.messageId }
                val messages = messageEntities.map { it.toDomain(corrections[it.id]) }
                entity.toDomain(messages)
            }
        }
        .flowOn(ioDispatcher)

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun sendMessage(
        conversationId: String?,
        englishText: String,
        mode: ConversationMode
    ): Flow<Message> = flow {
        // The backend requires a real, already-created conversation id before it will accept
        // any message over the socket (see chatSocket.js) — create one now if this is a
        // brand-new chat, and cache it locally so the FK on messages.conversationId is satisfied.
        val realConversationId = conversationId ?: startConversationRemoteAndCache(mode)

        val userMessage = Message(
            id = UUID.randomUUID().toString(),
            conversationId = realConversationId,
            sender = Sender.USER,
            englishText = englishText,
            tamilTranslation = null,
            timestamp = System.currentTimeMillis(),
            correction = null
        )
        emit(userMessage)
        messageDao.upsert(userMessage.toEntity())

        socketManager.connect()
        socketManager.sendMessage(realConversationId, englishText)

        emitAll(
            socketManager.observeMessageEvents().transformWhile { event ->
                when (event) {
                    is SocketManager.SocketEvent.MessageChunk -> {
                        emit(
                            Message(
                                id = STREAMING_MESSAGE_ID,
                                conversationId = realConversationId,
                                sender = Sender.AI,
                                englishText = event.partialText,
                                tamilTranslation = null,
                                timestamp = System.currentTimeMillis(),
                                correction = null
                            )
                        )
                        true // keep collecting further chunks
                    }

                    is SocketManager.SocketEvent.MessageComplete -> {
                        val aiMessage = event.message.toDomain()
                        messageDao.upsert(aiMessage.toEntity())
                        aiMessage.correction?.let { correctionDao.upsert(it.toEntity(aiMessage.id)) }
                        emit(aiMessage)
                        false // full AI turn received, stop collecting for this send
                    }

                    is SocketManager.SocketEvent.ConnectionError -> {
                        throw IllegalStateException(event.reason)
                    }
                }
            }
        )
    }.flowOn(ioDispatcher)

    private suspend fun startConversationRemoteAndCache(mode: ConversationMode): String {
        val newId = apiService.startConversation(StartConversationRequest(mode.toApiValue()))
            .data.conversation.id
        val now = System.currentTimeMillis()
        conversationDao.upsert(
            ConversationEntity(
                id = newId,
                title = "New Conversation",
                createdAt = now,
                updatedAt = now
            )
        )
        return newId
    }

    override suspend fun deleteConversation(conversationId: String): Result<Unit> = runCatching {
        apiService.deleteConversation(conversationId)
        conversationDao.deleteById(conversationId)
    }

    override suspend fun uploadAudio(filePath: String): Result<String> = runCatching {
        val file = java.io.File(filePath)
        val requestBody = file.asRequestBody("audio/m4a".toMediaType())
        val part = okhttp3.MultipartBody.Part.createFormData("audio", file.name, requestBody)
        apiService.uploadAudio(part).audioUrl
    }

    override suspend fun exportTranscript(conversationId: String): Result<Nothing> =
        Result.failure(NotImplementedError("Transcript export is not implemented yet"))

    private suspend fun refreshConversationsFromRemote() {
        try {
            val remoteConversations = apiService.getConversations()
            remoteConversations.forEach { cacheConversationDto(it) }
        } catch (_: Exception) {
            // Offline-first: swallow network errors here, the Room-backed flow above still
            // serves the last known-good cache to the UI.
        }
    }

    private suspend fun refreshConversationFromRemote(conversationId: String) {
        try {
            cacheConversationDto(apiService.getConversation(conversationId))
        } catch (_: Exception) {
            // Offline-first: ignore, rely on cache.
        }
    }

    private suspend fun cacheConversationDto(dto: ConversationDto) {
        conversationDao.upsert(dto.toEntity())
        messageDao.upsertAll(dto.messages.map { it.toDomain().toEntity() })
        dto.messages.forEach { messageDto ->
            messageDto.correction?.let { correctionDto ->
                correctionDao.upsert(correctionDto.toDomain().toEntity(messageDto.id))
            }
        }
    }

    companion object {
        private const val STREAMING_MESSAGE_ID = "streaming"
    }
}
