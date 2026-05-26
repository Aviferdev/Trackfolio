package es.aviferdev.n3to.ui.portfolio.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.aviferdev.n3to.domain.model.Portfolio
import es.aviferdev.n3to.domain.usecase.asset.GetPriceReminderIntervalUseCase
import es.aviferdev.n3to.domain.usecase.assetcategory.GetAllAssetCategoriesIncludingArchivedUseCase
import es.aviferdev.n3to.domain.usecase.assetmetadata.GetRegionsUseCase
import es.aviferdev.n3to.domain.usecase.assetmetadata.GetSectorsUseCase
import es.aviferdev.n3to.domain.usecase.platform.GetPlatformsUseCase
import es.aviferdev.n3to.domain.usecase.portfolio.DeletePortfolioUseCase
import es.aviferdev.n3to.domain.usecase.portfolio.GetPortfoliosByAccountUseCase
import es.aviferdev.n3to.domain.usecase.portfolio.SavePortfolioUseCase
import es.aviferdev.n3to.domain.usecase.portfolio.UpdatePortfolioUseCase
import es.aviferdev.n3to.ui.account.AccountSession
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PortfolioSettingsUiState(
    val portfolios: List<Portfolio> = emptyList(),
    val categoriesCount: Int = 0,
    val platformsCount: Int = 0,
    val sectorsCount: Int = 0,
    val regionsCount: Int = 0,
    val priceReminderInterval: Int = 7,
    val showAddPortfolioSheet: Boolean = false,
    val editingPortfolio: Portfolio? = null,
    val deletingPortfolio: Portfolio? = null,
    val noAccountError: Boolean = false
)

private data class CountsSnapshot(
    val categories: Int,
    val platforms: Int,
    val sectors: Int,
    val regions: Int
)

private data class PortfolioSheetState(
    val showAddSheet: Boolean = false,
    val editing: Portfolio? = null,
    val deleting: Portfolio? = null,
    val noAccountError: Boolean = false
)

@OptIn(ExperimentalCoroutinesApi::class)
class PortfolioSettingsViewModel(
    private val session: AccountSession,
    private val getPortfoliosByAccount: GetPortfoliosByAccountUseCase,
    private val savePortfolio: SavePortfolioUseCase,
    private val updatePortfolio: UpdatePortfolioUseCase,
    private val deletePortfolio: DeletePortfolioUseCase,
    getAssetCategoriesIncludingArchived: GetAllAssetCategoriesIncludingArchivedUseCase,
    getSectors: GetSectorsUseCase,
    getRegions: GetRegionsUseCase,
    getPlatforms: GetPlatformsUseCase,
    private val getPriceReminderInterval: GetPriceReminderIntervalUseCase
) : ViewModel() {

    private val _portfolioSheet = MutableStateFlow(PortfolioSheetState())
    private val _priceReminderInterval = MutableStateFlow(getPriceReminderInterval.get())

    private val countsFlow = combine(
        getAssetCategoriesIncludingArchived().map { list -> list.count { !it.archived } },
        getPlatforms().map { it.size },
        getSectors().map { it.size },
        getRegions().map { it.size }
    ) { categories, platforms, sectors, regions ->
        CountsSnapshot(categories, platforms, sectors, regions)
    }

    val uiState: StateFlow<PortfolioSettingsUiState> = combine(
        combine(
            session.selectedAccountId.flatMapLatest { id ->
                if (id == null) flowOf(emptyList()) else getPortfoliosByAccount(id)
            },
            countsFlow
        ) { portfolios, counts -> portfolios to counts },
        _portfolioSheet,
        _priceReminderInterval
    ) { (portfolios, counts), portfolioSheet, interval ->
        PortfolioSettingsUiState(
            portfolios = portfolios,
            categoriesCount = counts.categories,
            platformsCount = counts.platforms,
            sectorsCount = counts.sectors,
            regionsCount = counts.regions,
            priceReminderInterval = interval,
            showAddPortfolioSheet = portfolioSheet.showAddSheet,
            editingPortfolio = portfolioSheet.editing,
            deletingPortfolio = portfolioSheet.deleting,
            noAccountError = portfolioSheet.noAccountError
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PortfolioSettingsUiState())

    // ── Price reminder ────────────────────────────────────────────────────────
    fun setReminderInterval(days: Int) {
        getPriceReminderInterval.set(days)
        _priceReminderInterval.value = days
    }

    // ── Portfolio actions ─────────────────────────────────────────────────────
    fun openAddPortfolioSheet() {
        _portfolioSheet.update { it.copy(showAddSheet = true) }
    }

    fun closeAddPortfolioSheet() {
        _portfolioSheet.update { it.copy(showAddSheet = false) }
    }

    fun openEditPortfolioSheet(portfolio: Portfolio) {
        _portfolioSheet.update { it.copy(editing = portfolio) }
    }

    fun closeEditPortfolioSheet() {
        _portfolioSheet.update { it.copy(editing = null) }
    }

    fun requestDeletePortfolio(portfolio: Portfolio) {
        _portfolioSheet.update { it.copy(deleting = portfolio) }
    }

    fun cancelDeletePortfolio() {
        _portfolioSheet.update { it.copy(deleting = null) }
    }

    fun dismissNoAccountError() {
        _portfolioSheet.update { it.copy(noAccountError = false) }
    }

    fun addPortfolio(name: String, description: String?) {
        val accountId = session.selectedAccountId.value
        if (accountId == null) {
            _portfolioSheet.update { it.copy(showAddSheet = false, noAccountError = true) }
            return
        }
        viewModelScope.launch {
            savePortfolio(accountId, name, description)
            closeAddPortfolioSheet()
        }
    }

    fun updatePortfolioEntry(portfolio: Portfolio, name: String, description: String?) {
        viewModelScope.launch {
            updatePortfolio(portfolio.copy(name = name, description = description))
            closeEditPortfolioSheet()
        }
    }

    fun deletePortfolioEntry(portfolioId: String) {
        viewModelScope.launch {
            deletePortfolio(portfolioId)
            cancelDeletePortfolio()
        }
    }
}
