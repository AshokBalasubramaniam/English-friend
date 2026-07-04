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
