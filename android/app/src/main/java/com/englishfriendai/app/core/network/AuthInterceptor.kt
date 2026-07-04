package com.englishfriendai.app.core.network

import com.englishfriendai.app.core.security.EncryptedPrefsManager
import com.englishfriendai.app.core.util.Constants
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

/**
 * Attaches the current session's bearer token to every outgoing request. Reads synchronously
 * from [EncryptedPrefsManager] since OkHttp interceptors run on a network dispatcher thread,
 * not a coroutine — DataStore's suspend/Flow API would require blocking bridge code here anyway.
 */
class AuthInterceptor @Inject constructor(
    private val encryptedPrefsManager: EncryptedPrefsManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val accessToken = encryptedPrefsManager.getAccessToken()

        val authorizedRequest = if (!accessToken.isNullOrBlank()) {
            originalRequest.newBuilder()
                .addHeader(Constants.HEADER_AUTHORIZATION, "${Constants.BEARER_PREFIX}$accessToken")
                .build()
        } else {
            originalRequest
        }

        // TODO: on a 401 response here, trigger RefreshTokenRequest via ApiService.refreshToken()
        // and retry once with the new access token. Left as a follow-up for the real
        // authenticator/interceptor chain once the backend's refresh contract is finalized.
        return chain.proceed(authorizedRequest)
    }
}
