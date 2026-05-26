package es.aviferdev.n3to.ui.inflation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.aviferdev.n3to.domain.model.InflationDataPoint
import es.aviferdev.n3to.domain.usecase.inflation.GetInflationHistoryUseCase
import es.aviferdev.n3to.domain.usecase.taxprofile.GetActiveTaxProfileSnapshotUseCase
import es.aviferdev.n3to.platform.nowLocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class InflationUiState(
    val isLoading: Boolean = true,
    val data: Map<String, List<InflationDataPoint>> = emptyMap(),
    val selectedCountries: List<String> = emptyList(),
    val error: Boolean = false
)

class InflationViewModel(
    private val getInflationHistory: GetInflationHistoryUseCase,
    private val getActiveTaxProfile: GetActiveTaxProfileSnapshotUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(InflationUiState())
    val uiState: StateFlow<InflationUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val defaultCountry = getActiveTaxProfile(nowLocalDate())
                ?.profile?.countryCode ?: "ES"
            val initial = listOf(defaultCountry)
            _uiState.update { it.copy(isLoading = true, selectedCountries = initial) }
            fetchFor(initial)
        }
    }

    fun toggleCountry(code: String) {
        val current = _uiState.value.selectedCountries.toMutableList()
        if (code in current) {
            if (current.size > 1) current.remove(code)
        } else {
            current.add(code)
        }
        _uiState.update { it.copy(isLoading = true, selectedCountries = current) }
        viewModelScope.launch { fetchFor(current) }
    }

    private suspend fun fetchFor(countries: List<String>) {
        try {
            val result = getInflationHistory(countries)
            _uiState.update {
                it.copy(
                    isLoading = false,
                    data = result,
                    error = result.isEmpty() && countries.isNotEmpty()
                )
            }
        } catch (_: Exception) {
            _uiState.update { it.copy(isLoading = false, error = true) }
        }
    }
}
