package com.englishfriendai.app.core.network

import com.englishfriendai.app.core.security.EncryptedPrefsManager
import com.englishfriendai.app.core.util.Constants
import com.englishfriendai.app.data.remote.dto.RefreshTokenRequest
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

/**
 * Refreshes the access token and retries once when a request comes back 401.
 *
 * [ApiService] is injected via [Provider] rather than directly: this [TokenAuthenticator] is
 * itself a dependency of the [okhttp3.OkHttpClient] that [ApiService]'s Retrofit instance is
 * built from (see NetworkModule), so a direct `ApiService` parameter would be a circular
 * dependency. `Provider` defers resolution until [authenticate] actually runs.
 */
@Singleton
class TokenAuthenticator @Inject constructor(
    private val encryptedPrefsManager: EncryptedPrefsManager,
    private val apiServiceProvider: Provider<ApiService>
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        // Already retried once for this request chain - give up rather than loop forever.
        if (responseCount(response) >= 2) return null

        val refreshToken = encryptedPrefsManager.getRefreshToken() ?: return null

        val newAccessToken = runBlocking {
            runCatching {
                val envelope = apiServiceProvider.get().refreshToken(RefreshTokenRequest(refreshToken))
                encryptedPrefsManager.saveAccessToken(envelope.data.accessToken)
                encryptedPrefsManager.saveRefreshToken(envelope.data.refreshToken)
                envelope.data.accessToken
            }.getOrNull()
        } ?: run {
            // Refresh token itself is invalid/expired - clear the session so the app can
            // route back to login instead of retrying with stale credentials forever.
            encryptedPrefsManager.clear()
            null
        } ?: return null

        return response.request.newBuilder()
            .header(Constants.HEADER_AUTHORIZATION, "${Constants.BEARER_PREFIX}$newAccessToken")
            .build()
    }

    private fun responseCount(response: Response): Int {
        var result = 1
        var prior = response.priorResponse
        while (prior != null) {
            result++
            prior = prior.priorResponse
        }
        return result
    }
}
