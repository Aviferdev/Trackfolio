package es.aviferdev.trackfolio.ui.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.benasher44.uuid.uuid4
import es.aviferdev.trackfolio.domain.model.Category
import es.aviferdev.trackfolio.domain.model.IncomeType
import es.aviferdev.trackfolio.domain.model.Issuer
import es.aviferdev.trackfolio.domain.model.IssuerType
import es.aviferdev.trackfolio.domain.model.Transaction
import es.aviferdev.trackfolio.domain.model.TransactionType
import es.aviferdev.trackfolio.domain.usecase.category.GetCategoriesByTypeUseCase
import es.aviferdev.trackfolio.domain.usecase.issuer.GetIssuersUseCase
import es.aviferdev.trackfolio.domain.usecase.issuer.SaveIssuerUseCase
import es.aviferdev.trackfolio.domain.usecase.transaction.SaveTransactionUseCase
import es.aviferdev.trackfolio.domain.usecase.transaction.UpdateTransactionUseCase
import es.aviferdev.trackfolio.ui.account.AccountSession
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

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
    private val getIssuers: GetIssuersUseCase,
    private val saveIssuer: SaveIssuerUseCase,
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
    var notes by mutableStateOf("")
        private set

    // ── Gastos: categorías ────────────────────────────────────────────────────
    var categories by mutableStateOf<List<Category>>(emptyList())
        private set
    var selectedCategoryId by mutableStateOf("")
        private set

    // ── Ingresos: tipo de ingreso ─────────────────────────────────────────────
    var selectedIncomeType by mutableStateOf<IncomeType?>(null)
        private set

    // ── Campos fiscales de ingreso ────────────────────────────────────────────
    var grossAmount by mutableStateOf("")
        private set
    var irpfPercent by mutableStateOf("")
        private set
    var socialSecurityAmount by mutableStateOf("")
        private set
    var commissionAmount by mutableStateOf("")
        private set

    // ── Emisor ────────────────────────────────────────────────────────────────
    var issuers by mutableStateOf<List<Issuer>>(emptyList())
        private set
    var selectedIssuerId by mutableStateOf<String?>(null)
        private set
    var newIssuerName by mutableStateOf("")
        private set
    var showNewIssuerField by mutableStateOf(false)
        private set

    private var issuerJob: Job? = null

    // ── Cálculos ──────────────────────────────────────────────────────────────

    /** Neto calculado según el tipo de ingreso seleccionado. */
    val calculatedNet: Double?
        get() {
            val it = selectedIncomeType ?: return null
            val gross = grossAmount.replace(',', '.').toDoubleOrNull() ?: return null
            if (gross <= 0) return null

            return when (it) {
                IncomeType.SALARY -> {
                    val ss  = socialSecurityAmount.replace(',', '.').toDoubleOrNull() ?: 0.0
                    val pct = irpfPercent.replace(',', '.').toDoubleOrNull() ?: 0.0
                    val baseIrpf = gross - ss
                    val irpf = baseIrpf * pct / 100.0
                    gross - ss - irpf
                }
                IncomeType.BOND_DEPOSIT -> {
                    val pct  = irpfPercent.replace(',', '.').toDoubleOrNull() ?: 0.0
                    val comm = commissionAmount.replace(',', '.').toDoubleOrNull() ?: 0.0
                    val irpf = gross * pct / 100.0
                    gross - irpf - comm
                }
                IncomeType.EXEMPT_INCOME -> gross  // Sin retenciones
                else -> {
                    // BANK_INTEREST, DIVIDEND, BONUS_PRIZE: bruto - IRPF
                    val pct = irpfPercent.replace(',', '.').toDoubleOrNull() ?: 0.0
                    val irpf = gross * pct / 100.0
                    gross - irpf
                }
            }
        }

    // ── Validación ────────────────────────────────────────────────────────────
    val isValid: Boolean
        get() {
            if (type == TransactionType.EXPENSE) {
                val amtOk = amount.replace(',', '.').toDoubleOrNull()?.let { it > 0 } == true
                return amtOk && selectedCategoryId.isNotEmpty()
            }
            // INCOME
            val incType = selectedIncomeType ?: return false
            val gross = grossAmount.replace(',', '.').toDoubleOrNull()
            if (incType == IncomeType.EXEMPT_INCOME) {
                // Solo necesita importe
                return gross != null && gross > 0
            }
            // Para tipos con IRPF
            val pct = irpfPercent.replace(',', '.').toDoubleOrNull()
            val grossOk = gross != null && gross > 0
            val pctOk   = pct != null && pct >= 0
            // Emisor requerido si el tipo lo necesita
            val issuerOk = if (incType.issuerType != null) {
                selectedIssuerId != null || newIssuerName.isNotBlank()
            } else true
            return grossOk && pctOk && issuerOk
        }

    init { loadCategories() }

    // ── Acciones de tipo ──────────────────────────────────────────────────────

    fun onTypeChange(newType: TransactionType) {
        type = newType
        if (newType == TransactionType.EXPENSE) {
            clearIncomeFields()
            loadCategories()
        } else {
            categories = emptyList()
            selectedCategoryId = ""
        }
    }

    fun onIncomeTypeChange(incomeType: IncomeType) {
        selectedIncomeType = incomeType
        // Limpiar campos que no aplican
        if (!incomeType.hasSocialSecurity) socialSecurityAmount = ""
        if (!incomeType.hasCommission) commissionAmount = ""
        if (!incomeType.hasIrpf) { irpfPercent = ""; grossAmount = "" }
        // Sugerir porcentaje por defecto
        if (irpfPercent.isBlank() && incomeType.defaultIrpfPercent != null) {
            irpfPercent = incomeType.defaultIrpfPercent.toString().replace('.', ',')
        }
        // Cargar emisores del tipo correspondiente
        selectedIssuerId = null
        newIssuerName = ""
        showNewIssuerField = false
        loadIssuersForType(incomeType)
    }

    // ── Acciones de campos ────────────────────────────────────────────────────

    fun onAmountChange(value: String) { amount = filterDecimal(value) }
    fun onGrossAmountChange(value: String) { grossAmount = filterDecimal(value) }
    fun onIrpfPercentChange(value: String) { irpfPercent = filterDecimal(value) }
    fun onSocialSecurityChange(value: String) { socialSecurityAmount = filterDecimal(value) }
    fun onCommissionChange(value: String) { commissionAmount = filterDecimal(value) }
    fun onCategoryChange(categoryId: String) { selectedCategoryId = categoryId }
    fun onNotesChange(value: String) { notes = value }

    fun onIssuerSelected(issuerId: String) {
        selectedIssuerId = issuerId
        showNewIssuerField = false
        newIssuerName = ""
    }

    fun onNewIssuerToggle() {
        showNewIssuerField = !showNewIssuerField
        if (showNewIssuerField) selectedIssuerId = null
    }

    fun onNewIssuerNameChange(value: String) { newIssuerName = value }

    // ── Edición ───────────────────────────────────────────────────────────────

    fun loadForEdit(transaction: Transaction) {
        editingTransaction = transaction
        type  = transaction.type
        notes = transaction.notes ?: ""

        if (transaction.isIncome) {
            selectedIncomeType       = transaction.incomeType
            grossAmount              = transaction.grossAmount?.toString()?.replace('.', ',') ?: ""
            irpfPercent              = transaction.irpfPercent?.toString()?.replace('.', ',') ?: ""
            socialSecurityAmount     = transaction.socialSecurityAmount?.toString()?.replace('.', ',') ?: ""
            commissionAmount         = transaction.commissionAmount?.toString()?.replace('.', ',') ?: ""
            selectedIssuerId         = transaction.issuerId
            amount                   = ""
            selectedCategoryId       = ""
            transaction.incomeType?.let { loadIssuersForType(it) }
        } else {
            clearIncomeFields()
            amount = transaction.amount.toString().replace('.', ',')
            loadCategoriesAndSelect(transaction.type, transaction.categoryId)
        }
    }

    fun resetForCreate() {
        editingTransaction = null
        amount             = ""
        type               = TransactionType.EXPENSE
        notes              = ""
        clearIncomeFields()
        loadCategories()
    }

    // ── Guardar ───────────────────────────────────────────────────────────────

    fun save() {
        if (!isValid) return
        _uiState.value = AddTransactionUiState.Loading
        viewModelScope.launch {
            val accountId = session.selectedAccountId.value ?: run {
                _uiState.value = AddTransactionUiState.Error("No hay cuenta seleccionada")
                return@launch
            }
            val now = Clock.System.now().toEpochMilliseconds()

            if (type == TransactionType.EXPENSE) {
                saveExpense(accountId, now)
            } else {
                saveIncome(accountId, now)
            }
        }
    }

    private suspend fun saveExpense(accountId: String, now: Long) {
        val netAmount = amount.replace(',', '.').toDouble()
        val transaction = buildTransaction(
            accountId = accountId,
            netAmount = netAmount,
            now       = now
        )
        persistTransaction(transaction)
    }

    private suspend fun saveIncome(accountId: String, now: Long) {
        val incType = selectedIncomeType!!

        // Resolver emisor (crear nuevo si es necesario)
        var finalIssuerId: String? = selectedIssuerId
        var finalIssuerName: String? = null

        if (incType.issuerType != null) {
            if (showNewIssuerField && newIssuerName.isNotBlank()) {
                val newId = uuid4().toString()
                val newIssuer = Issuer(
                    id        = newId,
                    accountId = accountId,
                    name      = newIssuerName.trim(),
                    type      = incType.issuerType,
                    createdAt = Clock.System.now().toEpochMilliseconds()
                )
                saveIssuer(newIssuer).onFailure {
                    _uiState.value = AddTransactionUiState.Error("Error al crear emisor: ${it.message}")
                    return
                }
                finalIssuerId   = newId
                finalIssuerName = newIssuerName.trim()
            } else {
                finalIssuerName = issuers.find { it.id == finalIssuerId }?.name
            }
        }

        val net = calculatedNet ?: run {
            _uiState.value = AddTransactionUiState.Error("No se pudo calcular el neto")
            return
        }

        val transaction = buildTransaction(
            accountId            = accountId,
            netAmount            = net,
            now                  = now,
            incomeType           = incType,
            grossAmount          = grossAmount.replace(',', '.').toDoubleOrNull(),
            irpfPercent          = if (incType.hasIrpf) irpfPercent.replace(',', '.').toDoubleOrNull() else null,
            socialSecurityAmount = if (incType.hasSocialSecurity) socialSecurityAmount.replace(',', '.').toDoubleOrNull() else null,
            commissionAmount     = if (incType.hasCommission) commissionAmount.replace(',', '.').toDoubleOrNull() else null,
            issuerId             = finalIssuerId,
            issuerName           = finalIssuerName
        )
        persistTransaction(transaction)
    }

    private fun buildTransaction(
        accountId: String,
        netAmount: Double,
        now: Long,
        incomeType: IncomeType? = null,
        grossAmount: Double? = null,
        irpfPercent: Double? = null,
        socialSecurityAmount: Double? = null,
        commissionAmount: Double? = null,
        issuerId: String? = null,
        issuerName: String? = null
    ): Transaction {
        val existing = editingTransaction
        return Transaction(
            id                   = existing?.id ?: uuid4().toString(),
            accountId            = accountId,
            amount               = netAmount,
            type                 = type,
            categoryId           = if (type == TransactionType.EXPENSE) selectedCategoryId else null,
            date                 = existing?.date ?: now,
            notes                = notes.ifBlank { null },
            createdAt            = existing?.createdAt ?: now,
            incomeType           = incomeType,
            grossAmount          = grossAmount,
            irpfPercent          = irpfPercent,
            socialSecurityAmount = socialSecurityAmount,
            commissionAmount     = commissionAmount,
            issuerId             = issuerId,
            issuerName           = issuerName
        )
    }

    private suspend fun persistTransaction(transaction: Transaction) {
        val result = if (editingTransaction != null) {
            updateTransaction(transaction)
        } else {
            saveTransaction(transaction)
        }
        result
            .onSuccess { _uiState.value = AddTransactionUiState.Success }
            .onFailure { _uiState.value = AddTransactionUiState.Error(it.message ?: "Error") }
    }

    fun clear() { _uiState.value = AddTransactionUiState.Idle }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun clearIncomeFields() {
        selectedIncomeType   = null
        grossAmount          = ""
        irpfPercent          = ""
        socialSecurityAmount = ""
        commissionAmount     = ""
        selectedIssuerId     = null
        newIssuerName        = ""
        showNewIssuerField   = false
        issuers              = emptyList()
    }

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

    private fun loadIssuersForType(incomeType: IncomeType) {
        val issuerType = incomeType.issuerType ?: run {
            issuers = emptyList()
            return
        }
        val accountId = session.selectedAccountId.value ?: return
        issuerJob?.cancel()
        issuerJob = getIssuers(accountId, issuerType)
            .onEach { list ->
                issuers = list
                // Auto-seleccionar si la edición tiene issuerId
                if (selectedIssuerId != null && list.none { it.id == selectedIssuerId }) {
                    selectedIssuerId = null
                }
            }
            .launchIn(viewModelScope)
    }

    private fun filterDecimal(value: String): String =
        value.filter { it.isDigit() || it == ',' || it == '.' }
}
