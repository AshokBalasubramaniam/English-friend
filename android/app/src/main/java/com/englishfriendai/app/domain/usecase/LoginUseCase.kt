package com.englishfriendai.app.domain.usecase

import com.englishfriendai.app.domain.model.User
import com.englishfriendai.app.domain.repository.AuthRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): Result<User> {
        if (email.isBlank() || password.isBlank()) {
            return Result.failure(IllegalArgumentException("Email and password are required"))
        }
        return authRepository.login(email, password)
    }
}
