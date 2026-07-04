package com.englishfriendai.app.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ConversationDto(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("created_at") val createdAt: Long,
    @SerializedName("updated_at") val updatedAt: Long,
    @SerializedName("messages") val messages: List<MessageDto> = emptyList(),
    @SerializedName("score") val score: ProgressScoreDto?
)

data class ProgressScoreDto(
    @SerializedName("grammar") val grammar: Float,
    @SerializedName("vocabulary") val vocabulary: Float,
    @SerializedName("fluency") val fluency: Float,
    @SerializedName("confidence") val confidence: Float,
    @SerializedName("pronunciation") val pronunciation: Float,
    @SerializedName("overall") val overall: Float
)

data class StartConversationRequest(
    @SerializedName("mode") val mode: String
)

// Backend returns { success, data: { conversation: {...} } } — see conversationController.js.
// Only the id is modeled here; the fuller ConversationDto above doesn't yet match this
// endpoint's raw Mongoose document shape (no "title"/"messages" fields exist server-side).
data class StartConversationEnvelope(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: StartConversationData
)

data class StartConversationData(
    @SerializedName("conversation") val conversation: ConversationRefDto
)

data class ConversationRefDto(
    @SerializedName("_id") val id: String
)
