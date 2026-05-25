package es.aviferdev.n3to.ui.transaction

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import es.aviferdev.n3to.platform.nowLocalDate
import es.aviferdev.n3to.platform.nowLocalDateTime
import es.aviferdev.n3to.platform.nowMillis
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.benasher44.uuid.uuid4
import es.aviferdev.n3to.domain.model.Category
import es.aviferdev.n3to.domain.model.IncomeType
import es.aviferdev.n3to.domain.model.Issuer
import es.aviferdev.n3to.domain.model.MonthlyTotals
import es.aviferdev.n3to.domain.model.TaxLine
import es.aviferdev.n3to.domain.model.TaxProfileSnapshot
import es.aviferdev.n3to.domain.model.TaxRole
import es.aviferdev.n3to.domain.model.Transaction
import es.aviferdev.n3to.domain.model.TransactionType
import es.aviferdev.n3to.domain.usecase.category.GetAllCategoriesIncludingArchivedUseCase
import es.aviferdev.n3to.domain.usecase.category.GetCategoriesByTypeUseCase
import es.aviferdev.n3to.domain.usecase.fiscal.CalculateIrpfUseCase
import es.aviferdev.n3to.domain.usecase.fiscal.CalculateNetIncomeUseCase
import es.aviferdev.n3to.domain.usecase.issuer.GetIssuersUseCase
import es.aviferdev.n3to.domain.usecase.taxprofile.GetActiveTaxProfileSnapshotUseCase
import es.aviferdev.n3to.domain.usecase.transaction.DeleteTransactionUseCase
import es.aviferdev.n3to.domain.usecase.transaction.GetMonthlyTotalsUseCase
import es.aviferdev.n3to.domain.usecase.transaction.GetOldestTransactionDateUseCase
import es.aviferdev.n3to.domain.usecase.transaction.GetTransactionsByMonthUseCase
import es.aviferdev.n3to.domain.usecase.transaction.SaveTransactionUseCase
import es.aviferdev.n3to.domain.usecase.transaction.UpdateTransactionUseCase
import es.aviferdev.n3to.ui.account.AccountSession
import es.aviferdev.n3to.ui.home.viewmodel.AddTransactionError
import es.aviferdev.n3to.ui.home.viewmodel.AddTransactionUiState
import es.aviferdev.n3to.ui.home.viewmodel.IAddTransactionForm
import es.aviferdev.n3to.ui.home.viewmodel.IncomeInputMode
import es.aviferdev.n3to.ui.home.viewmodel.IrpfInputMode
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.math.abs
import kotlin.math.floor

data class TransactionListUiState(
    val transactions: List<Transaction> = emptyList(),
    val filteredTransactions: List<Transaction> = emptyList(),
    val totals: MonthlyTotals? = null,
    val categoryNames: Map<String, String> = emptyMap(),
    val year: String = "",
    val month: String = "",
    val searchQuery: String = "",
    val isLoading: Boolean = true,
    val canGoBack: Boolean = true,
    val canGoForward: Boolean = false
)

@OptIn(ExperimentalCoroutinesApi::class)
class TransactionViewModel(
    // ── Transaction list ──────────────────────────────────────────────────────
    private val getTransactionsByMonth: GetTransactionsByMonthUseCase,
    private val getMonthlyTotals: GetMonthlyTotalsUseCase,
    private val deleteTransactionUseCase: DeleteTransactionUseCase,
    private val getAllCategoriesIncludingArchived: GetAllCategoriesIncludingArchivedUseCase,
    private val getOldestDate: GetOldestTransactionDateUseCase,
    private val session: AccountSession,
    // ── Add / edit transaction form ───────────────────────────────────────────
    private val saveTransaction: SaveTransactionUseCase,
    private val updateTransaction: UpdateTransactionUseCase,
    private val getCategoriesByType: GetCategoriesByTypeUseCase,
    private val getIssuers: GetIssuersUseCase,
    private val getActiveTaxProfile: GetActiveTaxProfileSnapshotUseCase,
    private val calculateIrpf: CalculateIrpfUseCase,
    private val calculateNetIncome: CalculateNetIncomeUseCase
) : ViewModel(), IAddTransactionForm {

    // ════════════════════════════════════════════════════════════════════════════
    // Transaction list
    // ════════════════════════════════════════════════════════════════════════════

    private val now = nowLocalDateTime()

    private val _selectedPeriod = MutableStateFlow(
        Pair(now.year.toString(), now.monthNumber.toString().padStart(2, '0'))
    )

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _oldestYearMonth = MutableStateFlow<Pair<Int, Int>?>(null)

    override var type by mutableStateOf(TransactionType.EXPENSE)
        private set

    init {
        viewModelScope.launch {
            session.selectedAccountId.flatMapLatest { accountId ->
                if (accountId == null) flowOf(null)
                else getOldestDate(accountId)
            }.collect { epochMillis ->
                _oldestYearMonth.value = epochMillis?.let {
                    val ld = kotlinx.datetime.Instant.fromEpochMilliseconds(it)
                        .toLocalDateTime(TimeZone.currentSystemDefault())
                    ld.year to ld.monthNumber
                }
            }
        }
        loadCategories()
    }

    val uiState: StateFlow<TransactionListUiState> = combine(
        session.selectedAccountId,
        _selectedPeriod,
        _searchQuery,
        _oldestYearMonth
    ) { accountId, period, query, oldest ->
        data class Params(
            val accountId: String?,
            val period: Pair<String, String>,
            val query: String,
            val oldest: Pair<Int, Int>?
        )
        Params(accountId, period, query, oldest)
    }.flatMapLatest { params ->
        val (year, month) = params.period
        val query = params.query
        val oldest = params.oldest
        val prevMonth = if (month.toInt() == 1) Pair(year.toInt() - 1, 12) else Pair(
            year.toInt(),
            month.toInt() - 1
        )
        val canGoBack =
            oldest == null || prevMonth.first > oldest.first || (prevMonth.first == oldest.first && prevMonth.second >= oldest.second)
        val nowDate = nowLocalDateTime()
        val canGoForward = !(year.toInt() == nowDate.year && month.toInt() == nowDate.monthNumber)

        val accountId = params.accountId
        if (accountId == null) {
            getAllCategoriesIncludingArchived("").map { all ->
                val categoryNames = all.associate { it.id to it.name }
                TransactionListUiState(
                    categoryNames = categoryNames,
                    year = year,
                    month = month,
                    searchQuery = query,
                    isLoading = false,
                    canGoBack = canGoBack,
                    canGoForward = canGoForward
                )
            }
        } else {
            combine(
                getTransactionsByMonth(accountId, year, month),
                getMonthlyTotals(accountId, year, month),
                getAllCategoriesIncludingArchived(accountId).map { all -> all.associate { it.id to it.name } }
            ) { transactions, totals, categoryNames ->
                val filtered = if (query.isBlank()) transactions
                else transactions.filter { t ->
                    val q = query.lowercase()
                    val label = resolveLabel(t, categoryNames).lowercase()
                    val note = t.notes?.lowercase() ?: ""
                    val issuer = t.issuerName?.lowercase() ?: ""
                    val amountFormatted = formatAmountSearch(t.amount)
                    label.contains(q) || note.contains(q) || issuer.contains(q) || amountFormatted.contains(q)
                }
                TransactionListUiState(
                    transactions = transactions,
                    filteredTransactions = filtered,
                    totals = totals,
                    categoryNames = categoryNames,
                    year = year,
                    month = month,
                    searchQuery = query,
                    isLoading = false,
                    canGoBack = canGoBack,
                    canGoForward = canGoForward
                )
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TransactionListUiState(
            year = _selectedPeriod.value.first,
            month = _selectedPeriod.value.second
        )
    )

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun previousMonth() {
        val (y, m) = _selectedPeriod.value
        val month = m.toInt()
        val year = y.toInt()
        val target = if (month == 1) Pair((year - 1).toString(), "12")
        else Pair(y, (month - 1).toString().padStart(2, '0'))
        val oldest = _oldestYearMonth.value
        if (oldest != null) {
            val (oYear, oMonth) = oldest
            val tYear = target.first.toInt(); val tMonth = target.second.toInt()
            if (tYear < oYear || (tYear == oYear && tMonth < oMonth)) return
        }
        _selectedPeriod.value = target
    }

    fun nextMonth() {
        val (y, m) = _selectedPeriod.value
        val month = m.toInt(); val year = y.toInt()
        val nowDate = nowLocalDateTime()
        if (year == nowDate.year && month == nowDate.monthNumber) return
        _selectedPeriod.value = if (month == 12) Pair((year + 1).toString(), "01")
        else Pair(y, (month + 1).toString().padStart(2, '0'))
    }

    fun deleteTransaction(id: String) {
        viewModelScope.launch { deleteTransactionUseCase(id) }
    }

    private fun formatAmountSearch(amount: Double): String {
        val absVal = kotlin.math.abs(amount)
        val intPart = absVal.toLong()
        val frac = ((absVal - intPart) * 100 + 0.5).toLong()
        val intStr = intPart.toString()
        val fracStr = frac.toString().padStart(2, '0')
        return "$intStr$fracStr,$intStr.$fracStr,$intStr,$fracStr,$intStr"
    }

    companion object {
        fun resolveLabel(transaction: Transaction, categoryNames: Map<String, String>): String {
            if (transaction.isLinkedToAsset) {
                return transaction.notes ?: "Inversión"
            }
            return if (transaction.isIncome) {
                transaction.incomeType?.label ?: "Ingreso"
            } else {
                transaction.categoryId?.let { categoryNames[it] } ?: "Gasto"
            }
        }
    }

    // ════════════════════════════════════════════════════════════════════════════
    // IAddTransactionForm — add / edit transaction form state
    // ════════════════════════════════════════════════════════════════════════════

    private val _formUiState = MutableStateFlow<AddTransactionUiState>(AddTransactionUiState.Idle)
    override val formUiState: StateFlow<AddTransactionUiState> = _formUiState.asStateFlow()

    private var editingTransaction: Transaction? = null
    override val isEditing: Boolean get() = editingTransaction != null

    override var amount by mutableStateOf("")
        private set
    override var notes by mutableStateOf("")
        private set
    override var dateMillis by mutableStateOf(nowMillis())
        private set

    override var categories by mutableStateOf<List<Category>>(emptyList())
        private set
    override var selectedCategoryId by mutableStateOf("")
        private set

    override var selectedIncomeType by mutableStateOf<IncomeType?>(null)
        private set

    override var incomeInputMode by mutableStateOf(IncomeInputMode.FISCAL)
        private set
    override var netAmount by mutableStateOf("")
        private set

    override var grossAmount by mutableStateOf("")
        private set
    override var irpfPercent by mutableStateOf("")
        private set
    override var irpfFixedAmount by mutableStateOf("")
        private set
    override var irpfInputMode by mutableStateOf(IrpfInputMode.PERCENT)
        private set
    override var socialSecurityAmount by mutableStateOf("")
        private set
    override var commissionAmount by mutableStateOf("")
        private set

    override var issuers by mutableStateOf<List<Issuer>>(emptyList())
        private set
    override var selectedIssuerId by mutableStateOf<String?>(null)
        private set

    private var newIssuerName by mutableStateOf("")
    private var showNewIssuerField by mutableStateOf(false)
    private var issuerJob: Job? = null

    private var activeTaxProfile by mutableStateOf<TaxProfileSnapshot?>(null)

    override val withholdingTaxLabel: String
        get() {
            val incType = selectedIncomeType ?: return "Retención fiscal"
            val profile = activeTaxProfile ?: return "Retención fiscal"
            return profile.profile.templatesFor(incType)
                .firstOrNull { it.role == TaxRole.INCOME_TAX }
                ?.name ?: "Retención fiscal"
        }

    override val socialContributionLabel: String
        get() {
            val incType = selectedIncomeType ?: return "Cotizaciones sociales"
            val profile = activeTaxProfile ?: return "Cotizaciones sociales"
            val names = profile.profile.templatesFor(incType)
                .filter { it.role == TaxRole.SOCIAL_CONTRIBUTION }
                .map { it.name }
            return if (names.isEmpty()) "Cotizaciones sociales" else names.joinToString(" + ")
        }

    override val showWithholdingField: Boolean
        get() {
            val incType = selectedIncomeType ?: return false
            val profile = activeTaxProfile
            if (profile == null || profile.profile.countryCode == null) return incType.hasWithholdingTax
            return profile.profile.templatesFor(incType).any { it.role == TaxRole.INCOME_TAX }
        }

    override val showSocialContributionField: Boolean
        get() {
            val incType = selectedIncomeType ?: return false
            val profile = activeTaxProfile
            if (profile == null || profile.profile.countryCode == null) return incType.hasSocialContribution
            return profile.profile.templatesFor(incType).any { it.role == TaxRole.SOCIAL_CONTRIBUTION }
        }

    override val calculatedNet: Double?
        get() {
            val incType = selectedIncomeType ?: return null
            val gross = grossAmount.replace(',', '.').toDoubleOrNull() ?: return null
            if (gross <= 0) return null
            val ss = socialSecurityAmount.replace(',', '.').toDoubleOrNull() ?: 0.0
            val comm = commissionAmount.replace(',', '.').toDoubleOrNull() ?: 0.0
            return calculateNetIncome.calculate(
                incType, gross, ss, comm, irpfInputMode == IrpfInputMode.PERCENT, irpfPercent, irpfFixedAmount
            )
        }

    override val isValid: Boolean
        get() {
            if (type == TransactionType.EXPENSE) {
                val amtOk = amount.replace(',', '.').toDoubleOrNull()?.let { it > 0 } == true
                return amtOk && selectedCategoryId.isNotEmpty()
            }
            val incType = selectedIncomeType ?: return false
            if (incomeInputMode == IncomeInputMode.NET_ONLY) {
                return netAmount.replace(',', '.').toDoubleOrNull()?.let { it > 0 } == true
                        && selectedIssuerId != null
            }
            val gross = grossAmount.replace(',', '.').toDoubleOrNull()
            if (incType == IncomeType.EXEMPT_INCOME) return gross != null && gross > 0
            val irpfOk = when (irpfInputMode) {
                IrpfInputMode.PERCENT -> irpfPercent.replace(',', '.').toDoubleOrNull()?.let { it >= 0 } == true
                IrpfInputMode.AMOUNT -> irpfFixedAmount.replace(',', '.').toDoubleOrNull()?.let { it >= 0 } == true
            }
            return gross != null && gross > 0 && irpfOk && selectedIssuerId != null
        }

    override fun onTypeChange(newType: TransactionType) {
        type = newType
        if (newType == TransactionType.EXPENSE) {
            clearIncomeFields()
            loadCategories()
        } else {
            categories = emptyList()
            selectedCategoryId = ""
        }
    }

    override fun onIncomeTypeChange(incomeType: IncomeType) {
        selectedIncomeType = incomeType
        incomeInputMode = IncomeInputMode.FISCAL
        netAmount = ""
        if (!incomeType.hasSocialContribution) socialSecurityAmount = ""
        if (!incomeType.hasCommission) commissionAmount = ""
        if (!incomeType.hasWithholdingTax) { irpfPercent = ""; grossAmount = "" }
        irpfInputMode = IrpfInputMode.PERCENT
        irpfFixedAmount = ""
        selectedIssuerId = null
        newIssuerName = ""
        showNewIssuerField = false
        loadIssuersForType(incomeType)
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

    override fun onAmountChange(value: String) { amount = filterDecimal(value) }
    override fun onGrossAmountChange(value: String) { grossAmount = filterDecimal(value) }
    override fun onIrpfPercentChange(value: String) { irpfPercent = filterDecimal(value) }
    override fun onIrpfFixedAmountChange(value: String) { irpfFixedAmount = filterDecimal(value) }

    override fun onIrpfInputModeChange(mode: IrpfInputMode) {
        irpfInputMode = mode
        when (mode) {
            IrpfInputMode.PERCENT -> irpfFixedAmount = ""
            IrpfInputMode.AMOUNT -> irpfPercent = ""
        }
    }

    override fun onSocialSecurityChange(value: String) { socialSecurityAmount = filterDecimal(value) }
    override fun onCommissionChange(value: String) { commissionAmount = filterDecimal(value) }
    override fun onCategoryChange(categoryId: String) { selectedCategoryId = categoryId }
    override fun onNotesChange(value: String) { notes = value }
    override fun onDateChange(millis: Long) { dateMillis = millis }

    override fun onIncomeModeChange(mode: IncomeInputMode) {
        incomeInputMode = mode
        if (mode == IncomeInputMode.NET_ONLY) {
            grossAmount = ""; irpfPercent = ""; irpfFixedAmount = ""
            socialSecurityAmount = ""; commissionAmount = ""
        } else {
            netAmount = ""
        }
    }

    override fun onNetAmountChange(value: String) { netAmount = filterDecimal(value) }

    override fun onIssuerSelected(issuerId: String) {
        selectedIssuerId = issuerId
        showNewIssuerField = false
        newIssuerName = ""
    }

    fun loadForEdit(transaction: Transaction) {
        editingTransaction = transaction
        type = transaction.type
        notes = transaction.notes ?: ""
        dateMillis = transaction.date
        if (transaction.isIncome) {
            selectedIncomeType = transaction.incomeType
            incomeInputMode = if (transaction.isNetOnly) IncomeInputMode.NET_ONLY else IncomeInputMode.FISCAL
            netAmount = if (transaction.isNetOnly) formatAmountForEdit(transaction.amount) else ""
            grossAmount = formatAmountForEdit(transaction.grossAmount)
            irpfPercent = formatAmountForEdit(transaction.taxLines.firstOrNull { it.role == TaxRole.INCOME_TAX }?.percent)
            socialSecurityAmount = formatAmountForEdit(transaction.taxLines.firstOrNull { it.role == TaxRole.SOCIAL_CONTRIBUTION }?.amount)
            commissionAmount = formatAmountForEdit(transaction.commissionAmount)
            selectedIssuerId = transaction.issuerId
            amount = ""; selectedCategoryId = ""
            transaction.incomeType?.let { loadIssuersForType(it) }
            viewModelScope.launch { activeTaxProfile = getActiveTaxProfile(nowLocalDate()) }
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

    override fun save() {
        if (!isValid) return
        _formUiState.value = AddTransactionUiState.Loading
        viewModelScope.launch {
            val accountId = session.selectedAccountId.value ?: run {
                _formUiState.value = AddTransactionUiState.Error(AddTransactionError.NoAccount)
                return@launch
            }
            val now = nowMillis()
            if (type == TransactionType.EXPENSE) saveExpense(accountId, now)
            else saveIncome(accountId, now)
        }
    }

    private suspend fun saveExpense(accountId: String, now: Long) {
        val netAmt = amount.replace(',', '.').toDouble()
        persistTransaction(buildTransaction(accountId = accountId, netAmount = netAmt, now = now))
    }

    private suspend fun saveIncome(accountId: String, now: Long) {
        val incType = selectedIncomeType!!
        if (incomeInputMode == IncomeInputMode.NET_ONLY) {
            val net = netAmount.replace(',', '.').toDoubleOrNull() ?: run {
                _formUiState.value = AddTransactionUiState.Error(AddTransactionError.InvalidNet)
                return
            }
            val finalIssuerName = issuers.find { it.id == selectedIssuerId }?.name
            persistTransaction(buildTransaction(accountId = accountId, netAmount = net, now = now, incomeType = incType, issuerId = selectedIssuerId, issuerName = finalIssuerName))
            return
        }
        val finalIssuerId = selectedIssuerId
        val finalIssuerName = finalIssuerId?.let { id -> issuers.find { it.id == id }?.name }
        val net = calculatedNet ?: run {
            _formUiState.value = AddTransactionUiState.Error(AddTransactionError.CalculateNet)
            return
        }
        val gross = grossAmount.replace(',', '.').toDoubleOrNull()
        val builtTaxLines = buildList {
            if (incType.hasWithholdingTax && gross != null) {
                val ssVal = if (incType.hasSocialContribution) socialSecurityAmount.replace(',', '.').toDoubleOrNull() ?: 0.0 else 0.0
                val pct = calculateIrpf.resolvePercent(gross, ssVal, irpfInputMode == IrpfInputMode.PERCENT, irpfPercent, irpfFixedAmount)
                add(TaxLine(name = "Retención", role = TaxRole.INCOME_TAX, percent = pct, amount = gross * pct / 100.0))
            }
            if (incType.hasSocialContribution) {
                val ssVal = socialSecurityAmount.replace(',', '.').toDoubleOrNull()
                if (ssVal != null && ssVal > 0) add(TaxLine(name = "Cotización Social", role = TaxRole.SOCIAL_CONTRIBUTION, percent = null, amount = ssVal))
            }
        }
        persistTransaction(buildTransaction(
            accountId = accountId, netAmount = net, now = now, incomeType = incType,
            grossAmount = gross, taxLines = builtTaxLines,
            commissionAmount = if (incType.hasCommission) commissionAmount.replace(',', '.').toDoubleOrNull() else null,
            issuerId = finalIssuerId, issuerName = finalIssuerName
        ))
    }

    private fun buildTransaction(
        accountId: String, netAmount: Double, now: Long,
        incomeType: IncomeType? = null, grossAmount: Double? = null,
        taxLines: List<TaxLine> = emptyList(), commissionAmount: Double? = null,
        issuerId: String? = null, issuerName: String? = null
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
        val result = if (editingTransaction != null) updateTransaction(transaction) else saveTransaction(transaction)
        result
            .onSuccess { _formUiState.value = AddTransactionUiState.Success }
            .onFailure { _formUiState.value = AddTransactionUiState.Error(AddTransactionError.Unknown(it.message)) }
    }

    override fun clear() { _formUiState.value = AddTransactionUiState.Idle }

    private fun clearIncomeFields() {
        selectedIncomeType = null
        incomeInputMode = IncomeInputMode.FISCAL
        netAmount = ""; grossAmount = ""; irpfPercent = ""; irpfFixedAmount = ""
        irpfInputMode = IrpfInputMode.PERCENT
        socialSecurityAmount = ""; commissionAmount = ""
        selectedIssuerId = null; newIssuerName = ""; showNewIssuerField = false
        issuers = emptyList()
    }

    private fun loadCategories() { loadCategoriesAndSelect(type, null) }

    private fun loadCategoriesAndSelect(forType: TransactionType, selectId: String?) {
        val accountId = session.selectedAccountId.value ?: return
        getCategoriesByType(accountId, forType)
            .onEach { list ->
                categories = list
                selectedCategoryId = if (selectId != null && list.any { it.id == selectId }) selectId
                else list.firstOrNull()?.id ?: ""
            }
            .launchIn(viewModelScope)
    }

    private fun loadIssuersForType(incomeType: IncomeType) {
        session.selectedAccountId.value?.let { accountId ->
            issuerJob?.cancel()
            issuerJob = getIssuers(accountId, incomeType.issuerType)
                .onEach { list ->
                    issuers = list
                    if (selectedIssuerId != null && list.none { it.id == selectedIssuerId }) {
                        selectedIssuerId = null
                    }
                }
                .launchIn(viewModelScope)
        }
    }

    private fun formatAmountForEdit(amount: Double?): String {
        if (amount == null) return ""
        val absAmt = abs(amount)
        val rounded = (absAmt * 100 + 0.5).toLong()
        val euros = rounded / 100; val cents = rounded % 100
        val sign = if (amount < 0) "-" else ""
        return "$sign$euros,${cents.toString().padStart(2, '0')}"
    }

    private fun filterDecimal(value: String): String =
        value.filter { it.isDigit() || it == ',' || it == '.' }

    private fun formatPercent(value: Double): String =
        if (value == floor(value)) value.toLong().toString()
        else value.toString().replace('.', ',')
}
