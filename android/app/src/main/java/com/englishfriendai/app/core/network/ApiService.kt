package com.englishfriendai.app.core.network

import com.englishfriendai.app.data.remote.dto.AuthEnvelope
import com.englishfriendai.app.data.remote.dto.ConversationDto
import com.englishfriendai.app.data.remote.dto.GoogleLoginRequest
import com.englishfriendai.app.data.remote.dto.LoginRequest
import com.englishfriendai.app.data.remote.dto.MessageDto
import com.englishfriendai.app.data.remote.dto.ProgressDto
import com.englishfriendai.app.data.remote.dto.RefreshEnvelope
import com.englishfriendai.app.data.remote.dto.RefreshTokenRequest
import com.englishfriendai.app.data.remote.dto.RegisterRequest
import com.englishfriendai.app.data.remote.dto.SendMessageRequest
import com.englishfriendai.app.data.remote.dto.SettingsEnvelope
import com.englishfriendai.app.data.remote.dto.StartConversationEnvelope
import com.englishfriendai.app.data.remote.dto.StartConversationRequest
import com.englishfriendai.app.data.remote.dto.UpdateSettingsRequest
import com.englishfriendai.app.data.remote.dto.UploadAudioResponse
import com.englishfriendai.app.data.remote.dto.VocabularyDto
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

/**
 * Retrofit contract for the English Friend AI backend. All calls are `suspend` so repositories
 * can invoke them from coroutines; streaming chat itself goes over [SocketManager] instead.
 */
interface ApiService {

    @POST("api/v1/auth/login")
    suspend fun login(@Body request: LoginRequest): AuthEnvelope

    @POST("api/v1/auth/register")
    suspend fun register(@Body request: RegisterRequest): AuthEnvelope

    @POST("api/v1/auth/google")
    suspend fun googleLogin(@Body request: GoogleLoginRequest): AuthEnvelope

    @POST("api/v1/auth/refresh")
    suspend fun refreshToken(@Body request: RefreshTokenRequest): RefreshEnvelope

    @POST("api/v1/conversations/{conversationId}/messages")
    suspend fun sendMessage(
        @Path("conversationId") conversationId: String,
        @Body request: SendMessageRequest
    ): MessageDto

    @POST("api/v1/conversations")
    suspend fun startConversation(@Body request: StartConversationRequest): StartConversationEnvelope

    @GET("api/v1/conversations")
    suspend fun getConversations(): List<ConversationDto>

    @GET("api/v1/conversations/{id}")
    suspend fun getConversation(@Path("id") conversationId: String): ConversationDto

    @DELETE("api/v1/conversations/{id}")
    suspend fun deleteConversation(@Path("id") conversationId: String)

    @GET("api/v1/vocabulary")
    suspend fun getVocabulary(): List<VocabularyDto>

    @GET("api/v1/progress")
    suspend fun getProgress(): ProgressDto

    @PATCH("api/v1/settings")
    suspend fun updateSettings(@Body request: UpdateSettingsRequest): SettingsEnvelope

    // NOTE: no backend route exists for this yet (see routes/messageRoutes.js) — this will
    // 404 until an audio-upload endpoint is added server-side.
    @Multipart
    @POST("api/v1/audio/upload")
    suspend fun uploadAudio(@Part audio: MultipartBody.Part): UploadAudioResponse
}
