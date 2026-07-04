package com.englishfriendai.app.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.englishfriendai.app.core.util.isValidEmail
import com.englishfriendai.app.domain.model.User
import com.englishfriendai.app.domain.usecase.GoogleLoginUseCase
import com.englishfriendai.app.domain.usecase.LoginUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class LoginUiState {
    data object Idle : LoginUiState()
    data object Loading : LoginUiState()
    data class Success(val user: User) : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}

data class LoginFormState(
    val email: String = "",
    val password: String = "",
    val emailError: String? = null,
    val passwordError: String? = null
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val googleLoginUseCase: GoogleLoginUseCase
) : ViewModel() {

    private val _formState = MutableStateFlow(LoginFormState())
    val formState: StateFlow<LoginFormState> = _formState.asStateFlow()

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChanged(value: String) {
        _formState.value = _formState.value.copy(email = value, emailError = null)
    }

    fun onPasswordChanged(value: String) {
        _formState.value = _formState.value.copy(password = value, passwordError = null)
    }

    fun login() {
        val current = _formState.value
        val emailError = if (!current.email.isValidEmail()) "Enter a valid email" else null
        val passwordError = if (current.password.isBlank()) "Password is required" else null

        if (emailError != null || passwordError != null) {
            _formState.value = current.copy(emailError = emailError, passwordError = passwordError)
            return
        }

        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            loginUseCase(current.email, current.password)
                .onSuccess { user -> _uiState.value = LoginUiState.Success(user) }
                .onFailure { throwable ->
                    _uiState.value = LoginUiState.Error(throwable.message ?: "Login failed")
                }
        }
    }

    fun loginWithGoogle(idToken: String) {
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            googleLoginUseCase(idToken)
                .onSuccess { user -> _uiState.value = LoginUiState.Success(user) }
                .onFailure { throwable ->
                    _uiState.value = LoginUiState.Error(throwable.message ?: "Google sign-in failed")
                }
        }
    }
}
