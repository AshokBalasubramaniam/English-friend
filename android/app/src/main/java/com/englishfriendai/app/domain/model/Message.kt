package com.englishfriendai.app.domain.model

enum class Sender { USER, AI }

/** Language mode the user has selected for the current conversation turn. */
enum class ConversationMode { ENGLISH, TAMIL_ENGLISH, TAMIL }

data class Message(
    val id: String,
    val conversationId: String,
    val sender: Sender,
    val englishText: String,
    val tamilTranslation: String?,
    val timestamp: Long,
    val correction: Correction?,
    val audioUrl: String? = null
)
