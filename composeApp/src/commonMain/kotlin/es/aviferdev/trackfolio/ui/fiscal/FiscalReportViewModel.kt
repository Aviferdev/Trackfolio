package es.aviferdev.trackfolio.ui.fiscal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.aviferdev.trackfolio.domain.model.FiscalReportData
import es.aviferdev.trackfolio.domain.pdf.PdfReportGenerator
import es.aviferdev.trackfolio.domain.usecase.fiscal.GetFiscalReportDataUseCase
import es.aviferdev.trackfolio.ui.account.AccountSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

data class FiscalReportUiState(
    val isLoading: Boolean          = true,
    val reportData: FiscalReportData? = null,
    val selectedYear: String        = currentYear(),
    val isGenerating: Boolean       = false,
    val successMessage: String?     = null,
    val errorMessage: String?       = null
)

private fun currentYear(): String =
    Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).year.toString()

class FiscalReportViewModel(
    private val getFiscalReportData: GetFiscalReportDataUseCase,
    private val pdfGenerator: PdfReportGenerator,
    private val session: AccountSession
) : ViewModel() {

    private val _uiState = MutableStateFlow(FiscalReportUiState())
    val uiState: StateFlow<FiscalReportUiState> = _uiState.asStateFlow()

    init {
        loadReport()
    }

    private fun loadReport() {
        val accountId = session.selectedAccountId.value ?: return
        val year      = _uiState.value.selectedYear

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            getFiscalReportData(accountId, year)
                .catch { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading    = false,
                        errorMessage = e.message ?: "Error al cargar los datos"
                    )
                }
                .collectLatest { data ->
                    _uiState.value = _uiState.value.copy(isLoading = false, reportData = data)
                }
        }
    }

    fun selectYear(year: String) {
        if (year == _uiState.value.selectedYear) return
        _uiState.value = _uiState.value.copy(selectedYear = year, reportData = null)
        loadReport()
    }

    fun previousYear() {
        val prev = (_uiState.value.selectedYear.toIntOrNull() ?: return) - 1
        selectYear(prev.toString())
    }

    fun nextYear() {
        val next = (_uiState.value.selectedYear.toIntOrNull() ?: return) + 1
        val now  = currentYear().toInt()
        if (next > now) return
        selectYear(next.toString())
    }

    fun generatePdf() {
        val data = _uiState.value.reportData ?: return
        _uiState.value = _uiState.value.copy(isGenerating = true, errorMessage = null)

        pdfGenerator.generate(data) { success, error ->
            _uiState.value = _uiState.value.copy(
                isGenerating   = false,
                successMessage = if (success) "PDF generado. Elige dónde guardarlo o compartirlo." else null,
                errorMessage   = error
            )
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(successMessage = null, errorMessage = null)
    }
}
