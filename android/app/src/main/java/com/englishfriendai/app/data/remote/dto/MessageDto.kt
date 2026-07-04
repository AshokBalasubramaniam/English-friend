package com.englishfriendai.app.data.remote.dto

import com.google.gson.annotations.SerializedName

data class SendMessageRequest(
    @SerializedName("conversation_id") val conversationId: String?,
    @SerializedName("english_text") val englishText: String,
    @SerializedName("mode") val mode: String
)

data class MessageDto(
    @SerializedName("id") val id: String,
    @SerializedName("conversation_id") val conversationId: String,
    @SerializedName("sender") val sender: String,
    @SerializedName("english_text") val englishText: String,
    @SerializedName("tamil_translation") val tamilTranslation: String?,
    @SerializedName("timestamp") val timestamp: Long,
    @SerializedName("correction") val correction: CorrectionDto?,
    @SerializedName("audio_url") val audioUrl: String?
)
