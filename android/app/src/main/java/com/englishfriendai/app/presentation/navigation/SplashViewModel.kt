package com.englishfriendai.app.presentation.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.englishfriendai.app.core.security.EncryptedPrefsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class SplashDestination {
    data object Undetermined : SplashDestination()
    data object AuthenticatedHome : SplashDestination()
    data object RequiresLogin : SplashDestination()
}

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val encryptedPrefsManager: EncryptedPrefsManager
) : ViewModel() {

    private val _destination = MutableStateFlow<SplashDestination>(SplashDestination.Undetermined)
    val destination: StateFlow<SplashDestination> = _destination.asStateFlow()

    init {
        viewModelScope.launch {
            // The refresh token (not the access token, which expires in 15m) is the real
            // signal for "is there a session worth trying": as long as it's present,
            // TokenAuthenticator can silently mint a fresh access token on the first API
            // call. Checking EncryptedPrefsManager directly (rather than a separately-tracked
            // "logged in" flag) avoids the two ever drifting out of sync.
            val hasSession = !encryptedPrefsManager.getRefreshToken().isNullOrBlank()
            _destination.value = if (hasSession) {
                SplashDestination.AuthenticatedHome
            } else {
                SplashDestination.RequiresLogin
            }
        }
    }
}
