package com.jayjaycode.miniproject.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.jayjaycode.miniproject.data.BusinessRepository
import com.jayjaycode.miniproject.data.FcmTokenRepository
import com.jayjaycode.miniproject.data.RescueRepository
import com.jayjaycode.miniproject.data.auth.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface AuthUiState {
    data object Loading : AuthUiState
    data object Unauthenticated : AuthUiState
    data class Authenticated(val user: FirebaseUser) : AuthUiState
}

class AuthViewModel(
    private val repository: AuthRepository = AuthRepository(),
    private val rescueRepository: RescueRepository = RescueRepository.instance,
    private val businessRepository: BusinessRepository = BusinessRepository.instance,
) : ViewModel() {

    init {
        businessRepository.startObservingProfile()
    }

    val authState: StateFlow<AuthUiState> = repository.authState
        .map { user ->
            when (user) {
                null -> AuthUiState.Unauthenticated
                else -> AuthUiState.Authenticated(user)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AuthUiState.Loading)

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _isResetLoading = MutableStateFlow(false)
    val isResetLoading: StateFlow<Boolean> = _isResetLoading.asStateFlow()

    private val _resetSuccessMessage = MutableStateFlow<String?>(null)
    val resetSuccessMessage: StateFlow<String?> = _resetSuccessMessage.asStateFlow()

    fun clearError() {
        _errorMessage.value = null
    }

    fun clearResetState() {
        _errorMessage.value = null
        _resetSuccessMessage.value = null
    }

    fun sendPasswordReset(email: String) {
        viewModelScope.launch {
            _isResetLoading.value = true
            _errorMessage.value = null
            _resetSuccessMessage.value = null
            try {
                repository.sendPasswordResetEmail(email).getOrThrow()
                _resetSuccessMessage.value =
                    "Password reset link sent to $email. Check your inbox and spam folder."
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage ?: "Could not send reset email"
            } finally {
                _isResetLoading.value = false
            }
        }
    }

    fun signIn(email: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val user = repository.signIn(email, password).getOrThrow()
                rescueRepository.ensureUserProfile(
                    displayName = user.displayName.orEmpty(),
                    email = user.email.orEmpty(),
                )
                rescueRepository.refreshRequestHistory()
                onSuccess()
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage ?: "Sign in failed"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun signUp(name: String, email: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val user = repository.signUp(name, email, password).getOrThrow()
                rescueRepository.ensureUserProfile(
                    displayName = name,
                    email = user.email.orEmpty(),
                )
                onSuccess()
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage ?: "Sign up failed"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            runCatching { FcmTokenRepository.instance.removeCurrentToken() }
            rescueRepository.clearActiveSession()
            businessRepository.clearSession()
            repository.signOut()
        }
    }
}
