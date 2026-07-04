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

        // 401 handling (refresh + retry) lives in TokenAuthenticator, not here — OkHttp only
        // invokes an Authenticator, not an Interceptor, in response to a 401 challenge.
        return chain.proceed(authorizedRequest)
    }
}
