package com.englishfriendai.app.domain.repository

import com.englishfriendai.app.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {

    /** Reactive stream of the currently logged-in user, or null when signed out. */
    fun observeCurrentUser(): Flow<User?>

    suspend fun login(email: String, password: String): Result<User>

    suspend fun register(name: String, email: String, password: String): Result<User>

    suspend fun googleLogin(idToken: String): Result<User>

    suspend fun refreshSession(): Result<Unit>

    suspend fun logout()
}
