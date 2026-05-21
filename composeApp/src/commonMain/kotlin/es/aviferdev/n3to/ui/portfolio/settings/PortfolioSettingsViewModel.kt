package es.aviferdev.n3to.ui.portfolio.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.aviferdev.n3to.domain.model.Asset
import es.aviferdev.n3to.domain.model.AssetCategory
import es.aviferdev.n3to.domain.model.AssetRegion
import es.aviferdev.n3to.domain.model.AssetSector
import es.aviferdev.n3to.domain.model.Platform
import es.aviferdev.n3to.domain.model.Portfolio
import es.aviferdev.n3to.domain.usecase.asset.GetAssetsByAccountUseCase
import es.aviferdev.n3to.domain.usecase.asset.GetPriceReminderIntervalUseCase
import es.aviferdev.n3to.domain.usecase.assetcategory.GetAllAssetCategoriesIncludingArchivedUseCase
import es.aviferdev.n3to.domain.usecase.assetmetadata.GetRegionsUseCase
import es.aviferdev.n3to.domain.usecase.assetmetadata.GetSectorsUseCase
import es.aviferdev.n3to.domain.usecase.platform.ArchivePlatformUseCase
import es.aviferdev.n3to.domain.usecase.platform.GetPlatformsUseCase
import es.aviferdev.n3to.domain.usecase.platform.RenamePlatformUseCase
import es.aviferdev.n3to.domain.usecase.platform.SavePlatformUseCase
import es.aviferdev.n3to.domain.usecase.portfolio.DeletePortfolioUseCase
import es.aviferdev.n3to.domain.usecase.portfolio.GetPortfoliosByAccountUseCase
import es.aviferdev.n3to.domain.usecase.portfolio.SavePortfolioUseCase
import es.aviferdev.n3to.domain.usecase.portfolio.UpdatePortfolioUseCase
import es.aviferdev.n3to.platform.nowMillis
import es.aviferdev.n3to.ui.account.AccountSession
import es.aviferdev.n3to.ui.portfolio.PlatformError
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
    val categories: List<AssetCategory> = emptyList(),
    val assets: List<Asset> = emptyList(),
    val allSectors: List<AssetSector> = emptyList(),
    val allRegions: List<AssetRegion> = emptyList(),
    val platforms: List<Platform> = emptyList(),
    val priceReminderInterval: Int = 7,
    val showAddPlatformSheet: Boolean = false,
    val editingPlatform: Platform? = null,
    val platformError: PlatformError? = null,
    val showAddPortfolioSheet: Boolean = false,
    val editingPortfolio: Portfolio? = null,
    val deletingPortfolio: Portfolio? = null,
    val noAccountError: Boolean = false,
    val showSectorSheet: Boolean = false,
    val showRegionSheet: Boolean = false
)

private data class PlatformSheetState(
    val showAddSheet: Boolean = false,
    val editing: Platform? = null,
    val error: PlatformError? = null
)

private data class PortfolioSheetState(
    val showAddSheet: Boolean = false,
    val editing: Portfolio? = null,
    val deleting: Portfolio? = null,
    val noAccountError: Boolean = false,
    val showSectorSheet: Boolean = false,
    val showRegionSheet: Boolean = false
)

private data class DataSnapshot(
    val portfolios: List<Portfolio>,
    val assets: List<Asset>,
    val categories: List<AssetCategory>,
    val sectors: List<AssetSector>,
    val regions: List<AssetRegion>
)

@OptIn(ExperimentalCoroutinesApi::class)
class PortfolioSettingsViewModel(
    private val session: AccountSession,
    private val getPortfoliosByAccount: GetPortfoliosByAccountUseCase,
    private val savePortfolio: SavePortfolioUseCase,
    private val updatePortfolio: UpdatePortfolioUseCase,
    private val deletePortfolio: DeletePortfolioUseCase,
    private val getAssetsByAccount: GetAssetsByAccountUseCase,
    private val getAssetCategoriesIncludingArchived: GetAllAssetCategoriesIncludingArchivedUseCase,
    private val getSectors: GetSectorsUseCase,
    private val getRegions: GetRegionsUseCase,
    private val getPlatforms: GetPlatformsUseCase,
    private val savePlatform: SavePlatformUseCase,
    private val renamePlatform: RenamePlatformUseCase,
    private val archivePlatform: ArchivePlatformUseCase,
    private val getPriceReminderInterval: GetPriceReminderIntervalUseCase
) : ViewModel() {

    private val _platformSheet = MutableStateFlow(PlatformSheetState())
    private val _portfolioSheet = MutableStateFlow(PortfolioSheetState())

    private val dataFlow = combine(
        session.selectedAccountId.flatMapLatest { id ->
            if (id == null) flowOf(emptyList()) else getPortfoliosByAccount(id)
        },
        session.selectedAccountId.flatMapLatest { id ->
            if (id == null) flowOf(emptyList()) else getAssetsByAccount(id)
        },
        getAssetCategoriesIncludingArchived().map { list -> list.filter { !it.archived } },
        getSectors(),
        getRegions()
    ) { portfolios, assets, categories, sectors, regions ->
        DataSnapshot(portfolios, assets, categories, sectors, regions)
    }

    val uiState: StateFlow<PortfolioSettingsUiState> = combine(
        combine(dataFlow, getPlatforms()) { data, platforms -> data to platforms },
        combine(_platformSheet, _portfolioSheet) { p, po -> p to po }
    ) { (data, platforms), (platformSheet, portfolioSheet) ->
        PortfolioSettingsUiState(
            portfolios = data.portfolios,
            categories = data.categories,
            assets = data.assets,
            allSectors = data.sectors,
            allRegions = data.regions,
            platforms = platforms,
            priceReminderInterval = getPriceReminderInterval.get(),
            showAddPlatformSheet = platformSheet.showAddSheet,
            editingPlatform = platformSheet.editing,
            platformError = platformSheet.error,
            showAddPortfolioSheet = portfolioSheet.showAddSheet,
            editingPortfolio = portfolioSheet.editing,
            deletingPortfolio = portfolioSheet.deleting,
            noAccountError = portfolioSheet.noAccountError,
            showSectorSheet = portfolioSheet.showSectorSheet,
            showRegionSheet = portfolioSheet.showRegionSheet
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PortfolioSettingsUiState())

    // ── Price reminder ────────────────────────────────────────────────────────
    fun setReminderInterval(days: Int) {
        getPriceReminderInterval.set(days)
    }

    // ── Platform actions ──────────────────────────────────────────────────────
    fun openAddPlatformSheet() {
        _platformSheet.update { it.copy(showAddSheet = true) }
    }

    fun closeAddPlatformSheet() {
        _platformSheet.update { it.copy(showAddSheet = false) }
    }

    fun openEditPlatformSheet(platform: Platform) {
        _platformSheet.update { it.copy(editing = platform) }
    }

    fun closeEditPlatformSheet() {
        _platformSheet.update { it.copy(editing = null) }
    }

    fun addPlatform(name: String, icon: String, notes: String?) {
        val trimmed = name.trim()
        if (trimmed.isBlank()) return
        if (uiState.value.platforms.any { it.name.equals(trimmed, ignoreCase = true) }) {
            _platformSheet.update { it.copy(error = PlatformError.AlreadyExists) }
            return
        }
        viewModelScope.launch {
            val now = nowMillis()
            val nextOrder = (uiState.value.platforms.maxOfOrNull { it.sortOrder } ?: -1) + 1
            savePlatform(
                Platform(
                    id = "platform_$now",
                    name = trimmed,
                    icon = icon.ifBlank { "🏦" },
                    sortOrder = nextOrder,
                    createdAt = now,
                    notes = notes?.take(200)?.ifBlank { null }
                )
            ).onFailure { _platformSheet.update { s -> s.copy(error = PlatformError.Unknown(it.message)) } }
            closeAddPlatformSheet()
        }
    }

    fun renamePlatform(id: String, newName: String, newIcon: String, notes: String?) {
        val trimmed = newName.trim()
        if (trimmed.isBlank()) return
        if (uiState.value.platforms.any { it.id != id && it.name.equals(trimmed, ignoreCase = true) }) {
            _platformSheet.update { it.copy(error = PlatformError.AlreadyExists) }
            return
        }
        viewModelScope.launch {
            renamePlatform.invoke(id, trimmed, newIcon.ifBlank { "🏦" }, notes?.take(200)?.ifBlank { null })
                .onFailure { _platformSheet.update { s -> s.copy(error = PlatformError.Unknown(it.message)) } }
            closeEditPlatformSheet()
        }
    }

    fun clearPlatformError() {
        _platformSheet.update { it.copy(error = null) }
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

    // ── Sector / Region sheets ─────────────────────────────────────────────────
    fun openSectorSheet() {
        _portfolioSheet.update { it.copy(showSectorSheet = true) }
    }

    fun closeSectorSheet() {
        _portfolioSheet.update { it.copy(showSectorSheet = false) }
    }

    fun openRegionSheet() {
        _portfolioSheet.update { it.copy(showRegionSheet = true) }
    }

    fun closeRegionSheet() {
        _portfolioSheet.update { it.copy(showRegionSheet = false) }
    }
}
