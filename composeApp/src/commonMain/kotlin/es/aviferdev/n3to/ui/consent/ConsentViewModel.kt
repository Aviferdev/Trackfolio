package es.aviferdev.n3to.ui.consent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.aviferdev.n3to.domain.model.ConsentPreferences
import es.aviferdev.n3to.domain.usecase.consent.SaveConsentUseCase
import es.aviferdev.n3to.platform.AnalyticsTracker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Tus datos financieros NUNCA se rastrean. Solo recopilamos datos de uso anónimos para mejorar la app.
 */
data class ConsentUiState(
    val analytics: Boolean = false,
    val crashReporting: Boolean = false,
    val isLoading: Boolean = false,
    val navigateToHome: Boolean = false
)

class ConsentViewModel(
    private val saveConsentUseCase: SaveConsentUseCase,
    private val analyticsTracker: AnalyticsTracker
) : ViewModel() {

    private val _uiState = MutableStateFlow(ConsentUiState())
    val uiState: StateFlow<ConsentUiState> = _uiState.asStateFlow()

    fun onAnalyticsToggle(enabled: Boolean) =
        _uiState.update { it.copy(analytics = enabled) }

    fun onCrashReportingToggle(enabled: Boolean) =
        _uiState.update { it.copy(crashReporting = enabled) }

    fun acceptAll() {
        _uiState.update { it.copy(analytics = true, crashReporting = true) }
        saveAndProceed()
    }

    fun rejectAll() {
        _uiState.update { it.copy(analytics = false, crashReporting = false) }
        saveAndProceed()
    }

    fun acceptSelection() = saveAndProceed()

    private fun saveAndProceed() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val state = _uiState.value

            saveConsentUseCase(
                ConsentPreferences(
                    analytics = state.analytics,
                    crashReporting = state.crashReporting
                )
            )

            analyticsTracker.setEnabled(state.analytics)

            _uiState.update { it.copy(isLoading = false, navigateToHome = true) }
        }
    }

    fun onNavigationHandled() = _uiState.update { it.copy(navigateToHome = false) }
}
