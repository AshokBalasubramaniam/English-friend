package com.englishfriendai.app.core.network

import com.englishfriendai.app.data.remote.dto.ConversationDto
import com.englishfriendai.app.data.remote.dto.GoogleLoginRequest
import com.englishfriendai.app.data.remote.dto.LoginRequest
import com.englishfriendai.app.data.remote.dto.LoginResponse
import com.englishfriendai.app.data.remote.dto.MessageDto
import com.englishfriendai.app.data.remote.dto.ProgressDto
import com.englishfriendai.app.data.remote.dto.RefreshTokenRequest
import com.englishfriendai.app.data.remote.dto.RefreshTokenResponse
import com.englishfriendai.app.data.remote.dto.RegisterRequest
import com.englishfriendai.app.data.remote.dto.SendMessageRequest
import com.englishfriendai.app.data.remote.dto.UploadAudioResponse
import com.englishfriendai.app.data.remote.dto.VocabularyDto
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

/**
 * Retrofit contract for the English Friend AI backend. All calls are `suspend` so repositories
 * can invoke them from coroutines; streaming chat itself goes over [SocketManager] instead.
 */
interface ApiService {

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): LoginResponse

    @POST("auth/google")
    suspend fun googleLogin(@Body request: GoogleLoginRequest): LoginResponse

    @POST("auth/refresh")
    suspend fun refreshToken(@Body request: RefreshTokenRequest): RefreshTokenResponse

    @POST("chat/message")
    suspend fun sendMessage(@Body request: SendMessageRequest): MessageDto

    @GET("conversations")
    suspend fun getConversations(): List<ConversationDto>

    @GET("conversations/{id}")
    suspend fun getConversation(@Path("id") conversationId: String): ConversationDto

    @DELETE("conversations/{id}")
    suspend fun deleteConversation(@Path("id") conversationId: String)

    @GET("vocabulary")
    suspend fun getVocabulary(): List<VocabularyDto>

    @GET("progress")
    suspend fun getProgress(): ProgressDto

    @Multipart
    @POST("chat/audio")
    suspend fun uploadAudio(@Part audio: MultipartBody.Part): UploadAudioResponse
}
