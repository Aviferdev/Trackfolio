package es.aviferdev.trackfolio.ui.settings.backup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.aviferdev.trackfolio.core.security.BackupResult
import es.aviferdev.trackfolio.core.security.DatabaseBackupManager
import es.aviferdev.trackfolio.domain.usecase.backup.SaveLastBackupDateUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class BackupUiState {
    data object Idle : BackupUiState()
    data object Loading : BackupUiState()
    data object Success : BackupUiState()
    data class Error(val message: String) : BackupUiState()
}

// Controla qué sheet de contraseña está abierto
enum class BackupAction { NONE, EXPORT, IMPORT }

data class BackupSheetState(
    val action: BackupAction = BackupAction.NONE,
    val password: String     = "",
    val confirmPassword: String = "",   // solo para exportar
    val passwordError: String?  = null,
    val backupState: BackupUiState = BackupUiState.Idle
)

class BackupViewModel(
    private val backupManager: DatabaseBackupManager,
    private val saveLastBackupDate: SaveLastBackupDateUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(BackupSheetState())
    val state: StateFlow<BackupSheetState> = _state.asStateFlow()

    fun openExport() {
        _state.value = BackupSheetState(action = BackupAction.EXPORT)
    }

    fun openImport() {
        _state.value = BackupSheetState(action = BackupAction.IMPORT)
    }

    fun dismiss() {
        _state.value = BackupSheetState()
    }

    fun onPasswordChange(value: String) {
        _state.value = _state.value.copy(password = value, passwordError = null)
    }

    fun onConfirmPasswordChange(value: String) {
        _state.value = _state.value.copy(confirmPassword = value, passwordError = null)
    }

    fun confirmExport() {
        println("[BackupVM] ▶ confirmExport() invocado")
        val s = _state.value
        when {
            s.password.length < 6 -> {
                println("[BackupVM] · password demasiado corta")
                _state.value = s.copy(passwordError = "La contraseña debe tener al menos 6 caracteres")
                return
            }
            s.password != s.confirmPassword -> {
                println("[BackupVM] · contraseñas no coinciden")
                _state.value = s.copy(passwordError = "Las contraseñas no coinciden")
                return
            }
        }
        println("[BackupVM] · validación OK → estado Loading")
        _state.value = s.copy(backupState = BackupUiState.Loading)
        viewModelScope.launch {
            println("[BackupVM] · dentro de viewModelScope.launch → llamando a backupManager.exportEncrypted")
            backupManager.exportEncrypted(s.password) { result ->
                println("[BackupVM] · callback de exportEncrypted recibido: $result")
                when (result) {
                    is BackupResult.Success -> {
                        saveLastBackupDate()
                        println("[BackupVM] · fecha de último backup guardada")
                    }
                    is BackupResult.Error -> { /* no hacer nada extra */ }
                }
                _state.value = when (result) {
                    is BackupResult.Success -> _state.value.copy(
                        backupState = BackupUiState.Success
                    )
                    is BackupResult.Error -> _state.value.copy(
                        backupState = BackupUiState.Error(result.message)
                    )
                }
                println("[BackupVM] · estado actualizado a ${_state.value.backupState}")
            }
            println("[BackupVM] · backupManager.exportEncrypted retornó (callback puede llegar después)")
        }
    }

    fun confirmImport() {
        val s = _state.value
        if (s.password.isBlank()) {
            _state.value = s.copy(passwordError = "Introduce la contraseña del backup")
            return
        }
        _state.value = s.copy(backupState = BackupUiState.Loading)
        viewModelScope.launch {
            backupManager.importEncrypted(s.password) { result ->
                _state.value = when (result) {
                    is BackupResult.Success -> _state.value.copy(
                        backupState = BackupUiState.Success
                    )
                    is BackupResult.Error -> _state.value.copy(
                        backupState = BackupUiState.Error(result.message),
                        passwordError = result.message
                    )
                }
            }
        }
    }

    fun clearResult() {
        _state.value = _state.value.copy(backupState = BackupUiState.Idle)
    }
}
