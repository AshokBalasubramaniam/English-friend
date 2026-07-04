package com.englishfriendai.app.domain.model

data class User(
    val id: String,
    val name: String,
    val email: String,
    val avatarUrl: String?,
    val streakDays: Int
)
