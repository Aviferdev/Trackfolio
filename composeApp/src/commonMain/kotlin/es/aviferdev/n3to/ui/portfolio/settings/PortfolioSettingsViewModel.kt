package es.aviferdev.n3to.ui.portfolio.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.aviferdev.n3to.domain.model.Asset
import es.aviferdev.n3to.domain.model.FixedIncomePosition
import es.aviferdev.n3to.domain.model.Portfolio
import es.aviferdev.n3to.domain.usecase.portfolio.PortfolioOpenItems
import es.aviferdev.n3to.domain.usecase.asset.GetPriceReminderIntervalUseCase
import es.aviferdev.n3to.domain.usecase.assetcategory.GetAllAssetCategoriesIncludingArchivedUseCase
import es.aviferdev.n3to.domain.usecase.assetmetadata.GetRegionsUseCase
import es.aviferdev.n3to.domain.usecase.assetmetadata.GetSectorsUseCase
import es.aviferdev.n3to.domain.usecase.platform.GetPlatformsUseCase
import es.aviferdev.n3to.domain.usecase.portfolio.ArchivePortfolioUseCase
import es.aviferdev.n3to.domain.usecase.portfolio.CheckPortfolioCanBeArchivedUseCase
import es.aviferdev.n3to.domain.usecase.portfolio.GetPortfoliosByAccountUseCase
import es.aviferdev.n3to.domain.usecase.portfolio.SavePortfolioUseCase
import es.aviferdev.n3to.domain.usecase.portfolio.TransferAssetToPortfolioUseCase
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

data class PortfolioArchiveBlockState(
    val portfolio: Portfolio,
    val openAssets: List<Asset>,
    val openFixedIncome: List<FixedIncomePosition>
)

data class PortfolioTransferState(
    val asset: Asset,
    val sourcePortfolio: Portfolio,
    val availablePortfolios: List<Portfolio>
)

data class PortfolioSettingsUiState(
    val portfolios: List<Portfolio> = emptyList(),
    val categoriesCount: Int = 0,
    val platformsCount: Int = 0,
    val sectorsCount: Int = 0,
    val regionsCount: Int = 0,
    val priceReminderInterval: Int = 7,
    val showAddPortfolioSheet: Boolean = false,
    val editingPortfolio: Portfolio? = null,
    val confirmingArchivePortfolio: Portfolio? = null,
    val archiveBlockedState: PortfolioArchiveBlockState? = null,
    val transferState: PortfolioTransferState? = null,
    val isCheckingArchive: Boolean = false,
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
    val confirmingArchive: Portfolio? = null,
    val archiveBlockedState: PortfolioArchiveBlockState? = null,
    val transferState: PortfolioTransferState? = null,
    val isCheckingArchive: Boolean = false,
    val noAccountError: Boolean = false
)

@OptIn(ExperimentalCoroutinesApi::class)
class PortfolioSettingsViewModel(
    private val session: AccountSession,
    private val getPortfoliosByAccount: GetPortfoliosByAccountUseCase,
    private val savePortfolio: SavePortfolioUseCase,
    private val updatePortfolio: UpdatePortfolioUseCase,
    private val archivePortfolio: ArchivePortfolioUseCase,
    private val checkCanArchive: CheckPortfolioCanBeArchivedUseCase,
    private val transferAsset: TransferAssetToPortfolioUseCase,
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

    private val portfoliosFlow = session.selectedAccountId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList()) else getPortfoliosByAccount(id)
    }

    val uiState: StateFlow<PortfolioSettingsUiState> = combine(
        combine(portfoliosFlow, countsFlow) { portfolios, counts -> portfolios to counts },
        _portfolioSheet,
        _priceReminderInterval
    ) { (portfolios, counts), sheet, interval ->
        PortfolioSettingsUiState(
            portfolios = portfolios,
            categoriesCount = counts.categories,
            platformsCount = counts.platforms,
            sectorsCount = counts.sectors,
            regionsCount = counts.regions,
            priceReminderInterval = interval,
            showAddPortfolioSheet = sheet.showAddSheet,
            editingPortfolio = sheet.editing,
            confirmingArchivePortfolio = sheet.confirmingArchive,
            archiveBlockedState = sheet.archiveBlockedState,
            transferState = sheet.transferState,
            isCheckingArchive = sheet.isCheckingArchive,
            noAccountError = sheet.noAccountError
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PortfolioSettingsUiState())

    // ── Price reminder ────────────────────────────────────────────────────────
    fun setReminderInterval(days: Int) {
        getPriceReminderInterval.set(days)
        _priceReminderInterval.value = days
    }

    // ── Portfolio add/edit ────────────────────────────────────────────────────
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

    fun addPortfolio(name: String) {
        val accountId = session.selectedAccountId.value
        if (accountId == null) {
            _portfolioSheet.update { it.copy(showAddSheet = false, noAccountError = true) }
            return
        }
        viewModelScope.launch {
            savePortfolio(accountId, name)
            closeAddPortfolioSheet()
        }
    }

    fun updatePortfolioEntry(portfolio: Portfolio, name: String) {
        viewModelScope.launch {
            updatePortfolio(portfolio.copy(name = name, description = null))
            closeEditPortfolioSheet()
        }
    }

    // ── Archive flow ──────────────────────────────────────────────────────────

    fun requestArchivePortfolio(portfolio: Portfolio) {
        viewModelScope.launch {
            _portfolioSheet.update { it.copy(isCheckingArchive = true) }
            val open = checkCanArchive(portfolio.id)
            if (open.isEmpty) {
                _portfolioSheet.update {
                    it.copy(isCheckingArchive = false, confirmingArchive = portfolio)
                }
            } else {
                _portfolioSheet.update {
                    it.copy(
                        isCheckingArchive = false,
                        archiveBlockedState = PortfolioArchiveBlockState(
                            portfolio, open.openAssets, open.openFixedIncome
                        )
                    )
                }
            }
        }
    }

    fun confirmArchivePortfolio(portfolioId: String) {
        viewModelScope.launch {
            archivePortfolio(portfolioId)
            _portfolioSheet.update { it.copy(confirmingArchive = null, archiveBlockedState = null) }
        }
    }

    fun cancelArchive() {
        _portfolioSheet.update {
            it.copy(confirmingArchive = null, archiveBlockedState = null, transferState = null)
        }
    }

    // ── Transfer flow ─────────────────────────────────────────────────────────

    fun requestTransferAsset(asset: Asset, sourcePortfolio: Portfolio) {
        val available = uiState.value.portfolios.filter { it.id != sourcePortfolio.id }
        _portfolioSheet.update {
            it.copy(transferState = PortfolioTransferState(asset, sourcePortfolio, available))
        }
    }

    fun confirmTransfer(assetId: String, targetPortfolioId: String?) {
        val sourcePortfolio = _portfolioSheet.value.transferState?.sourcePortfolio ?: return
        viewModelScope.launch {
            transferAsset(assetId, targetPortfolioId)
            val remaining = checkCanArchive(sourcePortfolio.id)
            _portfolioSheet.update { state ->
                if (remaining.isEmpty) {
                    state.copy(
                        transferState = null,
                        archiveBlockedState = null,
                        confirmingArchive = sourcePortfolio
                    )
                } else {
                    state.copy(
                        transferState = null,
                        archiveBlockedState = state.archiveBlockedState?.copy(
                            openAssets = remaining.openAssets,
                            openFixedIncome = remaining.openFixedIncome
                        )
                    )
                }
            }
        }
    }

    fun cancelTransfer() {
        _portfolioSheet.update { it.copy(transferState = null) }
    }

    fun dismissNoAccountError() {
        _portfolioSheet.update { it.copy(noAccountError = false) }
    }
}
