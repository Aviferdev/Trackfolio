package es.aviferdev.n3to.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.aviferdev.n3to.core.premium.PremiumManager
import es.aviferdev.n3to.core.premium.PremiumStatus
import es.aviferdev.n3to.domain.model.ConsentPreferences
import es.aviferdev.n3to.domain.usecase.consent.GetConsentUseCase
import es.aviferdev.n3to.domain.usecase.consent.RevokeConsentUseCase
import es.aviferdev.n3to.domain.usecase.consent.SaveConsentUseCase
import es.aviferdev.n3to.platform.AnalyticsTracker
import es.aviferdev.n3to.platform.CrashlyticsTracker
import es.aviferdev.n3to.platform.PurchaseResult
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PrivacySettingsUiState(
    val preferences: ConsentPreferences = ConsentPreferences(),
    val premiumStatus: PremiumStatus = PremiumStatus(),
    val isLoading: Boolean = true,
    val showRevokeConfirmation: Boolean = false,
    val revokeCompleted: Boolean = false
)

class PrivacySettingsViewModel(
    private val getConsentUseCase: GetConsentUseCase,
    private val saveConsentUseCase: SaveConsentUseCase,
    private val revokeConsentUseCase: RevokeConsentUseCase,
    private val analyticsTracker: AnalyticsTracker,
    private val crashlyticsTracker: CrashlyticsTracker,
    private val premiumManager: PremiumManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(PrivacySettingsUiState())
    val uiState: StateFlow<PrivacySettingsUiState> = _uiState.asStateFlow()

    init {
        loadPreferences()
        observePremium()
    }

    private fun loadPreferences() {
        viewModelScope.launch {
            val prefs = getConsentUseCase()
            _uiState.update { it.copy(preferences = prefs, isLoading = false) }
        }
    }

    private fun observePremium() {
        viewModelScope.launch {
            premiumManager.status.collect { status ->
                _uiState.update { it.copy(premiumStatus = status) }
            }
        }
    }

    fun updateAnalytics(enabled: Boolean) = updateAndSave { it.copy(analytics = enabled) }
    fun updateCrashReporting(enabled: Boolean) = updateAndSave { it.copy(crashReporting = enabled) }

    private fun updateAndSave(transform: (ConsentPreferences) -> ConsentPreferences) {
        viewModelScope.launch {
            val updated = transform(_uiState.value.preferences)
            _uiState.update { it.copy(preferences = updated) }
            saveConsentUseCase(updated)
            analyticsTracker.setEnabled(updated.analytics)
            crashlyticsTracker.setCrashReportingEnabled(updated.crashReporting)
        }
    }

    fun requestRevokeAll() = _uiState.update { it.copy(showRevokeConfirmation = true) }
    fun dismissRevokeConfirmation() = _uiState.update { it.copy(showRevokeConfirmation = false) }

    fun confirmRevokeAll() {
        viewModelScope.launch {
            revokeConsentUseCase()
            analyticsTracker.setEnabled(false)
            crashlyticsTracker.setCrashReportingEnabled(false)
            _uiState.update {
                it.copy(
                    preferences = ConsentPreferences(),
                    showRevokeConfirmation = false,
                    revokeCompleted = true
                )
            }
        }
    }

    fun onRevokeCompletedHandled() = _uiState.update { it.copy(revokeCompleted = false) }

    // ─── Restore purchases ────────────────────────────────────────

    private val _restoreEvent = MutableSharedFlow<RestoreResult>()
    val restoreEvent: SharedFlow<RestoreResult> = _restoreEvent.asSharedFlow()

    fun restorePurchases() {
        viewModelScope.launch {
            when (val result = premiumManager.restorePurchases()) {
                is PurchaseResult.Success -> _restoreEvent.emit(RestoreResult.Success)
                is PurchaseResult.Error -> _restoreEvent.emit(RestoreResult.Error(result.message))
                is PurchaseResult.Cancelled -> _restoreEvent.emit(RestoreResult.Error("Restauración cancelada"))
            }
        }
    }
}

sealed class RestoreResult {
    data object Success : RestoreResult()
    data class Error(val message: String) : RestoreResult()
}
