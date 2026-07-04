package com.englishfriendai.app.domain.model

data class ConversationScore(
    val grammar: Float,
    val vocabulary: Float,
    val fluency: Float,
    val confidence: Float,
    val pronunciation: Float,
    val overall: Float
)
