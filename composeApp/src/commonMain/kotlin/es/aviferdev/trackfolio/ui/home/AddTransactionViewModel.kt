package es.aviferdev.trackfolio.ui.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.aviferdev.trackfolio.domain.model.Category
import es.aviferdev.trackfolio.domain.model.IncomeTaxType
import es.aviferdev.trackfolio.domain.model.Transaction
import es.aviferdev.trackfolio.domain.model.TransactionType
import es.aviferdev.trackfolio.domain.usecase.category.GetCategoriesByTypeUseCase
import es.aviferdev.trackfolio.domain.usecase.transaction.SaveTransactionUseCase
import es.aviferdev.trackfolio.domain.usecase.transaction.UpdateTransactionUseCase
import es.aviferdev.trackfolio.ui.account.AccountSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlin.math.abs

sealed class AddTransactionUiState {
    data object Idle    : AddTransactionUiState()
    data object Loading : AddTransactionUiState()
    data object Success : AddTransactionUiState()
    data class Error(val message: String) : AddTransactionUiState()
}

class AddTransactionViewModel(
    private val saveTransaction: SaveTransactionUseCase,
    private val updateTransaction: UpdateTransactionUseCase,
    private val getCategoriesByType: GetCategoriesByTypeUseCase,
    private val session: AccountSession
) : ViewModel() {

    private val _uiState = MutableStateFlow<AddTransactionUiState>(AddTransactionUiState.Idle)
    val uiState: StateFlow<AddTransactionUiState> = _uiState.asStateFlow()

    private var editingTransaction: Transaction? = null
    val isEditing: Boolean get() = editingTransaction != null

    // ── Campos base ───────────────────────────────────────────────────────────
    var amount by mutableStateOf("")
        private set
    var type by mutableStateOf(TransactionType.EXPENSE)
        private set
    var categories by mutableStateOf<List<Category>>(emptyList())
        private set
    var selectedCategoryId by mutableStateOf("")
        private set
    var notes by mutableStateOf("")
        private set

    // ── Campos fiscales (solo INCOME) ─────────────────────────────────────────
    /** ¿El usuario quiere añadir información fiscal a este ingreso? */
    var showFiscalFields by mutableStateOf(false)
        private set
    /** Importe bruto (antes de retención). Vacío = sin datos. */
    var grossAmount by mutableStateOf("")
        private set
    /** Porcentaje de IRPF retenido. */
    var irpfPercent by mutableStateOf("")
        private set
    /** Tipo de rendimiento seleccionado. */
    var selectedTaxType by mutableStateOf<IncomeTaxType?>(null)
        private set

    /**
     * Importe neto calculado a partir del bruto y el porcentaje de IRPF.
     * Si el usuario no ha introducido datos fiscales, es null.
     */
    val calculatedNet: Double?
        get() {
            if (!showFiscalFields) return null
            val gross = grossAmount.replace(',', '.').toDoubleOrNull() ?: return null
            val pct   = irpfPercent.replace(',', '.').toDoubleOrNull() ?: return null
            return gross * (1.0 - pct / 100.0)
        }

    // ── Validación ────────────────────────────────────────────────────────────
    val isValid: Boolean
        get() {
            val baseOk = selectedCategoryId.isNotEmpty()
            if (type == TransactionType.INCOME && showFiscalFields) {
                // En modo fiscal el campo principal es el bruto
                val gross = grossAmount.replace(',', '.').toDoubleOrNull()
                val pct   = irpfPercent.replace(',', '.').toDoubleOrNull()
                return baseOk
                    && gross != null && gross > 0.0
                    && pct   != null && pct   >= 0.0
                    && selectedTaxType != null
            }
            val amtOk = amount.replace(',', '.').toDoubleOrNull()?.let { it > 0 } == true
            return baseOk && amtOk
        }

    init { loadCategories() }

    fun loadForEdit(transaction: Transaction) {
        editingTransaction = transaction
        type  = transaction.type
        notes = transaction.notes ?: ""

        if (transaction.grossAmount != null && transaction.taxType != null) {
            showFiscalFields  = true
            grossAmount       = transaction.grossAmount.toString().replace('.', ',')
            irpfPercent       = (transaction.irpfPercent ?: 0.0).toString().replace('.', ',')
            selectedTaxType   = transaction.taxType
            amount            = "" // calculado; no se muestra directamente
        } else {
            showFiscalFields = false
            grossAmount      = ""
            irpfPercent      = ""
            selectedTaxType  = null
            amount           = transaction.amount.toString().replace('.', ',')
        }
        loadCategoriesAndSelect(transaction.type, transaction.categoryId)
    }

    fun resetForCreate() {
        editingTransaction = null
        amount             = ""
        type               = TransactionType.EXPENSE
        notes              = ""
        showFiscalFields   = false
        grossAmount        = ""
        irpfPercent        = ""
        selectedTaxType    = null
        loadCategories()
    }

    fun onAmountChange(value: String) {
        amount = value.filter { it.isDigit() || it == ',' || it == '.' }
    }

    fun onTypeChange(newType: TransactionType) {
        type = newType
        if (newType == TransactionType.EXPENSE) {
            showFiscalFields = false
            grossAmount      = ""
            irpfPercent      = ""
            selectedTaxType  = null
        }
        loadCategories()
    }

    fun onCategoryChange(categoryId: String) { selectedCategoryId = categoryId }
    fun onNotesChange(value: String) { notes = value }

    fun onToggleFiscalFields(enabled: Boolean) {
        showFiscalFields = enabled
        if (!enabled) {
            grossAmount     = ""
            irpfPercent     = ""
            selectedTaxType = null
        }
    }

    fun onGrossAmountChange(value: String) {
        grossAmount = value.filter { it.isDigit() || it == ',' || it == '.' }
    }

    fun onIrpfPercentChange(value: String) {
        irpfPercent = value.filter { it.isDigit() || it == ',' || it == '.' }
    }

    fun onTaxTypeChange(taxType: IncomeTaxType) {
        selectedTaxType = taxType
        // Sugiere el porcentaje por defecto del tipo seleccionado si el campo está vacío
        if (irpfPercent.isBlank() && taxType.defaultIrpfPercent != null) {
            irpfPercent = taxType.defaultIrpfPercent.toString().replace('.', ',')
        }
    }

    fun save() {
        if (!isValid) return
        _uiState.value = AddTransactionUiState.Loading
        viewModelScope.launch {
            val accountId = session.selectedAccountId.value ?: run {
                _uiState.value = AddTransactionUiState.Error("No hay cuenta seleccionada")
                return@launch
            }
            val now = Clock.System.now().toEpochMilliseconds()

            // Determinar importe neto final
            val netAmount: Double
            val finalGross: Double?
            val finalIrpfPct: Double?
            val finalTaxType: IncomeTaxType?

            if (type == TransactionType.INCOME && showFiscalFields) {
                val gross = grossAmount.replace(',', '.').toDouble()
                val pct   = irpfPercent.replace(',', '.').toDouble()
                netAmount      = gross * (1.0 - pct / 100.0)
                finalGross     = gross
                finalIrpfPct   = pct
                finalTaxType   = selectedTaxType
            } else {
                netAmount    = amount.replace(',', '.').toDouble()
                finalGross   = null
                finalIrpfPct = null
                finalTaxType = null
            }

            val existing = editingTransaction
            if (existing != null) {
                val updated = existing.copy(
                    amount      = netAmount,
                    type        = type,
                    categoryId  = selectedCategoryId,
                    notes       = notes.ifBlank { null },
                    grossAmount = finalGross,
                    irpfPercent = finalIrpfPct,
                    taxType     = finalTaxType
                )
                updateTransaction(updated)
                    .onSuccess { _uiState.value = AddTransactionUiState.Success }
                    .onFailure { _uiState.value = AddTransactionUiState.Error(it.message ?: "Error") }
            } else {
                val transaction = Transaction(
                    id          = generateId(),
                    accountId   = accountId,
                    amount      = netAmount,
                    type        = type,
                    categoryId  = selectedCategoryId,
                    date        = now,
                    notes       = notes.ifBlank { null },
                    createdAt   = now,
                    grossAmount = finalGross,
                    irpfPercent = finalIrpfPct,
                    taxType     = finalTaxType
                )
                saveTransaction(transaction)
                    .onSuccess { _uiState.value = AddTransactionUiState.Success }
                    .onFailure { _uiState.value = AddTransactionUiState.Error(it.message ?: "Error") }
            }
        }
    }

    fun clear() { _uiState.value = AddTransactionUiState.Idle }

    private fun loadCategories() { loadCategoriesAndSelect(type, null) }

    private fun loadCategoriesAndSelect(forType: TransactionType, selectId: String?) {
        getCategoriesByType(forType)
            .onEach { list ->
                categories = list
                selectedCategoryId = if (selectId != null && list.any { it.id == selectId })
                    selectId
                else
                    list.firstOrNull()?.id ?: ""
            }
            .launchIn(viewModelScope)
    }

    private fun generateId(): String {
        val chars = "abcdefghijklmnopqrstuvwxyz0123456789"
        return "tx_" + (1..29).map { chars.random() }.joinToString("")
    }
}
