package com.englishfriendai.app.domain.usecase

import com.englishfriendai.app.domain.model.User
import com.englishfriendai.app.domain.repository.AuthRepository
import javax.inject.Inject

class RegisterUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(name: String, email: String, password: String): Result<User> {
        if (name.isBlank() || email.isBlank() || password.length < 8) {
            return Result.failure(IllegalArgumentException("Invalid registration details"))
        }
        return authRepository.register(name, email, password)
    }
}
