package com.englishfriendai.app.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.englishfriendai.app.core.util.isValidEmail
import com.englishfriendai.app.core.util.isValidPassword
import com.englishfriendai.app.domain.model.User
import com.englishfriendai.app.domain.usecase.RegisterUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class RegisterUiState {
    data object Idle : RegisterUiState()
    data object Loading : RegisterUiState()
    data class Success(val user: User) : RegisterUiState()
    data class Error(val message: String) : RegisterUiState()
}

data class RegisterFormState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val nameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null
)

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val registerUseCase: RegisterUseCase
) : ViewModel() {

    private val _formState = MutableStateFlow(RegisterFormState())
    val formState: StateFlow<RegisterFormState> = _formState.asStateFlow()

    private val _uiState = MutableStateFlow<RegisterUiState>(RegisterUiState.Idle)
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun onNameChanged(value: String) {
        _formState.value = _formState.value.copy(name = value, nameError = null)
    }

    fun onEmailChanged(value: String) {
        _formState.value = _formState.value.copy(email = value, emailError = null)
    }

    fun onPasswordChanged(value: String) {
        _formState.value = _formState.value.copy(password = value, passwordError = null)
    }

    fun register() {
        val current = _formState.value
        val nameError = if (current.name.isBlank()) "Name is required" else null
        val emailError = if (!current.email.isValidEmail()) "Enter a valid email" else null
        val passwordError = if (!current.password.isValidPassword()) "Password must be 8+ characters" else null

        if (nameError != null || emailError != null || passwordError != null) {
            _formState.value = current.copy(
                nameError = nameError,
                emailError = emailError,
                passwordError = passwordError
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = RegisterUiState.Loading
            registerUseCase(current.name, current.email, current.password)
                .onSuccess { user -> _uiState.value = RegisterUiState.Success(user) }
                .onFailure { throwable ->
                    _uiState.value = RegisterUiState.Error(throwable.message ?: "Registration failed")
                }
        }
    }
}
