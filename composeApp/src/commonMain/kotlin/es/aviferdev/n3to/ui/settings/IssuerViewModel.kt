package es.aviferdev.n3to.ui.settings

import es.aviferdev.n3to.platform.nowMillis
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.aviferdev.n3to.domain.model.Issuer
import es.aviferdev.n3to.domain.model.IssuerType
import es.aviferdev.n3to.domain.usecase.issuer.ArchiveIssuerUseCase
import es.aviferdev.n3to.domain.usecase.issuer.GetIssuersUseCase
import es.aviferdev.n3to.domain.usecase.issuer.RenameIssuerUseCase
import es.aviferdev.n3to.domain.usecase.issuer.SaveIssuerUseCase
import es.aviferdev.n3to.ui.account.AccountSession
import com.benasher44.uuid.uuid4
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class IssuerListUiState(
    val issuersByType: Map<IssuerType, List<Issuer>> = emptyMap(),
    val showAddSheet: Boolean          = false,
    val addType: IssuerType            = IssuerType.EMPLOYER,
    val editing: Issuer?               = null,
    val pendingDelete: Issuer?         = null,
    val error: String?                 = null
)

class IssuerViewModel(
    private val getIssuers: GetIssuersUseCase,
    private val saveIssuer: SaveIssuerUseCase,
    private val renameIssuer: RenameIssuerUseCase,
    private val archiveIssuer: ArchiveIssuerUseCase,
    private val session: AccountSession
) : ViewModel() {

    private val _uiState = MutableStateFlow(IssuerListUiState())
    val uiState: StateFlow<IssuerListUiState> = _uiState.asStateFlow()

    private val jobs = mutableMapOf<IssuerType, Job>()

    init { observeAccount() }

    private fun observeAccount() {
        session.selectedAccountId
            .onEach { accountId ->
                jobs.values.forEach { it.cancel() }
                jobs.clear()
                if (accountId != null) {
                    IssuerType.entries.forEach { type -> loadIssuers(accountId, type) }
                } else {
                    _uiState.update { it.copy(issuersByType = emptyMap()) }
                }
            }
            .launchIn(viewModelScope)
    }

    private fun loadIssuers(accountId: String, type: IssuerType) {
        jobs[type]?.cancel()
        jobs[type] = getIssuers(accountId, type)
            .onEach { list ->
                _uiState.update { state ->
                    state.copy(issuersByType = state.issuersByType + (type to list))
                }
            }
            .launchIn(viewModelScope)
    }

    // ── Añadir ────────────────────────────────────────────────────────────────
    fun openAddSheet(type: IssuerType) {
        _uiState.update { it.copy(showAddSheet = true, addType = type) }
    }

    fun closeAddSheet() {
        _uiState.update { it.copy(showAddSheet = false) }
    }

    fun addIssuer(name: String, icon: String, type: IssuerType) {
        val trimmed = name.trim()
        if (trimmed.isBlank()) return

        val existing = _uiState.value.issuersByType[type] ?: emptyList()
        if (existing.any { it.name.equals(trimmed, ignoreCase = true) }) {
            _uiState.update { it.copy(error = "Ya existe un emisor con ese nombre") }
            return
        }

        val accountId = session.selectedAccountId.value ?: return
        viewModelScope.launch {
            val issuer = Issuer(
                id        = uuid4().toString(),
                accountId = accountId,
                name      = trimmed,
                type      = type,
                icon      = icon,
                createdAt = nowMillis()
            )
            saveIssuer(issuer).onFailure { e ->
                _uiState.update { it.copy(error = e.message) }
            }
            _uiState.update { it.copy(showAddSheet = false) }
        }
    }

    // ── Editar ────────────────────────────────────────────────────────────────
    fun openEditSheet(issuer: Issuer) {
        _uiState.update { it.copy(editing = issuer) }
    }

    fun closeEditSheet() {
        _uiState.update { it.copy(editing = null) }
    }

    fun rename(id: String, newName: String, icon: String, type: IssuerType) {
        val trimmed = newName.trim()
        if (trimmed.isBlank()) return

        val existing = _uiState.value.issuersByType[type] ?: emptyList()
        if (existing.any { it.id != id && it.name.equals(trimmed, ignoreCase = true) }) {
            _uiState.update { it.copy(error = "Ya existe un emisor con ese nombre") }
            return
        }

        viewModelScope.launch {
            renameIssuer(id, trimmed, icon, type).onFailure { e ->
                _uiState.update { it.copy(error = e.message) }
            }
            _uiState.update { it.copy(editing = null) }
        }
    }

    // ── Archivar ──────────────────────────────────────────────────────────────
    fun requestDelete(issuer: Issuer) {
        _uiState.update { it.copy(pendingDelete = issuer) }
    }

    fun cancelDelete() {
        _uiState.update { it.copy(pendingDelete = null) }
    }

    fun confirmDelete() {
        val issuer = _uiState.value.pendingDelete ?: return
        viewModelScope.launch {
            archiveIssuer(issuer.id, issuer.type).onFailure { e ->
                _uiState.update { it.copy(error = e.message) }
            }
            _uiState.update { it.copy(pendingDelete = null) }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
