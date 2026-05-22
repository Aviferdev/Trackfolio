package es.aviferdev.n3to.ui.realestate

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.aviferdev.n3to.domain.model.*
import es.aviferdev.n3to.domain.usecase.category.GetCategoriesByTypeUseCase
import es.aviferdev.n3to.domain.usecase.loan.GetLoansByAccountUseCase
import es.aviferdev.n3to.domain.usecase.realestate.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class RealEstateDetailUiState(
    val property: RealEstateProperty? = null,
    val linkedLoan: Loan? = null,
    val rentalPeriods: List<RentalPeriod> = emptyList(),
    val linkedTransactions: List<Transaction> = emptyList(),
    val financialSummary: PropertyFinancialSummary? = null,
    val expenseCategories: List<Category> = emptyList(),
    val showMortgageReminder: Boolean = false,
    val showEditSheet: Boolean = false,
    val showValueSheet: Boolean = false,
    val showChangeRentalStatusSheet: Boolean = false,
    val showArchiveDialog: Boolean = false,
    val showSellSheet: Boolean = false,
    val isLoading: Boolean = false
)

private data class PropData(
    val property: RealEstateProperty?,
    val allLoans: List<Loan>
)

@OptIn(ExperimentalCoroutinesApi::class)
class RealEstateDetailViewModel(
    private val propertyId: String,
    private val getPropertyById: GetPropertyByIdUseCase,
    private val savePropertyUseCase: SavePropertyUseCase,
    private val updatePropertyValue: UpdatePropertyValueUseCase,
    private val archiveProperty: ArchivePropertyUseCase,
    private val getRentalPeriods: GetRentalPeriodsUseCase,
    private val getTransactionsByProperty: GetTransactionsByPropertyUseCase,
    private val getFinancialSummary: GetPropertyFinancialSummaryUseCase,
    private val changeRentalStatus: ChangeRentalStatusUseCase,
    private val dismissMortgageReminder: DismissMortgageReminderUseCase,
    private val linkLoanUseCase: LinkLoanUseCase,
    private val getLoan: GetLoansByAccountUseCase,
    private val sellPropertyUseCase: SellPropertyUseCase,
    private val getCategoriesByType: GetCategoriesByTypeUseCase
) : ViewModel() {

    private val _showEditSheet = MutableStateFlow(false)
    private val _showValueSheet = MutableStateFlow(false)
    private val _showChangeRentalStatusSheet = MutableStateFlow(false)
    private val _showArchiveDialog = MutableStateFlow(false)
    private val _showSellSheet = MutableStateFlow(false)

    private val propertyWithLoans = combine(
        getPropertyById(propertyId),
        getLoan(propertyId).onStart { emit(emptyList()) }
    ) { property, loans -> PropData(property, loans) }

    private val expenseCategoriesFlow = getPropertyById(propertyId)
        .flatMapLatest { property ->
            if (property != null) getCategoriesByType(property.accountId, TransactionType.EXPENSE)
            else flowOf(emptyList())
        }
        .map { cats -> cats.filter { it.name != "Ajuste de saldo" } }

    private data class SheetStates(
        val showEdit: Boolean = false,
        val showValue: Boolean = false,
        val showRental: Boolean = false,
        val showArchive: Boolean = false,
        val showSell: Boolean = false
    )

    val uiState: StateFlow<RealEstateDetailUiState> = combine(
        propertyWithLoans,
        expenseCategoriesFlow,
        combine(
            _showEditSheet, _showValueSheet, _showChangeRentalStatusSheet,
            _showArchiveDialog, _showSellSheet
        ) { a, b, c, d, e ->
            SheetStates(showEdit = a, showValue = b, showRental = c, showArchive = d, showSell = e)
        }
    ) { (property, loans), categories, sheets ->
        if (property == null) return@combine RealEstateDetailUiState(isLoading = true)

        val linkedLoan = property.linkedLoanId?.let { lid -> loans.find { it.id == lid } }
        val showReminder = property.hasPendingMortgageReminder

        RealEstateDetailUiState(
            property = property,
            linkedLoan = linkedLoan,
            expenseCategories = categories,
            showMortgageReminder = showReminder,
            showEditSheet = sheets.showEdit,
            showValueSheet = sheets.showValue,
            showChangeRentalStatusSheet = sheets.showRental,
            showArchiveDialog = sheets.showArchive,
            showSellSheet = sheets.showSell,
            isLoading = false
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        RealEstateDetailUiState(isLoading = true)
    )

    // ── Cargar datos secundarios ─────────────────────────────────────────────
    val rentalPeriods: StateFlow<List<RentalPeriod>> =
        getPropertyById(propertyId)
            .flatMapLatest { property ->
                if (property != null) getRentalPeriods(property.id)
                else flowOf(emptyList())
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val linkedTransactions: StateFlow<List<Transaction>> =
        getPropertyById(propertyId)
            .flatMapLatest { property ->
                if (property != null) getTransactionsByProperty(property.id)
                else flowOf(emptyList())
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val financialSummary: StateFlow<PropertyFinancialSummary?> = combine(
        getPropertyById(propertyId),
        linkedTransactions
    ) { property, transactions ->
        if (property == null) null
        else getFinancialSummary(property, transactions)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    // ── Controles de sheets ──────────────────────────────────────────────────
    fun showEditSheet() {
        _showEditSheet.value = true
    }

    fun hideEditSheet() {
        _showEditSheet.value = false
    }

    fun showValueSheet() {
        _showValueSheet.value = true
    }

    fun hideValueSheet() {
        _showValueSheet.value = false
    }

    fun showChangeRentalStatusSheet() {
        _showChangeRentalStatusSheet.value = true
    }

    fun hideChangeRentalStatusSheet() {
        _showChangeRentalStatusSheet.value = false
    }

    fun showArchiveDialog() {
        _showArchiveDialog.value = true
    }

    fun hideArchiveDialog() {
        _showArchiveDialog.value = false
    }

    fun showSellSheet() {
        _showSellSheet.value = true
    }

    fun hideSellSheet() {
        _showSellSheet.value = false
    }

    // ── Acciones ─────────────────────────────────────────────────────────────
    fun saveProperty(
        property: RealEstateProperty,
        purchaseExpenses: List<PropertyExpense> = emptyList()
    ) {
        viewModelScope.launch {
            savePropertyUseCase(property, purchaseExpenses)
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

    fun sellProperty(saleDate: Long, saleValue: Double, expenses: List<PropertyExpense>) {
        viewModelScope.launch {
            val property = uiState.value.property ?: return@launch
            sellPropertyUseCase(
                propertyId = property.id,
                saleDate = saleDate,
                saleValue = saleValue,
                saleExpenses = expenses,
                accountId = property.accountId,
                propertyName = property.name
            ).onSuccess { hideSellSheet() }
        }
    }
}
