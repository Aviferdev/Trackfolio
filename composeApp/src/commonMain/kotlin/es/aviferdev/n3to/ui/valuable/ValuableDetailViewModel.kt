package es.aviferdev.n3to.ui.valuable

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.aviferdev.n3to.domain.model.Loan
import es.aviferdev.n3to.domain.model.Transaction
import es.aviferdev.n3to.domain.model.Valuable
import es.aviferdev.n3to.domain.model.ValuableExpense
import es.aviferdev.n3to.domain.model.ValuableSummary
import es.aviferdev.n3to.domain.usecase.valuable.DeleteValuableUseCase
import es.aviferdev.n3to.domain.usecase.valuable.GetValuableDetailUseCase
import es.aviferdev.n3to.domain.usecase.valuable.LinkLoanToValuableUseCase
import es.aviferdev.n3to.domain.usecase.valuable.SaveValuableUseCase
import es.aviferdev.n3to.domain.usecase.valuable.SellValuableUseCase
import es.aviferdev.n3to.domain.usecase.loan.GetLoansByAccountUseCase
import es.aviferdev.n3to.domain.usecase.valuable.UpdateValuableEstimatedValueUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ValuableDetailUiState(
    val summary: ValuableSummary? = null,
    val linkedTransactions: List<Transaction> = emptyList(),
    val showEditSheet: Boolean = false,
    val showSellSheet: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val showValueDialog: Boolean = false,
    val showLoanPicker: Boolean = false,
    val availableLoans: List<Loan> = emptyList(),
    val isLoading: Boolean = false
)

class ValuableDetailViewModel(
    private val valuableId: String,
    private val getDetail: GetValuableDetailUseCase,
    private val saveValuableUseCase: SaveValuableUseCase,
    private val sellValuableUseCase: SellValuableUseCase,
    private val deleteValuableUseCase: DeleteValuableUseCase,
    private val updateEstimatedValue: UpdateValuableEstimatedValueUseCase,
    private val linkLoanToValuable: LinkLoanToValuableUseCase,
    private val getLoansByAccount: GetLoansByAccountUseCase
) : ViewModel() {

    private val _showEditSheet = MutableStateFlow(false)
    private val _showSellSheet = MutableStateFlow(false)
    private val _showDeleteDialog = MutableStateFlow(false)
    private val _showValueDialog = MutableStateFlow(false)
    private val _showLoanPicker = MutableStateFlow(false)
    private val _availableLoans = MutableStateFlow<List<Loan>>(emptyList())

    private data class SheetStates(
        val showEdit: Boolean = false,
        val showSell: Boolean = false,
        val showDelete: Boolean = false,
        val showValue: Boolean = false,
        val showLoanPicker: Boolean = false
    )

    val uiState: StateFlow<ValuableDetailUiState> = combine(
        getDetail(valuableId),
        combine(
            _showEditSheet, _showSellSheet, _showDeleteDialog,
            _showValueDialog, _showLoanPicker
        ) { a, b, c, d, e ->
            SheetStates(
                showEdit = a,
                showSell = b,
                showDelete = c,
                showValue = d,
                showLoanPicker = e
            )
        },
        _availableLoans
    ) { summary, sheets, loans ->
        ValuableDetailUiState(
            summary = summary,
            showEditSheet = sheets.showEdit,
            showSellSheet = sheets.showSell,
            showDeleteDialog = sheets.showDelete,
            showValueDialog = sheets.showValue,
            showLoanPicker = sheets.showLoanPicker,
            availableLoans = loans
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        ValuableDetailUiState(isLoading = true)
    )

    fun showEditSheet() {
        _showEditSheet.value = true
    }

    fun hideEditSheet() {
        _showEditSheet.value = false
    }

    fun showSellSheet() {
        _showSellSheet.value = true
    }

    fun hideSellSheet() {
        _showSellSheet.value = false
    }

    fun showDeleteDialog() {
        _showDeleteDialog.value = true
    }

    fun hideDeleteDialog() {
        _showDeleteDialog.value = false
    }

    fun showValueDialog() {
        _showValueDialog.value = true
    }

    fun hideValueDialog() {
        _showValueDialog.value = false
    }

    fun showLoanPicker() {
        viewModelScope.launch {
            val summary = uiState.value.summary ?: return@launch
            getLoansByAccount(summary.valuable.accountId).firstOrNull()?.let {
                _availableLoans.value = it
            }
            _showLoanPicker.value = true
        }
    }

    fun hideLoanPicker() {
        _showLoanPicker.value = false
    }

    fun saveValuable(
        valuable: Valuable,
        purchaseExpenses: List<ValuableExpense>,
        holdingExpenses: List<ValuableExpense>
    ) {
        viewModelScope.launch {
            saveValuableUseCase(valuable, purchaseExpenses, holdingExpenses)
                .onSuccess { hideEditSheet() }
        }
    }

    fun sellValuable(saleDate: Long, salePrice: Double, saleExpenses: List<ValuableExpense>) {
        viewModelScope.launch {
            val summary = uiState.value.summary ?: return@launch
            sellValuableUseCase(
                valuableId = summary.valuable.id,
                saleDate = saleDate,
                salePrice = salePrice,
                saleExpenses = saleExpenses,
                accountId = summary.valuable.accountId,
                valuableName = summary.valuable.name
            ).onSuccess { hideSellSheet() }
        }
    }

    fun updateEstimatedValue(newValue: Double) {
        viewModelScope.launch {
            updateEstimatedValue(valuableId, newValue)
        }
    }

    fun linkLoan(loanId: String) {
        viewModelScope.launch {
            linkLoanToValuable(valuableId, loanId)
        }
    }

    fun deleteValuable() {
        viewModelScope.launch {
            deleteValuableUseCase(valuableId)
                .onSuccess { hideDeleteDialog() }
        }
    }
}
