package es.aviferdev.n3to.ui.home.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.aviferdev.n3to.core.VersionManager
import es.aviferdev.n3to.domain.model.Asset
import es.aviferdev.n3to.domain.model.CategoryBudgetStatus
import es.aviferdev.n3to.domain.model.EmergencyFundStatus
import es.aviferdev.n3to.domain.model.FixedIncomePosition
import es.aviferdev.n3to.domain.model.HomeBalance
import es.aviferdev.n3to.domain.model.LimitType
import es.aviferdev.n3to.domain.model.MonthlyGoalProgress
import es.aviferdev.n3to.domain.model.TransactionType
import es.aviferdev.n3to.domain.repository.CategoryBudgetRepository
import es.aviferdev.n3to.domain.usecase.account.SetInitialBalanceUseCase
import es.aviferdev.n3to.domain.usecase.asset.GetOutdatedAssetsUseCase
import es.aviferdev.n3to.domain.usecase.asset.SavePriceReminderShownUseCase
import es.aviferdev.n3to.domain.usecase.asset.ShouldShowPriceReminderUseCase
import es.aviferdev.n3to.domain.usecase.asset.UpdateAssetCurrentPriceUseCase
import es.aviferdev.n3to.domain.usecase.budget.GetCategoryBudgetStatusUseCase
import es.aviferdev.n3to.domain.usecase.category.GetCategoriesByTypeUseCase
import es.aviferdev.n3to.domain.usecase.emergencyfund.GetEmergencyFundStatusUseCase
import es.aviferdev.n3to.domain.usecase.fixedincome.GetNearMaturityPositionsUseCase
import es.aviferdev.n3to.domain.usecase.goal.GetCurrentMonthProgressUseCase
import es.aviferdev.n3to.domain.usecase.home.GetHomeBalanceUseCase
import es.aviferdev.n3to.domain.usecase.portfolio.GetPortfolioValueHistoryUseCase
import es.aviferdev.n3to.platform.nowMillis
import es.aviferdev.n3to.platform.nowYear
import es.aviferdev.n3to.ui.account.AccountSession
import es.aviferdev.n3to.ui.common.loading.GlobalLoadingManager
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
    data object Loading : HomeUiState()
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
    private val categoryBudgetRepository: CategoryBudgetRepository
) : ViewModel() {

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
            initialValue = HomeUiState.Loading
        )

    private val _priceReminderState = MutableStateFlow(PriceReminderState())
    val priceReminderState: StateFlow<PriceReminderState> = _priceReminderState.asStateFlow()

    private val _nearMaturityState = MutableStateFlow(NearMaturityState())
    val nearMaturityState: StateFlow<NearMaturityState> = _nearMaturityState.asStateFlow()

    private val _goalProgressState = MutableStateFlow(GoalProgressState())
    val goalProgressState: StateFlow<GoalProgressState> = _goalProgressState.asStateFlow()

    private val _emergencyFundStatusState = MutableStateFlow(EmergencyFundStatus.NOT_CONFIGURED)
    val emergencyFundStatus: StateFlow<EmergencyFundStatus> =
        _emergencyFundStatusState.asStateFlow()

    // ── Presupuestos ─────────────────────────────────────────────────────────────
    private val _budgetStatus = MutableStateFlow<List<CategoryBudgetStatus>>(emptyList())
    val budgetStatus: StateFlow<List<CategoryBudgetStatus>> = _budgetStatus.asStateFlow()

    /** Estado de actualización de versión (delegado en [VersionManager]). */
    val versionStatus: StateFlow<VersionManager.Status> = versionManager.status

    init {
        // Observar cambios de estado para mostrar/ocultar loading global
        viewModelScope.launch {
            uiState.collect { state ->
                when (state) {
                    is HomeUiState.Loading -> loadingManager.show("Cargando inicio...")
                    else -> loadingManager.hide()
                }
            }
        }
        checkPriceReminder()
        loadNearMaturityPositions()
        loadGoalProgress()
        loadEmergencyFundStatus()
        loadBudgetStatus()
    }

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
            // Obtener el activo para pasar su categoryId
            val asset = _priceReminderState.value.outdatedAssets.find { it.id == assetId }
            val result = updateAssetCurrentPrice(assetId, newPrice, now, asset?.assetCategoryId)
            if (result.isSuccess) {
                // Forzar refresco del histórico de la gráfica de portfolio
                getPortfolioValueHistory.triggerRefresh()

                val current = _priceReminderState.value
                val newUpdatedIds = current.updatedAssetIds + assetId
                _priceReminderState.value = current.copy(
                    updatedAssetIds = newUpdatedIds
                )

                // Si todos los activos se han actualizado, marcar como completado
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
                categoryBudgetRepository.saveBudget(categoryId, annualLimit, limitType)
            } else {
                categoryBudgetRepository.deleteBudget(categoryId)
            }
        }
    }
}
