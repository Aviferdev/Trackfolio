package es.aviferdev.n3to.ui.fiscal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.aviferdev.n3to.domain.model.FiscalReportData
import es.aviferdev.n3to.domain.model.TaxProfileSnapshot
import es.aviferdev.n3to.domain.pdf.PdfReportGenerator
import es.aviferdev.n3to.domain.usecase.fiscal.GetFiscalReportDataUseCase
import es.aviferdev.n3to.domain.usecase.taxprofile.GetActiveTaxProfileSnapshotUseCase
import es.aviferdev.n3to.domain.usecase.transaction.GetOldestTransactionDateUseCase
import es.aviferdev.n3to.platform.nowLocalDate
import es.aviferdev.n3to.platform.nowYear
import es.aviferdev.n3to.ui.account.AccountSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

data class FiscalReportUiState(
    val isLoading: Boolean = true,
    val reportData: FiscalReportData? = null,
    val selectedYear: String = currentYear(),
    val canGoBack: Boolean = true,
    val canGoForward: Boolean = false,
    val activeTaxProfile: TaxProfileSnapshot? = null,
    val isGenerating: Boolean = false,
    val showPasswordSheet: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null,
    val hasNetOnlyIncomes: Boolean = false
)

private fun currentYear(): String = nowYear().toString()

class FiscalReportViewModel(
    private val getFiscalReportData: GetFiscalReportDataUseCase,
    private val getActiveTaxProfile: GetActiveTaxProfileSnapshotUseCase,
    private val getOldestDate: GetOldestTransactionDateUseCase,
    private val pdfGenerator: PdfReportGenerator,
    private val session: AccountSession
) : ViewModel() {

    private val _uiState = MutableStateFlow(FiscalReportUiState())
    val uiState: StateFlow<FiscalReportUiState> = _uiState.asStateFlow()

    private val _oldestYear = MutableStateFlow<Int?>(null)

    init {
        viewModelScope.launch {
            session.selectedAccountId.flatMapLatest { accountId ->
                if (accountId == null) flowOf(null)
                else getOldestDate(accountId)
            }.collect { epochMillis ->
                _oldestYear.value = epochMillis?.let {
                    Instant.fromEpochMilliseconds(it)
                        .toLocalDateTime(TimeZone.currentSystemDefault()).year
                }
                recomputeNavFlags()
            }
        }
        loadReport()
    }

    private fun recomputeNavFlags() {
        val yearInt = _uiState.value.selectedYear.toIntOrNull() ?: return
        val oldest = _oldestYear.value
        _uiState.value = _uiState.value.copy(
            canGoBack = oldest == null || yearInt > oldest,
            canGoForward = yearInt < currentYear().toInt()
        )
    }

    private fun loadReport() {
        val accountId = session.selectedAccountId.value ?: return
        val year = _uiState.value.selectedYear

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val today = nowLocalDate()
            val yearInt = year.toIntOrNull()
            val queryDate = when {
                yearInt == null -> today
                yearInt >= today.year -> today
                else -> LocalDate(yearInt, 12, 31)
            }
            val taxProfile = getActiveTaxProfile(queryDate)
            _uiState.value = _uiState.value.copy(activeTaxProfile = taxProfile)

            getFiscalReportData(accountId, year)
                .catch { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Error al cargar los datos"
                    )
                }
                .collectLatest { data ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        reportData = data,
                        hasNetOnlyIncomes = data.hasNetOnlyIncomes
                    )
                }
        }
    }

    fun selectYear(year: String) {
        if (year == _uiState.value.selectedYear) return
        val yearInt = year.toIntOrNull() ?: return
        val oldest = _oldestYear.value
        _uiState.value = _uiState.value.copy(
            selectedYear = year,
            reportData = null,
            canGoBack = oldest == null || yearInt > oldest,
            canGoForward = yearInt < currentYear().toInt()
        )
        loadReport()
    }

    fun previousYear() {
        val prev = (_uiState.value.selectedYear.toIntOrNull() ?: return) - 1
        selectYear(prev.toString())
    }

    fun nextYear() {
        val next = (_uiState.value.selectedYear.toIntOrNull() ?: return) + 1
        if (next > currentYear().toInt()) return
        selectYear(next.toString())
    }

    fun generatePdf() {
        _uiState.value = _uiState.value.copy(showPasswordSheet = true)
    }

    fun cancelPasswordSheet() {
        _uiState.value = _uiState.value.copy(showPasswordSheet = false)
    }

    fun confirmGeneratePdf(password: String) {
        val data = _uiState.value.reportData ?: return
        _uiState.value =
            _uiState.value.copy(showPasswordSheet = false, isGenerating = true, errorMessage = null)

        val pwd = password.ifBlank { null }
        pdfGenerator.generate(data, pwd) { success, error ->
            _uiState.value = _uiState.value.copy(
                isGenerating = false,
                successMessage = if (success) "PDF generado. Elige dónde guardarlo o compartirlo." else null,
                errorMessage = error
            )
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(successMessage = null, errorMessage = null)
    }
}
