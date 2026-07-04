package com.englishfriendai.app.data.repository

import com.englishfriendai.app.core.network.ApiService
import com.englishfriendai.app.core.security.EncryptedPrefsManager
import com.englishfriendai.app.data.local.datastore.UserPreferencesDataStore
import com.englishfriendai.app.data.mapper.toDomain
import com.englishfriendai.app.data.remote.dto.GoogleLoginRequest
import com.englishfriendai.app.data.remote.dto.LoginRequest
import com.englishfriendai.app.data.remote.dto.LoginResponse
import com.englishfriendai.app.data.remote.dto.RefreshTokenRequest
import com.englishfriendai.app.data.remote.dto.RegisterRequest
import com.englishfriendai.app.di.IoDispatcher
import com.englishfriendai.app.domain.model.User
import com.englishfriendai.app.domain.repository.AuthRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val encryptedPrefsManager: EncryptedPrefsManager,
    private val userPreferencesDataStore: UserPreferencesDataStore,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : AuthRepository {

    // In-memory cache of the current session's user; the scaffold keeps the source of truth
    // for "is logged in" in EncryptedPrefsManager (access token) plus this in-memory profile.
    // TODO: persist the user profile itself (e.g. a small UserEntity) so observeCurrentUser()
    // survives process death without needing a network round-trip.
    private val currentUserState = MutableStateFlow<User?>(null)

    override fun observeCurrentUser(): Flow<User?> = currentUserState.asStateFlow()

    override suspend fun login(email: String, password: String): Result<User> = withContext(ioDispatcher) {
        runCatching {
            val response = apiService.login(LoginRequest(email, password))
            persistSession(response)
            response.user.toDomain()
        }
    }

    override suspend fun register(name: String, email: String, password: String): Result<User> =
        withContext(ioDispatcher) {
            runCatching {
                val response = apiService.register(RegisterRequest(name, email, password))
                persistSession(response)
                response.user.toDomain()
            }
        }

    override suspend fun googleLogin(idToken: String): Result<User> = withContext(ioDispatcher) {
        runCatching {
            val response = apiService.googleLogin(GoogleLoginRequest(idToken))
            persistSession(response)
            response.user.toDomain()
        }
    }

    override suspend fun refreshSession(): Result<Unit> = withContext(ioDispatcher) {
        runCatching {
            val refreshToken = encryptedPrefsManager.getRefreshToken()
                ?: throw IllegalStateException("No refresh token available")
            val response = apiService.refreshToken(RefreshTokenRequest(refreshToken))
            encryptedPrefsManager.saveAccessToken(response.accessToken)
            encryptedPrefsManager.saveRefreshToken(response.refreshToken)
            userPreferencesDataStore.saveSessionToken(response.accessToken)
        }
    }

    override suspend fun logout() {
        encryptedPrefsManager.clear()
        userPreferencesDataStore.saveSessionToken(null)
        currentUserState.value = null
    }

    private suspend fun persistSession(response: LoginResponse) {
        encryptedPrefsManager.saveAccessToken(response.accessToken)
        encryptedPrefsManager.saveRefreshToken(response.refreshToken)
        userPreferencesDataStore.saveSessionToken(response.accessToken)
        userPreferencesDataStore.saveStreakDays(response.user.streakDays)
        currentUserState.value = response.user.toDomain()
    }
}
