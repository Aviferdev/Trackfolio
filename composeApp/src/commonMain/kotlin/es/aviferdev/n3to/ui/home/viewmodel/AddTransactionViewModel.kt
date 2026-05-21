package es.aviferdev.n3to.ui.home.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.benasher44.uuid.uuid4
import es.aviferdev.n3to.domain.model.Category
import es.aviferdev.n3to.domain.model.IncomeType
import es.aviferdev.n3to.domain.model.Issuer
import es.aviferdev.n3to.domain.model.TaxLine
import es.aviferdev.n3to.domain.model.TaxProfileSnapshot
import es.aviferdev.n3to.domain.model.TaxRole
import es.aviferdev.n3to.domain.model.Transaction
import es.aviferdev.n3to.domain.model.TransactionType
import es.aviferdev.n3to.domain.usecase.category.GetCategoriesByTypeUseCase
import es.aviferdev.n3to.domain.usecase.fiscal.CalculateIrpfUseCase
import es.aviferdev.n3to.domain.usecase.fiscal.CalculateNetIncomeUseCase
import es.aviferdev.n3to.domain.usecase.issuer.GetIssuersUseCase
import es.aviferdev.n3to.domain.usecase.taxprofile.GetActiveTaxProfileSnapshotUseCase
import es.aviferdev.n3to.domain.usecase.transaction.SaveTransactionUseCase
import es.aviferdev.n3to.domain.usecase.transaction.UpdateTransactionUseCase
import es.aviferdev.n3to.platform.nowLocalDate
import es.aviferdev.n3to.platform.nowMillis
import es.aviferdev.n3to.ui.account.AccountSession
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.floor

sealed class AddTransactionUiState {
    data object Idle : AddTransactionUiState()
    data object Loading : AddTransactionUiState()
    data object Success : AddTransactionUiState()
    data class Error(val message: String) : AddTransactionUiState()
}

class AddTransactionViewModel(
    private val saveTransaction: SaveTransactionUseCase,
    private val updateTransaction: UpdateTransactionUseCase,
    private val getCategoriesByType: GetCategoriesByTypeUseCase,
    private val getIssuers: GetIssuersUseCase,
    private val getActiveTaxProfile: GetActiveTaxProfileSnapshotUseCase,
    private val calculateIrpf: CalculateIrpfUseCase,
    private val calculateNetIncome: CalculateNetIncomeUseCase,
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
    var dateMillis by mutableStateOf(nowMillis())
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

    // ── Perfil fiscal activo ──────────────────────────────────────────────────
    var activeTaxProfile by mutableStateOf<TaxProfileSnapshot?>(null)
        private set

    /** Nombre de la retención sobre la renta según el perfil activo (IRPF, Income Tax, Federal Tax…). */
    val withholdingTaxLabel: String
        get() {
            val incType = selectedIncomeType ?: return "Retención fiscal"
            val profile = activeTaxProfile ?: return "Retención fiscal"
            return profile.profile.templatesFor(incType)
                .firstOrNull { it.role == TaxRole.INCOME_TAX }
                ?.name ?: "Retención fiscal"
        }

    /** Nombres de las cotizaciones sociales según el perfil activo (Seg. Social, NI, FICA…). */
    val socialContributionLabel: String
        get() {
            val incType = selectedIncomeType ?: return "Cotizaciones sociales"
            val profile = activeTaxProfile ?: return "Cotizaciones sociales"
            val names = profile.profile.templatesFor(incType)
                .filter { it.role == TaxRole.SOCIAL_CONTRIBUTION }
                .map { it.name }
            return if (names.isEmpty()) "Cotizaciones sociales" else names.joinToString(" + ")
        }

    /**
     * Si el perfil activo tiene template de INCOME_TAX para este tipo → muestra campo retención.
     * Si no hay perfil (o es CUSTOM sin templates) → cae al flag del enum.
     */
    val showWithholdingField: Boolean
        get() {
            val incType = selectedIncomeType ?: return false
            val profile = activeTaxProfile
            if (profile == null || profile.profile.countryCode == null) return incType.hasWithholdingTax
            return profile.profile.templatesFor(incType).any { it.role == TaxRole.INCOME_TAX }
        }

    /** Igual que showWithholdingField pero para cotizaciones sociales. */
    val showSocialContributionField: Boolean
        get() {
            val incType = selectedIncomeType ?: return false
            val profile = activeTaxProfile
            if (profile == null || profile.profile.countryCode == null) return incType.hasSocialContribution
            return profile.profile.templatesFor(incType)
                .any { it.role == TaxRole.SOCIAL_CONTRIBUTION }
        }

    // ── Cálculos ──────────────────────────────────────────────────────────────

    /** Neto calculado según el tipo de ingreso seleccionado. */
    val calculatedNet: Double?
        get() {
            val incType = selectedIncomeType ?: return null
            val gross = grossAmount.replace(',', '.').toDoubleOrNull() ?: return null
            if (gross <= 0) return null
            val ss = socialSecurityAmount.replace(',', '.').toDoubleOrNull() ?: 0.0
            val comm = commissionAmount.replace(',', '.').toDoubleOrNull() ?: 0.0
            val isPercentMode = irpfInputMode == IrpfInputMode.PERCENT
            return calculateNetIncome.calculate(
                incType,
                gross,
                ss,
                comm,
                isPercentMode,
                irpfPercent,
                irpfFixedAmount
            )
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

    init {
        loadCategories()
    }

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
        if (!incomeType.hasSocialContribution) socialSecurityAmount = ""
        if (!incomeType.hasCommission) commissionAmount = ""
        if (!incomeType.hasWithholdingTax) {
            irpfPercent = ""; grossAmount = ""
        }
        // Resetear modo IRPF
        irpfInputMode = IrpfInputMode.PERCENT
        irpfFixedAmount = ""
        // Cargar emisores del tipo correspondiente
        selectedIssuerId = null
        newIssuerName = ""
        showNewIssuerField = false
        loadIssuersForType(incomeType)
        // Cargar perfil fiscal activo para labels y pre-relleno de porcentaje
        viewModelScope.launch {
            val snapshot = getActiveTaxProfile(nowLocalDate()) ?: return@launch
            activeTaxProfile = snapshot
            if (incomeType.hasWithholdingTax) {
                val irpfTemplate = snapshot.profile.templatesFor(incomeType)
                    .firstOrNull { it.role == TaxRole.INCOME_TAX }
                if (irpfTemplate?.defaultPercent != null) {
                    irpfPercent = formatPercent(irpfTemplate.defaultPercent)
                }
            }
        }
    }

    // ── Acciones de campos ────────────────────────────────────────────────────

    fun onAmountChange(value: String) {
        amount = filterDecimal(value)
    }

    fun onGrossAmountChange(value: String) {
        grossAmount = filterDecimal(value)
    }

    fun onIrpfPercentChange(value: String) {
        irpfPercent = filterDecimal(value)
    }

    fun onIrpfFixedAmountChange(value: String) {
        irpfFixedAmount = filterDecimal(value)
    }

    fun onIrpfInputModeChange(mode: IrpfInputMode) {
        irpfInputMode = mode
        // Limpiar el campo del modo contrario para evitar confusión
        when (mode) {
            IrpfInputMode.PERCENT -> irpfFixedAmount = ""
            IrpfInputMode.AMOUNT -> irpfPercent = ""
        }
    }

    fun onSocialSecurityChange(value: String) {
        socialSecurityAmount = filterDecimal(value)
    }

    fun onCommissionChange(value: String) {
        commissionAmount = filterDecimal(value)
    }

    fun onCategoryChange(categoryId: String) {
        selectedCategoryId = categoryId
    }

    fun onNotesChange(value: String) {
        notes = value
    }

    fun onDateChange(millis: Long) {
        dateMillis = millis
    }

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

    fun onNetAmountChange(value: String) {
        netAmount = filterDecimal(value)
    }

    fun onIssuerSelected(issuerId: String) {
        selectedIssuerId = issuerId
        showNewIssuerField = false
        newIssuerName = ""
    }

    fun onNewIssuerToggle() {
        showNewIssuerField = !showNewIssuerField
        if (showNewIssuerField) selectedIssuerId = null
    }

    fun onNewIssuerNameChange(value: String) {
        newIssuerName = value
    }

    // ── Edición ───────────────────────────────────────────────────────────────

    fun loadForEdit(transaction: Transaction) {
        editingTransaction = transaction
        type = transaction.type
        notes = transaction.notes ?: ""
        dateMillis = transaction.date

        if (transaction.isIncome) {
            selectedIncomeType = transaction.incomeType
            incomeInputMode =
                if (transaction.isNetOnly) IncomeInputMode.NET_ONLY else IncomeInputMode.FISCAL
            netAmount = if (transaction.isNetOnly) formatAmountForEdit(transaction.amount) else ""
            grossAmount = formatAmountForEdit(transaction.grossAmount)
            irpfPercent =
                formatAmountForEdit(transaction.taxLines.firstOrNull { it.role == TaxRole.INCOME_TAX }?.percent)
            socialSecurityAmount =
                formatAmountForEdit(transaction.taxLines.firstOrNull { it.role == TaxRole.SOCIAL_CONTRIBUTION }?.amount)
            commissionAmount = formatAmountForEdit(transaction.commissionAmount)
            selectedIssuerId = transaction.issuerId
            amount = ""
            selectedCategoryId = ""
            transaction.incomeType?.let { loadIssuersForType(it) }
            viewModelScope.launch {
                activeTaxProfile = getActiveTaxProfile(nowLocalDate())
            }
        } else {
            clearIncomeFields()
            amount = formatAmountForEdit(transaction.amount)
            loadCategoriesAndSelect(transaction.type, transaction.categoryId)
        }
    }

    fun resetForCreate() {
        editingTransaction = null
        amount = ""
        type = TransactionType.EXPENSE
        notes = ""
        dateMillis = nowMillis()
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
            val now = nowMillis()

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
            now = now
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
                accountId = accountId,
                netAmount = net,
                now = now,
                incomeType = incType,
                issuerId = selectedIssuerId,
                issuerName = finalIssuerName
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

        val gross = grossAmount.replace(',', '.').toDoubleOrNull()
        val builtTaxLines = buildList {
            if (incType.hasWithholdingTax && gross != null) {
                val ssVal =
                    if (incType.hasSocialContribution) socialSecurityAmount.replace(',', '.')
                        .toDoubleOrNull() ?: 0.0 else 0.0
                val pct = calculateIrpf.resolvePercent(
                    gross,
                    ssVal,
                    irpfInputMode == IrpfInputMode.PERCENT,
                    irpfPercent,
                    irpfFixedAmount
                )
                add(
                    TaxLine(
                        name = "Retención",
                        role = TaxRole.INCOME_TAX,
                        percent = pct,
                        amount = gross * pct / 100.0
                    )
                )
            }
            if (incType.hasSocialContribution) {
                val ssVal = socialSecurityAmount.replace(',', '.').toDoubleOrNull()
                if (ssVal != null && ssVal > 0) add(
                    TaxLine(
                        name = "Cotización Social",
                        role = TaxRole.SOCIAL_CONTRIBUTION,
                        percent = null,
                        amount = ssVal
                    )
                )
            }
        }
        val transaction = buildTransaction(
            accountId = accountId,
            netAmount = net,
            now = now,
            incomeType = incType,
            grossAmount = gross,
            taxLines = builtTaxLines,
            commissionAmount = if (incType.hasCommission) commissionAmount.replace(',', '.')
                .toDoubleOrNull() else null,
            issuerId = finalIssuerId,
            issuerName = finalIssuerName
        )
        persistTransaction(transaction)
    }

    private fun buildTransaction(
        accountId: String,
        netAmount: Double,
        now: Long,
        incomeType: IncomeType? = null,
        grossAmount: Double? = null,
        taxLines: List<TaxLine> = emptyList(),
        commissionAmount: Double? = null,
        issuerId: String? = null,
        issuerName: String? = null
    ): Transaction {
        val existing = editingTransaction
        return Transaction(
            id = existing?.id ?: uuid4().toString(),
            accountId = accountId,
            amount = netAmount,
            type = type,
            categoryId = if (type == TransactionType.EXPENSE) selectedCategoryId else null,
            date = dateMillis,
            notes = notes.ifBlank { null },
            createdAt = existing?.createdAt ?: now,
            incomeType = incomeType,
            grossAmount = grossAmount,
            taxLines = taxLines,
            commissionAmount = commissionAmount,
            issuerId = issuerId,
            issuerName = issuerName
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

    fun clear() {
        _uiState.value = AddTransactionUiState.Idle
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun clearIncomeFields() {
        selectedIncomeType = null
        incomeInputMode = IncomeInputMode.FISCAL
        netAmount = ""
        grossAmount = ""
        irpfPercent = ""
        irpfFixedAmount = ""
        irpfInputMode = IrpfInputMode.PERCENT
        socialSecurityAmount = ""
        commissionAmount = ""
        selectedIssuerId = null
        newIssuerName = ""
        showNewIssuerField = false
        issuers = emptyList()
    }

    private fun loadCategories() {
        loadCategoriesAndSelect(type, null)
    }

    private fun loadCategoriesAndSelect(forType: TransactionType, selectId: String?) {
        val accountId = session.selectedAccountId.value ?: return
        getCategoriesByType(accountId, forType)
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
        val abs = abs(amount)
        val rounded = (abs * 100 + 0.5).toLong()
        val euros = rounded / 100
        val cents = rounded % 100
        val sign = if (amount < 0) "-" else ""
        return "$sign$euros,${cents.toString().padStart(2, '0')}"
    }

    private fun filterDecimal(value: String): String =
        value.filter { it.isDigit() || it == ',' || it == '.' }

    private fun formatPercent(value: Double): String =
        if (value == floor(value)) value.toLong().toString()
        else value.toString().replace('.', ',')
}

/** Modo de entrada del IRPF: porcentual o importe fijo. */
enum class IrpfInputMode { PERCENT, AMOUNT }

/** Modo de entrada de ingresos: con desglose fiscal o solo neto. */
enum class IncomeInputMode { FISCAL, NET_ONLY }
