package com.englishfriendai.app.data.remote.dto

import com.google.gson.annotations.SerializedName

data class SendMessageRequest(
    @SerializedName("text") val englishText: String
)

// Matches the raw Mongoose Message document (see models/Message.js) — the backend has no
// response transform, so field names are its actual schema names, not a custom API shape.
// Note: the backend never embeds a "correction" here (Correction is a separate collection,
// not joined into message responses yet), so this will always deserialize to null for now.
data class MessageDto(
    @SerializedName("_id") val id: String,
    @SerializedName("conversationId") val conversationId: String,
    @SerializedName("sender") val sender: String,
    @SerializedName("englishText") val englishText: String,
    @SerializedName("tamilTranslation") val tamilTranslation: String?,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("correction") val correction: CorrectionDto?,
    @SerializedName("audioUrl") val audioUrl: String?
)
