package com.englishfriendai.app.data.remote.dto

import com.google.gson.annotations.SerializedName

data class CorrectionDto(
    @SerializedName("original") val original: String,
    @SerializedName("corrected") val corrected: String,
    @SerializedName("reason") val reason: String,
    @SerializedName("example") val example: String,
    @SerializedName("difficulty_level") val difficultyLevel: String
)
