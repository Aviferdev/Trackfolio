package es.aviferdev.trackfolio.ui.annual

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.aviferdev.trackfolio.domain.model.AnnualSummary
import es.aviferdev.trackfolio.domain.usecase.transaction.GetAnnualSummaryUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

data class AnnualUiState(
    val summary: AnnualSummary? = null,
    val year: String = "",
    val isLoading: Boolean = true
)

class AnnualViewModel(
    private val getAnnualSummary: GetAnnualSummaryUseCase
) : ViewModel() {

    private val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    private val _year = MutableStateFlow(now.year.toString())
    val year: StateFlow<String> = _year

    val uiState: StateFlow<AnnualUiState> = _year
        .flatMapLatest { year ->
            getAnnualSummary(year).map { summary ->
                AnnualUiState(summary = summary, year = year, isLoading = false)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AnnualUiState(year = _year.value)
        )

    fun previousYear() {
        _year.value = (_year.value.toInt() - 1).toString()
    }

    fun nextYear() {
        val nowYear = Clock.System.now()
            .toLocalDateTime(TimeZone.currentSystemDefault()).year
        if (_year.value.toInt() >= nowYear) return
        _year.value = (_year.value.toInt() + 1).toString()
    }
}
