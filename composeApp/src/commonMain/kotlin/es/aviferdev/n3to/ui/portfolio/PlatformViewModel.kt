package es.aviferdev.n3to.ui.portfolio

import es.aviferdev.n3to.platform.nowMillis
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.aviferdev.n3to.domain.model.Platform
import es.aviferdev.n3to.domain.usecase.platform.ArchivePlatformUseCase
import es.aviferdev.n3to.domain.usecase.platform.GetPlatformsUseCase
import es.aviferdev.n3to.domain.usecase.platform.RenamePlatformUseCase
import es.aviferdev.n3to.domain.usecase.platform.SavePlatformUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class PlatformError {
    data object AlreadyExists : PlatformError()
    data class Unknown(val message: String?) : PlatformError()
}

data class PlatformListUiState(
    val platforms: List<Platform>      = emptyList(),
    val showAddSheet: Boolean          = false,
    val editing: Platform?             = null,
    val pendingDelete: Platform?       = null,
    val error: PlatformError?          = null
)

class PlatformViewModel(
    private val getPlatforms: GetPlatformsUseCase,
    private val savePlatform: SavePlatformUseCase,
    private val renamePlatform: RenamePlatformUseCase,
    private val archivePlatform: ArchivePlatformUseCase
) : ViewModel() {

    private val _showAddSheet  = MutableStateFlow(false)
    private val _editing       = MutableStateFlow<Platform?>(null)
    private val _pendingDelete = MutableStateFlow<Platform?>(null)
    private val _error         = MutableStateFlow<PlatformError?>(null)

    val uiState: StateFlow<PlatformListUiState> = combine(
        getPlatforms(),
        combine(_showAddSheet, _editing, _pendingDelete, _error) { s, e, p, err ->
            Quad(s, e, p, err)
        }
    ) { platforms, q ->
        PlatformListUiState(
            platforms     = platforms,
            showAddSheet  = q.a,
            editing       = q.b,
            pendingDelete = q.c,
            error         = q.d
        )
    }.stateIn(
        scope        = viewModelScope,
        started      = SharingStarted.WhileSubscribed(5_000),
        initialValue = PlatformListUiState()
    )

    fun openAddSheet()  { _showAddSheet.value = true }
    fun closeAddSheet() { _showAddSheet.value = false }

    fun openEditSheet(platform: Platform) { _editing.value = platform }
    fun closeEditSheet()                  { _editing.value = null }

    fun addPlatform(name: String, icon: String, notes: String?) {
        val trimmed = name.trim()
        if (trimmed.isBlank()) return
        if (uiState.value.platforms.any { it.name.equals(trimmed, ignoreCase = true) }) {
            _error.value = PlatformError.AlreadyExists
            return
        }
        val validatedNotes = notes?.take(200)?.ifBlank { null }
        viewModelScope.launch {
            val now = nowMillis()
            val nextOrder = (uiState.value.platforms.maxOfOrNull { it.sortOrder } ?: -1) + 1
            savePlatform(
                Platform(
                    id        = "platform_$now",
                    name      = trimmed,
                    icon      = icon.ifBlank { "🏦" },
                    sortOrder = nextOrder,
                    createdAt = now,
                    notes     = validatedNotes
                )
            ).onFailure { _error.value = PlatformError.Unknown(it.message) }
            _showAddSheet.value = false
        }
    }

    fun renamePlatform(id: String, newName: String, newIcon: String, notes: String?) {
        val trimmed = newName.trim()
        if (trimmed.isBlank()) return
        if (uiState.value.platforms.any { it.id != id && it.name.equals(trimmed, ignoreCase = true) }) {
            _error.value = PlatformError.AlreadyExists
            return
        }
        val validatedNotes = notes?.take(200)?.ifBlank { null }
        viewModelScope.launch {
            renamePlatform.invoke(id, trimmed, newIcon.ifBlank { "🏦" }, validatedNotes)
                .onFailure { _error.value = PlatformError.Unknown(it.message) }
            _editing.value = null
        }
    }

    fun requestDelete(platform: Platform) { _pendingDelete.value = platform }
    fun cancelDelete()                    { _pendingDelete.value = null }

    fun confirmDelete() {
        val p = _pendingDelete.value ?: return
        viewModelScope.launch {
            archivePlatform(p.id).onFailure { _error.value = PlatformError.Unknown(it.message) }
            _pendingDelete.value = null
        }
    }

    fun clearError() { _error.value = null }

    private data class Quad<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)
}
