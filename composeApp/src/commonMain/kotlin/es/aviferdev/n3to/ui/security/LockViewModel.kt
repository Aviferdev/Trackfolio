package es.aviferdev.n3to.ui.security

import androidx.lifecycle.ViewModel
import es.aviferdev.n3to.core.security.BiometricAuthenticator
import es.aviferdev.n3to.core.security.BiometricResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed class LockUiState {
    data object Idle : LockUiState()
    data object Authenticating : LockUiState()
    data object Unlocked : LockUiState()
    data class Error(val message: String) : LockUiState()
}

class LockViewModel(
    private val authenticator: BiometricAuthenticator
) : ViewModel() {

    private val _state = MutableStateFlow<LockUiState>(LockUiState.Idle)
    val state: StateFlow<LockUiState> = _state.asStateFlow()

    fun authenticate(title: String, subtitle: String, notAvailableMessage: String) {
        _state.value = LockUiState.Authenticating
        authenticator.authenticate(title, subtitle) { result ->
            _state.value = when (result) {
                is BiometricResult.Success -> LockUiState.Unlocked
                is BiometricResult.UserCancelled -> LockUiState.Idle
                is BiometricResult.NotAvailable -> LockUiState.Error(notAvailableMessage)
                is BiometricResult.Error -> LockUiState.Error(result.message)
            }
        }
    }
}
