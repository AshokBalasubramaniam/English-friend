package com.englishfriendai.app.data.mapper

import com.englishfriendai.app.data.remote.dto.UserDto
import com.englishfriendai.app.domain.model.User

fun UserDto.toDomain(): User = User(
    id = id,
    name = name,
    email = email,
    avatarUrl = avatarUrl,
    streakDays = streakDays
)
