package com.englishfriendai.app.domain.model

data class Conversation(
    val id: String,
    val title: String,
    val createdAt: Long,
    val updatedAt: Long,
    val messages: List<Message> = emptyList(),
    val score: ConversationScore? = null
)
