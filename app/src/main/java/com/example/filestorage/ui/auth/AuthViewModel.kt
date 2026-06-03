// name=app/src/main/java/com/example/filestorage/ui/auth/AuthViewModel.kt
package com.example.filestorage.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.filestorage.data.model.User
import com.example.filestorage.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// Simple UI state holder
data class AuthUiState(
    val isLoading: Boolean = false,
    val user: User? = null,
    val error: String? = null
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepo: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun register(email: String, password: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = authRepo.registerWithEmail(email, password)
            result.fold(
                onSuccess = { user ->
                    // update user metadata from Firestore
                    authRepo.fetchAndCacheUser(user.uid)
                    _uiState.update { it.copy(isLoading = false, user = null, error = null) }
                },
                onFailure = { t ->
                    _uiState.update { it.copy(isLoading = false, error = t.message ?: "Registration failed") }
                }
            )
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = authRepo.loginWithEmail(email, password)
            result.fold(
                onSuccess = { firebaseUser ->
                    // fetch metadata
                    val res = authRepo.fetchAndCacheUser(firebaseUser.uid)
                    res.fold(
                        onSuccess = { user -> _uiState.update { it.copy(isLoading = false, user = user, error = null) } },
                        onFailure = { t -> _uiState.update { it.copy(isLoading = false, error = t.message ?: "Failed fetching user") } }
                    )
                },
                onFailure = { t ->
                    _uiState.update { it.copy(isLoading = false, error = t.message ?: "Login failed") }
                }
            )
        }
    }

    fun loginWithGoogle(idToken: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = authRepo.loginWithGoogle(idToken)
            result.fold(
                onSuccess = { firebaseUser ->
                    val res = authRepo.fetchAndCacheUser(firebaseUser.uid)
                    res.fold(
                        onSuccess = { user -> _uiState.update { it.copy(isLoading = false, user = user, error = null) } },
                        onFailure = { t -> _uiState.update { it.copy(isLoading = false, error = t.message ?: "Failed fetching user") } }
                    )
                },
                onFailure = { t ->
                    _uiState.update { it.copy(isLoading = false, error = t.message ?: "Google sign-in failed") }
                }
            )
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepo.logout()
            _uiState.update { AuthUiState() }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
