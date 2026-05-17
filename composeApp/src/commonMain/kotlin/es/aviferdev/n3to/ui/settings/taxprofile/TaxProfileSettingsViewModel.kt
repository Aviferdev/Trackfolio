package es.aviferdev.n3to.ui.settings.taxprofile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.aviferdev.n3to.domain.model.TaxProfile
import es.aviferdev.n3to.domain.model.TaxProfileSnapshot
import es.aviferdev.n3to.domain.usecase.taxprofile.DeleteTaxProfileSnapshotUseCase
import es.aviferdev.n3to.domain.usecase.taxprofile.GetAllTaxProfileSnapshotsUseCase
import es.aviferdev.n3to.domain.usecase.taxprofile.SaveTaxProfileSnapshotUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

data class TaxProfileSettingsUiState(
    val snapshots: List<TaxProfileSnapshot> = emptyList(),
    val showAddSheet: Boolean = false,
    val selectedProfile: TaxProfile = TaxProfile.SPAIN,
    val effectiveDateMillis: Long = 0L,
    val pendingDeleteId: String? = null,
    val isSaving: Boolean = false,
    val error: String? = null
) {
    val isAddValid: Boolean get() = effectiveDateMillis > 0L
}

class TaxProfileSettingsViewModel(
    private val getAll: GetAllTaxProfileSnapshotsUseCase,
    private val save: SaveTaxProfileSnapshotUseCase,
    private val delete: DeleteTaxProfileSnapshotUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(TaxProfileSettingsUiState())
    val uiState: StateFlow<TaxProfileSettingsUiState> = _uiState.asStateFlow()

    init {
        getAll()
            .onEach { list -> _uiState.update { it.copy(snapshots = list) } }
            .launchIn(viewModelScope)
    }

    fun openAddSheet() {
        _uiState.update { it.copy(showAddSheet = true, selectedProfile = TaxProfile.SPAIN, effectiveDateMillis = 0L) }
    }

    fun closeAddSheet() {
        _uiState.update { it.copy(showAddSheet = false) }
    }

    fun onProfileChange(profile: TaxProfile) {
        _uiState.update { it.copy(selectedProfile = profile) }
    }

    fun onDateChange(millis: Long) {
        _uiState.update { it.copy(effectiveDateMillis = millis) }
    }

    fun confirmAdd() {
        val state = _uiState.value
        if (!state.isAddValid) return
        val date = millisToLocalDate(state.effectiveDateMillis)
        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            save(state.selectedProfile, date)
                .onFailure { e -> _uiState.update { it.copy(error = e.message) } }
            _uiState.update { it.copy(showAddSheet = false, isSaving = false) }
        }
    }

    fun requestDelete(id: String) {
        _uiState.update { it.copy(pendingDeleteId = id) }
    }

    fun cancelDelete() {
        _uiState.update { it.copy(pendingDeleteId = null) }
    }

    fun confirmDelete() {
        val id = _uiState.value.pendingDeleteId ?: return
        viewModelScope.launch {
            delete(id).onFailure { e -> _uiState.update { it.copy(error = e.message) } }
            _uiState.update { it.copy(pendingDeleteId = null) }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    private fun millisToLocalDate(millis: Long): LocalDate =
        Instant.fromEpochMilliseconds(millis)
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .date
}
