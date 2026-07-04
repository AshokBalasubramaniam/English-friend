package com.englishfriendai.app.data.remote.dto

import com.google.gson.annotations.SerializedName

data class UploadAudioResponse(
    @SerializedName("audio_url") val audioUrl: String,
    @SerializedName("transcript") val transcript: String?
)
