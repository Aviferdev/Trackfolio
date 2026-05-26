package es.aviferdev.n3to.ui.home.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.aviferdev.n3to.core.VersionManager
import es.aviferdev.n3to.core.security.BalanceVisibilityManager
import es.aviferdev.n3to.core.security.BiometricAuthenticator
import es.aviferdev.n3to.core.security.BiometricResult
import es.aviferdev.n3to.domain.model.Account
import es.aviferdev.n3to.domain.model.Asset
import es.aviferdev.n3to.domain.model.CategoryBudgetStatus
import es.aviferdev.n3to.domain.model.EmergencyFundStatus
import es.aviferdev.n3to.domain.model.FixedIncomePosition
import es.aviferdev.n3to.domain.model.HomeBalance
import es.aviferdev.n3to.domain.model.LimitType
import es.aviferdev.n3to.domain.model.MonthlyGoalProgress
import es.aviferdev.n3to.domain.model.TransactionType
import es.aviferdev.n3to.domain.usecase.account.GetAccountsUseCase
import es.aviferdev.n3to.domain.usecase.account.SetInitialBalanceUseCase
import es.aviferdev.n3to.domain.usecase.asset.GetOutdatedAssetsUseCase
import es.aviferdev.n3to.domain.usecase.asset.SavePriceReminderShownUseCase
import es.aviferdev.n3to.domain.usecase.asset.ShouldShowPriceReminderUseCase
import es.aviferdev.n3to.domain.usecase.asset.UpdateAssetCurrentPriceUseCase
import es.aviferdev.n3to.domain.usecase.budget.GetCategoryBudgetStatusUseCase
import es.aviferdev.n3to.domain.usecase.category.DeleteCategoryBudgetUseCase
import es.aviferdev.n3to.domain.usecase.category.GetCategoriesByTypeUseCase
import es.aviferdev.n3to.domain.usecase.category.SaveCategoryBudgetUseCase
import es.aviferdev.n3to.domain.usecase.emergencyfund.GetEmergencyFundStatusUseCase
import es.aviferdev.n3to.domain.usecase.fixedincome.GetNearMaturityPositionsUseCase
import es.aviferdev.n3to.domain.usecase.goal.GetCurrentMonthProgressUseCase
import es.aviferdev.n3to.domain.usecase.home.GetHomeBalanceUseCase
import es.aviferdev.n3to.domain.usecase.portfolio.GetPortfolioValueHistoryUseCase
import es.aviferdev.n3to.platform.nowMillis
import es.aviferdev.n3to.platform.nowYear
import es.aviferdev.n3to.ui.account.AccountSession
import es.aviferdev.n3to.ui.common.loading.GlobalLoadingManager
import es.aviferdev.n3to.ui.reconciliation.ReconciliationUiState
import es.aviferdev.n3to.ui.reconciliation.ReconciliationViewModel
import es.aviferdev.n3to.ui.settings.backup.BackupReminderState
import es.aviferdev.n3to.ui.settings.backup.BackupSheetState
import es.aviferdev.n3to.ui.settings.backup.BackupViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class HomeUiState {
    data class Loading(val message: String) : HomeUiState()
    data class Success(
        val balance: HomeBalance,
        val categoryNames: Map<String, String>,
    ) : HomeUiState()

    data class Error(val message: String) : HomeUiState()
}

data class PriceReminderState(
    val showBanner: Boolean = false,
    val outdatedAssets: List<Asset> = emptyList(),
    val updatedAssetIds: Set<String> = emptySet(),
    val showUpdateSheet: Boolean = false
)

data class NearMaturityState(
    val showBanner: Boolean = false,
    val positions: List<FixedIncomePosition> = emptyList()
)

data class GoalProgressState(
    val progress: MonthlyGoalProgress? = null,
    val showCard: Boolean = true
)

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(
    private val getHomeBalance: GetHomeBalanceUseCase,
    private val getCategoriesByType: GetCategoriesByTypeUseCase,
    private val setInitialBalance: SetInitialBalanceUseCase,
    private val session: AccountSession,
    private val shouldShowPriceReminder: ShouldShowPriceReminderUseCase,
    private val getOutdatedAssets: GetOutdatedAssetsUseCase,
    private val updateAssetCurrentPrice: UpdateAssetCurrentPriceUseCase,
    private val savePriceReminderShown: SavePriceReminderShownUseCase,
    private val getNearMaturityPositions: GetNearMaturityPositionsUseCase? = null,
    private val loadingManager: GlobalLoadingManager,
    private val getPortfolioValueHistory: GetPortfolioValueHistoryUseCase,
    private val versionManager: VersionManager,
    private val getCurrentMonthProgress: GetCurrentMonthProgressUseCase? = null,
    private val getEmergencyFundStatus: GetEmergencyFundStatusUseCase,
    private val getCategoryBudgetStatus: GetCategoryBudgetStatusUseCase,
    private val saveCategoryBudget: SaveCategoryBudgetUseCase,
    private val deleteCategoryBudget: DeleteCategoryBudgetUseCase,
    private val getAccounts: GetAccountsUseCase,
    private val reconciliationDelegate: ReconciliationViewModel,
    private val backupDelegate: BackupViewModel,
    private val balanceVisibility: BalanceVisibilityManager,
    private val authenticator: BiometricAuthenticator
) : ViewModel() {

    // ── Balance principal ─────────────────────────────────────────────────────

    val uiState: StateFlow<HomeUiState> = session.selectedAccountId
        .flatMapLatest { accountId ->
            combine(
                getHomeBalance(accountId),
                getCategoriesByType(accountId ?: "", TransactionType.EXPENSE)
            ) { balance, expenseCategories ->
                val categoryNames = expenseCategories.associate { it.id to it.name }
                HomeUiState.Success(
                    balance = balance,
                    categoryNames = categoryNames,
                )
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeUiState.Loading("")
        )

    // ── Cuentas ───────────────────────────────────────────────────────────────

    private val _accounts = MutableStateFlow<List<Account>>(emptyList())
    val accounts: StateFlow<List<Account>> = _accounts.asStateFlow()

    val selectedAccountId: StateFlow<String?> = session.selectedAccountId

    // ── Recordatorio de precio ────────────────────────────────────────────────

    private val _priceReminderState = MutableStateFlow(PriceReminderState())
    val priceReminderState: StateFlow<PriceReminderState> = _priceReminderState.asStateFlow()

    // ── Vencimientos próximos ─────────────────────────────────────────────────

    private val _nearMaturityState = MutableStateFlow(NearMaturityState())
    val nearMaturityState: StateFlow<NearMaturityState> = _nearMaturityState.asStateFlow()

    // ── Progreso de objetivo mensual ──────────────────────────────────────────

    private val _goalProgressState = MutableStateFlow(GoalProgressState())
    val goalProgressState: StateFlow<GoalProgressState> = _goalProgressState.asStateFlow()

    // ── Fondo de emergencia ───────────────────────────────────────────────────

    private val _emergencyFundStatusState = MutableStateFlow(EmergencyFundStatus.NOT_CONFIGURED)
    val emergencyFundStatus: StateFlow<EmergencyFundStatus> =
        _emergencyFundStatusState.asStateFlow()

    // ── Presupuestos ──────────────────────────────────────────────────────────

    private val _budgetStatus = MutableStateFlow<List<CategoryBudgetStatus>>(emptyList())
    val budgetStatus: StateFlow<List<CategoryBudgetStatus>> = _budgetStatus.asStateFlow()

    // ── Versión ───────────────────────────────────────────────────────────────

    val versionStatus: StateFlow<VersionManager.Status> = versionManager.status

    // ── Reconciliación (delegada) ─────────────────────────────────────────────

    val reconciliationUiState: StateFlow<ReconciliationUiState> = reconciliationDelegate.uiState

    // ── Backup (delegado) ─────────────────────────────────────────────────────

    val backupSheetState: StateFlow<BackupSheetState> = backupDelegate.state
    val backupReminderState: StateFlow<BackupReminderState> = backupDelegate.reminderState

    // ── Formulario de nueva transacción ───────────────────────────────────────

    private val _showAddTransaction = MutableStateFlow(false)
    val showAddTransaction: StateFlow<Boolean> = _showAddTransaction.asStateFlow()

    // ── Init ──────────────────────────────────────────────────────────────────

    init {
        viewModelScope.launch {
            uiState.collect { state ->
                when (state) {
                    is HomeUiState.Loading -> loadingManager.show("Cargando inicio...")
                    else -> loadingManager.hide()
                }
            }
        }
        viewModelScope.launch {
            getAccounts().collect { accounts ->
                _accounts.value = accounts
                if (session.selectedAccountId.value == null && accounts.isNotEmpty()) {
                    val ready = accounts.firstOrNull { !it.needsInitialBalance } ?: accounts.firstOrNull()
                    ready?.let { session.selectAccount(it.id) }
                }
                val currentId = session.selectedAccountId.value
                if (currentId != null && accounts.none { it.id == currentId }) {
                    val ready = accounts.firstOrNull { !it.needsInitialBalance }
                    ready?.let { session.selectAccount(it.id) } ?: session.clearSelection()
                }
            }
        }
        checkPriceReminder()
        loadNearMaturityPositions()
        loadGoalProgress()
        loadEmergencyFundStatus()
        loadBudgetStatus()
    }

    // ── Cuentas ───────────────────────────────────────────────────────────────

    fun selectAccount(id: String) {
        session.selectAccount(id)
    }

    // ── Visibilidad de saldos ─────────────────────────────────────────────────

    fun requestShowBalances(titleText: String, subtitleText: String) {
        balanceVisibility.requestShow {
            authenticator.authenticate(titleText, subtitleText) { result ->
                when (result) {
                    is BiometricResult.Success -> balanceVisibility.onBiometricSuccess()
                    else -> Unit
                }
            }
        }
    }

    fun hideBalances() {
        balanceVisibility.hide()
    }

    // ── Formulario de transacción ─────────────────────────────────────────────

    fun openAddTransaction() {
        _showAddTransaction.value = true
    }

    fun closeAddTransaction() {
        _showAddTransaction.value = false
    }

    // ── Reconciliación ────────────────────────────────────────────────────────

    fun checkReconciliationReminder(accountId: String) =
        reconciliationDelegate.checkReminder(accountId)

    fun openReconciliationSheet(computedBalance: Double) =
        reconciliationDelegate.openBottomSheet(computedBalance)

    fun closeReconciliationSheet() = reconciliationDelegate.closeBottomSheet()

    fun dismissReconciliationBanner() = reconciliationDelegate.dismissBanner()

    fun updateRealBalance(input: String) = reconciliationDelegate.updateRealBalance(input)

    fun reconcile() = reconciliationDelegate.reconcile()

    // ── Backup ────────────────────────────────────────────────────────────────

    fun openBackupIntervalDialog() = backupDelegate.openIntervalDialog()
    fun dismissBackupIntervalDialog() = backupDelegate.dismissIntervalDialog()
    fun saveBackupInterval(days: Int) = backupDelegate.saveReminderInterval(days)
    fun openBackupExport() = backupDelegate.openExport()
    fun dismissBackup() = backupDelegate.dismiss()
    fun onBackupPasswordChange(value: String) = backupDelegate.onPasswordChange(value)
    fun onBackupConfirmPasswordChange(value: String) = backupDelegate.onConfirmPasswordChange(value)
    fun confirmBackupExport() = backupDelegate.confirmExport()
    fun confirmBackupImport() = backupDelegate.confirmImport()
    fun clearBackupResult() = backupDelegate.clearResult()

    // ── Price reminder ────────────────────────────────────────────────────────

    private fun loadBudgetStatus() {
        viewModelScope.launch {
            session.selectedAccountId
                .flatMapLatest { accountId ->
                    if (accountId == null) {
                        emptyFlow()
                    } else {
                        val currentYear = nowYear().toString()
                        getCategoryBudgetStatus(accountId, currentYear)
                    }
                }
                .collect { statuses ->
                    _budgetStatus.value = statuses
                }
        }
    }

    private fun checkPriceReminder() {
        if (!shouldShowPriceReminder()) return

        viewModelScope.launch {
            session.selectedAccountId
                .flatMapLatest { accountId ->
                    accountId?.let {
                        getOutdatedAssets(it)
                    } ?: emptyFlow()
                }
                .collect { outdated ->
                    _priceReminderState.value = _priceReminderState.value.copy(
                        showBanner = outdated.isNotEmpty(),
                        outdatedAssets = outdated
                    )
                }
        }
    }

    private fun loadNearMaturityPositions() {
        if (getNearMaturityPositions == null) return

        viewModelScope.launch {
            session.selectedAccountId
                .flatMapLatest { accountId ->
                    accountId?.let {
                        getNearMaturityPositions(it)
                    } ?: emptyFlow()
                }
                .collect { positions ->
                    _nearMaturityState.value = _nearMaturityState.value.copy(
                        showBanner = positions.isNotEmpty(),
                        positions = positions
                    )
                }
        }
    }

    fun dismissVersionBanner(latestVersion: String) {
        versionManager.dismissBanner(latestVersion)
    }

    fun dismissNearMaturityBanner() {
        _nearMaturityState.value = _nearMaturityState.value.copy(showBanner = false)
    }

    fun dismissReminder() {
        savePriceReminderShown()
        _priceReminderState.value = _priceReminderState.value.copy(
            showBanner = false,
            showUpdateSheet = false
        )
    }

    fun openUpdateSheet() {
        _priceReminderState.value = _priceReminderState.value.copy(
            showUpdateSheet = true
        )
    }

    fun closeUpdateSheet() {
        _priceReminderState.value = _priceReminderState.value.copy(
            showUpdateSheet = false
        )
    }

    fun updateAssetPrice(assetId: String, newPrice: Double) {
        viewModelScope.launch {
            val now = nowMillis()
            val asset = _priceReminderState.value.outdatedAssets.find { it.id == assetId }
            val result = updateAssetCurrentPrice(assetId, newPrice, now, asset?.assetCategoryId)
            if (result.isSuccess) {
                getPortfolioValueHistory.triggerRefresh()

                val current = _priceReminderState.value
                val newUpdatedIds = current.updatedAssetIds + assetId
                _priceReminderState.value = current.copy(
                    updatedAssetIds = newUpdatedIds
                )

                val allDone = current.outdatedAssets.all { it.id in newUpdatedIds }
                if (allDone) {
                    savePriceReminderShown()
                    _priceReminderState.value = _priceReminderState.value.copy(
                        showBanner = false,
                        showUpdateSheet = false
                    )
                }
            }
        }
    }

    private fun loadGoalProgress() {
        if (getCurrentMonthProgress == null) return

        viewModelScope.launch {
            session.selectedAccountId
                .flatMapLatest { accountId ->
                    if (accountId == null) {
                        emptyFlow()
                    } else {
                        getCurrentMonthProgress(accountId)
                    }
                }
                .collect { progress ->
                    _goalProgressState.value = GoalProgressState(
                        progress = progress,
                        showCard = true
                    )
                }
        }
    }

    private fun loadEmergencyFundStatus() {
        viewModelScope.launch {
            session.selectedAccountId
                .flatMapLatest { accountId ->
                    if (accountId == null) {
                        emptyFlow()
                    } else {
                        getEmergencyFundStatus(accountId)
                    }
                }
                .collect { status ->
                    _emergencyFundStatusState.value = status
                }
        }
    }

    fun setInitialBalance(amount: Double) {
        val accountId = (uiState.value as? HomeUiState.Success)
            ?.balance?.selectedAccount?.id ?: return
        viewModelScope.launch {
            setInitialBalance.invoke(accountId, amount)
            session.selectAccount(accountId)
        }
    }

    fun saveBudgetLimit(categoryId: String, annualLimit: Double, limitType: LimitType) {
        viewModelScope.launch {
            if (annualLimit > 0.0) {
                saveCategoryBudget(categoryId, annualLimit, limitType)
            } else {
                deleteCategoryBudget(categoryId)
            }
        }
    }

}
