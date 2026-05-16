package es.aviferdev.n3to.ui.account

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Singleton Koin (single) que almacena la cuenta seleccionada globalmente.
 * Todos los ViewModels que necesiten saber la cuenta activa consumen este Flow.
 */
class AccountSession {
    private val _selectedAccountId = MutableStateFlow<String?>(null)
    val selectedAccountId: StateFlow<String?> = _selectedAccountId.asStateFlow()

    fun selectAccount(id: String) {
        _selectedAccountId.value = id
    }

    fun clearSelection() {
        _selectedAccountId.value = null
    }
}
