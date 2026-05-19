package es.aviferdev.n3to.ui.savingsrates

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.aviferdev.n3to.domain.model.SavingsRate
import es.aviferdev.n3to.domain.model.SavingsRateType
import es.aviferdev.n3to.domain.usecase.savingsrates.GetSavingsRatesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SavingsRatesUiState(
    val shortTermRates: List<SavingsRate> = emptyList(),
    val mediumTermRates: List<SavingsRate> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedTab: SavingsRateType = SavingsRateType.SHORT_TERM,
    val lastUpdatedAt: Long? = null,
    val isStale: Boolean = false
)

class SavingsRatesViewModel(
    private val getSavingsRates: GetSavingsRatesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SavingsRatesUiState())
    val uiState: StateFlow<SavingsRatesUiState> = _uiState.asStateFlow()

    init {
        loadAll(forceRefresh = false)
    }

    fun selectTab(type: SavingsRateType) {
        _uiState.update { it.copy(selectedTab = type) }
    }

    fun refresh() {
        loadAll(forceRefresh = true)
    }

    private fun loadAll(forceRefresh: Boolean) {
        println("[SavingsRatesVM] 🚀 loadAll(forceRefresh=$forceRefresh)")
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            println("[SavingsRatesVM] ⏳ Lanzando corrutina...")
            val shortResult = getSavingsRates(SavingsRateType.SHORT_TERM, forceRefresh)
            val mediumResult = getSavingsRates(SavingsRateType.MEDIUM_TERM, forceRefresh)

            val shortRates = shortResult.getOrElse { emptyList() }
            val mediumRates = mediumResult.getOrElse { emptyList() }
            val lastTs = getSavingsRates.getLastFetchedAt(SavingsRateType.SHORT_TERM)

            println("[SavingsRatesVM] short=${shortRates.size} medium=${mediumRates.size}")
            println("[SavingsRatesVM] shortErr=${shortResult.exceptionOrNull()?.message}")
            println("[SavingsRatesVM] mediumErr=${mediumResult.exceptionOrNull()?.message}")

            val bothFailed = shortResult.isFailure && mediumResult.isFailure
            val isStale = bothFailed && (shortRates.isNotEmpty() || mediumRates.isNotEmpty())

            _uiState.update {
                it.copy(
                    shortTermRates = shortRates,
                    mediumTermRates = mediumRates,
                    isLoading = false,
                    error = if (bothFailed && shortRates.isEmpty()) {
                        shortResult.exceptionOrNull()?.message ?: "Error al cargar datos"
                    } else null,
                    lastUpdatedAt = lastTs,
                    isStale = isStale
                )
            }
            println("[SavingsRatesVM] ✅ uiState actualizado")
        }
    }
}
