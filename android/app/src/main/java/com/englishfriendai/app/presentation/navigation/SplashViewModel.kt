package com.englishfriendai.app.presentation.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.englishfriendai.app.data.local.datastore.UserPreferencesDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class SplashDestination {
    data object Undetermined : SplashDestination()
    data object AuthenticatedHome : SplashDestination()
    data object RequiresLogin : SplashDestination()
}

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val userPreferencesDataStore: UserPreferencesDataStore
) : ViewModel() {

    private val _destination = MutableStateFlow<SplashDestination>(SplashDestination.Undetermined)
    val destination: StateFlow<SplashDestination> = _destination.asStateFlow()

    init {
        viewModelScope.launch {
            val hasSession = !userPreferencesDataStore.userPreferencesFlow.first().sessionToken.isNullOrBlank()
            _destination.value = if (hasSession) {
                SplashDestination.AuthenticatedHome
            } else {
                SplashDestination.RequiresLogin
            }
        }
    }
}
