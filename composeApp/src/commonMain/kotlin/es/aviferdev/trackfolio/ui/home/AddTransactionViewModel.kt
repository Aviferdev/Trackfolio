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
import es.aviferdev.trackfolio.domain.model.Transaction
import es.aviferdev.trackfolio.domain.model.TransactionType
import es.aviferdev.trackfolio.domain.usecase.category.GetCategoriesByTypeUseCase
import es.aviferdev.trackfolio.domain.usecase.issuer.GetIssuersUseCase
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
    var dateMillis by mutableStateOf(Clock.System.now().toEpochMilliseconds())
        private set

    // ── Gastos: categorías ────────────────────────────────────────────────────
    var categories by mutableStateOf<List<Category>>(emptyList())
        private set
    var selectedCategoryId by mutableStateOf("")
        private set

    // ── Ingresos: tipo de ingreso ─────────────────────────────────────────────
    var selectedIncomeType by mutableStateOf<IncomeType?>(null)
        private set

    // ── Modo de entrada de ingresos ──────────────────────────────────────────
    var incomeInputMode by mutableStateOf(IncomeInputMode.FISCAL)
        private set
    var netAmount by mutableStateOf("")
        private set

    // ── Campos fiscales de ingreso ────────────────────────────────────────────
    var grossAmount by mutableStateOf("")
        private set
    var irpfPercent by mutableStateOf("")
        private set
    var irpfFixedAmount by mutableStateOf("")
        private set
    var irpfInputMode by mutableStateOf(IrpfInputMode.PERCENT)
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

    /** Calcula la retención IRPF según el modo de entrada (porcentual o fijo). */
    fun resolveIrpf(gross: Double, ssDeduction: Double = 0.0): Double {
        return when (irpfInputMode) {
            IrpfInputMode.PERCENT -> {
                val pct = irpfPercent.replace(',', '.').toDoubleOrNull() ?: 0.0
                val base = gross - ssDeduction
                base * pct / 100.0
            }
            IrpfInputMode.AMOUNT -> {
                irpfFixedAmount.replace(',', '.').toDoubleOrNull() ?: 0.0
            }
        }
    }

    /** Porcentaje efectivo de IRPF (para persistir siempre como %). */
    fun resolveIrpfPercent(gross: Double, ssDeduction: Double = 0.0): Double {
        return when (irpfInputMode) {
            IrpfInputMode.PERCENT -> irpfPercent.replace(',', '.').toDoubleOrNull() ?: 0.0
            IrpfInputMode.AMOUNT -> {
                val base = gross - ssDeduction
                val fixed = irpfFixedAmount.replace(',', '.').toDoubleOrNull() ?: 0.0
                if (base > 0) (fixed / base) * 100.0 else 0.0
            }
        }
    }

    /** Neto calculado según el tipo de ingreso seleccionado. */
    val calculatedNet: Double?
        get() {
            val it = selectedIncomeType ?: return null
            val gross = grossAmount.replace(',', '.').toDoubleOrNull() ?: return null
            if (gross <= 0) return null

            return when (it) {
                IncomeType.SALARY -> {
                    val ss   = socialSecurityAmount.replace(',', '.').toDoubleOrNull() ?: 0.0
                    val irpf = resolveIrpf(gross, ssDeduction = ss)
                    gross - ss - irpf
                }
                IncomeType.BOND_DEPOSIT -> {
                    val comm = commissionAmount.replace(',', '.').toDoubleOrNull() ?: 0.0
                    val irpf = resolveIrpf(gross)
                    gross - irpf - comm
                }
                IncomeType.EXEMPT_INCOME -> gross  // Sin retenciones
                else -> {
                    // BANK_INTEREST, DIVIDEND, BONUS_PRIZE: bruto - IRPF
                    val irpf = resolveIrpf(gross)
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

            // Modo solo neto
            if (incomeInputMode == IncomeInputMode.NET_ONLY) {
                return netAmount.replace(',', '.').toDoubleOrNull()?.let { it > 0 } == true
                        && selectedIssuerId != null
            }

            // Modo fiscal
            val gross = grossAmount.replace(',', '.').toDoubleOrNull()
            if (incType == IncomeType.EXEMPT_INCOME) {
                // Solo necesita importe
                return gross != null && gross > 0
            }
            // Para tipos con IRPF
            val irpfOk = when (irpfInputMode) {
                IrpfInputMode.PERCENT -> {
                    val pct = irpfPercent.replace(',', '.').toDoubleOrNull()
                    pct != null && pct >= 0
                }
                IrpfInputMode.AMOUNT -> {
                    val fixed = irpfFixedAmount.replace(',', '.').toDoubleOrNull()
                    fixed != null && fixed >= 0
                }
            }
            val grossOk = gross != null && gross > 0
            // Emisor requerido si el tipo lo necesita
            val issuerOk = selectedIssuerId != null
            return grossOk && irpfOk && issuerOk
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
        // Resetear modo de entrada
        incomeInputMode = IncomeInputMode.FISCAL
        netAmount = ""
        // Limpiar campos que no aplican
        if (!incomeType.hasSocialSecurity) socialSecurityAmount = ""
        if (!incomeType.hasCommission) commissionAmount = ""
        if (!incomeType.hasIrpf) { irpfPercent = ""; grossAmount = "" }
        // Resetear modo IRPF y sugerir porcentaje por defecto
        irpfInputMode = IrpfInputMode.PERCENT
        irpfFixedAmount = ""
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
    fun onIrpfFixedAmountChange(value: String) { irpfFixedAmount = filterDecimal(value) }
    fun onIrpfInputModeChange(mode: IrpfInputMode) {
        irpfInputMode = mode
        // Limpiar el campo del modo contrario para evitar confusión
        when (mode) {
            IrpfInputMode.PERCENT -> irpfFixedAmount = ""
            IrpfInputMode.AMOUNT  -> irpfPercent = ""
        }
    }
    fun onSocialSecurityChange(value: String) { socialSecurityAmount = filterDecimal(value) }
    fun onCommissionChange(value: String) { commissionAmount = filterDecimal(value) }
    fun onCategoryChange(categoryId: String) { selectedCategoryId = categoryId }
    fun onNotesChange(value: String) { notes = value }
    fun onDateChange(millis: Long) { dateMillis = millis }

    fun onIncomeModeChange(mode: IncomeInputMode) {
        incomeInputMode = mode
        if (mode == IncomeInputMode.NET_ONLY) {
            grossAmount = ""
            irpfPercent = ""
            irpfFixedAmount = ""
            socialSecurityAmount = ""
            commissionAmount = ""
        } else {
            netAmount = ""
        }
    }
    fun onNetAmountChange(value: String) { netAmount = filterDecimal(value) }

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
        dateMillis = transaction.date

        if (transaction.isIncome) {
            selectedIncomeType       = transaction.incomeType
            incomeInputMode          = if (transaction.isNetOnlyIncome) IncomeInputMode.NET_ONLY else IncomeInputMode.FISCAL
            netAmount                = if (transaction.isNetOnlyIncome) formatAmountForEdit(transaction.amount) else ""
            grossAmount              = formatAmountForEdit(transaction.grossAmount)
            irpfPercent              = formatAmountForEdit(transaction.irpfPercent)
            socialSecurityAmount     = formatAmountForEdit(transaction.socialSecurityAmount)
            commissionAmount         = formatAmountForEdit(transaction.commissionAmount)
            selectedIssuerId         = transaction.issuerId
            amount                   = ""
            selectedCategoryId       = ""
            transaction.incomeType?.let { loadIssuersForType(it) }
        } else {
            clearIncomeFields()
            amount = formatAmountForEdit(transaction.amount)
            loadCategoriesAndSelect(transaction.type, transaction.categoryId)
        }
    }

    fun resetForCreate() {
        editingTransaction = null
        amount             = ""
        type               = TransactionType.EXPENSE
        notes              = ""
        dateMillis         = Clock.System.now().toEpochMilliseconds()
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

        // Modo solo neto
        if (incomeInputMode == IncomeInputMode.NET_ONLY) {
            val net = netAmount.replace(',', '.').toDoubleOrNull() ?: run {
                _uiState.value = AddTransactionUiState.Error("Importe neto inválido")
                return
            }
            val finalIssuerName = issuers.find { it.id == selectedIssuerId }?.name
            val transaction = buildTransaction(
                accountId       = accountId,
                netAmount       = net,
                now             = now,
                incomeType      = incType,
                issuerId        = selectedIssuerId,
                issuerName      = finalIssuerName,
                isNetOnlyIncome = true
            )
            persistTransaction(transaction)
            return
        }

        val finalIssuerId: String? = selectedIssuerId
        var finalIssuerName: String? = null

        if (finalIssuerId != null) {
            finalIssuerName = issuers.find { it.id == finalIssuerId }?.name
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
            irpfPercent          = if (incType.hasIrpf) {
                val gross = grossAmount.replace(',', '.').toDoubleOrNull() ?: 0.0
                val ss = if (incType.hasSocialSecurity) socialSecurityAmount.replace(',', '.').toDoubleOrNull() ?: 0.0 else 0.0
                resolveIrpfPercent(gross, ss)
            } else null,
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
        issuerName: String? = null,
        isNetOnlyIncome: Boolean = false
    ): Transaction {
        val existing = editingTransaction
        return Transaction(
            id                   = existing?.id ?: uuid4().toString(),
            accountId            = accountId,
            amount               = netAmount,
            type                 = type,
            categoryId           = if (type == TransactionType.EXPENSE) selectedCategoryId else null,
            date                 = dateMillis,
            notes                = notes.ifBlank { null },
            createdAt            = existing?.createdAt ?: now,
            isNetOnlyIncome      = isNetOnlyIncome,
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
        incomeInputMode      = IncomeInputMode.FISCAL
        netAmount            = ""
        grossAmount          = ""
        irpfPercent          = ""
        irpfFixedAmount      = ""
        irpfInputMode        = IrpfInputMode.PERCENT
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
        session.selectedAccountId.value?.let { selectedAccountNotNull ->
            issuerJob?.cancel()
            issuerJob = getIssuers(selectedAccountNotNull, incomeType.issuerType)
                .onEach { list ->
                    issuers = list
                    // Auto-seleccionar si la edición tiene issuerId
                    if (selectedIssuerId != null && list.none { it.id == selectedIssuerId }) {
                        selectedIssuerId = null
                    }
                }
                .launchIn(viewModelScope)
        }
    }

    /** Formatea un importe Double para el campo de edición: redondea a 2 decimales, usa coma. */
    private fun formatAmountForEdit(amount: Double?): String {
        if (amount == null) return ""
        val abs = kotlin.math.abs(amount)
        val rounded = (abs * 100 + 0.5).toLong()
        val euros = rounded / 100
        val cents = rounded % 100
        val sign = if (amount < 0) "-" else ""
        return "$sign$euros,${cents.toString().padStart(2, '0')}"
    }

    private fun filterDecimal(value: String): String =
        value.filter { it.isDigit() || it == ',' || it == '.' }
}

/** Modo de entrada del IRPF: porcentual o importe fijo. */
enum class IrpfInputMode { PERCENT, AMOUNT }

/** Modo de entrada de ingresos: con desglose fiscal o solo neto. */
enum class IncomeInputMode { FISCAL, NET_ONLY }
