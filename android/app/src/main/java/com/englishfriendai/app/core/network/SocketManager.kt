package com.englishfriendai.app.core.network

import android.util.Log
import com.englishfriendai.app.core.security.EncryptedPrefsManager
import com.englishfriendai.app.core.util.Constants
import com.google.gson.Gson
import com.englishfriendai.app.data.remote.dto.MessageDto
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Wraps the socket.io-client-java library to expose real-time streaming chat as a [Flow] of
 * partial/complete AI replies, so [com.englishfriendai.app.data.repository.ConversationRepositoryImpl]
 * doesn't leak socket.io callback types into the domain layer.
 */
@Singleton
class SocketManager @Inject constructor(
    private val encryptedPrefsManager: EncryptedPrefsManager,
    private val gson: Gson,
    private val baseUrl: String
) {

    private var socket: Socket? = null

    sealed class SocketEvent {
        data class MessageChunk(val partialText: String) : SocketEvent()
        data class MessageComplete(val message: MessageDto) : SocketEvent()
        data class ConnectionError(val reason: String) : SocketEvent()
    }

    private fun buildSocket(): Socket {
        val options = IO.Options.builder()
            .setTransports(arrayOf("websocket"))
            .setAuth(
                mapOf("token" to (encryptedPrefsManager.getAccessToken() ?: ""))
            )
            .build()
        return IO.socket(java.net.URI.create(baseUrl), options)
    }

    fun connect() {
        if (socket?.connected() == true) return
        socket = buildSocket().also { it.connect() }
    }

    fun disconnect() {
        socket?.disconnect()
        socket?.off()
        socket = null
    }

    /**
     * Sends a chat message over the socket for streaming AI response chunks.
     * [conversationId] must already exist (see chatSocket.js — it rejects unknown ids)
     * and mode is set once at conversation creation, not per-message, so it isn't sent here.
     */
    fun sendMessage(conversationId: String, englishText: String) {
        val payload = JSONObject().apply {
            put("conversationId", conversationId)
            put("text", englishText)
        }
        socket?.emit(Constants.SOCKET_EVENT_SEND_MESSAGE, payload)
    }

    /** Cold flow of streaming events for the currently active chat session. */
    fun observeMessageEvents(): Flow<SocketEvent> = callbackFlow {
        val currentSocket = socket ?: buildSocket().also { socket = it; it.connect() }

        val chunkListener = io.socket.emitter.Emitter.Listener { args ->
            val text = (args.firstOrNull() as? JSONObject)?.optString("text").orEmpty()
            trySend(SocketEvent.MessageChunk(text))
        }
        val completeListener = io.socket.emitter.Emitter.Listener { args ->
            // chatSocket.js emits ai_reply_done as { message: {...} } — the message is nested,
            // not the top-level payload itself.
            val json = (args.firstOrNull() as? JSONObject)?.optJSONObject("message")?.toString()
            if (json == null) {
                Log.e(TAG, "ai_reply_done payload missing 'message' field")
                return@Listener
            }
            try {
                val dto = gson.fromJson(json, MessageDto::class.java)
                trySend(SocketEvent.MessageComplete(dto))
            } catch (e: Exception) {
                Log.e(TAG, "Failed to parse ai_reply_done payload", e)
            }
        }
        val errorListener = io.socket.emitter.Emitter.Listener { args ->
            // chatSocket.js emits ai_reply_error as { message: "..." }.
            val reason = (args.firstOrNull() as? JSONObject)?.optString("message")
                ?.takeIf { it.isNotBlank() } ?: "Unknown socket error"
            trySend(SocketEvent.ConnectionError(reason))
        }

        currentSocket.on(Constants.SOCKET_EVENT_MESSAGE_CHUNK, chunkListener)
        currentSocket.on(Constants.SOCKET_EVENT_MESSAGE_COMPLETE, completeListener)
        currentSocket.on(Constants.SOCKET_EVENT_ERROR, errorListener)

        awaitClose {
            currentSocket.off(Constants.SOCKET_EVENT_MESSAGE_CHUNK, chunkListener)
            currentSocket.off(Constants.SOCKET_EVENT_MESSAGE_COMPLETE, completeListener)
            currentSocket.off(Constants.SOCKET_EVENT_ERROR, errorListener)
        }
    }

    companion object {
        private const val TAG = "SocketManager"
    }
}
