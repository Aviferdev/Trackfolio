package es.aviferdev.n3to.ui.portfolio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.aviferdev.n3to.domain.model.Asset
import es.aviferdev.n3to.domain.model.AssetCategory
import es.aviferdev.n3to.domain.model.AssetRegionDistribution
import es.aviferdev.n3to.domain.model.AssetSectorRelation
import es.aviferdev.n3to.domain.model.PriceQuote
import es.aviferdev.n3to.domain.model.PriceSource
import es.aviferdev.n3to.domain.portfolio.PortfolioCalculator
import es.aviferdev.n3to.domain.usecase.asset.ArchiveAssetUseCase
import es.aviferdev.n3to.domain.usecase.asset.GetAssetsByAccountUseCase
import es.aviferdev.n3to.domain.usecase.asset.SaveAssetUseCase
import es.aviferdev.n3to.domain.usecase.asset.UnarchiveAssetUseCase
import es.aviferdev.n3to.domain.usecase.asset.UpdateAssetUseCase
import es.aviferdev.n3to.domain.usecase.asset.ValidateAssetIdentifierUseCase
import es.aviferdev.n3to.domain.usecase.assetcategory.GetAllAssetCategoriesIncludingArchivedUseCase
import es.aviferdev.n3to.domain.usecase.assetmetadata.DeleteAllRegionDistributionsUseCase
import es.aviferdev.n3to.domain.usecase.assetmetadata.DeleteAllSectorLinksUseCase
import es.aviferdev.n3to.domain.usecase.assetmetadata.GetRegionsByAssetUseCase
import es.aviferdev.n3to.domain.usecase.assetmetadata.GetRegionsUseCase
import es.aviferdev.n3to.domain.usecase.assetmetadata.GetSectorsByAssetUseCase
import es.aviferdev.n3to.domain.usecase.assetmetadata.GetSectorsUseCase
import es.aviferdev.n3to.domain.usecase.assetmetadata.SaveRegionDistributionUseCase
import es.aviferdev.n3to.domain.usecase.assetmetadata.SaveSectorRelationUseCase
import es.aviferdev.n3to.domain.usecase.assetplatform.GetPlatformsByAssetUseCase
import es.aviferdev.n3to.domain.usecase.assetplatform.LinkPlatformToAssetUseCase
import es.aviferdev.n3to.domain.usecase.assetplatform.UnlinkAllPlatformsFromAssetUseCase
import es.aviferdev.n3to.domain.usecase.assettransaction.GetTransactionsByAssetUseCase
import es.aviferdev.n3to.platform.nowMillis
import es.aviferdev.n3to.ui.account.AccountSession
import es.aviferdev.n3to.ui.theme.formatQty
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class CatalogError {
    data class CannotArchiveWithOpenPositions(val ticker: String, val qty: String) : CatalogError()
    data object AccountRequired : CatalogError()
    data object TickerAndNameRequired : CatalogError()
    data class AssetAlreadyExists(val ticker: String) : CatalogError()
    data class Unknown(val message: String?) : CatalogError()
}

data class AssetCatalogUiState(
    val assets: List<Asset> = emptyList(),
    val categories: List<AssetCategory> = emptyList(),
    val showAddSheet: Boolean = false,
    val addForCategoryId: String? = null,
    val editing: Asset? = null,
    val editingPlatformIds: Set<String> = emptySet(),
    val editingSectorIds: Set<String> = emptySet(),
    val editingRegionPercents: Map<String, Int> = emptyMap(),
    val editingFixedIncomePercent: Int = 0,
    val pendingArchive: Asset? = null,
    val error: CatalogError? = null,
    val allSectors: List<es.aviferdev.n3to.domain.model.AssetSector> = emptyList(),
    val allRegions: List<es.aviferdev.n3to.domain.model.AssetRegion> = emptyList()
)

@OptIn(ExperimentalCoroutinesApi::class)
class AssetCatalogViewModel(
    private val getAssetsByAccount: GetAssetsByAccountUseCase,
    private val saveAsset: SaveAssetUseCase,
    private val updateAsset: UpdateAssetUseCase,
    private val archiveAsset: ArchiveAssetUseCase,
    private val unarchiveAsset: UnarchiveAssetUseCase,
    private val getTransactionsByAsset: GetTransactionsByAssetUseCase,
    private val getAssetCategoriesIncludingArchived: GetAllAssetCategoriesIncludingArchivedUseCase,
    private val getPlatformsByAsset: GetPlatformsByAssetUseCase,
    private val getSectors: GetSectorsUseCase,
    private val getRegions: GetRegionsUseCase,
    private val getSectorsByAsset: GetSectorsByAssetUseCase,
    private val getRegionsByAsset: GetRegionsByAssetUseCase,
    private val deleteAllSectorLinks: DeleteAllSectorLinksUseCase,
    private val saveSectorRelation: SaveSectorRelationUseCase,
    private val deleteAllRegionDistributions: DeleteAllRegionDistributionsUseCase,
    private val saveRegionDistribution: SaveRegionDistributionUseCase,
    private val linkPlatformToAsset: LinkPlatformToAssetUseCase,
    private val unlinkAllPlatformsFromAsset: UnlinkAllPlatformsFromAssetUseCase,
    private val session: AccountSession,
    private val validateAssetIdentifier: ValidateAssetIdentifierUseCase? = null
) : ViewModel() {

    private val _showAddSheet = MutableStateFlow(false)
    private val _addForCategoryId = MutableStateFlow<String?>(null)
    private val _editing = MutableStateFlow<Asset?>(null)
    private val _editingPlatformIds = MutableStateFlow<Set<String>>(emptySet())
    private val _pendingArchive = MutableStateFlow<Asset?>(null)
    private val _error = MutableStateFlow<CatalogError?>(null)
    private val _editingSectorIds = MutableStateFlow<Set<String>>(emptySet())
    private val _editingRegionPercents = MutableStateFlow<Map<String, Int>>(emptyMap())
    private val _editingFixedIncomePercent = MutableStateFlow(0)

    private val allSectors: StateFlow<List<es.aviferdev.n3to.domain.model.AssetSector>> =
        getSectors()
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val allRegions: StateFlow<List<es.aviferdev.n3to.domain.model.AssetRegion>> =
        getRegions()
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val sheetStateFlowPart1 = combine(
        _showAddSheet,
        _addForCategoryId,
        _editing,
        _editingPlatformIds
    ) { show, catId, edit, platIds -> SheetPart1(show, catId, edit, platIds) }

    private val sheetStateFlowPart2 = combine(
        _editingSectorIds,
        _editingRegionPercents,
        _editingFixedIncomePercent,
        _pendingArchive
    ) { sectIds, regPerc, fixedIncPct, arch -> SheetPart2(sectIds, regPerc, fixedIncPct, arch) }

    private val sheetStateFlow = combine(sheetStateFlowPart1, sheetStateFlowPart2) { part1, part2 ->
        SheetState(
            part1.show,
            part1.catId,
            part1.edit,
            part1.platIds,
            part2.sectIds,
            part2.regPerc,
            part2.fixedIncPct,
            part2.arch
        )
    }

    private val metadataFlow =
        combine(allSectors, allRegions) { sectors, regions -> sectors to regions }

    val uiState: StateFlow<AssetCatalogUiState> = combine(
        session.selectedAccountId.flatMapLatest { id ->
            if (id == null) flowOf(emptyList()) else getAssetsByAccount(id)
        },
        getAssetCategoriesIncludingArchived().map { it.filter { c -> !c.archived } },
        sheetStateFlow,
        _error,
        metadataFlow
    ) { assets, categories, sheets, error, metadata ->
        AssetCatalogUiState(
            assets = assets,
            categories = categories,
            showAddSheet = sheets.show,
            addForCategoryId = sheets.catId,
            editing = sheets.edit,
            editingPlatformIds = sheets.platIds,
            editingSectorIds = sheets.sectIds,
            editingRegionPercents = sheets.regPerc,
            editingFixedIncomePercent = sheets.fixedIncPct,
            pendingArchive = sheets.arch,
            error = error,
            allSectors = metadata.first,
            allRegions = metadata.second
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AssetCatalogUiState()
    )

    fun openAddSheet() {
        _showAddSheet.value = true; _addForCategoryId.value = null
    }

    fun openAddSheetForCategory(categoryId: String) {
        _addForCategoryId.value = categoryId
        _showAddSheet.value = true
    }

    fun closeAddSheet() {
        _showAddSheet.value = false; _addForCategoryId.value = null
    }

    fun openEditSheet(asset: Asset) {
        _editing.value = asset
        viewModelScope.launch {
            val platforms = getPlatformsByAsset(asset.id).first()
            _editingPlatformIds.value = platforms.map { it.id }.toSet()

            val sectors = getSectorsByAsset(asset.id).first()
            _editingSectorIds.value = sectors.map { it.id }.toSet()

            val regions = getRegionsByAsset(asset.id).first()
            _editingRegionPercents.value = regions.associate { it.regionId to it.percent }

            _editingFixedIncomePercent.value = asset.fixedIncomePercent
        }
    }

    fun closeEditSheet() {
        _editing.value = null
        _editingPlatformIds.value = emptySet()
        _editingSectorIds.value = emptySet()
        _editingRegionPercents.value = emptyMap()
        _editingFixedIncomePercent.value = 0
    }

    fun requestArchive(asset: Asset) {
        viewModelScope.launch {
            val txs = getTransactionsByAsset(asset.id).first()
            val position = PortfolioCalculator.calculate(txs, asset.currentPrice)
            if (position.netQuantity > 0.0) {
                _error.value = CatalogError.CannotArchiveWithOpenPositions(
                    asset.ticker,
                    formatQty(position.netQuantity)
                )
            } else {
                _pendingArchive.value = asset
            }
        }
    }

    fun cancelArchive() {
        _pendingArchive.value = null
    }

    fun addAsset(
        ticker: String,
        name: String,
        notes: String?,
        assetCategoryId: String?,
        currentPrice: Double?,
        isin: String? = null,
        platformIds: Set<String> = emptySet(),
        fixedIncomePercent: Int = 0,
        sectorIds: Set<String> = emptySet(),
        regionPercents: Map<String, Int> = emptyMap(),
        portfolioId: String? = null
    ) {
        val accountId = session.selectedAccountId.value ?: run {
            _error.value = CatalogError.AccountRequired
            return
        }
        val tickerTrim = ticker.trim().uppercase()
        val nameTrim = name.trim()
        if (tickerTrim.isBlank() || nameTrim.isBlank()) {
            _error.value = CatalogError.TickerAndNameRequired
            return
        }
        if (uiState.value.assets.any {
                it.ticker.equals(tickerTrim, ignoreCase = true) && it.accountId == accountId
            }) {
            _error.value = CatalogError.AssetAlreadyExists(tickerTrim)
            return
        }
        viewModelScope.launch {
            val now = nowMillis()
            val asset = Asset(
                id = "asset_${now}_${(0..9999).random()}",
                accountId = accountId,
                portfolioId = portfolioId,
                ticker = tickerTrim,
                name = nameTrim,
                notes = notes?.ifBlank { null },
                createdAt = now,
                assetCategoryId = assetCategoryId,
                currentPrice = currentPrice,
                currentPriceUpdatedAt = if (currentPrice != null) now else null,
                isin = isin,
                priceSource = PriceSource.MANUAL,
                isinValidatedAt = if (isin != null) now else null,
                isinValidationError = null,
                fixedIncomePercent = fixedIncomePercent
            )
            saveAsset(asset)
                .onSuccess {
                    // Vincular sectores
                    sectorIds.forEach { sectorId ->
                        saveSectorRelation(
                            AssetSectorRelation(
                                assetId = asset.id,
                                sectorId = sectorId
                            )
                        )
                    }
                    // Guardar distribución regional
                    regionPercents.forEach { (regionId, percent) ->
                        if (percent > 0) {
                            saveRegionDistribution(
                                AssetRegionDistribution(
                                    assetId = asset.id,
                                    regionId = regionId,
                                    percent = percent
                                )
                            )
                        }
                    }
                    // Vincular plataformas
                    platformIds.forEach { platId ->
                        linkPlatformToAsset(asset.id, platId)
                    }
                }
                .onFailure { _error.value = CatalogError.Unknown(it.message) }
            _showAddSheet.value = false
            _addForCategoryId.value = null
        }
    }

    fun editAsset(
        original: Asset,
        ticker: String,
        name: String,
        notes: String?,
        assetCategoryId: String?,
        currentPrice: Double?,
        isin: String? = null,
        platformIds: Set<String> = emptySet(),
        fixedIncomePercent: Int = 0,
        sectorIds: Set<String> = emptySet(),
        regionPercents: Map<String, Int> = emptyMap(),
        portfolioId: String? = null
    ) {
        val tickerTrim = ticker.trim().uppercase()
        val nameTrim = name.trim()
        if (tickerTrim.isBlank() || nameTrim.isBlank()) {
            _error.value = CatalogError.TickerAndNameRequired
            return
        }
        viewModelScope.launch {
            val updatedAt = when {
                currentPrice == null -> null
                currentPrice == original.currentPrice -> original.currentPriceUpdatedAt
                else -> nowMillis()
            }
            updateAsset(
                original.copy(
                    ticker = tickerTrim,
                    name = nameTrim,
                    notes = notes?.ifBlank { null },
                    assetCategoryId = assetCategoryId,
                    currentPrice = currentPrice,
                    currentPriceUpdatedAt = updatedAt,
                    portfolioId = portfolioId,
                    isin = isin,
                    priceSource = PriceSource.MANUAL,
                    isinValidatedAt = if (isin != null) nowMillis() else null,
                    isinValidationError = null,
                    fixedIncomePercent = fixedIncomePercent
                )
            ).onSuccess {
                // Actualizar sectores: borrar todos y recrear
                deleteAllSectorLinks(original.id)
                sectorIds.forEach { sectorId ->
                    saveSectorRelation(
                        AssetSectorRelation(
                            assetId = original.id,
                            sectorId = sectorId
                        )
                    )
                }
                // Actualizar distribución regional
                deleteAllRegionDistributions(original.id)
                regionPercents.forEach { (regionId, percent) ->
                    if (percent > 0) {
                        saveRegionDistribution(
                            AssetRegionDistribution(
                                assetId = original.id,
                                regionId = regionId,
                                percent = percent
                            )
                        )
                    }
                }
                // Actualizar plataformas: borrar todas y recrear
                unlinkAllPlatformsFromAsset(original.id)
                platformIds.forEach { platId ->
                    linkPlatformToAsset(original.id, platId)
                }
            }.onFailure { _error.value = CatalogError.Unknown(it.message) }
            _editing.value = null
            _editingPlatformIds.value = emptySet()
        }
    }

    fun confirmArchive() {
        val asset = _pendingArchive.value ?: return
        viewModelScope.launch {
            archiveAsset(asset.id).onFailure { _error.value = CatalogError.Unknown(it.message) }
            _pendingArchive.value = null
        }
    }

    fun restoreAsset(assetId: String) {
        viewModelScope.launch {
            unarchiveAsset(assetId).onFailure { _error.value = CatalogError.Unknown(it.message) }
        }
    }

    fun clearError() {
        _error.value = null
    }

    /**
     * Valida un ISIN/ticker contra la API de cotizaciones.
     * @param identifier ISIN, ticker o símbolo crypto.
     * @param categoryId ID de la categoría del activo.
     * @return Result.success(PriceQuote) si el identificador es válido.
     */
    suspend fun validateIsin(identifier: String, categoryId: String?): Result<PriceQuote> {
        if (validateAssetIdentifier == null || categoryId == null) {
            return Result.failure(Exception("Validación no disponible"))
        }
        return validateAssetIdentifier(identifier, categoryId)
    }

    private data class SheetPart1(
        val show: Boolean,
        val catId: String?,
        val edit: Asset?,
        val platIds: Set<String>
    )

    private data class SheetPart2(
        val sectIds: Set<String>,
        val regPerc: Map<String, Int>,
        val fixedIncPct: Int,
        val arch: Asset?
    )

    private data class SheetState(
        val show: Boolean,
        val catId: String?,
        val edit: Asset?,
        val platIds: Set<String>,
        val sectIds: Set<String>,
        val regPerc: Map<String, Int>,
        val fixedIncPct: Int,
        val arch: Asset?
    )
}
