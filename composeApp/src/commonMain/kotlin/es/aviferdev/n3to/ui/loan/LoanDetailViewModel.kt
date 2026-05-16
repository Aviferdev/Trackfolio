package es.aviferdev.n3to.ui.loan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.aviferdev.n3to.domain.model.AmortizationEntry
import es.aviferdev.n3to.domain.model.Loan
import es.aviferdev.n3to.domain.model.LoanRateChange
import es.aviferdev.n3to.domain.repository.LoanRateChangeRepository
import es.aviferdev.n3to.domain.repository.LoanRepository
import es.aviferdev.n3to.domain.usecase.loan.ArchiveLoanUseCase
import es.aviferdev.n3to.domain.usecase.loan.GetAmortizationScheduleUseCase
import es.aviferdev.n3to.domain.usecase.loan.UpdateLoanRateUseCase
import es.aviferdev.n3to.domain.usecase.loan.UpdateLoanUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class LoanDetailUiState(
    val loan: Loan? = null,
    val schedule: List<AmortizationEntry> = emptyList(),
    val rateChanges: List<LoanRateChange> = emptyList(),
    val isLoading: Boolean = true,
    val showEditSheet: Boolean = false
)

class LoanDetailViewModel(
    private val loanId: String,
    private val loanRepository: LoanRepository,
    private val getAmortizationSchedule: GetAmortizationScheduleUseCase,
    private val updateLoanRate: UpdateLoanRateUseCase,
    private val updateLoan: UpdateLoanUseCase,
    private val archiveLoan: ArchiveLoanUseCase,
    private val rateChangeRepository: LoanRateChangeRepository
) : ViewModel() {

    private val _showRateSheet = MutableStateFlow(false)
    val showRateSheet: StateFlow<Boolean> = _showRateSheet.asStateFlow()

    private val _showEditSheet = MutableStateFlow(false)
    val showEditSheet: StateFlow<Boolean> = _showEditSheet.asStateFlow()

    val uiState: StateFlow<LoanDetailUiState> = combine(
        loanRepository.getById(loanId),
        getAmortizationSchedule(loanId),
        rateChangeRepository.getByLoan(loanId),
        _showEditSheet
    ) { loan, schedule, rateChanges, showEdit ->
        LoanDetailUiState(
            loan        = loan,
            schedule    = schedule,
            rateChanges = rateChanges,
            isLoading   = false,
            showEditSheet = showEdit
        )
    }.stateIn(
        scope        = viewModelScope,
        started      = SharingStarted.WhileSubscribed(5_000),
        initialValue = LoanDetailUiState()
    )

    fun openRateSheet() { _showRateSheet.value = true }
    fun closeRateSheet() { _showRateSheet.value = false }

    fun openEditSheet() { _showEditSheet.value = true }
    fun closeEditSheet() { _showEditSheet.value = false }

    fun updateRate(newRate: Double, effectiveDate: Long) {
        viewModelScope.launch {
            updateLoanRate(loanId, newRate, effectiveDate)
            _showRateSheet.value = false
        }
    }

    fun archive() {
        viewModelScope.launch { archiveLoan(loanId) }
    }
}
