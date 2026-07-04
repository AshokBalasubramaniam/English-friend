package com.englishfriendai.app.domain.usecase

import com.englishfriendai.app.domain.model.User
import com.englishfriendai.app.domain.repository.AuthRepository
import javax.inject.Inject

class GoogleLoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(idToken: String): Result<User> {
        if (idToken.isBlank()) {
            return Result.failure(IllegalArgumentException("Missing Google ID token"))
        }
        return authRepository.googleLogin(idToken)
    }
}
