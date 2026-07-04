package com.englishfriendai.app.data.remote.dto

import com.google.gson.annotations.SerializedName

data class VocabularyDto(
    @SerializedName("id") val id: String,
    @SerializedName("word") val word: String,
    @SerializedName("meaning") val meaning: String,
    @SerializedName("tamil_meaning") val tamilMeaning: String,
    @SerializedName("pronunciation") val pronunciation: String,
    @SerializedName("usage_example") val usageExample: String,
    @SerializedName("learned_at") val learnedAt: Long
)
