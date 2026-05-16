package es.aviferdev.trackfolio.ui.realestate

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.aviferdev.trackfolio.domain.model.*
import es.aviferdev.trackfolio.domain.repository.RealEstatePropertyRepository
import es.aviferdev.trackfolio.domain.usecase.realestate.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class RealEstateDetailUiState(
    val property: RealEstateProperty? = null,
    val linkedLoan: Loan? = null,
    val rentalPeriods: List<RentalPeriod> = emptyList(),
    val linkedTransactions: List<Transaction> = emptyList(),
    val financialSummary: PropertyFinancialSummary? = null,
    val showMortgageReminder: Boolean = false,
    val showEditSheet: Boolean = false,
    val showValueSheet: Boolean = false,
    val showChangeRentalStatusSheet: Boolean = false,
    val showArchiveDialog: Boolean = false,
    val isLoading: Boolean = false
)

private data class PropData(
    val property: RealEstateProperty?,
    val allLoans: List<Loan>
)

@OptIn(ExperimentalCoroutinesApi::class)
class RealEstateDetailViewModel(
    private val propertyId: String,
    private val propertyRepository: RealEstatePropertyRepository,
    private val savePropertyUseCase: SavePropertyUseCase,
    private val updatePropertyValue: UpdatePropertyValueUseCase,
    private val archiveProperty: ArchivePropertyUseCase,
    private val getRentalPeriods: GetRentalPeriodsUseCase,
    private val getTransactionsByProperty: GetTransactionsByPropertyUseCase,
    private val getFinancialSummary: GetPropertyFinancialSummaryUseCase,
    private val changeRentalStatus: ChangeRentalStatusUseCase,
    private val dismissMortgageReminder: DismissMortgageReminderUseCase,
    private val linkLoanUseCase: LinkLoanUseCase,
    getLoan: es.aviferdev.trackfolio.domain.usecase.loan.GetLoansByAccountUseCase
) : ViewModel() {

    private val _showEditSheet = MutableStateFlow(false)
    private val _showValueSheet = MutableStateFlow(false)
    private val _showChangeRentalStatusSheet = MutableStateFlow(false)
    private val _showArchiveDialog = MutableStateFlow(false)

    private val propertyWithLoans = combine(
        propertyRepository.getPropertyById(propertyId),
        getLoan(propertyId).onStart { emit(emptyList()) }
    ) { property, loans -> PropData(property, loans) }

    val uiState: StateFlow<RealEstateDetailUiState> = combine(
        propertyWithLoans,
        _showEditSheet, _showValueSheet, _showChangeRentalStatusSheet, _showArchiveDialog
    ) { (property, loans), showEdit, showValue, showRental, showArchive ->
        if (property == null) return@combine RealEstateDetailUiState(isLoading = true)

        val linkedLoan = property.linkedLoanId?.let { lid -> loans.find { it.id == lid } }
        val showReminder = property.hasPendingMortgageReminder

        RealEstateDetailUiState(
            property                 = property,
            linkedLoan               = linkedLoan,
            showMortgageReminder     = showReminder,
            showEditSheet            = showEdit,
            showValueSheet           = showValue,
            showChangeRentalStatusSheet = showRental,
            showArchiveDialog        = showArchive,
            isLoading                = false
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RealEstateDetailUiState(isLoading = true))

    // ── Cargar datos secundarios (períodos, transacciones, resumen) ──────────
    val rentalPeriods: StateFlow<List<RentalPeriod>> = propertyRepository.getPropertyById(propertyId)
        .flatMapLatest { property ->
            if (property != null) getRentalPeriods(property.id)
            else flowOf(emptyList())
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val linkedTransactions: StateFlow<List<Transaction>> = propertyRepository.getPropertyById(propertyId)
        .flatMapLatest { property ->
            if (property != null) getTransactionsByProperty(property.id)
            else flowOf(emptyList())
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val financialSummary: StateFlow<PropertyFinancialSummary?> = combine(
        propertyRepository.getPropertyById(propertyId),
        linkedTransactions
    ) { property, transactions ->
        if (property == null) null
        else getFinancialSummary(property, transactions)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    // ── Acciones ──────────────────────────────────────────────────────────────
    fun showEditSheet() { _showEditSheet.value = true }
    fun hideEditSheet() { _showEditSheet.value = false }

    fun showValueSheet() { _showValueSheet.value = true }
    fun hideValueSheet() { _showValueSheet.value = false }

    fun showChangeRentalStatusSheet() { _showChangeRentalStatusSheet.value = true }
    fun hideChangeRentalStatusSheet() { _showChangeRentalStatusSheet.value = false }

    fun showArchiveDialog() { _showArchiveDialog.value = true }
    fun hideArchiveDialog() { _showArchiveDialog.value = false }

    fun saveProperty(property: RealEstateProperty) {
        viewModelScope.launch {
            savePropertyUseCase(property)
                .onSuccess { hideEditSheet() }
        }
    }

    fun updateValue(newValue: Double) {
        viewModelScope.launch {
            updatePropertyValue(propertyId, newValue)
                .onSuccess { hideValueSheet() }
        }
    }

    fun changeStatus(newStatus: RentalStatus, effectiveDate: Long, monthlyRent: Double?) {
        viewModelScope.launch {
            changeRentalStatus(propertyId, newStatus, effectiveDate, monthlyRent)
                .onSuccess { hideChangeRentalStatusSheet() }
        }
    }

    fun archivePropertyAction() {
        viewModelScope.launch {
            archiveProperty(propertyId)
                .onSuccess { hideArchiveDialog() }
        }
    }

    fun dismissMortgageReminderAction() {
        viewModelScope.launch {
            dismissMortgageReminder(propertyId)
        }
    }

    fun linkLoan(loanId: String) {
        viewModelScope.launch {
            linkLoanUseCase(propertyId, loanId)
        }
    }
}
