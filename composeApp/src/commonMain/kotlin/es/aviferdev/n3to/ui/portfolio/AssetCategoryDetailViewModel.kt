package es.aviferdev.n3to.ui.portfolio

import es.aviferdev.n3to.platform.nowMillis
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.aviferdev.n3to.domain.model.Asset
import es.aviferdev.n3to.domain.model.AssetCategory
import es.aviferdev.n3to.domain.model.FixedIncomeRow
import es.aviferdev.n3to.domain.model.Platform
import es.aviferdev.n3to.domain.usecase.asset.ArchiveAssetUseCase
import es.aviferdev.n3to.domain.usecase.asset.CheckAssetArchivableUseCase
import es.aviferdev.n3to.domain.usecase.asset.GetAllAssetsIncludingArchivedUseCase
import es.aviferdev.n3to.domain.usecase.asset.GetAssetEditMetadataUseCase
import es.aviferdev.n3to.domain.usecase.asset.SaveAssetWithMetadataUseCase
import es.aviferdev.n3to.domain.usecase.asset.UnarchiveAssetUseCase
import es.aviferdev.n3to.domain.usecase.asset.UpdateAssetWithMetadataUseCase
import es.aviferdev.n3to.domain.usecase.assetcategory.GetAllAssetCategoriesIncludingArchivedUseCase
import es.aviferdev.n3to.domain.usecase.fixedincome.GetFixedIncomeRowsByCategoryUseCase
import es.aviferdev.n3to.domain.usecase.platform.CreateAndLinkPlatformUseCase
import es.aviferdev.n3to.domain.usecase.platform.GetPlatformsByCategoryUseCase
import es.aviferdev.n3to.domain.usecase.platform.GetPlatformsUseCase
import es.aviferdev.n3to.domain.usecase.platform.LinkPlatformToCategoryUseCase
import es.aviferdev.n3to.domain.usecase.platform.UnlinkPlatformFromCategoryUseCase
import es.aviferdev.n3to.ui.account.AccountSession
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class CategoryDetailError {
    data object AccountRequired : CategoryDetailError()
    data object TickerAndNameRequired : CategoryDetailError()
    data class CannotArchive(val ticker: String) : CategoryDetailError()
    data class PlatformAlreadyExists(val name: String) : CategoryDetailError()
    data class Unknown(val message: String?) : CategoryDetailError()
}

data class AssetCategoryDetailUiState(
    val category: AssetCategory?          = null,
    val activeAssets: List<Asset>         = emptyList(),
    val archivedAssets: List<Asset>       = emptyList(),
    val activeFixedIncome: List<FixedIncomeRow> = emptyList(),
    val categoryPlatforms: List<Platform> = emptyList(),
    val allPlatforms: List<Platform>      = emptyList(),
    val allCategories: List<AssetCategory> = emptyList(),
    val showAddSheet: Boolean             = false,
    val editing: Asset?                   = null,
    val editingPlatformIds: Set<String>   = emptySet(),
    val editingSectorIds: Set<String>     = emptySet(),
    val editingRegionPercents: Map<String, Int> = emptyMap(),
    val editingFixedIncomePercent: Int    = 0,
    val pendingArchive: Asset?            = null,
    val showLinkPlatformSheet: Boolean    = false,
    val error: CategoryDetailError?       = null,
    val allSectors: List<es.aviferdev.n3to.domain.model.AssetSector> = emptyList(),
    val allRegions: List<es.aviferdev.n3to.domain.model.AssetRegion> = emptyList()
)

@OptIn(ExperimentalCoroutinesApi::class)
class AssetCategoryDetailViewModel(
    private val categoryId: String,
    private val getAllAssetsIncludingArchived: GetAllAssetsIncludingArchivedUseCase,
    private val getAssetCategoriesIncludingArchived: GetAllAssetCategoriesIncludingArchivedUseCase,
    private val getPlatforms: GetPlatformsUseCase,
    private val getPlatformsByCategory: GetPlatformsByCategoryUseCase,
    private val saveAssetWithMetadata: SaveAssetWithMetadataUseCase,
    private val updateAssetWithMetadata: UpdateAssetWithMetadataUseCase,
    private val getAssetEditMetadata: GetAssetEditMetadataUseCase,
    private val archiveAsset: ArchiveAssetUseCase,
    private val unarchiveAsset: UnarchiveAssetUseCase,
    private val checkAssetArchivable: CheckAssetArchivableUseCase,
    private val getFixedIncomeRowsByCategory: GetFixedIncomeRowsByCategoryUseCase,
    private val linkPlatformToCategory: LinkPlatformToCategoryUseCase,
    private val unlinkPlatformFromCategory: UnlinkPlatformFromCategoryUseCase,
    private val createAndLinkPlatform: CreateAndLinkPlatformUseCase,
    private val session: AccountSession
) : ViewModel() {

    private val _showAddSheet           = MutableStateFlow(false)
    private val _editing                = MutableStateFlow<Asset?>(null)
    private val _editingPlatformIds     = MutableStateFlow<Set<String>>(emptySet())
    private val _editingSectorIds       = MutableStateFlow<Set<String>>(emptySet())
    private val _editingRegionPercents  = MutableStateFlow<Map<String, Int>>(emptyMap())
    private val _editingFiPercent       = MutableStateFlow(0)
    private val _pendingArchive         = MutableStateFlow<Asset?>(null)
    private val _error                  = MutableStateFlow<CategoryDetailError?>(null)
    private val _showLinkPlatformSheet  = MutableStateFlow(false)

    val uiState: StateFlow<AssetCategoryDetailUiState> = session.selectedAccountId
        .flatMapLatest { accountId ->
            if (accountId == null) flowOf(AssetCategoryDetailUiState())
            else {
                val assetsAndCategoriesFlow = combine(
                    getAllAssetsIncludingArchived(accountId),
                    getAssetCategoriesIncludingArchived()
                ) { assets, categories -> assets to categories }

                val platformsFlow = combine(
                    getPlatforms(),
                    getPlatformsByCategory(categoryId)
                ) { allPlats, catPlats -> allPlats to catPlats }

                val editingFlowPart1 = combine(
                    _showAddSheet,
                    _editing,
                    _editingPlatformIds
                ) { show, edit, platIds ->
                    Triple(show, edit, platIds)
                }

                val editingFlowPart2 = combine(
                    _editingSectorIds,
                    _editingRegionPercents,
                    _editingFiPercent
                ) { sectIds, regPerc, fiPct ->
                    Triple(sectIds, regPerc, fiPct)
                }

                val editingFlow = combine(editingFlowPart1, editingFlowPart2) { (show, edit, platIds), (sectIds, regPerc, fiPct) ->
                    EditingState(show, edit, platIds, sectIds, regPerc, fiPct)
                }

                val uiControlFlow = combine(
                    editingFlow,
                    _pendingArchive,
                    _error,
                    _showLinkPlatformSheet
                ) { editing, pending, err, linkSheet ->
                    UiControlState(editing, pending, err, linkSheet)
                }

                combine(assetsAndCategoriesFlow, platformsFlow, getFixedIncomeRowsByCategory(accountId, categoryId), uiControlFlow) { data, platforms, fiRows, ui ->
                    val (allAssets, categories) = data
                    val (allPlatforms, categoryPlatforms) = platforms
                    val category = categories.firstOrNull { it.id == categoryId }
                    val assetsInCategory = allAssets.filter { it.assetCategoryId == categoryId }
                    AssetCategoryDetailUiState(
                        category              = category,
                        activeAssets          = assetsInCategory.filter { !it.archived },
                        archivedAssets        = assetsInCategory.filter { it.archived },
                        activeFixedIncome     = fiRows,
                        categoryPlatforms     = categoryPlatforms,
                        allPlatforms          = allPlatforms,
                        allCategories         = categories.filter { !it.archived },
                        showAddSheet          = ui.editing.showAdd,
                        editing               = ui.editing.asset,
                        editingPlatformIds    = ui.editing.platIds,
                        editingSectorIds      = ui.editing.sectIds,
                        editingRegionPercents = ui.editing.regPerc,
                        editingFixedIncomePercent = ui.editing.fiPct,
                        pendingArchive        = ui.pendingArchive,
                        showLinkPlatformSheet = ui.showLinkPlatformSheet,
                        error                 = ui.error
                    )
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AssetCategoryDetailUiState())

    fun openAddSheet()  { _showAddSheet.value = true }
    fun closeAddSheet() { _showAddSheet.value = false }

    fun openEditSheet(asset: Asset) {
        _editing.value = asset
        viewModelScope.launch {
            val metadata = getAssetEditMetadata(asset.id)
            _editingPlatformIds.value    = metadata.platformIds
            _editingSectorIds.value      = metadata.sectorIds
            _editingRegionPercents.value = metadata.regionPercents
            _editingFiPercent.value      = metadata.fixedIncomePercent
        }
    }

    fun closeEditSheet() {
        _editing.value               = null
        _editingPlatformIds.value    = emptySet()
        _editingSectorIds.value      = emptySet()
        _editingRegionPercents.value = emptyMap()
        _editingFiPercent.value      = 0
    }

    fun requestArchive(asset: Asset) {
        viewModelScope.launch {
            val canArchive = checkAssetArchivable(asset.id, asset.currentPrice)
            if (canArchive) {
                _pendingArchive.value = asset
            } else {
                _error.value = CategoryDetailError.CannotArchive(asset.ticker)
            }
        }
    }

    fun cancelArchive() { _pendingArchive.value = null }

    fun confirmArchive() {
        val asset = _pendingArchive.value ?: return
        viewModelScope.launch {
            archiveAsset(asset.id).onFailure { _error.value = CategoryDetailError.Unknown(it.message) }
            _pendingArchive.value = null
        }
    }

    fun restoreAsset(assetId: String) {
        viewModelScope.launch {
            unarchiveAsset(assetId).onFailure { _error.value = CategoryDetailError.Unknown(it.message) }
        }
    }

    fun addAsset(
        ticker: String,
        name: String,
        notes: String?,
        currentPrice: Double?,
        platformIds: Set<String> = emptySet(),
        maturityDate: Long? = null,
        fixedIncomePercent: Int = 0,
        sectorIds: Set<String> = emptySet(),
        regionPercents: Map<String, Int> = emptyMap(),
        portfolioId: String? = null
    ) {
        val accountId = session.selectedAccountId.value ?: run {
            _error.value = CategoryDetailError.AccountRequired
            return
        }
        val tickerTrim = ticker.trim().uppercase()
        val nameTrim   = name.trim()
        if (tickerTrim.isBlank() || nameTrim.isBlank()) {
            _error.value = CategoryDetailError.TickerAndNameRequired
            return
        }
        viewModelScope.launch {
            val now   = nowMillis()
            val asset = Asset(
                id                    = "asset_${now}_${(0..9999).random()}",
                accountId             = accountId,
                ticker                = tickerTrim,
                name                  = nameTrim,
                notes                 = notes?.ifBlank { null },
                createdAt             = now,
                portfolioId           = portfolioId,
                assetCategoryId       = categoryId,
                currentPrice          = currentPrice,
                currentPriceUpdatedAt = if (currentPrice != null) now else null,
                maturityDate          = maturityDate
            )
            saveAssetWithMetadata(asset, fixedIncomePercent, sectorIds, regionPercents, platformIds)
                .onFailure { _error.value = CategoryDetailError.Unknown(it.message) }
            _showAddSheet.value = false
        }
    }

    fun editAsset(
        original: Asset,
        ticker: String,
        name: String,
        notes: String?,
        assetCategoryId: String?,
        currentPrice: Double?,
        platformIds: Set<String> = emptySet(),
        maturityDate: Long? = null,
        fixedIncomePercent: Int = 0,
        sectorIds: Set<String> = emptySet(),
        regionPercents: Map<String, Int> = emptyMap(),
        portfolioId: String? = null
    ) {
        val tickerTrim = ticker.trim().uppercase()
        val nameTrim   = name.trim()
        if (tickerTrim.isBlank() || nameTrim.isBlank()) {
            _error.value = CategoryDetailError.TickerAndNameRequired
            return
        }
        viewModelScope.launch {
            val updatedAt = when {
                currentPrice == null                  -> null
                currentPrice == original.currentPrice -> original.currentPriceUpdatedAt
                else                                  -> nowMillis()
            }
            val updatedAsset = original.copy(
                ticker                = tickerTrim,
                name                  = nameTrim,
                notes                 = notes?.ifBlank { null },
                assetCategoryId       = assetCategoryId,
                currentPrice          = currentPrice,
                currentPriceUpdatedAt = updatedAt,
                maturityDate          = maturityDate,
                portfolioId           = portfolioId
            )
            updateAssetWithMetadata(updatedAsset, fixedIncomePercent, sectorIds, regionPercents, platformIds)
                .onFailure { _error.value = CategoryDetailError.Unknown(it.message) }
            _editing.value            = null
            _editingPlatformIds.value = emptySet()
        }
    }

    fun clearError() { _error.value = null }

    // ── Plataformas de la categoría ───────────────────────────────────────────
    fun openLinkPlatformSheet()  { _showLinkPlatformSheet.value = true }
    fun closeLinkPlatformSheet() { _showLinkPlatformSheet.value = false }

    fun linkPlatform(platformId: String) {
        viewModelScope.launch {
            linkPlatformToCategory(platformId, categoryId)
                .onFailure { _error.value = CategoryDetailError.Unknown(it.message) }
        }
    }

    fun unlinkPlatform(platformId: String) {
        viewModelScope.launch {
            unlinkPlatformFromCategory(platformId, categoryId)
                .onFailure { _error.value = CategoryDetailError.Unknown(it.message) }
        }
    }

    fun createAndLinkPlatform(name: String, icon: String, notes: String?) {
        val trimmed = name.trim()
        if (trimmed.isBlank()) return
        if (uiState.value.allPlatforms.any { it.name.equals(trimmed, ignoreCase = true) }) {
            _error.value = CategoryDetailError.PlatformAlreadyExists("")
            return
        }
        val sortOrder = (uiState.value.allPlatforms.maxOfOrNull { it.sortOrder } ?: -1) + 1
        viewModelScope.launch {
            createAndLinkPlatform(
                name       = trimmed,
                icon       = icon,
                notes      = notes?.take(200)?.ifBlank { null },
                categoryId = categoryId,
                sortOrder  = sortOrder,
                createdAt  = nowMillis()
            )
                .onSuccess { _showLinkPlatformSheet.value = false }
                .onFailure { _error.value = CategoryDetailError.Unknown(it.message) }
        }
    }

    // ── Helpers internos ─────────────────────────────────────────────────────
    private data class EditingState(
        val showAdd: Boolean,
        val asset: Asset?,
        val platIds: Set<String>,
        val sectIds: Set<String>,
        val regPerc: Map<String, Int>,
        val fiPct: Int
    )

    private data class UiControlState(
        val editing: EditingState,
        val pendingArchive: Asset?,
        val error: CategoryDetailError?,
        val showLinkPlatformSheet: Boolean
    )
}
